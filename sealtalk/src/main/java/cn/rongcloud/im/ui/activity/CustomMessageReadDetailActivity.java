package cn.rongcloud.im.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import cn.rongcloud.im.ui.fragment.CustomMessageReadDetailFragment;
import io.rong.imkit.conversation.readreceipt.MessageReadDetailActivity;
import io.rong.imkit.utils.KitConstants;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.ReadReceiptInfoV5;

/**
 * 群组消息阅读状态详情页面
 *
 * @author rongcloud
 * @since 5.30.0
 */
public class CustomMessageReadDetailActivity extends MessageReadDetailActivity {

    @NonNull
    public static Intent newIntent(
            @NonNull Context context,
            @NonNull Message message,
            ReadReceiptInfoV5 readReceiptInfoV5) {
        Intent intent = new Intent(context, CustomMessageReadDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(KitConstants.KEY_MESSAGE, message);
        bundle.putParcelable(KitConstants.KEY_READ_RECEIPT_INFO_V5, readReceiptInfoV5);
        intent.putExtras(bundle);
        return intent;
    }

    @NonNull
    protected Fragment createFragment() {
        Bundle bundle = getIntent().getExtras() != null ? getIntent().getExtras() : new Bundle();
        CustomMessageReadDetailFragment fragment = new CustomMessageReadDetailFragment();
        fragment.setArguments(bundle);
        return fragment;
    }
}
