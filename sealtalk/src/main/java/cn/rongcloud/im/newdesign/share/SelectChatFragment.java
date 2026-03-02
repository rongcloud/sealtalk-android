package cn.rongcloud.im.newdesign.share;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.rongcloud.im.R;
import cn.rongcloud.im.newdesign.share.select.ShareSelectActivity;
import cn.rongcloud.im.ui.dialog.CommonDialog;
import cn.rongcloud.im.utils.ToastUtils;
import io.rong.imkit.IMCenter;
import io.rong.imkit.base.BaseViewModelFragment;
import io.rong.imkit.feature.forward.ForwardClickActions;
import io.rong.imkit.feature.forward.ForwardManager;
import io.rong.imkit.userinfo.RongUserInfoManager;
import io.rong.imkit.usermanage.ViewModelFactory;
import io.rong.imkit.usermanage.component.HeadComponent;
import io.rong.imkit.utils.RouteUtils;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.ConversationIdentifier;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.UserInfo;
import io.rong.message.ImageMessage;
import java.util.ArrayList;
import java.util.List;

/** 选择会话 Fragment 支持两种模式： 1. 图片分享模式 2. 消息转发模式 */
public class SelectChatFragment extends BaseViewModelFragment<ShareChatViewModel> {

    private static final int REQUEST_CODE_SHARE_SELECT = 1001;

    private HeadComponent headComponent;
    private View btnChooseFriend;
    private View btnChooseGroup;
    private RecyclerView rvConversations;
    private LinearLayout llEmpty;
    private TextView tvEmptyHint;

    private ShareChatConversationAdapter conversationListAdapter;

    // 图片分享模式相关
    private Uri shareImageUri;
    private ImageMessage shareImageMessage;

    // 消息转发模式相关
    private ForwardClickActions.ForwardType forwardType;
    private ArrayList<Integer> messageIds;

    @NonNull
    @Override
    protected ShareChatViewModel onCreateViewModel(@NonNull Bundle bundle) {
        return new ViewModelProvider(this, new ViewModelFactory(bundle))
                .get(ShareChatViewModel.class);
    }

    @NonNull
    @Override
    public View onCreateView(
            @NonNull Context context,
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle args) {
        View view = inflater.inflate(R.layout.select_chat_fragment, container, false);
        initViews(view);
        return view;
    }

    @Override
    protected void onViewReady(@NonNull ShareChatViewModel viewModel) {
        initShareContent();
        viewModel.getConversationListLiveData().observe(this, this::updateConversationList);
        viewModel.loadRecentConversations();
    }

    /** 初始化分享/转发内容 从 Intent 中读取参数，判断是图片分享模式还是消息转发模式 */
    private void initShareContent() {
        Intent intent = getActivity() != null ? getActivity().getIntent() : null;
        if (intent == null) {
            return;
        }

        // 检查是否是图片分享模式
        shareImageUri = intent.getParcelableExtra(ShareChatActivity.EXTRA_IMAGE_URI);
        if (shareImageUri != null) {
            shareImageMessage = ImageMessage.obtain(shareImageUri, shareImageUri, true);
        }

        // 检查是否是消息转发模式
        int forwardTypeValue = intent.getIntExtra(RouteUtils.FORWARD_TYPE, -1);
        if (forwardTypeValue != -1) {
            forwardType =
                    forwardTypeValue == ForwardClickActions.ForwardType.SINGLE.getValue()
                            ? ForwardClickActions.ForwardType.SINGLE
                            : ForwardClickActions.ForwardType.MULTI;
            messageIds = intent.getIntegerArrayListExtra(RouteUtils.MESSAGE_IDS);
        }
    }

    /**
     * 判断是否是消息转发模式
     *
     * @return true: 消息转发模式，false: 非消息转发模式
     */
    private boolean isForwardMode() {
        return forwardType != null && messageIds != null && !messageIds.isEmpty();
    }

    /**
     * 判断是否是图片分享模式
     *
     * @return true: 图片分享模式，false: 非图片分享模式
     */
    private boolean isImageShareMode() {
        return shareImageUri != null && shareImageMessage != null;
    }

