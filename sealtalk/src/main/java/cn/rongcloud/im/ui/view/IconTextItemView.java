package cn.rongcloud.im.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.rongcloud.im.R;

public class IconTextItemView extends LinearLayout {
    private ImageView ivHead;
    private TextView tvTitle;

    public IconTextItemView(Context context) {
        super(context);
        init(context, null);
    }

    public IconTextItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public IconTextItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(io.rong.imkit.R.layout.rc_item_group_info, this, true);
        ivHead = findViewById(R.id.iv_head);
        tvTitle = findViewById(R.id.tv_title);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IconTextItemView);

            int iconResId =
                    a.getResourceId(
                            R.styleable.IconTextItemView_icon,
                            io.rong.imkit.R.drawable.rc_default_portrait);
            ivHead.setImageResource(iconResId);

            String text = a.getString(R.styleable.IconTextItemView_text);
            tvTitle.setText(text);

            a.recycle();
        }
    }

    // 提供方法设置图标和文字
    public void setIcon(int resId) {
        ivHead.setImageResource(resId);
    }

    public void setText(String text) {
        tvTitle.setText(text);
    }
}
