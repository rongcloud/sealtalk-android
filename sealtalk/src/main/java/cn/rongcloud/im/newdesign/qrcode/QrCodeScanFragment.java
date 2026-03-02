package cn.rongcloud.im.newdesign.qrcode;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import cn.rongcloud.im.R;
import cn.rongcloud.im.newdesign.qrcode.QrCodeScanViewModel.ScanResult;
import cn.rongcloud.im.newdesign.qrcode.QrCodeScanViewModel.ScanResultType;
import cn.rongcloud.im.utils.NetworkUtils;
import cn.rongcloud.im.utils.PhotoUtils;
import cn.rongcloud.im.utils.ToastUtils;
import cn.rongcloud.im.utils.log.SLog;
import cn.rongcloud.im.utils.qrcode.QRCodeUtils;
import cn.rongcloud.im.utils.qrcode.barcodescanner.BarcodeResult;
import cn.rongcloud.im.utils.qrcode.barcodescanner.CaptureManager;
import cn.rongcloud.im.utils.qrcode.barcodescanner.DecoratedBarcodeView;
import io.rong.imkit.RongIM;
import io.rong.imkit.base.BaseViewModelFragment;
import io.rong.imkit.usermanage.ViewModelFactory;
import io.rong.imkit.usermanage.component.HeadComponent;
import io.rong.imkit.usermanage.friend.my.profile.MyProfileActivity;
import io.rong.imkit.usermanage.friend.user.profile.UserProfileActivity;
import io.rong.imlib.RongCoreClient;

/**
 * 二维码扫描 Fragment
 *
 * <p>扫描二维码并跳转到相应页面
 *
 * @author rongcloud
 * @since 5.12.0
 */
public class QrCodeScanFragment extends BaseViewModelFragment<QrCodeScanViewModel> {

    private static final String TAG = "QrCodeScanFragment";
    private static final int REQUEST_CODE_CAMERA = 1001;
    private static final int REQUEST_CODE_STORAGE = 1002;

    protected HeadComponent headComponent;
    protected DecoratedBarcodeView barcodeScannerView;
    protected TextView lightControlTv;
    protected TextView tipsTv;

    private CaptureManager captureManager;
    private PhotoUtils photoUtils;
    private boolean isCameraLightOn = false;
    private boolean hasProcessedResult = false; // 防止重复处理结果

    @NonNull
    @Override
    protected QrCodeScanViewModel onCreateViewModel(@NonNull Bundle bundle) {
        return new ViewModelProvider(this, new ViewModelFactory(bundle))
                .get(QrCodeScanViewModel.class);
    }

    @NonNull
    @Override
    public View onCreateView(
            @NonNull Context context,
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle args) {
        View view = inflater.inflate(R.layout.qrcode_fragment_scan, container, false);
        initViews(view);
        return view;
    }

    /** 初始化视图 */
    private void initViews(View view) {
        headComponent = view.findViewById(R.id.head_component);
        barcodeScannerView = view.findViewById(R.id.zxing_barcode_scanner);
        lightControlTv = view.findViewById(R.id.zxing_open_light);
        tipsTv = view.findViewById(R.id.zxing_user_tips);
        view.findViewById(R.id.zxing_select_pic).setOnClickListener(v -> selectFromAlbum());

        // 初始化扫码管理器（传入 savedInstanceState）
        Bundle savedInstanceState = getArguments() != null ? getArguments() : null;
        captureManager = new CaptureManager(requireActivity(), barcodeScannerView);
        captureManager.initializeFromIntent(requireActivity().getIntent(), savedInstanceState);
        captureManager.setOnCaptureResultListener(
                new CaptureManager.OnCaptureResultListener() {
                    @Override
                    public void onCaptureResult(BarcodeResult result) {
                        if (!hasProcessedResult) {
                            hasProcessedResult = true;
                            handleQrCodeResult(result.toString());
                        }
                    }
                });

        // 设置网络状态
        barcodeScannerView
                .getViewFinder()
                .networkChange(!NetworkUtils.isNetWorkAvailable(getContext()));
        if (!NetworkUtils.isNetWorkAvailable(getContext())) {
            captureManager.stopDecode();
        } else {
            captureManager.decode();
        }

        // 设置闪光灯监听
        barcodeScannerView.setTorchListener(
                new DecoratedBarcodeView.TorchListener() {
                    @Override
                    public void onTorchOn() {
                        lightControlTv.setText(R.string.zxing_close_light);
                        isCameraLightOn = true;
                    }

                    @Override
                    public void onTorchOff() {
                        lightControlTv.setText(R.string.zxing_open_light);
                        isCameraLightOn = false;
                    }
                });

        // 闪光灯控制
        lightControlTv.setOnClickListener(v -> switchCameraLight());

        // 初始化相册工具
        // ⭐ 传入 NO_CROP 类型，二维码扫描不需要裁剪图片
        photoUtils =
                new PhotoUtils(
                        new PhotoUtils.OnPhotoResultListener() {
                            @Override
                            public void onPhotoResult(Uri uri) {
                                // ⭐ 使用 Context 和 Uri 来解析图片，而不是使用 uri.getPath()
                                String result = QRCodeUtils.analyzeImage(requireContext(), uri);
                                handleQrCodeResult(result);
                            }

                            @Override
                            public void onPhotoCancel() {}
                        },
                        PhotoUtils.NO_CROP);
    }

