package cn.rongcloud.im.ui.test;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import cn.rongcloud.im.R;
import cn.rongcloud.im.im.IMManager;
import cn.rongcloud.im.ui.activity.SealTalkDebugTestActivity;
import cn.rongcloud.im.ui.view.CustomAgentFacadePage;
import cn.rongcloud.im.utils.MessageUtil;
import cn.rongcloud.im.utils.ToastUtils;
import io.rong.imkit.IMCenter;
import io.rong.imkit.conversation.ConversationFragment;
import io.rong.imkit.conversation.extension.InputMode;
import io.rong.imkit.conversation.extension.RongExtension;
import io.rong.imkit.conversation.extension.RongExtensionViewModel;
import io.rong.imkit.feature.forward.ForwardClickActions;
import io.rong.imkit.model.UiMessage;
import io.rong.imkit.utils.RouteUtils;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.ConversationIdentifier;
import io.rong.imlib.model.Message;
import io.rong.message.CombineV2Message;
import java.util.ArrayList;
import java.util.List;

/**
 * 自定义ConversationListFragment
 *
 * @author rongcloud
 */
public class CustomConversationFragment extends ConversationFragment {
    private static final String TAG = CustomConversationFragment.class.getSimpleName();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 私聊
        if (getRongExtension().getConversationType() == Conversation.ConversationType.PRIVATE) {
            RongExtensionViewModel mExtensionViewModel =
                    new ViewModelProvider(this).get(RongExtensionViewModel.class);
            // 创建一个ImageView
            ImageView imageView = new ImageView(getContext());
            // 设置图标资源
            imageView.setImageResource(io.rong.imkit.R.drawable.rc_agent_button);

            ConversationIdentifier conversationIdentifier =
                    getRongExtension().getConversationIdentifier();
            CustomAgentFacadePage customAgentFacadePage =
                    new CustomAgentFacadePage(
                            CustomConversationFragment.this, conversationIdentifier);
            View agentFacadeView = customAgentFacadePage.onCreateView();

            // 添加点击事件
            imageView.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if (mExtensionViewModel.getInputModeLiveData().getValue() != null
                                    && mExtensionViewModel
                                            .getInputModeLiveData()
                                            .getValue()
                                            .equals(InputMode.AgentMode)) {

                                getRongExtension().getInputEditText().requestFocus();
                                mExtensionViewModel
                                        .getInputModeLiveData()
                                        .setValue(InputMode.TextInput);
                            } else {
                                mExtensionViewModel
                                        .getInputModeLiveData()
                                        .setValue(InputMode.AgentMode);
                                // 点击按钮执行的操作，例如显示表情或插入特定文本
                                getRongExtension()
                                        .getInputEditContainer()
                                        .setBackgroundResource(
                                                io.rong.imkit.R.drawable.rc_agent_input_bg);

                                RelativeLayout container =
                                        getRongExtension()
                                                .getContainer(RongExtension.ContainerType.BOARD);
                                container.removeAllViews();
                                customAgentFacadePage.onResume();
                                container.addView(agentFacadeView);
                            }
                        }
                    });

            // 将ImageView添加到输入框容器
            getRongExtension().getInputEditContainer().addView(imageView);
        }
    }

    @Override
    protected void noMoreMessageToFetch() {
        // 如果没有更多消息了，关闭下拉刷新
        if (mRefreshLayout != null) {
            mRefreshLayout.setEnableRefresh(false);
        }
        if (IMManager.getInstance()
                        .getContext()
                        .getSharedPreferences("config", MODE_PRIVATE)
                        .getBoolean("isDebug", false)
                && IMManager.getInstance()
                        .getContext()
                        .getSharedPreferences(
                                SealTalkDebugTestActivity.SP_PERMISSION_NAME, MODE_PRIVATE)
                        .getBoolean(SealTalkDebugTestActivity.SP_HINT_NO_MORE_MESSAGE, false)) {
            ToastUtils.showToast(R.string.msg_no_more_to_fetch);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        boolean isCombineV2 =
                IMCenter.getInstance()
                        .getContext()
                        .getSharedPreferences(
                                SealTalkDebugTestActivity.SP_PERMISSION_NAME, MODE_PRIVATE)
                        .getBoolean(SealTalkDebugTestActivity.SP_COMBINE_V2, false);
        if (isCombineV2
                && requestCode == REQUEST_CODE_FORWARD
                && data != null
                && mMessageViewModel != null) {
            int forwardType = data.getIntExtra(RouteUtils.FORWARD_TYPE, 0);
            if (ForwardClickActions.ForwardType.MULTI.getValue() == forwardType) {
                List<Message> messageList = new ArrayList<>();
                for (UiMessage uiMessage : mMessageViewModel.getSelectedUiMessages()) {
                    messageList.add(uiMessage.getMessage());
                }
                ArrayList<Conversation> conversations =
                        data.<Conversation>getParcelableArrayListExtra("conversations");
                sendCombineMessage(
                        mMessageViewModel.getCurConversationType(), conversations, messageList);
                mMessageViewModel.quitEditMode();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sendCombineMessage(
            Conversation.ConversationType curConversationType,
            List<Conversation> conversations,
            List<Message> messages) {
        if (conversations == null || conversations.size() == 0) {
            return;
        }

        for (Conversation conversation : conversations) {
            CombineV2Message combineV2Message =
                    MessageUtil.convertMessage2CombineV2Message(curConversationType, messages);
            Message message =
                    Message.obtain(
                            conversation.getTargetId(),
                            conversation.getConversationType(),
                            combineV2Message);

            IMCenter.getInstance()
                    .sendMediaMessage(
                            message, null, null, (IRongCallback.ISendMediaMessageCallback) null);

            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                io.rong.common.rlog.RLog.e(TAG, "forwardMessageByStep e:" + e.toString());
            }
        }
    }
}