    private void initViews(View view) {
        headComponent = view.findViewById(R.id.head_component);
        btnChooseFriend = view.findViewById(R.id.ll_choose_friend);
        btnChooseGroup = view.findViewById(R.id.ll_choose_group);
        rvConversations = view.findViewById(R.id.rv_conversations);
        llEmpty = view.findViewById(R.id.ll_empty);
        tvEmptyHint = view.findViewById(R.id.tv_empty_hint);

        headComponent.setTitleText(R.string.seal_select_chat_title);
        headComponent.setLeftClickListener(v -> finishActivity());

        btnChooseFriend.setOnClickListener(
                v -> {
                    if (isImageShareMode()) {
                        startActivityForResult(
                                ShareSelectActivity.newFriendIntent(getContext(), shareImageUri),
                                REQUEST_CODE_SHARE_SELECT);
                    } else if (isForwardMode()) {
                        startActivityForResult(
                                ShareSelectActivity.newForwardFriendIntent(
                                        getContext(), forwardType, messageIds),
                                REQUEST_CODE_SHARE_SELECT);
                    } else {
                        ToastUtils.showToast(R.string.common_share_failed);
                    }
                });
        btnChooseGroup.setOnClickListener(
                v -> {
                    if (isImageShareMode()) {
                        startActivityForResult(
                                ShareSelectActivity.newGroupIntent(getContext(), shareImageUri),
                                REQUEST_CODE_SHARE_SELECT);
                    } else if (isForwardMode()) {
                        startActivityForResult(
                                ShareSelectActivity.newForwardGroupIntent(
                                        getContext(), forwardType, messageIds),
                                REQUEST_CODE_SHARE_SELECT);
                    } else {
                        ToastUtils.showToast(R.string.common_share_failed);
                    }
                });

        rvConversations.setLayoutManager(new LinearLayoutManager(getContext()));
        conversationListAdapter =
                new ShareChatConversationAdapter(
                        conversation -> {
                            if (conversation != null) {
                                handleConversationSelected(conversation);
                            }
                        });
        rvConversations.setAdapter(conversationListAdapter);
    }

    private void updateConversationList(List<Conversation> conversations) {
        if (conversations == null || conversations.isEmpty()) {
            showEmptyView();
            conversationListAdapter.setData(null);
        } else {
            showContentView();
            conversationListAdapter.setData(conversations);
        }
    }

    private void handleConversationSelected(Conversation conversation) {
        if (conversation == null) {
            ToastUtils.showToast(R.string.common_share_failed);
            return;
        }

        // 消息转发模式
        if (isForwardMode()) {
            handleForwardMessage(conversation);
            return;
        }

        // 图片分享模式
        if (isImageShareMode()) {
            handleShareImage(conversation);
            return;
        }

        ToastUtils.showToast(R.string.common_share_failed);
    }

    /**
     * 处理图片分享 显示确认对话框，用户确认后发送图片消息
     *
     * @param conversation 目标会话
     */
    private void handleShareImage(Conversation conversation) {
        String title = resolveConversationTitle(conversation);
        new ShareConfirmDialog.Builder()
                .setTitle(getString(R.string.seal_selected_contact_title))
                .setName(title)
                .setPortrait(conversation.getPortraitUrl())
                .setConversationType(conversation.getConversationType())
                .setDialogButtonClickListener(
                        new CommonDialog.OnDialogButtonClickListener() {
                            @Override
                            public void onPositiveClick(View v, Bundle bundle) {
                                sendShareMessage(conversation);
                            }

                            @Override
                            public void onNegativeClick(View v, Bundle bundle) {}
                        })
                .build()
                .show(getParentFragmentManager(), "share_confirm_dialog");
    }

