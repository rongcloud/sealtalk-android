package cn.rongcloud.im.openclaw.component;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.rongcloud.im.R;
import io.rong.imkit.config.IMKitThemeManager;

public class OpenClawPageUtils {
    public static final int COLOR_BACKGROUND = Color.rgb(243, 246, 249);
    public static final int COLOR_PRIMARY = Color.rgb(0, 71, 255);
    public static final int COLOR_TEXT_PRIMARY = Color.rgb(2, 8, 20);
    public static final int COLOR_TEXT_SECONDARY = Color.rgb(124, 131, 142);
    public static final int COLOR_CARD = Color.WHITE;
    public static final int COLOR_ICON_PURPLE = Color.rgb(151, 71, 255);

    public static LinearLayout verticalRoot(Context context) {
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(backgroundColor(context));
        root.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        return root;
    }

    public static TextView text(Context context, String value, int sp) {
        TextView textView = new TextView(context);
        textView.setText(value);
        textView.setTextSize(sp);
        textView.setTextColor(textPrimaryColor(context));
        textView.setIncludeFontPadding(true);
        return textView;
    }

    public static TextView titleText(Context context, String value, int sp) {
        TextView textView = text(context, value, sp);
        textView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return textView;
    }

    public static TextView secondaryText(Context context, String value, int sp) {
        TextView textView = text(context, value, sp);
        textView.setTextColor(textSecondaryColor(context));
        return textView;
    }

    public static TextView empty(Context context, String value) {
        TextView textView = secondaryText(context, value, 15);
        textView.setGravity(Gravity.CENTER);
        textView.setVisibility(View.GONE);
        return textView;
    }

    public static TextView primaryButton(Context context, String value) {
        TextView button = buttonText(context, value);
        button.setTextColor(Color.WHITE);
        button.setBackground(roundRect(primaryColor(context), dp(context, 6)));
        return button;
    }

    public static TextView secondaryButton(Context context, String value) {
        TextView button = buttonText(context, value);
        button.setTextColor(primaryColor(context));
        button.setBackground(roundRect(cardColor(context), dp(context, 6)));
        return button;
    }

    public static EditText input(Context context, String hint) {
        EditText input = new EditText(context);
        input.setSingleLine(true);
        input.setTextSize(17);
        input.setTextColor(textPrimaryColor(context));
        input.setHintTextColor(textSecondaryColor(context));
        input.setHint(hint);
        input.setPadding(dp(context, 12), 0, dp(context, 12), 0);
        input.setBackground(roundRect(cardColor(context), dp(context, 2)));
        return input;
    }

    public static EditText searchInput(Context context) {
        EditText input = input(context, context.getString(R.string.seal_search_hint));
        input.setTextSize(17);
        input.setPadding(dp(context, 12), 0, dp(context, 12), 0);
        input.setBackground(roundRect(cardColor(context), dp(context, 18)));
        input.setSingleLine(true);
        input.setEllipsize(TextUtils.TruncateAt.END);
        return input;
    }

    public static ImageView robotIcon(Context context, int sizeDp) {
        ImageView icon = new ImageView(context);
        icon.setImageResource(R.drawable.openclaw_ic_ai_robot_avatar);
        icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        icon.setLayoutParams(
                new LinearLayout.LayoutParams(dp(context, sizeDp), dp(context, sizeDp)));
        return icon;
    }

    public static LinearLayout card(Context context) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(roundRect(cardColor(context), dp(context, 10)));
        return card;
    }

    public static RecyclerView recyclerView(Context context) {
        RecyclerView recyclerView = new RecyclerView(context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setBackgroundColor(backgroundColor(context));
        recyclerView.setLayoutParams(new LinearLayout.LayoutParams(-1, 0, 1));
        return recyclerView;
    }

    public static int dp(Context context, int value) {
        return (int) (value * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    public static void padding(View view, int left, int top, int right, int bottom) {
        Context context = view.getContext();
        view.setPadding(
                dp(context, left), dp(context, top), dp(context, right), dp(context, bottom));
    }

    public static LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public static LinearLayout.LayoutParams matchWrapWithTop(Context context, int topDp) {
        LinearLayout.LayoutParams params = matchWrap();
        params.topMargin = dp(context, topDp);
        return params;
    }

    public static LinearLayout.LayoutParams size(Context context, int widthDp, int heightDp) {
        return new LinearLayout.LayoutParams(dp(context, widthDp), dp(context, heightDp));
    }

    public static GradientDrawable roundRect(@ColorInt int color, int radiusPx) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radiusPx);
        return drawable;
    }

    public static int primaryColor(Context context) {
        return colorFromAttr(context, io.rong.imkit.R.attr.rc_primary_color, COLOR_PRIMARY);
    }

    public static int textPrimaryColor(Context context) {
        return colorFromAttr(
                context, io.rong.imkit.R.attr.rc_text_primary_color, COLOR_TEXT_PRIMARY);
    }

    public static int textSecondaryColor(Context context) {
        return colorFromAttr(
                context, io.rong.imkit.R.attr.rc_text_secondary_color, COLOR_TEXT_SECONDARY);
    }

    public static int backgroundColor(Context context) {
        return colorFromAttr(
                context, io.rong.imkit.R.attr.rc_user_manager_background_color, COLOR_BACKGROUND);
    }

    public static int cardColor(Context context) {
        return colorFromAttr(context, io.rong.imkit.R.attr.rc_common_background_color, COLOR_CARD);
    }

    private static TextView buttonText(Context context, String value) {
        TextView button = new TextView(context);
        button.setText(value);
        button.setTextSize(17);
        button.setGravity(Gravity.CENTER);
        button.setIncludeFontPadding(false);
        button.setClickable(true);
        button.setFocusable(true);
        return button;
    }

    private static int colorFromAttr(Context context, int attrId, int fallback) {
        int color = IMKitThemeManager.getColorFromAttrId(context, attrId);
        return color == 0 ? fallback : color;
    }
}
