package cn.rongcloud.im.newdesign.qrcode;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import cn.rongcloud.im.R;
import cn.rongcloud.im.newdesign.qrcode.QrCodeDisplayViewModel.DisplayType;
import cn.rongcloud.im.newdesign.share.ShareChatActivity;
import cn.rongcloud.im.utils.ImageLoaderUtils;
import cn.rongcloud.im.utils.ToastUtils;
import cn.rongcloud.im.utils.ViewCapture;
import io.rong.imkit.base.BaseViewModelFragment;
import io.rong.imkit.usermanage.ViewModelFactory;
import io.rong.imkit.usermanage.component.HeadComponent;
import io.rong.imkit.utils.AndroidConstant;
import io.rong.imkit.utils.PermissionCheckUtil;
import io.rong.imlib.model.GroupInfo;
import io.rong.imlib.model.UserProfile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 二维码展示 Fragment
 *
 * <p>显示个人或群组二维码
 *
 * @author rongcloud
 * @since 5.12.0
 */
public class QrCodeDisplayFragment extends BaseViewModelFragment<QrCodeDisplayViewModel> {

    private static final int REQUEST_CODE_SAVE_TO_LOCAL = 1001;
    protected HeadComponent headComponent;
    protected LinearLayout qrCodeCardLl;
    protected ImageView portraitIv;
    protected TextView mainInfoTv;
    protected ImageView qrCodeIv;
    protected TextView qrCodeDescribeTv;

    @NonNull
    @Override
    protected QrCodeDisplayViewModel onCreateViewModel(@NonNull Bundle bundle) {
        return new ViewModelProvider(this, new ViewModelFactory(bundle))
                .get(QrCodeDisplayViewModel.class);
    }

    @NonNull
    @Override
    public View onCreateView(
            @NonNull Context context,
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle args) {
        View view = inflater.inflate(R.layout.profile_activity_show_qrcode, container, false);
        initViews(view);
        return view;
    }

    /** 初始化视图 */
    private void initViews(View view) {
        headComponent = view.findViewById(R.id.head_component);
        qrCodeCardLl = view.findViewById(R.id.profile_fl_card_capture_area_container);
        portraitIv = view.findViewById(R.id.profile_iv_card_info_portrait);
        mainInfoTv = view.findViewById(R.id.profile_tv_qr_info_main);
        qrCodeIv = view.findViewById(R.id.profile_iv_qr_code);
        qrCodeDescribeTv = view.findViewById(R.id.profile_tv_qr_card_info_describe);

        view.findViewById(R.id.profile_tv_qr_save_phone)
                .setOnClickListener(v -> saveQRCodeToLocal());
        view.findViewById(R.id.profile_tv_qr_share_to_sealtalk)
                .setOnClickListener(v -> shareToSealTalk());

        View wechatShareBtn = view.findViewById(R.id.profile_tv_qr_share_to_wechat);
        wechatShareBtn.setVisibility(View.GONE);
    }

