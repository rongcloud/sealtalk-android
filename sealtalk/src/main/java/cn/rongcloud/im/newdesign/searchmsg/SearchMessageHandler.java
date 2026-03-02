package cn.rongcloud.im.newdesign.searchmsg;

import androidx.annotation.NonNull;
import io.rong.imkit.base.MultiDataHandler;
import io.rong.imlib.IRongCoreCallback;
import io.rong.imlib.IRongCoreEnum;
import io.rong.imlib.RongCoreClient;
import io.rong.imlib.model.ConversationIdentifier;
import io.rong.imlib.model.Message;
import java.util.ArrayList;
import java.util.List;

/**
 * 搜索消息 Handler
 *
 * <p>封装消息搜索功能，使用单会话 searchMessages 接口
 *
 * @author rongcloud
 * @since 5.12.2
 */
public class SearchMessageHandler extends MultiDataHandler {

    private static final String TAG = SearchMessageHandler.class.getSimpleName();

    public static final DataKey<List<Message>> KEY_SEARCH_MESSAGES =
            DataKey.obtain("KEY_SEARCH_MESSAGES", (Class<List<Message>>) (Class<?>) List.class);

    public static final DataKey<Integer> KEY_SEARCH_MESSAGES_TOTAL_COUNT =
            DataKey.obtain("KEY_SEARCH_MESSAGES_TOTAL_COUNT", Integer.class);

    /**
     * 搜索消息
     *
     * @param conversationIdentifier 会话标识
     * @param query 搜索关键字
     */
    public void searchMessages(
            @NonNull ConversationIdentifier conversationIdentifier, @NonNull String query) {
        searchMessages(
                conversationIdentifier,
                query,
                new String[] {"RC:TxtMsg", "RC:ImgTextMsg", "RC:FileMsg"},
                100,
                0);
    }

    /**
     * 搜索消息（支持分页）
     *
     * @param conversationIdentifier 会话标识
     * @param keyword 搜索关键字
     * @param objectNameList 消息类型过滤
     * @param limit 返回数量限制
     * @param startTime 查询 startTime 之前的消息（传 0 时从最新消息开始）
     */
    public void searchMessages(
            @NonNull ConversationIdentifier conversationIdentifier,
            @NonNull String keyword,
            String[] objectNameList,
            int limit,
            long startTime) {
        RongCoreClient.getInstance()
                .searchMessages(
                        conversationIdentifier,
                        keyword,
                        objectNameList,
                        limit,
                        startTime,
                        new IRongCoreCallback.ResultCallback<List<Message>>() {
                            @Override
                            public void onSuccess(List<Message> messages) {
                                if (messages != null) {
                                    notifyDataChange(KEY_SEARCH_MESSAGES, messages);
                                } else {
                                    notifyDataChange(KEY_SEARCH_MESSAGES, new ArrayList<>());
                                }
                                notifyDataChange(KEY_SEARCH_MESSAGES_TOTAL_COUNT, 0);
                            }

                            @Override
                            public void onError(IRongCoreEnum.CoreErrorCode errorCode) {
                                notifyDataChange(KEY_SEARCH_MESSAGES, new ArrayList<>());
                                notifyDataChange(KEY_SEARCH_MESSAGES_TOTAL_COUNT, 0);
                            }
                        });
    }
}
