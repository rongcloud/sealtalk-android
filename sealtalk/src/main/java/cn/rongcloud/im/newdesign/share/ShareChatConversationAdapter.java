package cn.rongcloud.im.newdesign.share;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.widget.SelectableRoundedImageView;
import cn.rongcloud.im.utils.ImageLoaderUtils;
import io.rong.imkit.userinfo.RongUserInfoManager;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.UserInfo;
import java.util.ArrayList;
import java.util.List;

class ShareChatConversationAdapter
        extends RecyclerView.Adapter<ShareChatConversationAdapter.ViewHolder> {

    interface OnConversationClickListener {
        void onConversationClick(Conversation conversation);
    }

    private final List<Conversation> conversations = new ArrayList<>();
    private final OnConversationClickListener listener;

    ShareChatConversationAdapter(OnConversationClickListener listener) {
        this.listener = listener;
    }

    void setData(List<Conversation> data) {
        conversations.clear();
        if (data != null) {
            conversations.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.select_chat_conversation_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(conversations.get(position));
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final SelectableRoundedImageView ivPortrait;
        private final TextView tvName;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPortrait = itemView.findViewById(R.id.iv_portrait);
            tvName = itemView.findViewById(R.id.tv_name);
        }

        void bind(Conversation conversation) {
            if (conversation == null) {
                return;
            }
            String title = resolveConversationTitle(conversation);
            tvName.setText(title);

            String portraitUrl = conversation.getPortraitUrl();
            if (conversation.getConversationType() == Conversation.ConversationType.GROUP
                    || conversation.getConversationType()
                            == Conversation.ConversationType.ULTRA_GROUP) {
                ImageLoaderUtils.displayGroupPortraitImage(portraitUrl, ivPortrait);
            } else {
                ImageLoaderUtils.displayUserPortraitImage(portraitUrl, ivPortrait);
            }

            itemView.setOnClickListener(
                    v -> {
                        if (listener != null) {
                            listener.onConversationClick(conversation);
                        }
                    });
        }

        private String resolveConversationTitle(Conversation conversation) {
            String title = conversation.getConversationTitle();
            if (!TextUtils.isEmpty(title)) {
                return title;
            }
            Conversation.ConversationType type = conversation.getConversationType();
            if (type == Conversation.ConversationType.GROUP
                    || type == Conversation.ConversationType.ULTRA_GROUP) {
                Group groupInfo =
                        RongUserInfoManager.getInstance().getGroupInfo(conversation.getTargetId());
                if (groupInfo != null && !TextUtils.isEmpty(groupInfo.getName())) {
                    return groupInfo.getName();
                }
            } else {
                UserInfo userInfo =
                        RongUserInfoManager.getInstance().getUserInfo(conversation.getTargetId());
                if (userInfo != null && !TextUtils.isEmpty(userInfo.getName())) {
                    return userInfo.getName();
                }
            }
            return conversation.getTargetId();
        }
    }
}
