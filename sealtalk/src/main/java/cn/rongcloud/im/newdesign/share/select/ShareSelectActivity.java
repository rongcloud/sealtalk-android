package cn.rongcloud.im.newdesign.share.select;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import cn.rongcloud.im.R;
import cn.rongcloud.im.newdesign.share.ShareConfirmDialog;
import cn.rongcloud.im.ui.BaseActivity;
import cn.rongcloud.im.utils.ToastUtils;
import io.rong.imkit.IMCenter;
import io.rong.imkit.feature.forward.ForwardClickActions;
import io.rong.imkit.feature.forward.ForwardManager;
import io.rong.imkit.utils.RouteUtils;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.ConversationIdentifier;
import io.rong.imlib.model.Message;
import io.rong.message.ImageMessage;
import java.util.ArrayList;

/** 分享/转发选择好友或群组 Activity 支持两种模式： 1. 图片分享模式 2. 消息转发模式 */
public class ShareSelectActivity extends BaseActivity implements ShareSendHandler {

    private static final String EXTRA_MODE = "extra_mode";
    private static final String EXTRA_IMAGE_URI = "extra_image_uri";

    private static final int MODE_FRIEND = 1;
    private static final int MODE_GROUP = 2;

    // 图片分享模式
    private ImageMessage shareImageMessage;

    // 消息转发模式
    private ForwardClickActions.ForwardType forwardType;
    private ArrayList<Integer> messageIds;

    /** 创建好友列表图片分享 Intent */
    public static Intent newFriendIntent(@NonNull Context context, @NonNull Uri imageUri) {
        Intent intent = new Intent(context, ShareSelectActivity.class);
        intent.putExtra(EXTRA_MODE, MODE_FRIEND);
        intent.putExtra(EXTRA_IMAGE_URI, imageUri);
        return intent;
    }

    /** 创建群组列表图片分享 Intent */
    public static Intent newGroupIntent(@NonNull Context context, @NonNull Uri imageUri) {
        Intent intent = new Intent(context, ShareSelectActivity.class);
        intent.putExtra(EXTRA_MODE, MODE_GROUP);
        intent.putExtra(EXTRA_IMAGE_URI, imageUri);
        return intent;
    }

    /** 创建好友列表消息转发 Intent */
    public static Intent newForwardFriendIntent(
            @NonNull Context context,
            @NonNull ForwardClickActions.ForwardType forwardType,
            @NonNull ArrayList<Integer> messageIds) {
        Intent intent = new Intent(context, ShareSelectActivity.class);
        intent.putExtra(EXTRA_MODE, MODE_FRIEND);
        intent.putExtra(RouteUtils.FORWARD_TYPE, forwardType.getValue());
        intent.putIntegerArrayListExtra(RouteUtils.MESSAGE_IDS, messageIds);
        return intent;
    }

    /** 创建群组列表消息转发 Intent */
    public static Intent newForwardGroupIntent(
            @NonNull Context context,
            @NonNull ForwardClickActions.ForwardType forwardType,
            @NonNull ArrayList<Integer> messageIds) {
        Intent intent = new Intent(context, ShareSelectActivity.class);
        intent.putExtra(EXTRA_MODE, MODE_GROUP);
        intent.putExtra(RouteUtils.FORWARD_TYPE, forwardType.getValue());
        intent.putIntegerArrayListExtra(RouteUtils.MESSAGE_IDS, messageIds);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(io.rong.imkit.R.layout.rc_activity);
        initData();
        attachFragment();
    }

    /** 初始化数据 从 Intent 中读取参数，判断是图片分享模式还是消息转发模式 */
    private void initData() {
        Intent intent = getIntent();

        // 初始化图片分享数据
        Uri imageUri = intent.getParcelableExtra(EXTRA_IMAGE_URI);
        if (imageUri != null) {
            shareImageMessage = ImageMessage.obtain(imageUri, imageUri, true);
        }

        // 初始化消息转发数据
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
        return shareImageMessage != null;
    }

    private void attachFragment() {
        int mode = getIntent().getIntExtra(EXTRA_MODE, MODE_FRIEND);
        Fragment fragment =
                mode == MODE_GROUP ? new ShareGroupListFragment() : new ShareFriendListFragment();
        FragmentManager manager = getSupportFragmentManager();
        manager.popBackStack();
        manager.beginTransaction()
                .replace(io.rong.imkit.R.id.fl_fragment_container, fragment)
                .commit();
    }

