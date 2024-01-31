package cn.rongcloud.im.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.BaseActivity;
import cn.rongcloud.im.ui.fragment.CombinePreviewFragment;
import io.rong.imlib.model.Message;

/**
 * 合并转发页面
 *
 * @author rongcloud
 * @since 1.0
 */
public class CombinePreviewActivity extends BaseActivity {

    protected Message mCurrentMessage;

    public static final String KEY_MESSAGE = "KEY_MESSAGE";

    @NonNull
    public static Intent newIntent(@NonNull Context context, Message message) {
        Intent intent = new Intent(context, CombinePreviewActivity.class);
        Bundle extras = new Bundle();
        extras.putParcelable(KEY_MESSAGE, message);
        intent.putExtras(extras);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rc_combine_preview_activity);

        Bundle bundle = getIntent().getExtras();
        mCurrentMessage = bundle.getParcelable(KEY_MESSAGE);

        Fragment fragment = createFragment(getIntent().getExtras());
        FragmentManager manager = getSupportFragmentManager();
        manager.popBackStack();
        manager.beginTransaction().replace(R.id.fl_fragment_container, fragment).commit();
    }

    /**
     * 创建会话列表 Fragment
     *
     * @return {@link CombinePreviewFragment} since 1.0
     */
    @NonNull
    protected Fragment createFragment(Bundle extras) {
        CombinePreviewFragment combinePreviewFragment = new CombinePreviewFragment();
        combinePreviewFragment.setArguments(extras);
        return combinePreviewFragment;
    }
}
