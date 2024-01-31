package cn.rongcloud.im.ui.test;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import androidx.annotation.Nullable;
import cn.rongcloud.im.ui.activity.SealTalkDebugTestActivity;
import cn.rongcloud.im.utils.MessageUtil;
import io.rong.imkit.IMCenter;
import io.rong.imkit.conversation.ConversationFragment;
import io.rong.imkit.feature.forward.ForwardClickActions;
import io.rong.imkit.model.UiMessage;
import io.rong.imkit.utils.RouteUtils;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.model.Conversation;
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
