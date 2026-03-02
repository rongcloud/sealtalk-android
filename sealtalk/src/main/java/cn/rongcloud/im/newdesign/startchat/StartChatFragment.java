package cn.rongcloud.im.newdesign.startchat;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import io.rong.imkit.R;
import io.rong.imkit.base.BaseViewModelFragment;
import io.rong.imkit.model.ContactModel;
import io.rong.imkit.usermanage.ViewModelFactory;
import io.rong.imkit.usermanage.component.ContactListComponent;
import io.rong.imkit.usermanage.component.HeadComponent;
import io.rong.imkit.usermanage.component.SearchComponent;
import io.rong.imkit.utils.RouteUtils;
import io.rong.imlib.model.ConversationIdentifier;
import io.rong.imlib.model.FriendInfo;
import java.util.List;

/**
 * 好友列表页面
 *
 * @author rongcloud
 * @since 5.12.0
 */
public class StartChatFragment extends BaseViewModelFragment<StartChatViewModel> {

    protected ContactListComponent contactListComponent;
    protected SearchComponent searchComponent;
    protected HeadComponent headComponent;
    private FriendInfo selectedFriendInfo;

    @NonNull
    @Override
    protected StartChatViewModel onCreateViewModel(Bundle bundle) {
        return new ViewModelProvider(this, new ViewModelFactory(bundle))
                .get(StartChatViewModel.class);
    }

    @NonNull
    @Override
    public View onCreateView(
            @NonNull Context context,
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle args) {
        View view = inflater.inflate(cn.rongcloud.im.R.layout.rc_page_start_chat, container, false);
        headComponent = view.findViewById(R.id.rc_head_component);
        searchComponent = view.findViewById(R.id.rc_search_component);
        contactListComponent = view.findViewById(R.id.rc_contact_list_component);
        return view;
    }

    @Override
    protected void onViewReady(@NonNull StartChatViewModel viewModel) {
        headComponent.setRightClickListener(
                v -> {
                    // 点击确定按钮，跳转到会话页面
                    if (selectedFriendInfo != null) {
                        Bundle bundle = new Bundle();
                        String displayName =
                                !TextUtils.isEmpty(selectedFriendInfo.getRemark())
                                        ? selectedFriendInfo.getRemark()
                                        : selectedFriendInfo.getName();
                        bundle.putString("title", displayName);
                        RouteUtils.routeToConversationActivity(
                                getActivity(),
                                ConversationIdentifier.obtainPrivate(
                                        selectedFriendInfo.getUserId()),
                                bundle);
                        finishActivity();
                    }
                });

        // 设置搜索监听器，实时过滤联系人列表
        // 根据搜索关键字过滤联系人，保持选中状态
        searchComponent.setSearchQueryListener(viewModel::searchFriends);

        // 监听 ViewModel 中的联系人列表变化
        viewModel
                .getAllContactsLiveData()
                .observe(
                        getViewLifecycleOwner(),
                        contactModels -> {
                            if (contactModels != null && contactListComponent != null) {
                                contactListComponent.setContactList(contactModels);
                            }
                        });
        // 设置联系人列表点击事件
        contactListComponent.setOnItemClickListener(
                contactModel -> {
                    if (contactModel != null && contactModel.getBean() instanceof FriendInfo) {
                        FriendInfo friendInfo = (FriendInfo) contactModel.getBean();
                        selectedFriendInfo = friendInfo;

                        // 保存选中的好友 ID 到 ViewModel
                        viewModel.setSelectedUserId(friendInfo.getUserId());

                        // 获取当前所有联系人列表
                        List<ContactModel> contactModels =
                                viewModel.getAllContactsLiveData().getValue();
                        if (contactModels != null) {
                            // 遍历所有联系人，取消其他联系人的选中状态，只选中当前点击的联系人
                            for (ContactModel model : contactModels) {
                                if (model.getContactType() == ContactModel.ItemType.CONTENT) {
                                    if (model.getBean() instanceof FriendInfo) {
                                        FriendInfo info = (FriendInfo) model.getBean();
                                        if (info.getUserId().equals(friendInfo.getUserId())) {
                                            model.setCheckType(ContactModel.CheckType.CHECKED);
                                        } else {
                                            model.setCheckType(ContactModel.CheckType.UNCHECKED);
                                        }
                                    }
                                }
                            }
                            // 刷新列表
                            contactListComponent.setContactList(contactModels);
                        }
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        getViewModel().getAllFriends();
    }
}