    @Override
    protected void onViewReady(@NonNull QrCodeDisplayViewModel viewModel) {
        // 设置标题
        setupTitle(viewModel);

        // 设置返回按钮
        headComponent.setLeftClickListener(v -> finishActivity());

        // 观察数据变化
        observeViewModel(viewModel);

        // 加载数据
        viewModel.loadInfo();

        // 在布局完成后设置二维码尺寸（数据就绪后会自动生成）
        qrCodeIv.getViewTreeObserver()
                .addOnGlobalLayoutListener(
                        new ViewTreeObserver.OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
                                qrCodeIv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                ViewGroup.LayoutParams params = qrCodeIv.getLayoutParams();
                                if (params.width > 0 && params.height > 0) {
                                    // 设置尺寸，如果数据已就绪会立即生成，否则等待数据就绪
                                    viewModel.setQrCodeSize(params.width, params.height);
                                }
                            }
                        });
    }

    /** 设置标题 */
    private void setupTitle(QrCodeDisplayViewModel viewModel) {
        if (viewModel.getDisplayType() == DisplayType.GROUP) {
            headComponent.setTitleText(R.string.profile_group_qrcode);
            qrCodeDescribeTv.setText(R.string.profile_qrcode_group_tips);
        } else {
            headComponent.setTitleText(R.string.seal_main_mine_qrcode);
            qrCodeDescribeTv.setText(R.string.profile_qrcode_private_tips);
        }
    }

    /** 观察 ViewModel 数据变化 */
    private void observeViewModel(QrCodeDisplayViewModel viewModel) {
        viewModel.getUserProfileData().observe(this, this::updateUserInfo);
        viewModel.getGroupInfoData().observe(this, this::updateGroupInfo);
        viewModel.getQrCodeBitmapData().observe(this, this::updateQRCode);
        viewModel
                .getErrorData()
                .observe(
                        this,
                        errorCode -> {
                            if (errorCode != null) {
                                ToastUtils.showToast(
                                        "Error: " + errorCode.getMessage(), Toast.LENGTH_SHORT);
                            }
                        });
    }

    /** 更新用户信息 */
    private void updateUserInfo(UserProfile userProfile) {
        if (userProfile == null) {
            return;
        }

        ImageLoaderUtils.displayUserPortraitImage(userProfile.getPortraitUri(), portraitIv);
        mainInfoTv.setText(userProfile.getName());
    }

    /** 更新群组信息 */
    @SuppressLint("SetTextI18n")
    private void updateGroupInfo(GroupInfo groupInfo) {
        if (groupInfo == null) {
            return;
        }

        ImageLoaderUtils.displayGroupPortraitImage(groupInfo.getPortraitUri(), portraitIv);
        mainInfoTv.setText(groupInfo.getGroupName() + "(" + groupInfo.getMembersCount() + ")");

        qrCodeIv.setVisibility(View.VISIBLE);
        qrCodeDescribeTv.setVisibility(View.VISIBLE);
    }

    /** 更新二维码 */
    private void updateQRCode(Bitmap bitmap) {
        if (bitmap != null) {
            qrCodeIv.setImageBitmap(bitmap);
        }
    }

    /** 保存二维码到本地 */
    private void saveQRCodeToLocal() {
        if (Build.VERSION.SDK_INT < AndroidConstant.ANDROID_TIRAMISU) {
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (!PermissionCheckUtil.requestPermissions(
                    requireActivity(), permissions, REQUEST_CODE_SAVE_TO_LOCAL)) {
                return;
            }
        }

        performSaveToLocal();
    }

    /** 执行保存到本地 */
    private void performSaveToLocal() {
        Bitmap bitmap = getViewModel().getQrCodeBitmapData().getValue();
        if (bitmap == null) {
            ToastUtils.showToast(R.string.common_share_failed, Toast.LENGTH_SHORT);
            return;
        }

        new Thread(
                        () -> {
                            try {
                                File picturesDir =
                                        android.os.Environment.getExternalStoragePublicDirectory(
                                                android.os.Environment.DIRECTORY_PICTURES);
                                File saveDir = new File(picturesDir, "RongCloud");
                                if (!saveDir.exists()) {
                                    saveDir.mkdirs();
                                }

                                String fileName = "QRCode_" + System.currentTimeMillis() + ".png";
                                File file = new File(saveDir, fileName);

                                FileOutputStream fos = new FileOutputStream(file);
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                                fos.flush();
                                fos.close();

                                if (getContext() != null) {
                                    android.media.MediaScannerConnection.scanFile(
                                            getContext(),
                                            new String[] {file.getAbsolutePath()},
                                            null,
                                            null);
                                }

                                if (getActivity() != null) {
                                    getActivity()
                                            .runOnUiThread(
                                                    () -> {
                                                        String msg =
                                                                getString(
                                                                                R.string
                                                                                        .profile_save_picture_at)
                                                                        + ": "
                                                                        + file.getAbsolutePath();
                                                        ToastUtils.showToast(
                                                                msg, Toast.LENGTH_LONG);
                                                    });
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                if (getActivity() != null) {
                                    getActivity()
                                            .runOnUiThread(
                                                    () -> {
                                                        ToastUtils.showToast(
                                                                R.string.common_share_failed,
                                                                Toast.LENGTH_SHORT);
                                                    });
                                }
                            }
                        })
                .start();
    }

    /** 分享到 SealTalk */
    private void shareToSealTalk() {
        Bitmap bitmap = buildShareBitmapWithLightMode();
        Context context = getContext();
        if (bitmap == null || context == null) {
            ToastUtils.showToast(R.string.common_share_failed, Toast.LENGTH_SHORT);
            return;
        }

        File cacheDir = new File(context.getCacheDir(), "share_qr");
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        File imageFile = new File(cacheDir, "qr_share_" + System.currentTimeMillis() + ".png");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
        } catch (IOException e) {
            ToastUtils.showToast(R.string.common_share_failed, Toast.LENGTH_SHORT);
            return;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ignored) {
                }
            }
        }

        Uri uri =
                FileProvider.getUriForFile(
                        context,
                        context.getPackageName()
                                + getString(io.rong.imkit.R.string.rc_authorities_fileprovider),
                        imageFile);
        Intent intent = ShareChatActivity.newIntent(context, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }

    /** 使用强制日间模式渲染并截取分享图 */
    private Bitmap buildShareBitmapWithLightMode() {
        Context context = getContext();
        if (context == null) {
            return null;
        }

        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.uiMode =
                (configuration.uiMode & ~Configuration.UI_MODE_NIGHT_MASK)
                        | Configuration.UI_MODE_NIGHT_NO;
        Context lightContext = context.createConfigurationContext(configuration);
        Context themedContext =
                new ContextThemeWrapper(lightContext, io.rong.imkit.R.style.RCLivelyLightTheme);

        LayoutInflater inflater = LayoutInflater.from(themedContext);
        View shareView = inflater.inflate(R.layout.profile_activity_show_qrcode, null, false);

        ImageView portrait = shareView.findViewById(R.id.profile_iv_card_info_portrait);
        TextView mainInfo = shareView.findViewById(R.id.profile_tv_qr_info_main);
        ImageView qrImage = shareView.findViewById(R.id.profile_iv_qr_code);
        TextView describe = shareView.findViewById(R.id.profile_tv_qr_card_info_describe);
        View captureContainer = shareView.findViewById(R.id.profile_fl_card_capture_area_container);

        // 同步当前页面数据
        if (portraitIv.getDrawable() != null) {
            portrait.setImageDrawable(copyDrawable(portraitIv.getDrawable()));
        }
        mainInfo.setText(mainInfoTv.getText());

        if (qrCodeIv.getDrawable() != null) {
            qrImage.setImageDrawable(copyDrawable(qrCodeIv.getDrawable()));
        }
        describe.setText(qrCodeDescribeTv.getText());
        describe.setVisibility(qrCodeDescribeTv.getVisibility());

        // 测量并布局，仅截取卡片区域
        int widthSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        int heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        shareView.measure(widthSpec, heightSpec);
        int measuredWidth =
                captureContainer.getMeasuredWidth() > 0
                        ? captureContainer.getMeasuredWidth()
                        : qrCodeCardLl.getWidth();
        int measuredHeight =
                captureContainer.getMeasuredHeight() > 0
                        ? captureContainer.getMeasuredHeight()
                        : qrCodeCardLl.getHeight();
        captureContainer.layout(0, 0, measuredWidth, measuredHeight);

        return ViewCapture.getViewBitmap(captureContainer);
    }

    private Drawable copyDrawable(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            return new BitmapDrawable(getResources(), bitmap);
        }
        return drawable.getConstantState() != null
                ? drawable.getConstantState().newDrawable().mutate()
                : drawable.mutate();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_SAVE_TO_LOCAL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                performSaveToLocal();
            }
        }
    }
}
