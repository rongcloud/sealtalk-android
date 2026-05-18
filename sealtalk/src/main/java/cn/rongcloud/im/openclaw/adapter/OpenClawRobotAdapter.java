package cn.rongcloud.im.openclaw.adapter;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cn.rongcloud.im.openclaw.component.OpenClawPageUtils;
import cn.rongcloud.im.openclaw.model.OpenClawRobotInfo;
import cn.rongcloud.im.openclaw.model.OpenClawRobotRegistry;
import io.rong.imkit.config.RongConfigCenter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OpenClawRobotAdapter extends RecyclerView.Adapter<OpenClawRobotAdapter.ViewHolder> {
    private final List<OpenClawRobotInfo> allData = new ArrayList<>();
    private final List<OpenClawRobotInfo> data = new ArrayList<>();
    private final String actionText;
    private String filterText;
    private OnRobotClickListener itemClickListener;
    private OnRobotClickListener actionClickListener;

    public OpenClawRobotAdapter(String actionText) {
        this.actionText = actionText;
    }

    public void setData(List<OpenClawRobotInfo> robots) {
        allData.clear();
        if (robots != null) {
            allData.addAll(robots);
            OpenClawRobotRegistry.registerAll(robots);
        }
        filter(filterText);
    }

    public void filter(String text) {
        filterText = text;
        data.clear();
        if (TextUtils.isEmpty(text)) {
            data.addAll(allData);
        } else {
            String query = text.toLowerCase(Locale.ROOT);
            for (OpenClawRobotInfo robot : allData) {
                String name =
                        robot.getName() == null ? "" : robot.getName().toLowerCase(Locale.ROOT);
                String botId =
                        robot.getBotId() == null ? "" : robot.getBotId().toLowerCase(Locale.ROOT);
                if (name.contains(query) || botId.contains(query)) {
                    data.add(robot);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnRobotClickListener listener) {
        itemClickListener = listener;
    }

    public void setOnActionClickListener(OnRobotClickListener listener) {
        actionClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout container = new LinearLayout(parent.getContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setBackgroundColor(OpenClawPageUtils.cardColor(parent.getContext()));
        container.setLayoutParams(
                new RecyclerView.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout row = new LinearLayout(parent.getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(parent, 16), 0, dp(parent, 16), 0);
        container.addView(row, new LinearLayout.LayoutParams(-1, dp(parent, 51)));

        ImageView avatar = new ImageView(parent.getContext());
        avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        row.addView(avatar, new LinearLayout.LayoutParams(dp(parent, 32), dp(parent, 32)));

        LinearLayout textContainer = new LinearLayout(parent.getContext());
        textContainer.setOrientation(LinearLayout.VERTICAL);
        textContainer.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, -1, 1);
        textParams.setMarginStart(dp(parent, 12));
        row.addView(textContainer, textParams);

        TextView name = new TextView(parent.getContext());
        name.setTextSize(17);
        name.setTextColor(OpenClawPageUtils.textPrimaryColor(parent.getContext()));
        name.setSingleLine(true);
        name.setEllipsize(TextUtils.TruncateAt.END);
        textContainer.addView(name, new LinearLayout.LayoutParams(-1, -2));

        TextView botId = new TextView(parent.getContext());
        botId.setTextSize(12);
        botId.setTextColor(OpenClawPageUtils.textSecondaryColor(parent.getContext()));
        botId.setSingleLine(true);
        botId.setEllipsize(TextUtils.TruncateAt.END);
        textContainer.addView(botId, new LinearLayout.LayoutParams(-1, -2));

        TextView action = new TextView(parent.getContext());
        action.setTextSize(15);
        action.setTextColor(OpenClawPageUtils.primaryColor(parent.getContext()));
        action.setGravity(Gravity.CENTER);
        action.setPadding(dp(parent, 12), 0, dp(parent, 12), 0);
        row.addView(
                action,
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(parent, 44)));

        View divider = new View(parent.getContext());
        divider.setBackgroundColor(0x1A000000);
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(-1, 1);
        dividerParams.setMarginStart(dp(parent, 56));
        dividerParams.setMarginEnd(dp(parent, 16));
        container.addView(divider, dividerParams);
        return new ViewHolder(container, avatar, name, botId, action);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OpenClawRobotInfo robot = data.get(position);
        holder.name.setText(robot.getName());
        holder.botId.setText(robot.getBotId());
        holder.botId.setVisibility(TextUtils.isEmpty(actionText) ? View.GONE : View.VISIBLE);
        holder.action.setVisibility(TextUtils.isEmpty(actionText) ? View.GONE : View.VISIBLE);
        holder.action.setText(actionText);
        holder.itemView.setOnClickListener(
                v -> {
                    if (itemClickListener != null) {
                        itemClickListener.onRobotClick(robot);
                    }
                });
        holder.action.setOnClickListener(
                v -> {
                    if (actionClickListener != null) {
                        actionClickListener.onRobotClick(robot);
                    }
                });
        if (OpenClawRobotRegistry.shouldUseDefaultPortrait(robot.getPortraitUri())) {
            holder.avatar.setImageResource(cn.rongcloud.im.R.drawable.openclaw_ic_ai_robot_avatar);
        } else {
            RongConfigCenter.featureConfig()
                    .getKitImageEngine()
                    .loadUserPortrait(
                            holder.avatar.getContext(), robot.getPortraitUri(), holder.avatar);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private int dp(ViewGroup parent, int value) {
        return (int) (value * parent.getResources().getDisplayMetrics().density + 0.5f);
    }

    public interface OnRobotClickListener {
        void onRobotClick(OpenClawRobotInfo robot);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView avatar;
        final TextView name;
        final TextView botId;
        final TextView action;

        ViewHolder(
                @NonNull View itemView,
                ImageView avatar,
                TextView name,
                TextView botId,
                TextView action) {
            super(itemView);
            this.avatar = avatar;
            this.name = name;
            this.botId = botId;
            this.action = action;
        }
    }
}
