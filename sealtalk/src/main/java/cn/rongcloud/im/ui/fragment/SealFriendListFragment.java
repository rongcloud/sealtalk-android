package cn.rongcloud.im.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.view.IconTextItemView;
import io.rong.imkit.config.RongConfigCenter;
import io.rong.imkit.usermanage.friend.apply.ApplyFriendListActivity;
import io.rong.imkit.usermanage.friend.friendlist.FriendListFragment;
import io.rong.imkit.usermanage.friend.friendlist.FriendListViewModel;
import io.rong.imkit.usermanage.friend.my.profile.MyProfileActivity;
import io.rong.imkit.usermanage.group.application.GroupApplicationsActivity;
import io.rong.imkit.usermanage.group.grouplist.GroupListActivity;
import io.rong.imlib.IRongCoreCallback;
import io.rong.imlib.IRongCoreEnum;
import io.rong.imlib.RongCoreClient;
import io.rong.imlib.model.UserProfile;

public class SealFriendListFragment extends FriendListFragment {
    ImageView myAvatar;
    TextView myName;

    private IconTextItemView myGroupView;
    private IconTextItemView groupNotificationView;
    private IconTextItemView myFriendView;

    @NonNull
    @Override
    public View onCreateView(
            @NonNull Context context,
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle args) {
        View view = super.onCreateView(context, inflater, container, args);
        View headView =
                LayoutInflater.from(getContext())
                        .inflate(R.layout.seal_base_item, contactListComponent, false);
        myAvatar = headView.findViewById(R.id.iv_contact_portrait);
        myName = headView.findViewById(R.id.tv_contact_name);

        View myView = headView.findViewById(R.id.rc_item_my);
        myView.setOnClickListener(v -> startActivity(MyProfileActivity.newIntent(getContext())));
        myFriendView = headView.findViewById(R.id.rc_item_my_friend);
        myGroupView = headView.findViewById(R.id.rc_item_my_group);
        groupNotificationView = headView.findViewById(R.id.rc_item_group_notification);

        contactListComponent.addHeaderView(headView);
        return view;
    }

    @Override
    protected void onViewReady(@NonNull FriendListViewModel viewModel) {
        myFriendView.setOnClickListener(
                v -> startActivity(ApplyFriendListActivity.newIntent(getContext())));

        myGroupView.setOnClickListener(
                v -> startActivity(GroupListActivity.newIntent(getActivity())));

        groupNotificationView.setOnClickListener(
                v -> startActivity(GroupApplicationsActivity.newIntent(getActivity())));

        RongCoreClient.getInstance()
                .getMyUserProfile(
                        new IRongCoreCallback.ResultCallback<UserProfile>() {
                            @Override
                            public void onSuccess(UserProfile userProfile) {
                                myName.setText(userProfile.getName());
                                if (userProfile.getPortraitUri() != null) {
                                    RongConfigCenter.featureConfig()
                                            .getKitImageEngine()
                                            .loadUserPortrait(
                                                    getContext(),
                                                    userProfile.getPortraitUri(),
                                                    myAvatar);
                                }
                            }

                            @Override
                            public void onError(IRongCoreEnum.CoreErrorCode e) {}
                        });
        headComponent.getLeftTextView().setVisibility(View.GONE);
        super.onViewReady(viewModel);
    }

    @Override
    public void onResume() {
        super.onResume();
        RongCoreClient.getInstance()
                .getMyUserProfile(
                        new IRongCoreCallback.ResultCallback<UserProfile>() {
                            @Override
                            public void onSuccess(UserProfile userProfile) {
                                myName.setText(userProfile.getName());
                                if (userProfile.getPortraitUri() != null) {
                                    RongConfigCenter.featureConfig()
                                            .getKitImageEngine()
                                            .loadUserPortrait(
                                                    getContext(),
                                                    userProfile.getPortraitUri(),
                                                    myAvatar);
                                }
                            }

                            @Override
                            public void onError(IRongCoreEnum.CoreErrorCode e) {}
                        });
    }
}
