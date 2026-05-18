package cn.rongcloud.im.openclaw.group.list;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import cn.rongcloud.im.R;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.openclaw.group.select.SelectOpenClawRobotActivity;
import cn.rongcloud.im.openclaw.model.OpenClawRobotInfo;
import cn.rongcloud.im.utils.ToastUtils;
import io.rong.imkit.model.ContactModel;
import io.rong.imkit.usermanage.component.ContactListComponent;
import io.rong.imkit.usermanage.component.HeadComponent;
import io.rong.imkit.widget.CommonDialog;
import io.rong.imkit.widget.SettingItemView;
import io.rong.imlib.model.FriendInfo;
import java.util.ArrayList;
import java.util.List;

public class GroupOpenClawRobotsFragment extends Fragment {
    private GroupOpenClawRobotsViewModel viewModel;
    private ContactListComponent robotListComponent;
    private SettingItemView addRobotItem;
    private TextView emptyView;
    private String groupId;
    private boolean canManage;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(GroupOpenClawRobotsViewModel.class);
        if (getArguments() != null) {
            groupId = getArguments().getString(GroupOpenClawRobotsActivity.EXTRA_GROUP_ID);
            canManage = getArguments().getBoolean(GroupOpenClawRobotsActivity.EXTRA_CAN_MANAGE);
        }
        View view = inflater.inflate(R.layout.openclaw_page_group_robots, container, false);
        HeadComponent headComponent = view.findViewById(R.id.rc_head_component);
        headComponent.setLeftClickListener(v -> requireActivity().finish());
        addRobotItem = view.findViewById(R.id.siv_group_add_member);
        robotListComponent = view.findViewById(R.id.rc_group_list_component);
        emptyView = view.findViewById(R.id.rc_empty_tv);
        addRobotItem.setVisibility(canManage ? View.VISIBLE : View.GONE);
        addRobotItem.setOnClickListener(
                v ->
                        startActivity(
                                SelectOpenClawRobotActivity.newIntent(requireContext(), groupId)));
        robotListComponent.setShowItemRemoveButton(canManage);
        robotListComponent.setOnItemRemoveClickListener(
                contactModel -> {
                    OpenClawRobotInfo robot = (OpenClawRobotInfo) contactModel.getExtra();
                    if (robot != null) {
                        confirmRemoveRobot(robot);
                    }
                });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        loadData();
    }

    private void loadData() {
        if (TextUtils.isEmpty(groupId)) {
            updateList(null);
            return;
        }
        viewModel
                .getGroupRobots(groupId)
                .observe(
                        getViewLifecycleOwner(),
                        resource -> {
                            if (resource.status == Status.SUCCESS) {
                                updateList(resource.data);
                            } else if (resource.status == Status.ERROR) {
                                ToastUtils.showErrorToast(resource.code);
                                updateList(null);
                            }
                        });
    }

    private void updateList(List<OpenClawRobotInfo> robots) {
        List<ContactModel> contactModels = toContactModels(robots, ContactModel.CheckType.NONE);
        if (contactModels.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            robotListComponent.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            robotListComponent.setVisibility(View.VISIBLE);
            robotListComponent.setContactList(contactModels);
        }
    }

    private void confirmRemoveRobot(OpenClawRobotInfo robot) {
        String name = TextUtils.isEmpty(robot.getName()) ? robot.getBotId() : robot.getName();
        new CommonDialog.Builder()
                .setContentMessage(getString(R.string.openclaw_remove_robot_hint, name))
                .setDialogButtonClickListener((v, bundle) -> removeRobot(robot))
                .build()
                .show(getParentFragmentManager(), null);
    }

    private void removeRobot(OpenClawRobotInfo robot) {
        viewModel
                .removeRobot(groupId, robot.getBotId())
                .observe(
                        getViewLifecycleOwner(),
                        resource -> {
                            if (resource.status == Status.SUCCESS) {
                                ToastUtils.showToast(getString(R.string.openclaw_remove_success));
                                loadData();
                            } else if (resource.status == Status.ERROR) {
                                ToastUtils.showErrorToast(resource.code);
                            }
                        });
    }

    private List<ContactModel> toContactModels(
            List<OpenClawRobotInfo> robots, ContactModel.CheckType checkType) {
        List<ContactModel> contactModels = new ArrayList<>();
        if (robots == null) {
            return contactModels;
        }
        for (OpenClawRobotInfo robot : robots) {
            if (robot == null || TextUtils.isEmpty(robot.getBotId())) {
                continue;
            }
            FriendInfo friendInfo = new FriendInfo();
            friendInfo.setUserId(robot.getBotId());
            friendInfo.setName(
                    TextUtils.isEmpty(robot.getName()) ? robot.getBotId() : robot.getName());
            friendInfo.setPortraitUri(robot.getPortraitUri());
            ContactModel<FriendInfo> contactModel =
                    ContactModel.obtain(friendInfo, ContactModel.ItemType.CONTENT, checkType);
            contactModel.putExtra(robot);
            contactModels.add(contactModel);
        }
        return contactModels;
    }
}