    /**
     * 发送到会话（由 Fragment 通过 ShareSendHandler 接口调用） 根据当前模式（转发/分享）执行不同的处理逻辑
     *
     * @param type 会话类型
     * @param targetId 目标 ID（用户 ID 或群组 ID）
     * @param name 会话名称
     * @param portrait 头像 URL
     */
    @Override
    public void sendToConversation(
            Conversation.ConversationType type, String targetId, String name, String portrait) {
        // 消息转发模式
        if (isForwardMode()) {
            forwardToConversation(type, targetId, name, portrait);
            return;
        }

        // 图片分享模式
        if (isImageShareMode()) {
            shareImageToConversation(type, targetId, name, portrait);
            return;
        }

        ToastUtils.showToast(R.string.common_share_failed);
    }

    /**
     * 分享图片到会话 显示确认对话框，用户确认后直接发送图片消息
     *
     * @param type 会话类型
     * @param targetId 目标 ID
     * @param name 会话名称
     * @param portrait 头像 URL
     */
    private void shareImageToConversation(
            Conversation.ConversationType type, String targetId, String name, String portrait) {
        ConversationIdentifier identifier = ConversationIdentifier.obtain(type, targetId, "");
        Message message = Message.obtain(identifier, shareImageMessage);
        new ShareConfirmDialog.Builder()
                .setTitle(getString(R.string.seal_selected_contact_title))
                .setName(name)
                .setPortrait(portrait)
                .setConversationType(type)
                .setDialogButtonClickListener(
                        new cn.rongcloud.im.ui.dialog.CommonDialog.OnDialogButtonClickListener() {
                            @Override
                            public void onPositiveClick(android.view.View v, Bundle bundle) {
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
                                                        ToastUtils.showToast(
                                                                R.string.common_share_success);
                                                        finish();
                                                    }

                                                    @Override
                                                    public void onError(
                                                            Message message,
                                                            RongIMClient.ErrorCode errorCode) {
                                                        ToastUtils.showToast(
                                                                R.string.common_share_failed);
                                                    }
                                                });
                            }

                            @Override
                            public void onNegativeClick(android.view.View v, Bundle bundle) {}
                        })
                .build()
                .show(getSupportFragmentManager(), "share_confirm_dialog");
    }

    /**
     * 转发消息到会话 显示确认对话框，用户确认后通过 ForwardManager 返回结果
     *
     * <p>通过 ForwardManager.setForwardMessageResult() 方法设置转发结果并返回。 该方法会： 1. 将选中的会话列表添加到 Intent 的
     * "conversations" 参数中 2. 保留原 Intent 中的 FORWARD_TYPE 和 MESSAGE_IDS 参数 3. 设置 RESULT_OK 并结束当前
     * Activity 4. 最终回到 ConversationFragment，由 MessageViewModel 完成实际的消息转发
     *
     * @param type 会话类型
     * @param targetId 目标 ID
     * @param name 会话名称
     * @param portrait 头像 URL
     */
    private void forwardToConversation(
            Conversation.ConversationType type, String targetId, String name, String portrait) {
        new ShareConfirmDialog.Builder()
                .setTitle(getString(R.string.seal_selected_contact_title))
                .setName(name)
                .setPortrait(portrait)
                .setConversationType(type)
                .setDialogButtonClickListener(
                        new cn.rongcloud.im.ui.dialog.CommonDialog.OnDialogButtonClickListener() {
                            @Override
                            public void onPositiveClick(android.view.View v, Bundle bundle) {
                                ArrayList<Conversation> conversationList = new ArrayList<>();
                                conversationList.add(Conversation.obtain(type, targetId, ""));

                                // 使用 SDK 的转发管理器设置转发结果
                                // 这会将 conversationList 添加到 Intent 中，并保留原有的 FORWARD_TYPE 和
                                // MESSAGE_IDS
                                ForwardManager.setForwardMessageResult(
                                        ShareSelectActivity.this, conversationList);

                                // 显示成功提示（实际转发在 MessageViewModel 中完成）
                                ToastUtils.showToast(R.string.seal_forward__message_success);
                            }

                            @Override
                            public void onNegativeClick(android.view.View v, Bundle bundle) {}
                        })
                .build()
                .show(getSupportFragmentManager(), "share_confirm_dialog");
    }
}
