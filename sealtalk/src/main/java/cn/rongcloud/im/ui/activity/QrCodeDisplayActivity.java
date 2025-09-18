package cn.rongcloud.im.ui.activity;

import static io.rong.imkit.utils.PermissionCheckUtil.REQUEST_CODE_ASK_PERMISSIONS;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProviders;
import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.db.model.GroupEntity;
import cn.rongcloud.im.db.model.UserInfo;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.model.qrcode.QrCodeDisplayType;
import cn.rongcloud.im.ui.view.SealTitleBar;
import cn.rongcloud.im.utils.BuildVariantUtils;
import cn.rongcloud.im.utils.ImageLoaderUtils;
import cn.rongcloud.im.utils.ToastUtils;
import cn.rongcloud.im.utils.ViewCapture;
import cn.rongcloud.im.utils.log.SLog;
import cn.rongcloud.im.viewmodel.DisplayQRCodeViewModel;
import io.rong.imkit.picture.config.PictureConfig;
import io.rong.imkit.utils.PermissionCheckUtil;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.message.ImageMessage;
import java.io.File;
import java.util.ArrayList;

/** 显示二维码界面 */
public class QrCodeDisplayActivity extends TitleBaseActivity implements View.OnClickListener {
    private final String TAG = "QrCodeDisplayActivity";
    public static final int REQUEST_CODE_ASK_PERMISSIONS = 100;
    private final int REQUEST_CODE_FORWARD_TO_SEALTALK = 1000;

    private final int REQUEST_CODE_SAVE_TO_LOCAL = 1001;

    /** 分享类型定义：SealTalk */
    private final int SHARE_TYPE_SEALTALK = 0;

    /** 分享类型定义：微信 */
    private final int SHARE_TYPE_WECHAT = 1;

    private QrCodeDisplayType qrType;
    private String targetId;
    private String fromId;
    private SealTitleBar sealTitleBar;

    private LinearLayout qrCodeCardLl;
    private ImageView portraitIv;
    private TextView mainInfoTv;
    private TextView subInfoTv;
    private ImageView qrCodeIv;
    private TextView qrCodeDescribeTv;
    private TextView qrNoCodeDescribeTv;

    private DisplayQRCodeViewModel qrCodeViewModel;
    private int shareType = -1; // 分享类型，用于当保存

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sealTitleBar = getTitleBar();
        Intent intent = getIntent();
        if (intent == null) {
            SLog.d(TAG, "intent can't null, to finish.");
            finish();
            return;
        }

        qrType =
                (QrCodeDisplayType)
                        intent.getSerializableExtra(IntentExtra.SERIA_QRCODE_DISPLAY_TYPE);
        targetId = intent.getStringExtra(IntentExtra.STR_TARGET_ID);
        fromId = intent.getStringExtra(IntentExtra.START_FROM_ID);

        if (qrType == null || targetId == null) {
            SLog.d(TAG, "qrType and targetId can't null, to finish.");
            finish();
            return;
        }

        setContentView(getContentViewId());

