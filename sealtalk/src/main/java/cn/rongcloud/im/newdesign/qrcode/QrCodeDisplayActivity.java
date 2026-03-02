package cn.rongcloud.im.newdesign.qrcode;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import cn.rongcloud.im.newdesign.qrcode.QrCodeDisplayViewModel.DisplayType;
import cn.rongcloud.im.ui.BaseActivity;

/**
 * 二维码展示 Activity
 *
 * <p>显示个人或群组二维码，新版本实现
 *
 * @author rongcloud
 * @since 5.12.0
 */
public class QrCodeDisplayActivity extends BaseActivity {

    private static final String EXTRA_DISPLAY_TYPE = "display_type";
    private static final String EXTRA_TARGET_ID = "target_id";
    private static final String EXTRA_FROM_ID = "from_id";

    private Fragment fragment;

    /**
     * 创建显示个人二维码的 Intent
     *
     * @param context 上下文
     * @param userId 用户 ID
     * @return Intent
     */
    @NonNull
    public static Intent newIntentForPrivate(@NonNull Context context, @NonNull String userId) {
        Intent intent = new Intent(context, QrCodeDisplayActivity.class);
        intent.putExtra(EXTRA_DISPLAY_TYPE, DisplayType.PRIVATE.name());
        intent.putExtra(EXTRA_TARGET_ID, userId);
        return intent;
    }

    /**
     * 创建显示群组二维码的 Intent
     *
     * @param context 上下文
     * @param groupId 群组 ID
     * @param fromId 来源用户 ID（可选）
     * @return Intent
     */
    @NonNull
    public static Intent newIntentForGroup(
            @NonNull Context context, @NonNull String groupId, @Nullable String fromId) {
        Intent intent = new Intent(context, QrCodeDisplayActivity.class);
        intent.putExtra(EXTRA_DISPLAY_TYPE, DisplayType.GROUP.name());
        intent.putExtra(EXTRA_TARGET_ID, groupId);
        intent.putExtra(EXTRA_FROM_ID, fromId);
        return intent;
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
        QrCodeDisplayFragment fragment = new QrCodeDisplayFragment();
        fragment.setArguments(bundle);
        return fragment;
    }
}