    @Override
    protected void onViewReady(@NonNull QrCodeScanViewModel viewModel) {
        // 设置标题
        headComponent.setTitleText(R.string.seal_main_title_scan);

        // 设置返回按钮
        headComponent.setLeftClickListener(v -> finishActivity());

        // 设置右侧相册按钮
        headComponent.setRightText(getString(R.string.common_album));
        headComponent.setRightClickListener(v -> selectFromAlbum());

        // 观察扫描结果
        observeViewModel(viewModel);
    }

    /** 观察 ViewModel 数据变化 */
    private void observeViewModel(QrCodeScanViewModel viewModel) {
        viewModel
                .getScanResultData()
                .observe(
                        this,
                        scanResult -> {
                            if (scanResult != null) {
                                handleScanResult(scanResult);
                            }
                        });

        viewModel
                .getErrorData()
                .observe(
                        this,
                        errorMessage -> {
                            if (errorMessage != null) {
                                ToastUtils.showToast(errorMessage);
                                // 显示错误状态
                                if (barcodeScannerView != null
                                        && barcodeScannerView.getViewFinder() != null) {
                                    barcodeScannerView.getViewFinder().setAllowScanAnimation(false);
                                }
                                if (lightControlTv != null) {
                                    lightControlTv.setVisibility(View.INVISIBLE);
                                }
                                if (tipsTv != null) {
                                    tipsTv.setVisibility(View.INVISIBLE);
                                }
                                // 允许再次扫描
                                hasProcessedResult = false;
                            }
                        });
    }

    /** 切换摄像头照明 */
    private void switchCameraLight() {
        if (isCameraLightOn) {
            barcodeScannerView.setTorchOff();
        } else {
            barcodeScannerView.setTorchOn();
        }
    }

    /** 从相册中选择图片 */
    private void selectFromAlbum() {
        // ⭐ 传入 Fragment 而不是 Activity，这样结果会回调到 Fragment 的 onActivityResult
        photoUtils.selectPicture(this);
    }

    /**
     * 处理二维码扫描结果
     *
     * @param qrCodeText 二维码文本
     */
    private void handleQrCodeResult(String qrCodeText) {
        if (qrCodeText == null || qrCodeText.trim().isEmpty()) {
            SLog.d(TAG, "scanner result is null or empty");
            ToastUtils.showToast(R.string.zxing_qr_can_not_recognized);
            // 允许重新扫描
            hasProcessedResult = false;
            if (captureManager != null) {
                captureManager.decode();
            }
            return;
        }

        SLog.d(TAG, "Scanned QR code: " + qrCodeText);

        // 交给 ViewModel 处理
        getViewModel().handleQrCode(qrCodeText);
    }

    /**
     * 处理扫描结果并跳转
     *
     * @param scanResult 扫描结果
     */
    private void handleScanResult(ScanResult scanResult) {
        ScanResultType type = scanResult.getType();
        String targetId = scanResult.getTargetId();

        switch (type) {
            case USER:
                handleUserResult(targetId);
                break;
            case GROUP:
                handleGroupResult(targetId, scanResult.getFromUserId());
                break;
            case GROUP_ALREADY_IN:
                handleGroupAlreadyInResult(targetId, scanResult.getGroupName());
                break;
            case ERROR:
                // 错误已在 errorData 中处理
                break;
        }
    }

    /**
     * 处理用户扫描结果
     *
     * @param userId 用户 ID
     */
    private void handleUserResult(String userId) {
        String currentUserId = RongCoreClient.getInstance().getCurrentUserId();
        if (userId.equals(currentUserId)) {
            // 跳转到我的资料
            startActivity(MyProfileActivity.newIntent(getContext()));
        } else {
            // 跳转到用户资料
            startActivity(UserProfileActivity.newIntent(getContext(), userId));
        }
        finishActivity();
    }

    /**
     * 处理群组扫描结果（未加入）
     *
     * @param groupId 群组 ID
     * @param fromUserId 来源用户 ID
     */
    private void handleGroupResult(String groupId, String fromUserId) {
        // 跳转到加入群组页面
        startActivity(
                cn.rongcloud.im.newdesign.qrcode.JoinGroupActivity.newIntent(
                        getContext(), groupId));
        finishActivity();
    }

    /**
     * 处理群组扫描结果（已加入）
     *
     * @param groupId 群组 ID
     * @param groupName 群组名称
     */
    private void handleGroupAlreadyInResult(String groupId, String groupName) {
        // 跳转到群聊天界面
        RongIM.getInstance().startGroupChat(getContext(), groupId, groupName);
        finishActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (captureManager != null) {
            captureManager.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (captureManager != null) {
            captureManager.onPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (captureManager != null) {
            captureManager.onDestroy();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (captureManager != null) {
            captureManager.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (captureManager != null) {
            captureManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (photoUtils != null) {
            // ⭐ 传入 Fragment 而不是 Activity，与 selectPicture 保持一致
            photoUtils.onActivityResult(this, requestCode, resultCode, data);
        }
    }

    /**
     * 处理按键事件（支持音量键扫描）
     *
     * @param keyCode 按键代码
     * @param event 按键事件
     * @return 是否处理
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeScannerView != null && barcodeScannerView.onKeyDown(keyCode, event);
    }
}
