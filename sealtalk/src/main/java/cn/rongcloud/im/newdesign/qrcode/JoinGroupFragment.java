package cn.rongcloud.im.newdesign.qrcode;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import cn.rongcloud.im.R;
import cn.rongcloud.im.utils.ImageLoaderUtils;
import cn.rongcloud.im.utils.ToastUtils;
import io.rong.imkit.RongIM;
import io.rong.imkit.base.BaseViewModelFragment;
import io.rong.imkit.usermanage.ViewModelFactory;
import io.rong.imkit.usermanage.component.HeadComponent;
import io.rong.imkit.usermanage.interfaces.OnDataChangeEnhancedListener;
import io.rong.imlib.IRongCoreEnum;
import io.rong.imlib.model.GroupInfo;

/**
 * 加入群聊 Fragment
 *
 * <p>显示群组信息并处理加入群聊操作
 *
 * @author rongcloud
 * @since 5.12.2
 */
public class JoinGroupFragment extends BaseViewModelFragment<JoinGroupViewModel> {

    protected HeadComponent headComponent;
    protected ImageView groupPortraitIv;
    protected TextView groupNameTv;
    protected TextView joinGroupBtn;

    @NonNull
    @Override
    protected JoinGroupViewModel onCreateViewModel(@NonNull Bundle bundle) {
        return new ViewModelProvider(this, new ViewModelFactory(bundle))
                .get(JoinGroupViewModel.class);
    }

    @NonNull
    @Override
    public View onCreateView(
            @NonNull Context context,
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle args) {
        View view = inflater.inflate(R.layout.seal_activity_join_group, container, false);
        initViews(view);
        return view;
    }

    /** 初始化视图 */
    private void initViews(View view) {
        headComponent = view.findViewById(R.id.head_component);
        groupPortraitIv = view.findViewById(R.id.iv_group_portrait);
        groupNameTv = view.findViewById(R.id.tv_group_name);
        joinGroupBtn = view.findViewById(R.id.btn_join_group);

        joinGroupBtn.setOnClickListener(v -> onJoinGroupClick());
    }

    @Override
    protected void onViewReady(@NonNull JoinGroupViewModel viewModel) {
        // 设置标题
        headComponent.setTitleText(R.string.seal_join_group_title);

        // 设置返回按钮
        headComponent.setLeftClickListener(v -> finishActivity());

        // 观察数据变化
        observeViewModel(viewModel);

        // 加载群组信息
        viewModel.loadGroupInfo();
    }

    /** 观察 ViewModel 数据变化 */
    private void observeViewModel(JoinGroupViewModel viewModel) {
        viewModel.getGroupInfoData().observe(this, this::updateGroupInfo);
        viewModel
                .getErrorData()
                .observe(
                        this,
                        errorCode -> {
                            if (errorCode != null) {
                                String errorMessage =
                                        getString(R.string.seal_join_group_failed)
                                                + ": "
                                                + errorCode.getMessage();
                                ToastUtils.showToast(errorMessage, Toast.LENGTH_SHORT);
                            }
                        });
    }

    /** 更新群组信息 */
    private void updateGroupInfo(GroupInfo groupInfo) {
        if (groupInfo == null) {
            return;
        }

        ImageLoaderUtils.displayGroupPortraitImage(groupInfo.getPortraitUri(), groupPortraitIv);

        // 显示群名称和成员数量
        if (groupInfo.getMembersCount() > 0) {
            String groupNameWithCount =
                    groupInfo.getGroupName() + "(" + groupInfo.getMembersCount() + ")";
            groupNameTv.setText(groupNameWithCount);
        } else {
            groupNameTv.setText(groupInfo.getGroupName());
        }
    }

    /** 处理加入群组结果 */
    private void handleJoinResult(IRongCoreEnum.CoreErrorCode resultCode) {
        if (resultCode == null) {
            return;
        }

        // RC_GROUP_JOIN_GROUP_NEED_MANAGER_ACCEPT = 25424 表示需要等待审批
        if (resultCode == IRongCoreEnum.CoreErrorCode.RC_GROUP_JOIN_GROUP_NEED_MANAGER_ACCEPT) {
            ToastUtils.showToast(R.string.seal_join_group_wait_approve, Toast.LENGTH_LONG);
            // 延迟关闭页面
            if (getView() != null) {
                getView()
                        .postDelayed(
                                () -> {
                                    if (getActivity() != null) {
                                        finishActivity();
                                    }
                                },
                                1500);
            }
        } else if (resultCode == IRongCoreEnum.CoreErrorCode.SUCCESS
                || resultCode == IRongCoreEnum.CoreErrorCode.RC_GROUP_MEMBERS_ALREADY_IN_GROUP) {
            ToastUtils.showToast(R.string.seal_join_group_success, Toast.LENGTH_SHORT);
            // 跳转到群聊天界面
            GroupInfo groupInfo = getViewModel().getGroupInfoData().getValue();
            if (groupInfo != null) {
                RongIM.getInstance()
                        .startGroupChat(
                                getContext(), groupInfo.getGroupId(), groupInfo.getGroupName());
                finishActivity();
            }
        } else {
            String errorMessage =
                    getString(R.string.seal_join_group_failed) + ": " + resultCode.getMessage();
            ToastUtils.showToast(errorMessage, Toast.LENGTH_SHORT);
        }
    }

    /** 点击加入群组按钮 */
    private void onJoinGroupClick() {
        JoinGroupViewModel viewModel = getViewModel();
        if (viewModel != null) {
            // 禁用按钮避免重复点击
            joinGroupBtn.setEnabled(false);

            // 调用加入群组
            viewModel.joinGroup(
                    (OnDataChangeEnhancedListener<IRongCoreEnum.CoreErrorCode>)
                            this::handleJoinResult);

            // 延迟恢复按钮状态
            if (getView() != null) {
                getView()
                        .postDelayed(
                                () -> {
                                    if (joinGroupBtn != null) {
                                        joinGroupBtn.setEnabled(true);
                                    }
                                },
                                2000);
            }
        }
    }
}
