package cn.rongcloud.im.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.file.FileManager;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.task.SecurityTask;
import cn.rongcloud.im.ui.adapter.ImageUploadAdapter;
import cn.rongcloud.im.utils.ToastUtils;
import io.rong.imkit.activity.RongBaseActivity;
import io.rong.imkit.config.RongConfigCenter;
import io.rong.imkit.conversation.extension.component.plugin.IPluginRequestPermissionResultCallback;
import io.rong.imkit.picture.PictureSelector;
import io.rong.imkit.picture.config.PictureMimeType;
import io.rong.imkit.picture.entity.LocalMedia;
import io.rong.imkit.utils.PermissionCheckUtil;
import io.rong.imlib.RongIMClient;
import java.util.ArrayList;
import java.util.List;

/**
 * 举报详情页面
 *
 * @author rongcloud
 */
public class ReportDetailActivity extends RongBaseActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PREVIEW = 2;

    private static final int MAX_IMAGE_COUNT = 9;

    public static final String CATEGORY_NAME = "categoryName";
    public static final String SUB_CATEGORY_NAME = "subCategoryName";

    private TextView imageCountText;
    private EditText etReportContent;
    private RecyclerView recyclerView;
    private ImageUploadAdapter adapter;
    private List<Pair<Uri, ImageUploadAdapter.UploadStatus>> imageUploads;

    private FileManager fileManager;
    private SecurityTask securityTask;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<String> uploadedUrls = new ArrayList<>();

    public static Intent newIntent(
            Context context,
            int conversationType,
            String targetId,
            String categoryName,
            String subCategoryName) {
        Intent intent = new Intent(context, ReportDetailActivity.class);
        intent.putExtra(IntentExtra.SERIA_CONVERSATION_TYPE, conversationType);
        intent.putExtra(IntentExtra.STR_TARGET_ID, targetId);
        intent.putExtra(CATEGORY_NAME, categoryName);
        intent.putExtra(SUB_CATEGORY_NAME, subCategoryName);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fileManager = new FileManager(this);
        securityTask = new SecurityTask(this.getApplicationContext());
        setContentView(R.layout.activity_report_detail);

        if (mTitleBar != null) {
            mTitleBar.setTitle(getString(R.string.report_title));
            mTitleBar.setRightVisible(false);
        }

        imageCountText = findViewById(R.id.tv_image_count);
        etReportContent = findViewById(R.id.et_report_content);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        imageUploads = new ArrayList<>();
        adapter =
                new ImageUploadAdapter(
                        imageUploads,
                        this::uploadImage,
                        this::showImagePreview,
                        this::onAddImageClick);
        recyclerView.setAdapter(adapter);

        Button submitButton = findViewById(R.id.btn_submit);
        submitButton.setOnClickListener(v -> submitReport());

        updateImageCount();
        updateAddImageButton();
    }

    private void onAddImageClick() {
        dispatchTakePictureIntent();
    }

    private void dispatchTakePictureIntent() {
        if (PermissionCheckUtil.checkMediaStoragePermissions(this)) {
            PictureSelector.create(this)
                    .openGallery(PictureMimeType.ofImage())
                    .loadImageEngine(RongConfigCenter.featureConfig().getKitImageEngine())
                    .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                    .videoDurationLimit(1)
                    .gifSizeLimit(RongIMClient.getInstance().getGIFLimitSize() * 1024)
                    .maxSelectNum(MAX_IMAGE_COUNT - imageUploads.size())
                    .imageSpanCount(3)
                    .isGif(true)
                    .forResult(REQUEST_IMAGE_CAPTURE);
        } else {
            String[] permissions = PermissionCheckUtil.getMediaStoragePermissions(this);
            PermissionCheckUtil.requestPermissions(
                    this,
                    permissions,
                    IPluginRequestPermissionResultCallback.REQUEST_CODE_PERMISSION_PLUGIN);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            List<LocalMedia> selectList = PictureSelector.obtainMultipleResult(data);
            for (int i = 0; i < selectList.size(); i++) {
                LocalMedia item = selectList.get(i);
                Uri imageUri = Uri.parse(item.getPath());
                Pair<Uri, ImageUploadAdapter.UploadStatus> imageUpload =
                        new Pair<>(imageUri, ImageUploadAdapter.UploadStatus.PENDING);
                imageUploads.add(imageUpload);
                adapter.notifyItemInserted(imageUploads.size() - 1);
                handler.postDelayed(() -> uploadImage(imageUpload), 500L * i);
            }
            updateImageCount();
            updateAddImageButton();
        } else if (requestCode == REQUEST_IMAGE_PREVIEW && resultCode == RESULT_OK) {
            int position = data.getIntExtra(ReportImagePreviewActivity.EXTRA_IMAGE_POSITION, -1);
            if (position != -1) {
                removeImage(position);
            }
        }
    }

    private void uploadImage(Pair<Uri, ImageUploadAdapter.UploadStatus> imageUpload) {
        int index = imageUploads.indexOf(imageUpload);
        if (index >= 0) {
            imageUpload = imageUploads.get(index);
            updateUploadStatus(imageUpload, ImageUploadAdapter.UploadStatus.UPLOADING);

            LiveData<Resource<String>> resourceLiveData =
                    fileManager.uploadImage(imageUpload.first);

            Pair<Uri, ImageUploadAdapter.UploadStatus> finalImageUpload = imageUploads.get(index);
            resourceLiveData.observe(
                    this,
                    resource -> {
                        if (resource == null) {
                            return;
                        }
                        switch (resource.status) {
                            case SUCCESS:
                                uploadedUrls.add(resource.data);
                                updateUploadStatus(
                                        finalImageUpload, ImageUploadAdapter.UploadStatus.SUCCESS);
                                break;
                            case ERROR:
                                updateUploadStatus(
                                        finalImageUpload, ImageUploadAdapter.UploadStatus.FAILED);
                                break;
                            case LOADING:
                                updateUploadStatus(
                                        finalImageUpload,
                                        ImageUploadAdapter.UploadStatus.UPLOADING);
                                break;
                            default:
                                break;
                        }
                    });
        }
    }

    private void updateUploadStatus(
            Pair<Uri, ImageUploadAdapter.UploadStatus> imageUpload,
            ImageUploadAdapter.UploadStatus newStatus) {
        int index = imageUploads.indexOf(imageUpload);
        if (index >= 0) {
            imageUploads.set(index, new Pair<>(imageUpload.first, newStatus));
            runOnUiThread(() -> adapter.notifyItemChanged(index));
        }
    }

    private void updateAddImageButton() {
        if (imageUploads.size() >= MAX_IMAGE_COUNT) {
            // 移除添加图片按钮
            if (adapter.isAddButtonVisible()) {
                adapter.setAddButtonVisible(false);
                adapter.notifyItemRemoved(imageUploads.size());
            }
        } else {
            // 显示添加图片按钮
            if (!adapter.isAddButtonVisible()) {
                adapter.setAddButtonVisible(true);
                adapter.notifyItemInserted(imageUploads.size());
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private void updateImageCount() {
        imageCountText.setText(String.format("%d/%d", imageUploads.size(), MAX_IMAGE_COUNT));
    }

    private void showImagePreview(int index) {
        Uri imageUri = imageUploads.get(index).first;
        Intent intent = ReportImagePreviewActivity.newIntent(this, imageUri, index);
        startActivityForResult(intent, REQUEST_IMAGE_PREVIEW);
    }

    private void removeImage(int position) {
        imageUploads.remove(position);
        adapter.notifyItemRemoved(position);
        updateImageCount();
        updateAddImageButton();
    }

    private void submitReport() {
        // 获取举报内容
        String content = etReportContent.getText().toString().trim();

        // 检查内容长度是否符合要求
        if (content.length() > 200) {
            ToastUtils.showToast(R.string.report_content_too_long);
            return;
        }

        // 收集上传成功的图片URL
        for (Pair<Uri, ImageUploadAdapter.UploadStatus> imageUpload : imageUploads) {
            if (imageUpload.second != ImageUploadAdapter.UploadStatus.SUCCESS) {
                ToastUtils.showToast(R.string.upload_images_first);
                return;
            }
        }

        // 检查是否至少有一张图片成功上传
        if (uploadedUrls.isEmpty()) {
            ToastUtils.showToast(R.string.upload_at_least_one_image);
            return;
        }

        String[] pics = uploadedUrls.toArray(new String[0]);

        if (securityTask != null) {
            securityTask
                    .reportUser(
                            getIntentExtraConversationType(),
                            getIntentExtraTargetId(),
                            getIntentExtraCategoryName(),
                            getIntentExtraSubCategoryName(),
                            pics,
                            content)
                    .observe(
                            this,
                            resource -> {
                                if (resource != null) {
                                    switch (resource.status) {
                                        case SUCCESS:
                                            // 成功后关闭页面
                                            // ToastUtils.showToast(R.string.report_success);
                                            startActivity(ReportSuccessActivity.newIntent(this));
                                            finish();
                                            break;
                                        case ERROR:
                                            // 失败后提示用户
                                            ToastUtils.showToast(R.string.report_failed);
                                            break;
                                        case LOADING:
                                            // 可以在这里显示加载动画
                                            break;
                                        default:
                                            break;
                                    }
                                }
                            });
        }
    }

    private int getIntentExtraConversationType() {
        return getIntent().getIntExtra(IntentExtra.SERIA_CONVERSATION_TYPE, 1);
    }

    private String getIntentExtraTargetId() {
        return getIntent().getStringExtra(IntentExtra.STR_TARGET_ID);
    }

    private String getIntentExtraCategoryName() {
        return getIntent().getStringExtra(CATEGORY_NAME);
    }

    private String getIntentExtraSubCategoryName() {
        return getIntent().getStringExtra(SUB_CATEGORY_NAME);
    }
}
