package cn.rongcloud.im.newdesign.share;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.dialog.CommonDialog;
import cn.rongcloud.im.ui.view.UserInfoItemView;
import cn.rongcloud.im.utils.ImageLoaderUtils;
import io.rong.imlib.model.Conversation;

/** 分享确认弹窗，复用旧版转发样式 */
public class ShareConfirmDialog extends CommonDialog {

    private static final String ARG_TITLE = "arg_title";
    private static final String ARG_NAME = "arg_name";
    private static final String ARG_PORTRAIT = "arg_portrait";
    private static final String ARG_MESSAGE = "arg_message";
    private static final String ARG_CONVERSATION_TYPE = "arg_conversation_type";

    @Override
    protected View onCreateContentView(ViewGroup container) {
        View view = View.inflate(getContext(), R.layout.dialog_forward, null);

        Bundle params = getExpandParams();
        String title = params != null ? params.getString(ARG_TITLE) : null;
        String name = params != null ? params.getString(ARG_NAME) : null;
        String portrait = params != null ? params.getString(ARG_PORTRAIT) : null;
        String message = params != null ? params.getString(ARG_MESSAGE) : null;
        String conversationTypeStr =
                params != null ? params.getString(ARG_CONVERSATION_TYPE) : null;

        View multiContainer = view.findViewById(R.id.hsv_container);
        multiContainer.setVisibility(View.GONE);

        UserInfoItemView singleItem = view.findViewById(R.id.uiv_selected_info);
        singleItem.setVisibility(View.VISIBLE);

        TextView titleTv = view.findViewById(R.id.tv_title);
        if (!TextUtils.isEmpty(title)) {
            titleTv.setText(title);
        }

        TextView messageTv = view.findViewById(R.id.tv_message);
        if (TextUtils.isEmpty(message)) {
            message = getString(io.rong.imkit.R.string.rc_message_content_image);
        }
        messageTv.setText(message);

        singleItem.setName(name == null ? "" : name);

        // 根据会话类型显示不同的头像
        Conversation.ConversationType conversationType = parseConversationType(conversationTypeStr);
        if (conversationType == Conversation.ConversationType.GROUP
                || conversationType == Conversation.ConversationType.ULTRA_GROUP) {
            ImageLoaderUtils.displayGroupPortraitImage(portrait, singleItem.getHeaderImageView());
        } else {
            ImageLoaderUtils.displayUserPortraitImage(portrait, singleItem.getHeaderImageView());
        }

        return view;
    }

    /** 解析会话类型字符串 */
    private Conversation.ConversationType parseConversationType(String typeStr) {
        if (TextUtils.isEmpty(typeStr)) {
            return Conversation.ConversationType.PRIVATE;
        }
        try {
            return Conversation.ConversationType.valueOf(typeStr);
        } catch (Exception e) {
            return Conversation.ConversationType.PRIVATE;
        }
    }

    public static class Builder extends CommonDialog.Builder {
        private String title;
        private String name;
        private String portrait;
        private String message;
        private Conversation.ConversationType conversationType;

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setPortrait(String portrait) {
            this.portrait = portrait;
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        /** 设置会话类型，用于根据类型显示不同的头像 */
        public Builder setConversationType(Conversation.ConversationType conversationType) {
            this.conversationType = conversationType;
            return this;
        }

        @Override
        public CommonDialog build() {
            Bundle expand = new Bundle();
            expand.putString(ARG_TITLE, title);
            expand.putString(ARG_NAME, name);
            expand.putString(ARG_PORTRAIT, portrait);
            expand.putString(ARG_MESSAGE, message);
            if (conversationType != null) {
                expand.putString(ARG_CONVERSATION_TYPE, conversationType.name());
            }
            setExpandParams(expand);
            return super.build();
        }

        @Override
        protected CommonDialog getCurrentDialog() {
            return new ShareConfirmDialog();
        }
    }
}
