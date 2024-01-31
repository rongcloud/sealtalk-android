package cn.rongcloud.im.ui.adapter;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.activity.CombinePreviewActivity;
import cn.rongcloud.im.utils.MessageUtil;
import io.rong.imkit.conversation.messgelist.provider.BaseMessageItemProvider;
import io.rong.imkit.model.UiMessage;
import io.rong.imkit.widget.adapter.IViewProviderListener;
import io.rong.imkit.widget.adapter.ViewHolder;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.message.CombineV2Message;
import java.util.List;

/**
 * 合并转发消息展示 提供者
 *
 * @author rongcloud
 * @since 1.0
 */
public class CombineV2MessageItemProvider extends BaseMessageItemProvider<CombineV2Message> {

    public CombineV2MessageItemProvider() {}

    @Override
    protected ViewHolder onCreateMessageContentViewHolder(ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.rc_item_combine_v2_message, parent, false);
        return new ViewHolder(view.getContext(), view);
    }

    @Override
    protected void bindMessageContentViewHolder(
            ViewHolder holder,
            ViewHolder parentHolder,
            CombineV2Message combineMessage,
            UiMessage uiMessage,
            int position,
            List<UiMessage> list,
            IViewProviderListener<UiMessage> listener) {
        boolean isSender =
                uiMessage.getMessage().getMessageDirection().equals(Message.MessageDirection.SEND);
        holder.setTextColor(
                R.id.title,
                ContextCompat.getColor(holder.getContext(), R.color.rc_text_main_color));

        String title = MessageUtil.getTitle(holder.getContext(), combineMessage);
        holder.setText(R.id.title, title);

        LinearLayout llSummary = holder.getView(R.id.ll_summary);
        llSummary.removeAllViews();
        List<String> summarys = combineMessage.getSummaryList();
        for (int i = 0; i < summarys.size() && i < 4; i++) {
            TextView textView = new TextView(holder.getContext());
            textView.setText(summarys.get(i));
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setTextColor(
                    ContextCompat.getColor(holder.getContext(), R.color.rc_text_color_secondary));
            textView.setTextSize(12);
            textView.setMaxLines(1);
            llSummary.addView(textView);
        }
    }

    @Override
    protected boolean onItemClick(
            ViewHolder holder,
            CombineV2Message combineMessage,
            UiMessage uiMessage,
            int position,
            List<UiMessage> list,
            IViewProviderListener<UiMessage> listener) {
        holder.getContext()
                .startActivity(
                        CombinePreviewActivity.newIntent(
                                holder.getContext(), uiMessage.getMessage()));
        return false;
    }

    @Override
    protected boolean isMessageViewType(MessageContent messageContent) {
        return messageContent instanceof CombineV2Message;
    }

    @Override
    public Spannable getSummarySpannable(Context context, CombineV2Message combineMessage) {
        return new SpannableString(
                context.getString(R.string.rc_conversation_summary_content_combine));
    }
}
