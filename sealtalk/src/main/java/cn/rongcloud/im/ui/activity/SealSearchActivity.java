package cn.rongcloud.im.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.TextWatcher;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import cn.rongcloud.im.R;
import cn.rongcloud.im.db.model.FriendShipInfo;
import cn.rongcloud.im.db.model.GroupEntity;
import cn.rongcloud.im.ui.adapter.models.SearchConversationModel;
import cn.rongcloud.im.ui.fragment.SearchAllFragment;
import cn.rongcloud.im.ui.fragment.SearchBaseFragment;
import cn.rongcloud.im.ui.fragment.SearchConversationFragment;
import cn.rongcloud.im.ui.fragment.SearchFriendFragment;
import cn.rongcloud.im.ui.fragment.SearchGroupFragment;
import cn.rongcloud.im.ui.fragment.SearchMessageFragment;
import cn.rongcloud.im.ui.interfaces.OnChatItemClickListener;
import cn.rongcloud.im.ui.interfaces.OnContactItemClickListener;
import cn.rongcloud.im.ui.interfaces.OnGroupItemClickListener;
import cn.rongcloud.im.ui.interfaces.OnMessageRecordClickListener;
import cn.rongcloud.im.ui.interfaces.OnShowMoreClickListener;
import cn.rongcloud.im.utils.log.SLog;
import cn.rongcloud.im.viewmodel.SearchMessageModel;
import io.rong.imkit.RongIM;
import io.rong.imlib.model.ConversationIdentifier;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.SearchConversationResult;

