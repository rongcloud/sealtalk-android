package cn.rongcloud.im.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import cn.rongcloud.im.R;
import cn.rongcloud.im.common.LogTag;
import cn.rongcloud.im.im.IMManager;
import cn.rongcloud.im.model.ChatRoomAction;
import cn.rongcloud.im.model.ChatRoomResult;
import cn.rongcloud.im.ui.test.ChatRoomListenerTestActivity;
import cn.rongcloud.im.utils.ToastUtils;
import cn.rongcloud.im.utils.log.SLog;
import cn.rongcloud.im.viewmodel.AppViewModel;
import io.rong.imkit.RongIM;
import io.rong.imkit.utils.RouteUtils;
import io.rong.imlib.IRongCoreEnum;
import io.rong.imlib.model.ConversationIdentifier;
import java.util.ArrayList;
import java.util.List;

/** 主界面子界面-发现界面 */
public class MainDiscoveryFragment extends BaseFragment {
    private AppViewModel appViewModel;
    private List<ChatRoomResult> latestChatRoomList;

    @Override
    protected int getLayoutResId() {
        return R.layout.main_fragment_discovery;
    }

    @Override
    protected void onInitView(Bundle savedInstanceState, Intent intent) {
        findView(R.id.discovery_ll_chat_room_1, true);
        findView(R.id.discovery_ll_chat_room_2, true);
        findView(R.id.discovery_ll_chat_room_3, true);
        findView(R.id.discovery_ll_chat_room_4, true);
    }

    @Override
    protected void onInitViewModel() {
        super.onInitViewModel();

        appViewModel = ViewModelProviders.of(getActivity()).get(AppViewModel.class);

        // 获取聊天室列表
        appViewModel
                .getChatRoomList()
                .observe(
                        this,
                        listResource -> {
                            List<ChatRoomResult> chatRoomResultList = listResource.data;
                            if (chatRoomResultList != null) {
                                latestChatRoomList = new ArrayList<>();
                                /** 筛选出结果中 type 为 chatroom 的结果 */
                                for (ChatRoomResult roomResult : chatRoomResultList) {
                                    if ("chatroom".equals(roomResult.getType())) {
                                        latestChatRoomList.add(roomResult);
                                    }
                                }
                            }
                        });

        /*
         * 以下代码使用 lambda 表达式会崩溃，因为 lambda 特性复用时注册同一个 Observer 时引发崩溃
         */
        // 监听聊天室加入状态
        IMManager.getInstance()
                .getChatRoomAction()
                .observe(
                        this,
                        new Observer<ChatRoomAction>() {
                            @Override
                            public void onChanged(ChatRoomAction chatRoomAction) {
                                if (chatRoomAction.status == ChatRoomAction.Status.ERROR) {
                                    if (chatRoomAction.errorCode
                                            == IRongCoreEnum.CoreErrorCode.KICKED_FROM_CHATROOM) {
                                        ToastUtils.showToast(
                                                R.string
                                                        .discovery_chat_room_join_failure_by_kicked);
                                    } else {
                                        ToastUtils.showToast(
                                                R.string.discovery_chat_room_join_failure);
                                    }
                                } else {
                                    SLog.d(
                                            LogTag.IM,
                                            "ChatRoom action, status: "
                                                    + chatRoomAction.status.name()
                                                    + " - ChatRoom id:"
                                                    + chatRoomAction.roomId);
                                }
                            }
                        });
    }

    @Override
    protected void onClick(View v, int id) {
        if (id == R.id.discovery_ll_chat_room_1) {
            enterChatRoom(0, getString(R.string.discovery_chat_room_one));
        } else if (id == R.id.discovery_ll_chat_room_2) {
            enterChatRoom(1, getString(R.string.discovery_chat_room_two));
        } else if (id == R.id.discovery_ll_chat_room_3) {
            enterChatRoom(2, getString(R.string.discovery_chat_room_three));
        } else if (id == R.id.discovery_ll_chat_room_4) {
            enterChatRoom(3, getString(R.string.discovery_chat_room_four));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * 进入聊天室
     *
     * @param roomIndex
     * @param roomTitle
     */
    private void enterChatRoom(int roomIndex, String roomTitle) {
        if (roomIndex >= (latestChatRoomList != null ? latestChatRoomList.size() : 0)) {
            ToastUtils.showToast(R.string.discovery_join_chat_room_error);
            appViewModel.requestChatRoomList();
            return;
        }

        ChatRoomResult chatRoomResult = latestChatRoomList.get(roomIndex);
        String roomId = chatRoomResult.getId();

        // 在 ChatRoomBusinessProcessor 的 init 方法中会加入聊天室，所以此处直接进入聊天室页面
        if (IMManager.getInstance().getAppTask().isDebugMode()) {
            RouteUtils.registerActivity(
                    RouteUtils.RongActivityType.ConversationActivity,
                    ChatRoomListenerTestActivity.class);
        }
        RongIM.getInstance()
                .startConversation(
                        getActivity(), ConversationIdentifier.obtainChatroom(roomId), roomTitle);
    }
}