    /**
     * 处理消息转发 显示确认对话框，用户确认后通过 ForwardManager 返回结果
     *
     * @param conversation 目标会话
     */
    private void handleForwardMessage(Conversation conversation) {
        String title = resolveConversationTitle(conversation);

        new ShareConfirmDialog.Builder()
                .setTitle(getString(R.string.seal_selected_contact_title))
                .setName(title)
                .setPortrait(conversation.getPortraitUrl())
                .setConversationType(conversation.getConversationType())
                .setDialogButtonClickListener(
                        new CommonDialog.OnDialogButtonClickListener() {
                            @Override
                            public void onPositiveClick(View v, Bundle bundle) {
                                forwardMessageToConversation(conversation);
                            }

                            @Override
                            public void onNegativeClick(View v, Bundle bundle) {}
                        })
                .build()
                .show(getParentFragmentManager(), "share_confirm_dialog");
    }

    /**
     * 转发消息到指定会话
     *
     * <p>通过 ForwardManager.setForwardMessageResult() 方法设置转发结果并返回。 该方法会： 1. 将选中的会话列表添加到 Intent 的
     * "conversations" 参数中 2. 保留原 Intent 中的 FORWARD_TYPE 和 MESSAGE_IDS 参数 3. 设置 RESULT_OK 并结束当前
     * Activity 4. 回到 ConversationFragment，由 MessageViewModel 完成实际的消息转发
     *
     * @param conversation 目标会话
     */
    private void forwardMessageToConversation(Conversation conversation) {
        Activity activity = getActivity();
        if (activity == null) {
            ToastUtils.showToast(R.string.common_share_failed);
            return;
        }

        ArrayList<Conversation> conversationList = new ArrayList<>();
        conversationList.add(conversation);

        // 这会将 conversationList 添加到 Intent 中，并保留原有的 FORWARD_TYPE 和 MESSAGE_IDS
        // 使用 SDK 的转发管理器设置转发结果
        ForwardManager.setForwardMessageResult(activity, conversationList);

        // 显示成功提示（实际转发在 MessageViewModel 中完成）
        ToastUtils.showToast(R.string.seal_forward__message_success);
    }

    private void sendShareMessage(Conversation conversation) {
        ConversationIdentifier identifier = ConversationIdentifier.obtain(conversation);
        Message message = Message.obtain(identifier, shareImageMessage);
        IMCenter.getInstance()
                .sendMessage(
                        message,
                        null,
                        null,
                        new IRongCallback.ISendMessageCallback() {
                            @Override
                            public void onAttached(Message message) {}

                            @Override
                            public void onSuccess(Message message) {
                                ToastUtils.showToast(R.string.common_share_success);
                                finishActivity();
                            }

                            @Override
                            public void onError(Message message, RongIMClient.ErrorCode errorCode) {
                                ToastUtils.showToast(R.string.common_share_failed);
                            }
                        });
    }

    private void showEmptyView() {
        rvConversations.setVisibility(View.GONE);
        llEmpty.setVisibility(View.VISIBLE);
        tvEmptyHint.setText(R.string.seal_select_chat_empty);
    }

    private void showContentView() {
        llEmpty.setVisibility(View.GONE);
        rvConversations.setVisibility(View.VISIBLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SHARE_SELECT && resultCode == Activity.RESULT_OK) {
            Activity activity = getActivity();
            if (activity != null) {
                activity.setResult(Activity.RESULT_OK, data);
                activity.finish();
            }
        }
    }

    /** 解析会话标题 优先级：ConversationTitle > RongUserInfoManager 中的信息 > TargetId */
    private String resolveConversationTitle(Conversation conversation) {
        String title = conversation.getConversationTitle();
        if (!TextUtils.isEmpty(title)) {
            return title;
        }
        Conversation.ConversationType type = conversation.getConversationType();
        if (type == Conversation.ConversationType.GROUP
                || type == Conversation.ConversationType.ULTRA_GROUP) {
            Group groupInfo =
                    RongUserInfoManager.getInstance().getGroupInfo(conversation.getTargetId());
            if (groupInfo != null && !TextUtils.isEmpty(groupInfo.getName())) {
                return groupInfo.getName();
            }
        } else {
            UserInfo userInfo =
                    RongUserInfoManager.getInstance().getUserInfo(conversation.getTargetId());
            if (userInfo != null && !TextUtils.isEmpty(userInfo.getName())) {
                return userInfo.getName();
            }
        }
        return conversation.getTargetId();
    }
}
