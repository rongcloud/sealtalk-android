package cn.rongcloud.im.ui.adapter.viewholders;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.adapter.models.SearchConversationModel;
import cn.rongcloud.im.ui.interfaces.OnChatItemClickListener;
import cn.rongcloud.im.utils.CharacterParser;
import cn.rongcloud.im.utils.ImageLoaderUtils;
import io.rong.imkit.config.IMKitThemeManager;
import io.rong.imlib.model.SearchConversationResult;

public class SearchConversationViewHolder extends BaseViewHolder<SearchConversationModel> {
    private ImageView portrait;
    private TextView tvName;
    private TextView tvDetail;
    private OnChatItemClickListener listener;
    private SearchConversationModel model;

    public SearchConversationViewHolder(@NonNull View itemView, OnChatItemClickListener l) {
        super(itemView);
        this.listener = l;
        portrait = itemView.findViewById(R.id.iv_portrait);
        tvName = itemView.findViewById(R.id.tv_name);
        tvDetail = itemView.findViewById(R.id.tv_detail);
        itemView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null) {
                            listener.OnChatItemClicked(model);
                        }
                    }
                });
    }

    @Override
    public void update(SearchConversationModel searchConversationModel) {
        model = searchConversationModel;
        SearchConversationResult result = searchConversationModel.getBean();
        tvName.setText(searchConversationModel.getName());
        if (result.getMatchCount() > 1) {
            String detailText =
                    String.format(
                            itemView.getContext().getString(R.string.seal_search_item_chat_records),
                            result.getMatchCount());
            SpannableStringBuilder coloredText = new SpannableStringBuilder(detailText);
            String matchCount = String.valueOf(result.getMatchCount());
            int start = detailText.indexOf(matchCount);
            if (start >= 0) {
                int color =
                        IMKitThemeManager.getColorFromAttrId(
                                itemView.getContext(), io.rong.imkit.R.attr.rc_primary_color);
                coloredText.setSpan(
                        new ForegroundColorSpan(color),
                        start,
                        start + matchCount.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            tvDetail.setText(coloredText);
        } else {
            tvDetail.setText(
                    CharacterParser.getColoredChattingRecord(
                            searchConversationModel.getFilter(),
                            result.getConversation().getLatestMessage(),
                            itemView.getContext()));
        }
        ImageLoaderUtils.displayUserPortraitImage(
                searchConversationModel.getPortraitUrl(), portrait);
    }
}
