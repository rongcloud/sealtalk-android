package cn.rongcloud.im.newdesign.search;

import androidx.annotation.NonNull;
import io.rong.imkit.base.MultiDataHandler;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.SearchConversationResult;
import java.util.ArrayList;
import java.util.List;

/**
 * 搜索会话 Handler
 *
 * <p>封装会话搜索功能
 *
 * @author rongcloud
 * @since 5.12.2
 */
public class SearchConversationHandler extends MultiDataHandler {

    private static final String TAG = SearchConversationHandler.class.getSimpleName();

    public static final DataKey<List<SearchConversationResult>> KEY_SEARCH_CONVERSATIONS =
            DataKey.obtain(
                    "KEY_SEARCH_CONVERSATIONS",
                    (Class<List<SearchConversationResult>>) (Class<?>) List.class);

    /**
     * 搜索会话
     *
     * @param query 搜索关键字
     */
    public void searchConversations(@NonNull String query) {
        RongIMClient.getInstance()
                .searchConversations(
                        query,
                        new Conversation.ConversationType[] {
                            Conversation.ConversationType.PRIVATE,
                            Conversation.ConversationType.GROUP
                        },
                        new String[] {"RC:TxtMsg", "RC:ImgTextMsg", "RC:FileMsg"},
                        new RongIMClient.ResultCallback<List<SearchConversationResult>>() {
                            @Override
                            public void onSuccess(
                                    List<SearchConversationResult> searchConversationResults) {
                                if (searchConversationResults != null) {
                                    notifyDataChange(
                                            KEY_SEARCH_CONVERSATIONS, searchConversationResults);
                                } else {
                                    notifyDataChange(KEY_SEARCH_CONVERSATIONS, new ArrayList<>());
                                }
                            }

                            @Override
                            public void onError(RongIMClient.ErrorCode errorCode) {
                                notifyDataChange(KEY_SEARCH_CONVERSATIONS, new ArrayList<>());
                            }
                        });
    }
}
