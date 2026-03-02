package cn.rongcloud.im.newdesign.qrcode;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import io.rong.imkit.base.BaseActivity;

/**
 * 二维码扫描 Activity
 *
 * <p>扫描二维码并跳转到相应页面，新版本实现
 *
 * @author rongcloud
 * @since 5.12.0
 */
public class QrCodeScanActivity extends BaseActivity {

    private Fragment fragment;

    /**
     * 创建扫码 Intent
     *
     * @param context 上下文
     * @return Intent
     */
    @NonNull
    public static Intent newIntent(@NonNull Context context) {
        return new Intent(context, QrCodeScanActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(io.rong.imkit.R.layout.rc_activity);

        fragment = createFragment();
        FragmentManager manager = getSupportFragmentManager();
        manager.popBackStack();
        manager.beginTransaction()
                .replace(io.rong.imkit.R.id.fl_fragment_container, fragment)
                .commit();
    }

    /**
     * 创建 Fragment
     *
     * @return Fragment 实例
     */
    @NonNull
    protected Fragment createFragment() {
        Bundle bundle = getIntent().getExtras() != null ? getIntent().getExtras() : new Bundle();
        QrCodeScanFragment fragment = new QrCodeScanFragment();
        fragment.setArguments(bundle);
        return fragment;
    }
}
