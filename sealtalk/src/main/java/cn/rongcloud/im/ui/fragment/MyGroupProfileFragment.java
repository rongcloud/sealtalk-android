package cn.rongcloud.im.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.activity.SealSearchActivity;
import io.rong.imkit.usermanage.group.profile.GroupProfileFragment;
import io.rong.imkit.widget.SettingItemView;

/**
 * 功能描述:
 *
 * <p>创建时间: 2024/9/4
 *
 * @author haogaohui
 * @since 1.0
 */
public class MyGroupProfileFragment extends GroupProfileFragment {

    protected SettingItemView mSearchMessagesItem;

    @NonNull
    @Override
    public View onCreateView(
            @NonNull Context context,
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle args) {
        View view = super.onCreateView(context, inflater, container, args);
        mSearchMessagesItem = view.findViewById(R.id.siv_search_messages);

        mSearchMessagesItem.setOnClickListener(
                v -> {
                    // 点击搜索消息
                    Intent intent = new Intent(getContext(), SealSearchActivity.class);
                    startActivity(intent);
                });

        return view;
    }
}
