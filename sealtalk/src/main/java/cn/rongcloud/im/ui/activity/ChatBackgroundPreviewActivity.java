package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import androidx.annotation.Nullable;
import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.ui.BaseActivity;
import cn.rongcloud.im.ui.dialog.OperatePictureBottomDialog;
import cn.rongcloud.im.utils.ImageLoaderUtils;
import io.rong.imkit.usermanage.component.HeadComponent;

/** 图片预览页面 支持多种场景：预置背景预览、相册选择预览、发送图片预览、用户头像预览 */
public class ChatBackgroundPreviewActivity extends BaseActivity implements View.OnClickListener {

    private HeadComponent headComponent;
    private ImageView ivContent;
    private RelativeLayout rlSendActions;
    private LinearLayout llSelectOrigin;
    private CheckBox cbSelectOrigin;
    private View btnSend;
    private String uri;

    // 预览类型常量
    public static final int FROM_ALUMB = 0x1247; // 从相册选择
    public static final int FROM_DEFAULT = 0x1237; // 默认背景预览
    public static final int FROM_PRESET = 0x1238; // 预置图片预览
    public static final int FROM_RECENT_PICTURE = 0x1222; // 发送图片
    public static final int FROM_EDIT_USER_DESCRIBE = 0x1224; // 用户头像

    private int previewType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_background_preview);
        Intent intent = getIntent();
        uri = intent.getStringExtra(IntentExtra.URL);
        previewType = intent.getIntExtra(IntentExtra.IMAGE_PREVIEW_TYPE, 0);
        initView();
    }

    private void initView() {
        headComponent = findViewById(R.id.head_component);
        ivContent = findViewById(R.id.iv_content);
        rlSendActions = findViewById(R.id.rl_send_actions);
        llSelectOrigin = findViewById(R.id.ll_select_origin);
        cbSelectOrigin = findViewById(R.id.cb_select_origin);
        btnSend = findViewById(R.id.btn_send);

        llSelectOrigin.setOnClickListener(this);
        btnSend.setOnClickListener(this);

        // 设置左侧返回按钮
        headComponent.setLeftClickListener(v -> finish());

        // 根据不同的预览类型设置UI
        setupUIByType();

        // 加载图片
        loadImage();
    }

    /** 根据预览类型设置UI */
    private void setupUIByType() {
        switch (previewType) {
            case FROM_PRESET:
                // 预置图片预览
                headComponent.setTitleText(getString(R.string.seal_select_chat_bg_preset));
                headComponent.setRightText(getString(io.rong.imkit.R.string.rc_confirm));
                headComponent.setRightClickListener(v -> handleConfirmClick());
                break;
            case FROM_DEFAULT:
                // 默认背景预览（仅查看）
                headComponent.setTitleText(getString(R.string.seal_select_chat_bg_title));
                headComponent.getRightTextView().setVisibility(View.GONE);
                break;
            case FROM_ALUMB:
                // 从相册选择的背景预览
                headComponent.setTitleText(getString(R.string.seal_select_chat_bg_title));
                headComponent.setRightText(getString(io.rong.imkit.R.string.rc_confirm));
                headComponent.setRightClickListener(v -> handleConfirmClick());
                break;
            case FROM_RECENT_PICTURE:
                // 发送图片预览
                headComponent.setTitleText(getString(R.string.seal_select_chat_bg_title));
                rlSendActions.setVisibility(View.VISIBLE);
                headComponent.getRightTextView().setVisibility(View.GONE);
                break;
            case FROM_EDIT_USER_DESCRIBE:
                // 用户头像预览
                headComponent.setTitleText(getString(R.string.profile_picture_detail));
                headComponent.setRightText(getString(io.rong.imkit.R.string.rc_confirm));
                headComponent.setRightClickListener(v -> showOperatePictureDialog());
                break;
        }
    }

    /** 加载图片 */
    private void loadImage() {
        if (uri.toLowerCase().startsWith("http://") || uri.toLowerCase().startsWith("https://")) {
            ImageLoaderUtils.displayUserDescritpionImage(uri, ivContent);
        } else {
            ivContent.setImageURI(Uri.parse(uri));
        }
    }

    /** 处理确认按钮点击 */
    private void handleConfirmClick() {
        switch (previewType) {
            case FROM_PRESET:
            case FROM_ALUMB:
                // 设置为聊天背景
                Intent intent = new Intent();
                intent.putExtra(IntentExtra.URL, uri);
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
    }

    /** 显示头像操作对话框 */
    private void showOperatePictureDialog() {
        OperatePictureBottomDialog operatePictureBottomDialog = new OperatePictureBottomDialog();
        operatePictureBottomDialog.setOnDialogButtonClickListener(
                new OperatePictureBottomDialog.OnDialogButtonClickListener() {
                    @Override
                    public void onClickSave() {
                        Intent intentSend = new Intent();
                        intentSend.putExtra(
                                IntentExtra.OPERATE_PICTURE_ACTION,
                                EditUserDescribeActivity.OPERATE_PICTURE_SAVE);
                        setResult(RESULT_OK, intentSend);
                        finish();
                    }

                    @Override
                    public void onClickDelete() {
                        Intent intentSend = new Intent();
                        intentSend.putExtra(
                                IntentExtra.OPERATE_PICTURE_ACTION,
                                EditUserDescribeActivity.OPERATE_PICTURE_DELETE);
                        setResult(RESULT_OK, intentSend);
                        finish();
                    }
                });
        operatePictureBottomDialog.show(getSupportFragmentManager(), null);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ll_select_origin) {
            // 切换原图选项
            cbSelectOrigin.setChecked(!cbSelectOrigin.isChecked());
        } else if (id == R.id.btn_send) {
            // 发送图片
            Intent intentSend = new Intent();
            intentSend.putExtra(IntentExtra.URL, "file://" + uri);
            intentSend.putExtra(IntentExtra.ORGIN, cbSelectOrigin.isChecked());
            setResult(RESULT_OK, intentSend);
            finish();
        }
    }
}
