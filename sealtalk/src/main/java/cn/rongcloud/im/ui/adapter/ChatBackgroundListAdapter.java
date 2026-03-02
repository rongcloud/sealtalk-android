package cn.rongcloud.im.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cn.rongcloud.im.R;

/** 预置背景图片适配器 */
public class ChatBackgroundListAdapter
        extends RecyclerView.Adapter<ChatBackgroundListAdapter.ViewHolder> {

    private Context context;
    private OnItemClickListener listener;
    private String selectedBackgroundUri;

    // 用于表示清除背景的特殊标识
    public static final int CLEAR_BACKGROUND = -1;

    // 预置背景图片资源ID数组（第一个位置保留用于清除背景）
    private int[] backgroundDrawables = {
        CLEAR_BACKGROUND, // 第一个位置用于清除背景
        R.drawable.seal_default_chat_bg2,
        R.drawable.seal_default_chat_bg3,
        R.drawable.seal_default_chat_bg4,
        R.drawable.seal_default_chat_bg5,
        R.drawable.seal_default_chat_bg6
    };

    public ChatBackgroundListAdapter(Context context) {
        this.context = context;
    }

    public void setSelectedBackground(String uri) {
        this.selectedBackgroundUri = uri;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_preset_background, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int drawableId = backgroundDrawables[position];

        // 第一个位置显示"清除背景"选项
        if (position == 0) {
            // 设置为浅灰色背景，表示清除背景
            holder.ivBackground.setImageDrawable(null);
            holder.ivBackground.setBackgroundColor(0xFFF5F5F5);

            // 检查是否当前没有背景（即清除背景状态）
            boolean isSelected = selectedBackgroundUri == null || selectedBackgroundUri.isEmpty();
            holder.ivSelected.setVisibility(isSelected ? View.VISIBLE : View.GONE);
        } else {
            // 其他位置显示正常的背景图片
            holder.ivBackground.setImageResource(drawableId);
            holder.ivBackground.setBackgroundColor(0xFFF5F5F5); // 清除背景色

            // 检查是否为当前选中的背景
            boolean isSelected =
                    selectedBackgroundUri != null
                            && selectedBackgroundUri.contains(
                                    context.getResources().getResourceEntryName(drawableId));
            holder.ivSelected.setVisibility(isSelected ? View.VISIBLE : View.GONE);
        }

        holder.itemView.setOnClickListener(
                v -> {
                    if (listener != null) {
                        listener.onItemClick(drawableId);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return backgroundDrawables.length;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBackground;
        View ivSelected;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBackground = itemView.findViewById(R.id.iv_background);
            ivSelected = itemView.findViewById(R.id.iv_selected);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int drawableId);
    }
}
