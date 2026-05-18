package cn.rongcloud.im.openclaw.group.select;

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
import cn.rongcloud.im.im.message.SealGroupNotificationMessage;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.openclaw.group.list.GroupOpenClawRobotsActivity;
import cn.rongcloud.im.openclaw.model.OpenClawRobotInfo;
import cn.rongcloud.im.openclaw.model.OpenClawRobotRegistry;
import cn.rongcloud.im.utils.ToastUtils;
import io.rong.imkit.IMCenter;
import io.rong.imkit.model.ContactModel;
import io.rong.imkit.usermanage.component.ContactListComponent;
import io.rong.imkit.usermanage.component.HeadComponent;
import io.rong.imkit.usermanage.component.SearchComponent;
import io.rong.imkit.usermanage.interfaces.OnActionClickListener;
import io.rong.imkit.utils.RouteUtils;
import io.rong.imlib.RongCoreClient;
import io.rong.imlib.model.ConversationIdentifier;
import io.rong.imlib.model.FriendInfo;
import io.rong.imlib.model.Message;
import io.rong.message.GroupNotificationMessage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SelectOpenClawRobotFragment extends Fragment {
    private SelectOpenClawRobotViewModel viewModel;
    private HeadComponent headComponent;
    private ContactListComponent contactListComponent;
    private TextView emptyView;
    private String groupId;
    private List<OpenClawRobotInfo> myRobots;
    private List<OpenClawRobotInfo> groupRobots;
    private final Set<String> selectedBotIds = new HashSet<>();
    private String query;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(SelectOpenClawRobotViewModel.class);
        if (getArguments() != null) {
            groupId = getArguments().getString(GroupOpenClawRobotsActivity.EXTRA_GROUP_ID);
        }
        View view =
                inflater.inflate(R.layout.openclaw_page_group_robot_selection, container, false);
        headComponent = view.findViewById(R.id.rc_head_component);
        SearchComponent searchComponent = view.findViewById(R.id.rc_search_component);
        contactListComponent = view.findViewById(R.id.rc_contact_list_component);
        emptyView = view.findViewById(R.id.rc_empty_tv);
        headComponent.setLeftClickListener(v -> requireActivity().finish());
        headComponent.setRightTextViewEnable(false);
        headComponent.setRightClickListener(v -> confirmSelection());
        searchComponent.setSearchQueryListener(
                new SearchComponent.OnSearchQueryListener() {
                    @Override
                    public void onSearch(String queryText) {
                        query = queryText;
                        updateAvailableRobots();
                    }
                });
        contactListComponent.setOnItemClickListener(
                new OnActionClickListener<ContactModel>() {
                    @Override
                    public void onActionClick(ContactModel contactModel) {}

                    @Override
                    @SuppressWarnings("unchecked")
                    public <E> void onActionClickWithConfirm(
                            ContactModel contactModel, OnConfirmClickListener<E> listener) {
                        OpenClawRobotInfo robot = (OpenClawRobotInfo) contactModel.getExtra();
                        if (robot == null || TextUtils.isEmpty(robot.getBotId())) {
                            return;
                        }
                        boolean checked = selectedBotIds.contains(robot.getBotId());
                        ContactModel.CheckType newCheckType =
                                checked
                                        ? ContactModel.CheckType.UNCHECKED
                                        : ContactModel.CheckType.CHECKED;
                        if (listener != null) {
                            listener.onActionClick((E) Boolean.TRUE);
                        }
                        contactModel.setCheckType(newCheckType);
                        if (checked) {
                            selectedBotIds.remove(robot.getBotId());
                        } else {
                            selectedBotIds.add(robot.getBotId());
                        }
                        updateConfirmState();
                    }
                });
        loadData();
        return view;
    }

    private void loadData() {
        viewModel
                .getMyRobots()
                .observe(
                        getViewLifecycleOwner(),
                        resource -> {
                            if (resource.status == Status.SUCCESS) {
                                myRobots =
                                        resource.data == null ? new ArrayList<>() : resource.data;
                                OpenClawRobotRegistry.registerAll(requireContext(), myRobots);
                                updateAvailableRobots();
                            } else if (resource.status == Status.ERROR) {
                                ToastUtils.showErrorToast(resource.code);
                                myRobots =
                                        OpenClawRobotRegistry.getRegisteredRobots(requireContext());
                                updateAvailableRobots();
                            }
                        });
        if (TextUtils.isEmpty(groupId)) {
            groupRobots = new ArrayList<>();
            updateAvailableRobots();
            return;
        }
        viewModel
                .getGroupRobots(groupId)
                .observe(
                        getViewLifecycleOwner(),
                        resource -> {
                            if (resource.status == Status.SUCCESS) {
                                groupRobots =
                                        resource.data == null ? new ArrayList<>() : resource.data;
                                updateAvailableRobots();
                            } else if (resource.status == Status.ERROR) {
                                ToastUtils.showErrorToast(resource.code);
                                groupRobots = new ArrayList<>();
                                updateAvailableRobots();
                            }
                        });
    }

    private void updateAvailableRobots() {
        if (myRobots == null || groupRobots == null) {
            return;
        }
        Set<String> addedBotIds = new HashSet<>();
        for (OpenClawRobotInfo robot : groupRobots) {
            if (robot != null && !TextUtils.isEmpty(robot.getBotId())) {
                addedBotIds.add(robot.getBotId());
            }
        }
        List<OpenClawRobotInfo> availableRobots = new ArrayList<>();
        for (OpenClawRobotInfo robot : myRobots) {
            if (robot == null || TextUtils.isEmpty(robot.getBotId())) {
                continue;
            }
            if (addedBotIds.contains(robot.getBotId()) || !matchesQuery(robot)) {
                continue;
            }
            availableRobots.add(robot);
        }
        updateList(availableRobots);
    }

    private boolean matchesQuery(OpenClawRobotInfo robot) {
        if (TextUtils.isEmpty(query)) {
            return true;
        }
        String normalizedQuery = query.toLowerCase(Locale.ROOT);
        String name = robot.getName() == null ? "" : robot.getName().toLowerCase(Locale.ROOT);
        String botId = robot.getBotId() == null ? "" : robot.getBotId().toLowerCase(Locale.ROOT);
        return name.contains(normalizedQuery) || botId.contains(normalizedQuery);
    }

    private void updateList(List<OpenClawRobotInfo> robots) {
        List<ContactModel> contactModels = toContactModels(robots);
        if (contactModels.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            contactListComponent.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            contactListComponent.setVisibility(View.VISIBLE);
            contactListComponent.setContactList(contactModels);
        }
        updateConfirmState();
    }

    private List<ContactModel> toContactModels(List<OpenClawRobotInfo> robots) {
        List<ContactModel> contactModels = new ArrayList<>();
        if (robots == null) {
            return contactModels;
        }
        for (OpenClawRobotInfo robot : robots) {
            FriendInfo friendInfo = new FriendInfo();
            friendInfo.setUserId(robot.getBotId());
            friendInfo.setName(
                    TextUtils.isEmpty(robot.getName()) ? robot.getBotId() : robot.getName());
            friendInfo.setPortraitUri(robot.getPortraitUri());
            ContactModel.CheckType checkType =
                    selectedBotIds.contains(robot.getBotId())
                            ? ContactModel.CheckType.CHECKED
                            : ContactModel.CheckType.UNCHECKED;
            ContactModel<FriendInfo> contactModel =
                    ContactModel.obtain(friendInfo, ContactModel.ItemType.CONTENT, checkType);
            contactModel.putExtra(robot);
            contactModels.add(contactModel);
        }
        return contactModels;
    }

    private void updateConfirmState() {
        if (headComponent != null) {
            headComponent.setRightTextViewEnable(!selectedBotIds.isEmpty());
        }
    }

    private void confirmSelection() {
        List<OpenClawRobotInfo> selectedRobots = new ArrayList<>();
        List<String> selectedIds = new ArrayList<>();
        for (OpenClawRobotInfo robot : myRobots) {
            if (robot != null && selectedBotIds.contains(robot.getBotId())) {
                selectedRobots.add(robot);
                selectedIds.add(robot.getBotId());
            }
        }
        addRobots(selectedRobots, selectedIds);
    }

    private void addRobots(List<OpenClawRobotInfo> robots, List<String> botIds) {
        if (TextUtils.isEmpty(groupId) || robots.isEmpty() || botIds.isEmpty()) {
            return;
        }
        viewModel
                .addRobots(groupId, botIds)
                .observe(
                        getViewLifecycleOwner(),
                        resource -> {
                            if (resource.status == Status.SUCCESS) {
                                OpenClawRobotRegistry.registerAll(requireContext(), robots);
                                sendRobotsAddedMessage(robots);
                                ToastUtils.showToast(getString(R.string.openclaw_add_success));
                                RouteUtils.routeToConversationActivity(
                                        requireContext(),
                                        ConversationIdentifier.obtainGroup(groupId));
                                requireActivity().finish();
                            } else if (resource.status == Status.ERROR) {
                                ToastUtils.showErrorToast(resource.code);
                            }
                        });
    }

    private void sendRobotsAddedMessage(List<OpenClawRobotInfo> robots) {
        JSONObject dataObj = new JSONObject();
        try {
            JSONArray targetUserIds = new JSONArray();
            JSONArray targetUserDisplayNames = new JSONArray();
            for (OpenClawRobotInfo robot : robots) {
                targetUserIds.put(robot.getBotId());
                targetUserDisplayNames.put(robot.getName());
            }
            dataObj.put("targetUserIds", targetUserIds);
            dataObj.put("targetUserDisplayNames", targetUserDisplayNames);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        SealGroupNotificationMessage message =
                SealGroupNotificationMessage.obtain(
                        RongCoreClient.getInstance().getCurrentUserId(),
                        GroupNotificationMessage.GROUP_OPERATION_ADD,
                        dataObj.toString());
        ConversationIdentifier identifier = ConversationIdentifier.obtainGroup(groupId);
        IMCenter.getInstance().sendMessage(Message.obtain(identifier, message), null, null, null);
    }
}
