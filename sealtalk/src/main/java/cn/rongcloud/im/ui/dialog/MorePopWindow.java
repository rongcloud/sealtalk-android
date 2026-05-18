package cn.rongcloud.im.ui.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupWindow;
import cn.rongcloud.im.R;
import cn.rongcloud.im.im.IMManager;
import cn.rongcloud.im.sp.MinorModeCache;
import cn.rongcloud.im.ui.activity.SealTalkDebugTestActivity;

public class MorePopWindow extends PopupWindow implements PopupWindow.OnDismissListener {
    private Activity context;
    private OnPopWindowItemClickListener listener;
    private View contentView;
    private static final float ALPHA_TRANSPARENT_COMPLETE = 1.0f;

    public interface OnPopWindowItemClickListener {
        void onStartChartClick();

        void onCreateGroupClick();

        void onAddFriendClick();

        void onScanClick();

        void onAiAssistantClick();
    }

    @SuppressLint("InflateParams")
    public MorePopWindow(final Activity context, OnPopWindowItemClickListener listener) {
        this.listener = listener;
        this.context = context;
        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentView = inflater.inflate(R.layout.main_popup_title_more, null);

        // 设置SelectPicPopupWindow的View
        this.setContentView(contentView);
        // 设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(LayoutParams.WRAP_CONTENT);
        // 设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(LayoutParams.WRAP_CONTENT);
        // 设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        this.setOutsideTouchable(true);
        // 刷新状态
        this.update();
        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0000000000);
        // 点back键和其他地方使其消失,设置了这个才能触发OnDismisslistener ，设置其他控件变化等操作
        this.setBackgroundDrawable(dw);

        setOnDismissListener(this);

        // 设置SelectPicPopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.style.AnimationMainTitleMore);
        contentView
                .findViewById(R.id.btn_start_chat)
                .setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (listener != null) {
                                    listener.onStartChartClick();
                                }
                                dismiss();
                            }
                        });
        contentView
                .findViewById(R.id.btn_create_group)
                .setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (listener != null) {
                                    listener.onCreateGroupClick();
                                }
                                dismiss();
                            }
                        });
        contentView
                .findViewById(R.id.btn_add_friends)
                .setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (listener != null) {
                                    listener.onAddFriendClick();
                                }
                                dismiss();
                            }
                        });
        contentView
                .findViewById(R.id.btn_ai_assistant)
                .setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (listener != null) {
                                    listener.onAiAssistantClick();
                                }
                                dismiss();
                            }
                        });
        contentView
                .findViewById(R.id.btn_scan)
                .setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (listener != null) {
                                    listener.onScanClick();
                                }
                                dismiss();
                            }
                        });

        // 未成年人模式下隐藏添加好友和创建群组入口
        updateMinorModeRestrictions();
    }

    /** 根据未成年人模式状态更新UI */
    private void updateMinorModeRestrictions() {
        String currentUserId = IMManager.getInstance().getCurrentId();
        boolean isMinorModeEnabled = MinorModeCache.getInstance().isMinorModeEnabled(currentUserId);

        // 隐藏添加好友入口
        View btnAddFriends = contentView.findViewById(R.id.btn_add_friends);
        if (btnAddFriends != null) {
            btnAddFriends.setVisibility(!isMinorModeEnabled ? View.VISIBLE : View.GONE);
        }

        // 隐藏创建群组入口
        View btnCreateGroup = contentView.findViewById(R.id.btn_create_group);
        if (btnCreateGroup != null) {
            btnCreateGroup.setVisibility(!isMinorModeEnabled ? View.VISIBLE : View.GONE);
        }

        // 隐藏扫一扫入口
        View btnScan = contentView.findViewById(R.id.btn_scan);
        if (btnScan != null) {
            btnScan.setVisibility(!isMinorModeEnabled ? View.VISIBLE : View.GONE);
        }

        View btnAiAssistant = contentView.findViewById(R.id.btn_ai_assistant);
        if (btnAiAssistant != null) {
            btnAiAssistant.setVisibility(
                    SealTalkDebugTestActivity.isUserManagementEnabled(context)
                            ? View.VISIBLE
                            : View.GONE);
        }
        updatePopupItemBackgrounds();
    }

    private void updatePopupItemBackgrounds() {
        View[] items =
                new View[] {
                    contentView.findViewById(R.id.btn_start_chat),
                    contentView.findViewById(R.id.btn_create_group),
                    contentView.findViewById(R.id.btn_add_friends),
                    contentView.findViewById(R.id.btn_scan),
                    contentView.findViewById(R.id.btn_ai_assistant)
                };
        int firstVisible = -1;
        int lastVisible = -1;
        for (int i = 0; i < items.length; i++) {
            View item = items[i];
            if (item != null && item.getVisibility() == View.VISIBLE) {
                if (firstVisible < 0) {
                    firstVisible = i;
                }
                lastVisible = i;
            }
        }
        for (int i = 0; i < items.length; i++) {
            View item = items[i];
            if (item == null || item.getVisibility() != View.VISIBLE) {
                continue;
            }
            if (i == firstVisible && i == lastVisible) {
                item.setBackgroundResource(R.drawable.seal_sp_pop_item_single_bg);
            } else if (i == firstVisible) {
                item.setBackgroundResource(R.drawable.seal_sp_pop_item_top_bg);
            } else if (i == lastVisible) {
                item.setBackgroundResource(R.drawable.seal_sp_pop_item_bottom_bg);
            } else {
                item.setBackgroundResource(R.drawable.seal_sp_pop_item_normal_bg);
            }
        }
    }

    /**
     * 显示popupWindow
     *
     * @param parent
     */
    public void showPopupWindow(View parent) {
        if (!this.isShowing()) {
            // 以下拉方式显示popupwindow
            this.showAsDropDown(parent, 0, 0);
        } else {
            this.dismiss();
        }
    }

    /**
     * @param parent
     * @param alpha
     */
    public void showPopupWindow(View parent, float alpha, int xoff, int yoff) {
        if (!this.isShowing()) {
            // 以下拉方式显示popupwindow
            this.showAsDropDown(parent, xoff, yoff);
            setAlpha(alpha);
        } else {
            this.dismiss();
            setAlpha(ALPHA_TRANSPARENT_COMPLETE);
        }
    }

    private void setAlpha(float bgAlpha) {
        if (context == null || context.getWindow() == null) {
            return;
        }
        Window window = context.getWindow();
        WindowManager.LayoutParams lp = context.getWindow().getAttributes();
        // 0.0-1.0
        lp.alpha = bgAlpha;
        window.setAttributes(lp);
        // everything behind this window will be dimmed.
        // 此方法用来设置浮动层，防止部分手机变暗无效
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    @Override
    public void onDismiss() {
        super.dismiss();
        setAlpha(ALPHA_TRANSPARENT_COMPLETE);
    }
}
