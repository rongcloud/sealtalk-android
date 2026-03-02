package cn.rongcloud.im.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import cn.rongcloud.im.R;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.model.UserSimpleInfo;
import cn.rongcloud.im.utils.ToastUtils;
import cn.rongcloud.im.viewmodel.BlackListViewModel;
import io.rong.imkit.model.ContactModel;
import io.rong.imkit.usermanage.component.ContactListComponent;
import io.rong.imkit.widget.CommonDialog;
import io.rong.imlib.model.FriendInfo;
import java.util.ArrayList;
import java.util.List;

public class BlackListActivity extends TitleBaseActivity {
    private ContactListComponent contactListComponent;
    private TextView emptyView;
    private BlackListViewModel blackListViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blacklist);
        initView();
        initViewModel();
    }

    /** 初始化布局 */
    private void initView() {
        getTitleBar().setTitle(R.string.seal_privacy_blacklist);
        emptyView = findViewById(R.id.tv_is_null);
        contactListComponent = findViewById(R.id.rc_blacklist_component);

        // 设置移除按钮点击监听
        contactListComponent.setOnItemRemoveClickListener(
                contactModel -> {
                    if (contactModel.getBean() instanceof FriendInfo) {
                        FriendInfo userSimpleInfo = (FriendInfo) contactModel.getBean();
                        showRemoveDialog(userSimpleInfo);
                    }
                });
    }

    /**
     * 显示移除确认对话框
     *
     * @param userSimpleInfo 用户信息
     */
    private void showRemoveDialog(FriendInfo userSimpleInfo) {
        String userName = userSimpleInfo.getName();
        String message =
                getString(R.string.profile_detail_remove_from_blacklist) + " " + userName + "?";

        new CommonDialog.Builder()
                .setContentMessage(message)
                .setDialogButtonClickListener(
                        (v, bundle) -> {
                            blackListViewModel.removeFromBlackList(userSimpleInfo.getUserId());
                        })
                .build()
                .show(getSupportFragmentManager(), null);
    }

    /** 初始化Viewmodel */
    private void initViewModel() {
        blackListViewModel = ViewModelProviders.of(this).get(BlackListViewModel.class);
        blackListViewModel
                .getBlackListResult()
                .observe(
                        this,
                        new Observer<Resource<List<UserSimpleInfo>>>() {
                            @Override
                            public void onChanged(Resource<List<UserSimpleInfo>> listResource) {
                                if (listResource != null && listResource.data != null) {
                                    List<ContactModel> contactModels =
                                            convertToContactModels(listResource.data);
                                    if (contactModels.isEmpty()) {
                                        emptyView.setVisibility(View.VISIBLE);
                                        contactListComponent.setVisibility(View.GONE);
                                    } else {
                                        emptyView.setVisibility(View.GONE);
                                        contactListComponent.setVisibility(View.VISIBLE);
                                        contactListComponent.setContactList(contactModels);
                                    }
                                }
                            }
                        });

        // 获取移除黑名单结果
        blackListViewModel
                .getRemoveBlackListResult()
                .observe(
                        this,
                        new Observer<Resource<Void>>() {
                            @Override
                            public void onChanged(Resource<Void> resource) {
                                if (resource.status == Status.SUCCESS) {
                                    ToastUtils.showToast(R.string.common_remove_successful);
                                } else if (resource.status == Status.ERROR) {
                                    ToastUtils.showToast(resource.message);
                                }
                            }
                        });
    }

    /**
     * 将 UserSimpleInfo 列表转换为 ContactModel 列表
     *
     * @param userList 用户信息列表
     * @return ContactModel 列表
     */
    private List<ContactModel> convertToContactModels(List<UserSimpleInfo> userList) {
        List<ContactModel> contactModels = new ArrayList<>();
        if (userList != null) {
            for (UserSimpleInfo userInfo : userList) {
                FriendInfo friendInfo = new FriendInfo();
                friendInfo.setUserId(userInfo.getId());
                friendInfo.setName(userInfo.getName());
                friendInfo.setPortraitUri(userInfo.getPortraitUri());
                contactModels.add(ContactModel.obtain(friendInfo, ContactModel.ItemType.CONTENT));
            }
        }
        return contactModels;
    }
}
