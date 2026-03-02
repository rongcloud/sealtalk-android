package cn.rongcloud.im.newdesign.share;

import androidx.annotation.NonNull;
import io.rong.imkit.base.MultiDataHandler;
import io.rong.imlib.IRongCoreCallback;
import io.rong.imlib.IRongCoreEnum;
import io.rong.imlib.RongCoreClient;
import io.rong.imlib.model.Conversation;
import java.util.ArrayList;
import java.util.List;

class ShareChatHandler extends MultiDataHandler {

    static final DataKey<List<Conversation>> KEY_RECENT_CONVERSATIONS =
            DataKey.obtain(
                    "KEY_RECENT_CONVERSATIONS", (Class<List<Conversation>>) (Class<?>) List.class);

    void loadRecentConversations(
            long timeStamp,
            int count,
            @NonNull Conversation.ConversationType... conversationTypes) {
        RongCoreClient.getInstance()
                .getConversationListByPage(
                        new IRongCoreCallback.ResultCallback<List<Conversation>>() {
                            @Override
                            public void onSuccess(List<Conversation> conversations) {
                                notifyDataChange(
                                        KEY_RECENT_CONVERSATIONS,
                                        conversations != null ? conversations : new ArrayList<>());
                            }

                            @Override
                            public void onError(IRongCoreEnum.CoreErrorCode coreErrorCode) {
                                notifyDataChange(KEY_RECENT_CONVERSATIONS, new ArrayList<>());
                            }
                        },
                        timeStamp,
                        count,
                        conversationTypes);
    }
}
