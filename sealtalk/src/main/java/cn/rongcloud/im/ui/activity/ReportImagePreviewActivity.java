package cn.rongcloud.im.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import cn.rongcloud.im.R;
import com.bumptech.glide.Glide;
import io.rong.imkit.activity.RongBaseActivity;

/**
 * 举报预览图片
 *
 * @author rongcloud
 */
public class ReportImagePreviewActivity extends RongBaseActivity {

    static final String EXTRA_IMAGE_URI = "extra_image_uri";
    static final String EXTRA_IMAGE_POSITION = "extra_image_position";
    private int imagePosition;

    public static Intent newIntent(Context context, Uri imageUri, int imagePosition) {
        Intent intent = new Intent(context, ReportImagePreviewActivity.class);
        intent.putExtra(EXTRA_IMAGE_URI, imageUri);
        intent.putExtra(EXTRA_IMAGE_POSITION, imagePosition);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_image_preview);

        if (mTitleBar != null) {
            mTitleBar.setRightVisible(false);
        }

        ImageView previewImageView = findViewById(R.id.iv_preview_image);
        Button deleteButton = findViewById(R.id.btn_delete_image);
        // 设置删除按钮点击事件
        deleteButton.setOnClickListener(v -> deleteImage());

        // 获取传递过来的图片及其位置
        Uri imageUri = getIntent().getParcelableExtra(EXTRA_IMAGE_URI);
        imagePosition = getIntent().getIntExtra(EXTRA_IMAGE_POSITION, -1);

        if (imageUri != null) {
            Glide.with(this).load(imageUri).into(previewImageView);
        }
    }

    private void deleteImage() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_IMAGE_POSITION, imagePosition);
        setResult(RESULT_OK, resultIntent);
        finish(); // 关闭预览页面并返回
    }
}
