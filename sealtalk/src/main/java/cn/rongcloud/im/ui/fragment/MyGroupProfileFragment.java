package cn.rongcloud.im.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import cn.rongcloud.im.R;
import cn.rongcloud.im.newdesign.qrcode.QrCodeDisplayActivity;
import cn.rongcloud.im.newdesign.searchmsg.SearchMessageActivity;
import cn.rongcloud.im.utils.ToastUtils;
import io.rong.imkit.IMCenter;
import io.rong.imkit.usermanage.group.profile.GroupProfileFragment;
import io.rong.imkit.utils.KitConstants;
import io.rong.imkit.widget.CommonDialog;
import io.rong.imkit.widget.SettingItemView;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.ConversationIdentifier;
import io.rong.imlib.model.GroupInfo;

/**
 * 功能描述:
 *
 * <p>创建时间: 2024/9/4
 *
 * @author haogaohui
 * @since 1.0
 */
public class MyGroupProfileFragment extends GroupProfileFragment {

    protected SettingItemView mClearMessagesItem;
    protected SettingItemView mSearchMessagesItem;
    private ConversationIdentifier conversationIdentifier;

    @NonNull
    @Override
    public View onCreateView(
            @NonNull Context context,
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle args) {
        View view = super.onCreateView(context, inflater, container, args);
        conversationIdentifier =
                getArguments().getParcelable(KitConstants.KEY_CONVERSATION_IDENTIFIER);
        mSearchMessagesItem = view.findViewById(R.id.siv_search_messages);
        mClearMessagesItem = view.findViewById(R.id.siv_clear_messages);

        mSearchMessagesItem.setOnClickListener(
                v -> {
                    // 点击搜索消息
                    startActivity(
                            SearchMessageActivity.newIntent(getContext(), conversationIdentifier));
                });

        mClearMessagesItem.setOnClickListener(
                v -> {
                    if (conversationIdentifier != null) {
                        showCleanMessageDialog();
                    }
                });

        view.findViewById(R.id.siv_qrcode)
                .setOnClickListener(
                        v -> {
                            GroupInfo groupInfo = getViewModel().getGroupInfoLiveData().getValue();
                            if (groupInfo != null) {
                                startActivity(
                                        QrCodeDisplayActivity.newIntentForGroup(
                                                getContext(), groupInfo.getGroupId(), ""));
                            }
                        });

        return view;
    }

    private void showCleanMessageDialog() {
        new CommonDialog.Builder()
                .setContentMessage(getString(R.string.profile_clean_group_chat_history))
                .setButtonText(io.rong.imkit.R.string.rc_clear, io.rong.imkit.R.string.rc_cancel)
                .setDialogButtonClickListener(
                        new CommonDialog.OnDialogButtonClickListener() {
                            @Override
                            public void onPositiveClick(View v, Bundle bundle) {
                                clearMessages();
                            }
                        })
                .build()
                .show(getParentFragmentManager(), null);
    }

    private void clearMessages() {
        if (conversationIdentifier == null) {
            return;
        }
        IMCenter.getInstance()
                .cleanHistoryMessages(
                        conversationIdentifier,
                        0,
                        false,
                        new RongIMClient.OperationCallback() {
                            @Override
                            public void onSuccess() {
                                ToastUtils.showToast(R.string.common_clear_success);
                            }

                            @Override
                            public void onError(RongIMClient.ErrorCode errorCode) {
                                ToastUtils.showToast(R.string.common_clear_failure);
                            }
                        });
        RongIMClient.getInstance()
                .cleanRemoteHistoryMessages(
                        conversationIdentifier.getType(),
                        conversationIdentifier.getTargetId(),
                        System.currentTimeMillis(),
                        null);
    }
}
