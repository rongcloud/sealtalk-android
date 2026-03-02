package cn.rongcloud.im.newdesign.share.select;

import io.rong.imlib.model.Conversation;

interface ShareSendHandler {
    void sendToConversation(
            Conversation.ConversationType type, String targetId, String name, String portrait);
}
