package cn.rongcloud.im.newdesign.searchmsg;

import android.os.Bundle;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.rong.common.rlog.RLog;
import io.rong.imkit.base.BaseViewModel;
import io.rong.imlib.model.ConversationIdentifier;
import io.rong.imlib.model.Message;
import java.util.ArrayList;
import java.util.List;

/**
 * 消息搜索 ViewModel
 *
 * <p>负责消息搜索功能
 *
 * @author rongcloud
 * @since 5.12.2
 */
public class SearchMessageViewModel extends BaseViewModel {

    private static final String TAG = SearchMessageViewModel.class.getSimpleName();
    private static final String ARG_CONVERSATION_IDENTIFIER = "conversation_identifier";

    private final MutableLiveData<List<Message>> messageResultData = new MutableLiveData<>();
    private SearchMessageHandler searchMessageHandler;
    private String currentQuery = "";
    private final ConversationIdentifier conversationIdentifier;

    public SearchMessageViewModel(@NonNull Bundle arguments) {
        super(arguments);
        conversationIdentifier = arguments.getParcelable(ARG_CONVERSATION_IDENTIFIER);
        initHandler();
    }

    /** 初始化 Handler */
    private void initHandler() {
        searchMessageHandler = new SearchMessageHandler();
        searchMessageHandler.addDataChangeListener(
                SearchMessageHandler.KEY_SEARCH_MESSAGES,
                messages -> {
                    RLog.d(
                            TAG,
                            "Message search result: " + (messages != null ? messages.size() : 0));
                    messageResultData.postValue(messages != null ? messages : new ArrayList<>());
                });
    }

    /**
     * 搜索消息
     *
     * @param query 搜索关键字
     */
    public void search(@NonNull String query) {
        if (TextUtils.isEmpty(query)) {
            clearResults();
            return;
        }
        if (conversationIdentifier == null) {
            RLog.w(TAG, "search: conversationIdentifier is null, skip search.");
            clearResults();
            return;
        }

        currentQuery = query;
        searchMessageHandler.searchMessages(conversationIdentifier, query);
    }

    /** 清除搜索结果 */
    private void clearResults() {
        currentQuery = "";
        messageResultData.setValue(new ArrayList<>());
    }

    public LiveData<List<Message>> getMessageResultData() {
        return messageResultData;
    }

    public String getCurrentQuery() {
        return currentQuery;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (searchMessageHandler != null) {
            searchMessageHandler.stop();
        }
    }
}
