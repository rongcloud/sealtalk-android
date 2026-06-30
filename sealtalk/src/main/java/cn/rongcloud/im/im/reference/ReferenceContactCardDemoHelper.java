package cn.rongcloud.im.im.reference;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.activity.SealTalkDebugTestActivity;
import io.rong.contactcard.message.ContactMessage;
import io.rong.imkit.config.RongConfigCenter;
import io.rong.imkit.feature.reference.ReferenceContentInputBarProvider;
import io.rong.imkit.feature.reference.ReferenceContentMessageItemProvider;
import io.rong.imkit.feature.reference.ReferenceInputBarAction;
import io.rong.imkit.feature.reference.ReferenceMenuItemFilter;
import io.rong.imkit.model.UiMessage;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;

public final class ReferenceContactCardDemoHelper {
    private static final String CONTACT_CARD_OBJECT_NAME = "RC:CardMsg";

    private ReferenceContactCardDemoHelper() {}

    public static void apply(Context context) {
        if (!SealTalkDebugTestActivity.isReferenceContactCardCustomContentEnabled(context)) {
            return;
        }
        RongConfigCenter.conversationConfig()
                .setReferenceMenuItemFilter(
                        new ReferenceMenuItemFilter() {
                            @Override
                            public boolean shouldShowReferenceMenuItem(UiMessage uiMessage) {
                                if (uiMessage == null || uiMessage.getMessage() == null) {
                                    return false;
                                }
                                MessageContent content = uiMessage.getMessage().getContent();
                                if (content instanceof ContactMessage) {
                                    return true;
                                }
                                return RongConfigCenter.conversationConfig()
                                        .getDefaultReferenceMenuItemFilter()
                                        .shouldShowReferenceMenuItem(uiMessage);
                            }
                        });
        RongConfigCenter.conversationConfig()
                .addReferenceContentMessageItemProvider(
                        CONTACT_CARD_OBJECT_NAME, new ContactCardReferenceContentMessageProvider());
        RongConfigCenter.conversationConfig()
                .addReferenceContentInputBarProvider(
                        CONTACT_CARD_OBJECT_NAME,
                        new ContactCardReferenceContentInputBarProvider());
    }

    private static final class ContactCardReferenceContentMessageProvider
            extends ReferenceContentMessageItemProvider<ContactMessage> {
        @Override
        public View onCreateView(Context context, ViewGroup parent) {
            return LayoutInflater.from(context)
                    .inflate(R.layout.item_reference_contact_card_message, parent, false);
        }

        @Override
        public void onBindView(View view, Message quotedMessage, ContactMessage quotedContent) {
            if (view == null || quotedContent == null) {
                return;
            }
            ImageView avatar = view.findViewById(R.id.iv_contact_avatar);
            TextView name = view.findViewById(R.id.tv_contact_name);
            TextView id = view.findViewById(R.id.tv_contact_id);
            if (avatar != null) {
                RongConfigCenter.featureConfig()
                        .getKitImageEngine()
                        .loadUserPortrait(view.getContext(), quotedContent.getImgUrl(), avatar);
            }
            if (name != null) {
                name.setText(
                        TextUtils.isEmpty(quotedContent.getName())
                                ? view.getContext()
                                        .getString(io.rong.contactcard.R.string.rc_plugins_contact)
                                : quotedContent.getName());
            }
            if (id != null) {
                id.setText(TextUtils.isEmpty(quotedContent.getId()) ? "" : quotedContent.getId());
            }
        }

        @Override
        public boolean isReferenceContentType(MessageContent content) {
            return content instanceof ContactMessage;
        }
    }

    private static final class ContactCardReferenceContentInputBarProvider
            extends ReferenceContentInputBarProvider<ContactMessage> {
        @Override
        public View onCreateView(
                Context context, ViewGroup parent, ReferenceInputBarAction action) {
            View view =
                    LayoutInflater.from(context)
                            .inflate(R.layout.item_reference_contact_card_input_bar, parent, false);
            View cancel = view.findViewById(R.id.tv_cancel_reference);
            if (cancel != null) {
                cancel.setOnClickListener(
                        v -> {
                            if (action != null) {
                                action.cancelReference();
                            }
                        });
            }
            return view;
        }

        @Override
        public void onBindView(View view, Message quotedMessage, ContactMessage quotedContent) {
            if (view == null || quotedContent == null) {
                return;
            }
            ImageView avatar = view.findViewById(R.id.iv_contact_avatar);
            TextView name = view.findViewById(R.id.tv_contact_name);
            TextView id = view.findViewById(R.id.tv_contact_id);
            if (avatar != null) {
                RongConfigCenter.featureConfig()
                        .getKitImageEngine()
                        .loadUserPortrait(view.getContext(), quotedContent.getImgUrl(), avatar);
            }
            if (name != null) {
                name.setText(
                        TextUtils.isEmpty(quotedContent.getName())
                                ? view.getContext()
                                        .getString(io.rong.contactcard.R.string.rc_plugins_contact)
                                : quotedContent.getName());
            }
            if (id != null) {
                id.setText(TextUtils.isEmpty(quotedContent.getId()) ? "" : quotedContent.getId());
            }
        }

        @Override
        public boolean isReferenceContentType(MessageContent content) {
            return content instanceof ContactMessage;
        }
    }
}
