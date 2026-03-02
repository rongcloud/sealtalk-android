package cn.rongcloud.im.ui.adapter.viewholders;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.adapter.SelectableConversationAdapter;
import cn.rongcloud.im.ui.adapter.models.CheckableContactModel;
import io.rong.imkit.config.IMKitThemeManager;
import io.rong.imkit.config.RongConfigCenter;
import io.rong.imkit.userinfo.RongUserInfoManager;
import io.rong.imlib.model.Conversation;

/**
 * 可选择的会话列表 ViewHolder 用于清除聊天消息功能
 *
 * @author rongcloud
 */
public class SelectableConversationViewHolder
        extends CheckableBaseViewHolder<CheckableContactModel> {

    private TextView tvTitle;
    private ImageView ivHead;
    private ImageView ivSelect;
    private SelectableConversationAdapter.OnItemClickListener listener;
    private CheckableContactModel model;
    private Context context;

    public SelectableConversationViewHolder(
            @NonNull View itemView, SelectableConversationAdapter.OnItemClickListener listener) {
        super(itemView);
        this.context = itemView.getContext();
        this.listener = listener;
        tvTitle = itemView.findViewById(R.id.tv_title);
        ivHead = itemView.findViewById(R.id.iv_head);
        ivSelect = itemView.findViewById(R.id.iv_conversation_select);

        itemView.setOnClickListener(
                v -> {
                    if (listener != null && model != null) {
                        listener.onItemClick(model);
                    }
                });
    }

    @Override
    public void update(CheckableContactModel checkableContactModel) {
        this.model = checkableContactModel;

        if (checkableContactModel == null) {
            return;
        }

        // 更新选择框状态
        updateCheck(ivSelect, checkableContactModel.getCheckType());

        Object bean = checkableContactModel.getBean();

        // 直接处理 Conversation 类型
        if (bean instanceof Conversation) {
            Conversation conversation = (Conversation) bean;
            updateConversation(conversation);
        }
    }

    /**
     * 更新 Conversation 类型的显示
     *
     * @param conversation 会话对象
     */
    private void updateConversation(Conversation conversation) {
        // 设置会话名称，使用 resolveConversationTitle 方法
        String title = resolveConversationTitle(conversation);
        tvTitle.setText(title);

        // 加载会话头像
        if (conversation.getConversationType() == Conversation.ConversationType.PRIVATE) {
            // 单聊头像
            RongConfigCenter.featureConfig()
                    .getKitImageEngine()
                    .loadUserPortrait(context, conversation.getTargetId(), ivHead);
        } else if (conversation.getConversationType() == Conversation.ConversationType.GROUP) {
            // 群聊头像
            RongConfigCenter.featureConfig()
                    .getKitImageEngine()
                    .loadGroupPortrait(context, conversation.getTargetId(), ivHead);
        } else {
            // 其他类型使用默认头像
            ivHead.setImageResource(
                    IMKitThemeManager.getAttrResId(
                            ivHead.getContext(),
                            io.rong.imkit.R.attr.rc_conversation_list_cell_portrait_msg_img));
        }
    }

    /**
     * 解析会话标题 优先级：ConversationTitle > RongUserInfoManager 中的信息 > TargetId
     *
     * @param conversation 会话对象
     * @return 会话标题
     */
    private String resolveConversationTitle(Conversation conversation) {
        String title = conversation.getConversationTitle();
        if (!TextUtils.isEmpty(title)) {
            return title;
        }
        Conversation.ConversationType type = conversation.getConversationType();
        if (type == Conversation.ConversationType.GROUP
                || type == Conversation.ConversationType.ULTRA_GROUP) {
            io.rong.imlib.model.Group groupInfo =
                    RongUserInfoManager.getInstance().getGroupInfo(conversation.getTargetId());
            if (groupInfo != null && !TextUtils.isEmpty(groupInfo.getName())) {
                return groupInfo.getName();
            }
        } else {
            io.rong.imlib.model.UserInfo userInfo =
                    RongUserInfoManager.getInstance().getUserInfo(conversation.getTargetId());
            if (userInfo != null && !TextUtils.isEmpty(userInfo.getName())) {
                return userInfo.getName();
            }
        }
        return conversation.getTargetId();
    }
}
