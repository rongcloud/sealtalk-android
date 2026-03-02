package cn.rongcloud.im.newdesign.startchat;

import android.os.Bundle;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.rong.imkit.IMCenter;
import io.rong.imkit.base.BaseViewModel;
import io.rong.imkit.model.ContactModel;
import io.rong.imkit.usermanage.handler.FriendInfoHandler;
import io.rong.imkit.utils.StringUtils;
import io.rong.imlib.listener.FriendEventListener;
import io.rong.imlib.model.DirectionType;
import io.rong.imlib.model.FriendApplicationStatus;
import io.rong.imlib.model.FriendApplicationType;
import io.rong.imlib.model.FriendInfo;
import io.rong.imlib.model.QueryFriendsDirectionType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 好友列表页面ViewModel
 *
 * @author rongcloud
 * @since 5.12.0
 */
public class StartChatViewModel extends BaseViewModel {

    private final MutableLiveData<List<ContactModel>> allContactsLiveData = new MutableLiveData<>();
    private final FriendInfoHandler friendInfoHandler;
    private List<FriendInfo> allFriendInfos = new ArrayList<>(); // 缓存所有好友信息
    private String selectedUserId = null; // 当前选中的好友 ID

    private final FriendEventListener listener =
            new FriendEventListener() {
                @Override
                public void onFriendAdd(
                        DirectionType directionType,
                        String userId,
                        String userName,
                        String portraitUri,
                        long operationTime) {
                    friendInfoHandler.getFriends(QueryFriendsDirectionType.Both);
                }

                @Override
                public void onFriendDelete(
                        DirectionType directionType, List<String> userIds, long operationTime) {
                    friendInfoHandler.getFriends(QueryFriendsDirectionType.Both);
                }

                @Override
                public void onFriendApplicationStatusChanged(
                        String userId,
                        FriendApplicationType applicationType,
                        FriendApplicationStatus status,
                        DirectionType directionType,
                        long operationTime,
                        String extra) {}

                @Override
                public void onFriendCleared(long operationTime) {
                    friendInfoHandler.getFriends(QueryFriendsDirectionType.Both);
                }

                @Override
                public void onFriendInfoChangedSync(
                        String userId,
                        String remark,
                        Map<String, String> extProfile,
                        long operationTime) {
                    friendInfoHandler.getFriends(QueryFriendsDirectionType.Both);
                }
            };

    public StartChatViewModel(@NonNull Bundle arguments) {
        super(arguments);
        friendInfoHandler = new FriendInfoHandler();
        friendInfoHandler.addDataChangeListener(
                FriendInfoHandler.KEY_GET_FRIENDS,
                new SafeDataHandler<List<FriendInfo>>() {
                    @Override
                    public void onDataChange(List<FriendInfo> friendInfos) {
                        allFriendInfos = friendInfos; // 缓存所有好友信息
                        List<ContactModel> contactModels = sortAndCategorizeContacts(friendInfos);
                        allContactsLiveData.postValue(contactModels); // 初始状态下，展示所有联系人
                    }
                });

        IMCenter.getInstance().addFriendEventListener(listener);
        getAllFriends();
    }

    public LiveData<List<ContactModel>> getAllContactsLiveData() {
        return allContactsLiveData;
    }

    private List<ContactModel> sortAndCategorizeContacts(List<FriendInfo> friendInfos) {
        // 排序处理，中文按首字母，英文按整个名字排序
        Collections.sort(
                friendInfos,
                (friend1, friend2) -> {
                    String name1 = getValidName(friend1);
                    String name2 = getValidName(friend2);
                    char firstChar1 = StringUtils.getFirstChar(name1.charAt(0));
                    char firstChar2 = StringUtils.getFirstChar(name2.charAt(0));
                    // 如果 firstChar1 是 #，把它放到最后
                    if (firstChar1 == '#') {
                        return 1;
                    }
                    // 如果 firstChar2 是 #，把它放到最后
                    if (firstChar2 == '#') {
                        return -1;
                    }
                    if (Character.isLetter(firstChar1) && Character.isLetter(firstChar2)) {
                        return firstChar1 - firstChar2;
                    } else {
                        return name1.compareToIgnoreCase(name2);
                    }
                });

        List<ContactModel> contactModels = new ArrayList<>();
        char lastCategory = '\0';

        for (FriendInfo friendInfo : friendInfos) {
            String name = getValidName(friendInfo);
            char firstChar = StringUtils.getFirstChar(name.charAt(0));

            // 如果首字母不同，添加一个标题 ContactModel
            if (firstChar != lastCategory) {
                contactModels.add(
                        ContactModel.obtain(
                                String.valueOf(firstChar),
                                ContactModel.ItemType.TITLE,
                                ContactModel.CheckType.NONE));
                lastCategory = firstChar;
            }

            // 添加联系人 ContactModel，根据选中状态设置 CheckType
            ContactModel.CheckType checkType =
                    (selectedUserId != null && selectedUserId.equals(friendInfo.getUserId()))
                            ? ContactModel.CheckType.CHECKED
                            : ContactModel.CheckType.UNCHECKED;
            contactModels.add(
                    ContactModel.obtain(friendInfo, ContactModel.ItemType.CONTENT, checkType));
        }

        return contactModels;
    }

    private String getValidName(FriendInfo friendInfo) {
        // 返回有效的名称，优先使用备注，其次是姓名，最后使用 "#" 作为占位符
        String name =
                !TextUtils.isEmpty(friendInfo.getRemark())
                        ? friendInfo.getRemark()
                        : friendInfo.getName();
        return !TextUtils.isEmpty(name) ? name : "#";
    }

    public void getAllFriends() {
        friendInfoHandler.getFriends(QueryFriendsDirectionType.Both);
    }

    /**
     * 设置选中的好友
     *
     * @param userId 好友 ID
     */
    public void setSelectedUserId(String userId) {
        this.selectedUserId = userId;
    }

    /**
     * 获取选中的好友 ID
     *
     * @return 好友 ID
     */
    public String getSelectedUserId() {
        return selectedUserId;
    }

    /**
     * 根据搜索关键字过滤联系人
     *
     * @param query 搜索关键字
     */
    public void searchFriends(String query) {
        if (TextUtils.isEmpty(query)) {
            // 如果搜索关键字为空，显示所有联系人
            List<ContactModel> contactModels = sortAndCategorizeContacts(allFriendInfos);
            allContactsLiveData.postValue(contactModels);
        } else {
            // 过滤好友列表
            List<FriendInfo> filteredFriends = new ArrayList<>();
            for (FriendInfo friendInfo : allFriendInfos) {
                String displayName = getValidName(friendInfo);
                // 判断显示名称是否包含搜索关键字（不区分大小写）
                if (displayName.toLowerCase().contains(query.toLowerCase())) {
                    filteredFriends.add(friendInfo);
                }
            }
            List<ContactModel> contactModels = sortAndCategorizeContacts(filteredFriends);
            allContactsLiveData.postValue(contactModels);
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        IMCenter.getInstance().removeFriendEventListener(listener);
        friendInfoHandler.stop();
    }
}