public class SealSearchActivity extends SealSearchBaseActivity
        implements TextWatcher,
                OnContactItemClickListener,
                OnGroupItemClickListener,
                OnChatItemClickListener,
                OnShowMoreClickListener,
                OnMessageRecordClickListener {
    private static final String TAG = "SealSearchActivity";
    private SearchAllFragment searchAllFragment;
    private SearchFriendFragment searchFriendFragment;
    private SearchConversationFragment searchConversationFragment;
    private SearchGroupFragment searchGroupFragment;
    private SearchMessageFragment searchMessageFragment;
    private SearchBaseFragment currentFragment; // 当前Fragment
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        searchAllFragment = new SearchAllFragment();
        searchAllFragment.init(this, this, this, this, null);
        searchFriendFragment = new SearchFriendFragment();
        searchFriendFragment.init(this);
        searchConversationFragment = new SearchConversationFragment();
        searchConversationFragment.init(this);
        searchGroupFragment = new SearchGroupFragment();
        searchGroupFragment.init(this);
        pushFragment(searchAllFragment);
    }

    /**
     * 点击好友
     *
     * @param friendShipInfo
     */
    @Override
    public void onItemContactClick(FriendShipInfo friendShipInfo) {
        Runnable task =
                new Runnable() {
                    @Override
                    public void run() {
                        String displayName = friendShipInfo.getDisplayName();
                        if (TextUtils.isEmpty(displayName)) {
                            displayName = friendShipInfo.getUser().getNickname();
                        }
                        RongIM.getInstance()
                                .startPrivateChat(
                                        SealSearchActivity.this,
                                        friendShipInfo.getUser().getId(),
                                        displayName);
                    }
                };
        runWithHideInputKeyboard(task);
    }

    /**
     * 点击聊天记录
     *
     * @param searchConversationModel
     */
    @Override
    public void OnChatItemClicked(SearchConversationModel searchConversationModel) {
        SearchConversationResult result = searchConversationModel.getBean();
        if (result.getMatchCount() == 1) {
            Runnable task =
                    () ->
                            RongIM.getInstance()
                                    .startConversation(
                                            SealSearchActivity.this,
                                            ConversationIdentifier.obtain(result.getConversation()),
                                            searchConversationModel.getName(),
                                            result.getConversation().getSentTime());
            runWithHideInputKeyboard(task);
        } else {

            searchMessageFragment = new SearchMessageFragment();
            searchMessageFragment.init(
                    SealSearchUltraGroupActivity.TYPE_SEARCH_MESSAGES,
                    this,
                    ConversationIdentifier.obtain(
                            searchConversationModel.getBean().getConversation()),
                    searchConversationModel.getName(),
                    searchConversationModel.getPortraitUrl(),
                    null,
                    null);
            pushFragment(searchMessageFragment);
        }
    }

    /**
     * 点击群组
     *
     * @param groupEntity
     */
    @Override
    public void onGroupClicked(GroupEntity groupEntity) {
        runWithHideInputKeyboard(
                () ->
                        RongIM.getInstance()
                                .startGroupChat(
                                        SealSearchActivity.this,
                                        groupEntity.getId(),
                                        groupEntity.getName()));
    }

    /**
     * 点击显示更多
     *
     * @param type
     */
    @Override
    public void onSearchShowMoreClicked(int type) {
        SLog.i(TAG, "ShowMore:" + R.string.seal_ac_search_more_chatting_records + " type:" + type);
        if (type == R.string.seal_search_more_chatting_records) {
            pushFragment(searchConversationFragment);
        } else if (type == R.string.seal_search_more_friend) {
            pushFragment(searchFriendFragment);
        } else if (type == R.string.seal_search_more_group) {
            pushFragment(searchGroupFragment);
        }
    }

    public void search(String search) {
        currentFragment.search(search);
    }

    @Override
    public void clear() {
        currentFragment.clear();
    }

    /**
     * 搜索消息记录点击
     *
     * @param searchMessageModel
     */
    @Override
    public void onMessageRecordClick(SearchMessageModel searchMessageModel) {
        Runnable task =
                () -> {
                    Message message = searchMessageModel.getBean();
                    RongIM.getInstance()
                            .startConversation(
                                    SealSearchActivity.this,
                                    ConversationIdentifier.obtain(message),
                                    searchMessageModel.getName(),
                                    message.getSentTime() + 2);
                };
        runWithHideInputKeyboard(task);
    }

    private void runWithHideInputKeyboard(Runnable task) {
        boolean keyboardOpen = isKeyboardOpenWithInsets();
        hideInputKeyboard();
        handler.postDelayed(task, keyboardOpen ? 250 : 0);
    }

    private void pushFragment(SearchBaseFragment fragment) {
        currentFragment = fragment;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fl_content_fragment, currentFragment);
        transaction.addToBackStack(fragment.getClass().getSimpleName());
        transaction.commit();
        search(search);
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() == 1) {
            // 只有searchAllFragment
            finish();
        } else {
            super.onBackPressed();
            getTitleBar().getEtSearch().setText(searchAllFragment.getInitSearch());
        }
    }

    /**
     * 通过 WindowInsetsCompat 判断软键盘是否打开 由于项目 minSdkVersion=21，所以不需要API级别判断 优先使用 Type.ime()，备选使用
     * Type.systemBars() 或 Type.navigationBars()
     */
    public boolean isKeyboardOpenWithInsets() {
        if (getWindow() == null) {
            return false;
        }

        try {
            WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(getWindow().getDecorView());
            if (insets != null) {
                // 优先使用 Type.ime() (最准确，API 23+ 兼容库支持)
                try {
                    return insets.isVisible(WindowInsetsCompat.Type.ime());
                } catch (Exception e) {
                    // 如果 Type.ime() 不支持，尝试其他方法
                    SLog.d(TAG, "Type.ime() not supported, trying fallback methods");

                    // 备选方案：检查系统栏的底部 insets 是否大于正常值
                    // 这是一个近似判断，不是100%准确
                    int systemBarsBottom =
                            insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
                    int navigationBarsBottom =
                            insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;

                    // 如果系统栏底部有额外空间，可能是有键盘
                    // 这个判断比较粗略，具体阈值可能需要根据设备调整
                    return Math.max(systemBarsBottom, navigationBarsBottom) > 100;
                }
            }
        } catch (Exception e) {
            SLog.w(TAG, "isKeyboardOpenWithInsets failed: " + e.getMessage());
        }
        return false;
    }
}
