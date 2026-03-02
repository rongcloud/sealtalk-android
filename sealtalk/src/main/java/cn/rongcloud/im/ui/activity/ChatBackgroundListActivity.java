package cn.rongcloud.im.ui.activity;

import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.sp.UserConfigCache;
import cn.rongcloud.im.ui.BaseActivity;
import cn.rongcloud.im.ui.adapter.ChatBackgroundListAdapter;
import cn.rongcloud.im.ui.adapter.decoration.GridSpacingItemDecoration;
import cn.rongcloud.im.utils.ToastUtils;
import io.rong.imkit.usermanage.component.HeadComponent;

/** 预置图片页面 显示系统预置的聊天背景图片供用户选择 */
public class ChatBackgroundListActivity extends BaseActivity {

    private HeadComponent headComponent;
    private ChatBackgroundListAdapter adapter;
    private UserConfigCache userConfig;
    private String selectedBackgroundUri;
    private String currentBackgroundUri;
    public static final int REQUEST_PREVIEW = 0x1211;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_background_list);
        userConfig = new UserConfigCache(this);
        currentBackgroundUri = userConfig.getChatbgUri();
        initView();
    }

    private void initView() {
        headComponent = findViewById(R.id.head_component);

        RecyclerView rvChatBackgroundList = findViewById(R.id.rv_chat_background_list);
        // 每行显示3个，间隔6dp
        rvChatBackgroundList.setLayoutManager(new GridLayoutManager(this, 3));
        rvChatBackgroundList.addItemDecoration(new GridSpacingItemDecoration(3, 6, false));

        adapter = new ChatBackgroundListAdapter(this);

        // 设置当前选中的背景
        if (!TextUtils.isEmpty(currentBackgroundUri)) {
            adapter.setSelectedBackground(currentBackgroundUri);
            selectedBackgroundUri = currentBackgroundUri;
        }

        // 点击图片：跳转到预览页面或清除背景
        adapter.setOnItemClickListener(
                drawableId -> {
                    // 如果是清除背景选项
                    if (drawableId == ChatBackgroundListAdapter.CLEAR_BACKGROUND) {
                        // 直接清除背景
                        selectedBackgroundUri = null;
                        userConfig.setChatbgUri("");
                        adapter.setSelectedBackground("");
                        ToastUtils.showToast(getString(R.string.seal_select_chat_bg_set_success));

                        // 通知调用方背景已清除
                        setResult(RESULT_OK);
                    } else {
                        // 正常背景图片，跳转到预览页面
                        String uri = drawableIdtoUri(drawableId);

                        Intent intent =
                                new Intent(
                                        ChatBackgroundListActivity.this,
                                        ChatBackgroundPreviewActivity.class);
                        intent.putExtra(IntentExtra.URL, uri);
                        intent.putExtra(
                                IntentExtra.IMAGE_PREVIEW_TYPE,
                                ChatBackgroundPreviewActivity.FROM_PRESET);
                        startActivityForResult(intent, REQUEST_PREVIEW);
                    }
                });

        rvChatBackgroundList.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PREVIEW && resultCode == RESULT_OK && data != null) {
            // 从预览页面返回，用户点击了确定按钮
            String confirmedUri = data.getStringExtra(IntentExtra.URL);
            if (!TextUtils.isEmpty(confirmedUri)) {
                // 保存选中的背景
                selectedBackgroundUri = confirmedUri;
                userConfig.setChatbgUri(confirmedUri);
                adapter.setSelectedBackground(confirmedUri);
                ToastUtils.showToast(getString(R.string.seal_select_chat_bg_set_success));

                // 通知调用方背景已设置成功
                setResult(RESULT_OK);
            }
        }
    }

    private String drawableIdtoUri(int id) {
        return ContentResolver.SCHEME_ANDROID_RESOURCE
                + "://"
                + getResources().getResourcePackageName(id)
                + "/"
                + getResources().getResourceTypeName(id)
                + "/"
                + getResources().getResourceEntryName(id);
    }
}
