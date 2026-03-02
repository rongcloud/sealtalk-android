package cn.rongcloud.im.newdesign.share;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.rong.imkit.base.BaseViewModel;
import io.rong.imlib.model.Conversation;
import java.util.ArrayList;
import java.util.List;

public class ShareChatViewModel extends BaseViewModel {
    private static final int MAX_CONVERSATION_COUNT = 10;

    private final MutableLiveData<List<Conversation>> conversationListLiveData =
            new MutableLiveData<>();
    private final ShareChatHandler shareChatHandler;

    private static final Conversation.ConversationType[] DEFAULT_TYPES =
            new Conversation.ConversationType[] {
                Conversation.ConversationType.PRIVATE, Conversation.ConversationType.GROUP
            };

    public ShareChatViewModel(@NonNull Bundle arguments) {
        super(arguments);
        shareChatHandler = new ShareChatHandler();
        shareChatHandler.addDataChangeListener(
                cn.rongcloud.im.newdesign.share.ShareChatHandler.KEY_RECENT_CONVERSATIONS,
                conversations -> conversationListLiveData.postValue(limit(conversations)));
    }

    public LiveData<List<Conversation>> getConversationListLiveData() {
        return conversationListLiveData;
    }

    public void loadRecentConversations() {
        shareChatHandler.loadRecentConversations(0, MAX_CONVERSATION_COUNT, DEFAULT_TYPES);
    }

    private List<Conversation> limit(List<Conversation> conversations) {
        if (conversations == null) {
            return new ArrayList<>();
        }
        if (conversations.size() > MAX_CONVERSATION_COUNT) {
            return new ArrayList<>(conversations.subList(0, MAX_CONVERSATION_COUNT));
        }
        return new ArrayList<>(conversations);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        shareChatHandler.stop();
    }
}