        initView();
        initViewModel();
    }

    public int getContentViewId() {
        return R.layout.profile_activity_show_qrcode;
    }

    private void initView() {
        qrNoCodeDescribeTv = findViewById(R.id.profile_tv_qr_card_info_no_code_describe);
        // 二维码描述
        qrCodeDescribeTv = findViewById(R.id.profile_tv_qr_card_info_describe);
        if (qrType == QrCodeDisplayType.GROUP) {
            sealTitleBar.setTitle(R.string.profile_group_qrcode);
            qrCodeDescribeTv.setText(R.string.profile_qrcode_group_tips);
        } else if (qrType == QrCodeDisplayType.PRIVATE) {
            sealTitleBar.setTitle(R.string.seal_main_mine_qrcode);
            qrCodeDescribeTv.setText(R.string.profile_qrcode_private_tips);
        }
        // 二维码卡片父容器
        qrCodeCardLl = findViewById(R.id.profile_fl_card_capture_area_container);
        // 二维码信息所属头像
        portraitIv = findViewById(R.id.profile_iv_card_info_portrait);
        // 二维码信息所属名称
        mainInfoTv = findViewById(R.id.profile_tv_qr_info_main);
        // 二维码信息所属副信息
        subInfoTv = findViewById(R.id.profile_tv_qr_info_sub);
        // 二维码图片
        qrCodeIv = findViewById(R.id.profile_iv_qr_code);
        // 保存图片
        findViewById(R.id.profile_tv_qr_save_phone).setOnClickListener(this);
        // 分享至 SealTalk
        findViewById(R.id.profile_tv_qr_share_to_sealtalk).setOnClickListener(this);
        // 分享至微信 - 根据构建变体控制可见性，Develop版本启用，PublishStore版本禁用
        View wechatShareBtn = findViewById(R.id.profile_tv_qr_share_to_wechat);
        if (!BuildVariantUtils.isPublishStoreBuild()) {
            wechatShareBtn.setOnClickListener(this);
            wechatShareBtn.setVisibility(View.VISIBLE);
        } else {
            wechatShareBtn.setVisibility(View.GONE);
        }
    }

    private void initViewModel() {
        qrCodeViewModel = ViewModelProviders.of(this).get(DisplayQRCodeViewModel.class);

        // 获取 QRCode 结果
        qrCodeViewModel
                .getQRCode()
                .observe(
                        this,
                        resource -> {
                            if (resource.data != null) {
                                qrCodeIv.setImageBitmap(resource.data);
                            }
                        });

        ViewGroup.LayoutParams qrCodeLayoutParams = qrCodeIv.getLayoutParams();

        if (qrType == QrCodeDisplayType.GROUP) {
            // 获取群组信息结果
            qrCodeViewModel
                    .getGroupInfo()
                    .observe(
                            this,
                            resource -> {
                                if (resource.data != null) {
                                    updateGroupInfo(resource.data);
                                }
                            });
            // 请求群组信息
            qrCodeViewModel.requestGroupInfo(targetId);
            // 获取群组二维码
            qrCodeViewModel.requestGroupQRCode(
                    targetId, fromId, qrCodeLayoutParams.width, qrCodeLayoutParams.height);
        } else if (qrType == QrCodeDisplayType.PRIVATE) {
            // 获取用户信息结果
            qrCodeViewModel
                    .getUserInfo()
                    .observe(
                            this,
                            resource -> {
                                if (resource.data != null) {
                                    updateUserInfo(resource.data);
                                }
                            });

            // 请求用户信息
            qrCodeViewModel.requestUserInfo(targetId);
            // 获取用户二维码
            qrCodeViewModel.requestUserQRCode(
                    targetId, qrCodeLayoutParams.width, qrCodeLayoutParams.height);
        }

        // 保存图片到本地
        qrCodeViewModel
                .getSaveLocalBitmapResult()
                .observe(
                        this,
                        resource -> {
                            if (resource.status == Status.SUCCESS) {
                                // 保存成功后加入媒体扫描中，使相册中可以显示此图片
                                MediaScannerConnection.scanFile(
                                        QrCodeDisplayActivity.this.getApplicationContext(),
                                        new String[] {resource.data},
                                        null,
                                        null);

                                String msg =
                                        QrCodeDisplayActivity.this.getString(
                                                        R.string.profile_save_picture_at)
                                                + ":"
                                                + resource.data;
                                ToastUtils.showToast(msg, Toast.LENGTH_LONG);
                            }
                        });

        // 分享至 SealTalk 或 微信
        qrCodeViewModel
                .getSaveCacheBitmapResult()
                .observe(
                        this,
                        resource -> {
                            if (resource.status == Status.SUCCESS) {
                                if (shareType == SHARE_TYPE_WECHAT) {
                                    shareToWeChat();
                                } else if (shareType == SHARE_TYPE_SEALTALK) {
                                    shareToSealTalk();
                                }
                            }
                        });
    }

    /**
     * 更新群组相关信息
     *
     * @param groupEntity
     */
    private void updateGroupInfo(GroupEntity groupEntity) {
        ImageLoaderUtils.displayGroupPortraitImage(groupEntity.getPortraitUri(), portraitIv);
        mainInfoTv.setText(groupEntity.getName());
        subInfoTv.setText(getString(R.string.common_member_count, groupEntity.getMemberCount()));
        // 0表示已开启群认证
        if (groupEntity.getCertiStatus() == 0) {
            qrCodeIv.setVisibility(View.INVISIBLE);
            qrCodeDescribeTv.setVisibility(View.INVISIBLE);
            qrNoCodeDescribeTv.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 更新用户相关信息
     *
     * @param userInfo
     */
    private void updateUserInfo(UserInfo userInfo) {
        ImageLoaderUtils.displayUserPortraitImage(userInfo.getPortraitUri(), portraitIv);
        mainInfoTv.setText(userInfo.getName());
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.profile_tv_qr_save_phone) {
            saveQRCodeToLocal();
        } else if (id == R.id.profile_tv_qr_share_to_sealtalk) {
            shareToSealTalk();
        } else if (id == R.id.profile_tv_qr_share_to_wechat) {
            shareToWeChat();
        }
    }

    /** 保存二维码到本地 */
    private void saveQRCodeToLocal() {
        if (Build.VERSION.SDK_INT >= 23) {
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (!PermissionCheckUtil.checkPermissions(this, permissions)) {
                PermissionCheckUtil.requestPermissions(
                        this, permissions, REQUEST_CODE_ASK_PERMISSIONS);
                return;
            }
        }

        qrCodeViewModel.saveQRCodeToLocal(ViewCapture.getViewBitmap(qrCodeCardLl));
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_SAVE_TO_LOCAL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                qrCodeViewModel.saveQRCodeToLocal(ViewCapture.getViewBitmap(qrCodeCardLl));
            }
        } else if (requestCode == PictureConfig.APPLY_STORAGE_PERMISSIONS_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //                mDialog.selectPicture();
            }
        }
    }

    /** 分享至 SealTalk */
    private void shareToSealTalk() {
        Resource<String> resource = qrCodeViewModel.getSaveCacheBitmapResult().getValue();
        if (resource != null && resource.data != null) {
            shareType = -1;
            // 跳转到转发
            File imageFile = new File(resource.data);
            Uri uri;

            // 使用 FileProvider 创建兼容的 URI
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                uri =
                        FileProvider.getUriForFile(
                                this,
                                getPackageName()
                                        + getResources()
                                                .getString(
                                                        io.rong.imkit.R.string
                                                                .rc_authorities_fileprovider),
                                imageFile);
            } else {
                uri = Uri.fromFile(imageFile);
            }

            try {
                ImageMessage imageMessage = ImageMessage.obtain(uri, uri, true);
                // 消息中发送目标需要在转发界面中选择，暂时只填充空消息
                Message message =
                        Message.obtain("", Conversation.ConversationType.NONE, imageMessage);
                Intent intent = new Intent(this, ForwardActivity.class);
                ArrayList<Message> messageList = new ArrayList<>();
                messageList.add(message);
                intent.putParcelableArrayListExtra(IntentExtra.FORWARD_MESSAGE_LIST, messageList);
                intent.putExtra(IntentExtra.BOOLEAN_ENABLE_TOAST, false);
                intent.putExtra(IntentExtra.BOOLEAN_FORWARD_USE_SDK, false);

                // 添加必要的文件访问权限
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                startActivityForResult(intent, REQUEST_CODE_FORWARD_TO_SEALTALK);
            } catch (Exception e) {
                ToastUtils.showToast(R.string.common_share_failed);
                e.printStackTrace();
            }
        } else {
            if (!checkHasStoragePermission()) {
                return;
            }
            shareType = SHARE_TYPE_SEALTALK;
            qrCodeViewModel.saveQRCodeToCache(ViewCapture.getViewBitmap(qrCodeCardLl));
        }
    }

    /** 分享至微信 */
    private void shareToWeChat() {
        // 根据构建变体控制微信分享功能 - Develop版本启用，PublishStore版本禁用
        if (BuildVariantUtils.isPublishStoreBuild()) {
            ToastUtils.showToast(R.string.common_share_failed);
            return;
        }

        Resource<String> resource = qrCodeViewModel.getSaveCacheBitmapResult().getValue();
        if (resource != null && resource.data != null) {
            shareType = -1;
            // Develop版本启用微信分享功能
            try {
                // 这里需要导入WXManager - Develop版本中可用
                Class<?> wxManagerClass = Class.forName("cn.rongcloud.im.wx.WXManager");
                Object wxManager = wxManagerClass.getMethod("getInstance").invoke(null);
                wxManagerClass
                        .getMethod("sharePicture", String.class)
                        .invoke(wxManager, resource.data);
            } catch (Exception e) {
                ToastUtils.showToast(R.string.common_share_failed);
                e.printStackTrace();
            }
        } else {
            if (!checkHasStoragePermission()) {
                return;
            }
            shareType = SHARE_TYPE_WECHAT;
            qrCodeViewModel.saveQRCodeToCache(ViewCapture.getViewBitmap(qrCodeCardLl));
        }
    }

    private boolean checkHasStoragePermission() {
        // Android 10 (API 29) 及以上使用新的存储权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10 及以上使用分区存储，无需 WRITE_EXTERNAL_STORAGE 权限
            return true;
        } else if (Build.VERSION.SDK_INT >= 23) {
            // 从6.0系统(API 23)开始，访问外置存储需要动态申请权限
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

            if (!PermissionCheckUtil.checkPermissions(this, permissions)) {
                PermissionCheckUtil.requestPermissions(
                        this, permissions, REQUEST_CODE_ASK_PERMISSIONS);
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_FORWARD_TO_SEALTALK) {
            if (resultCode == RESULT_OK) {
                ToastUtils.showToast(R.string.common_share_success);
            } else if (requestCode == RESULT_FIRST_USER) {
                ToastUtils.showToast(R.string.common_share_failed);
            }
        }
    }
}
