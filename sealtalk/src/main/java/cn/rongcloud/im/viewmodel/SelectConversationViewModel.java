package cn.rongcloud.im.viewmodel;

import android.app.Application;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import cn.rongcloud.im.R;
import cn.rongcloud.im.common.ThreadManager;
import cn.rongcloud.im.im.IMManager;
import cn.rongcloud.im.ui.adapter.models.CheckType;
import cn.rongcloud.im.ui.adapter.models.CheckableContactModel;
import cn.rongcloud.im.ui.adapter.models.ContactModel;
import cn.rongcloud.im.utils.SingleSourceLiveData;
import cn.rongcloud.im.utils.log.SLog;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SelectConversationViewModel extends AndroidViewModel {

    private static final String TAG = "SelectConversationViewModel";
    protected SingleSourceLiveData<List<CheckableContactModel>> coversationLiveData;
    private MutableLiveData<Integer> selectedCount = new MutableLiveData<>();
    private ArrayList<String> checkedList;
    private ArrayList<String> unCheckedList;
    private RongIMClient rongIMClient;
    private Context mContext;

    public SelectConversationViewModel(@NonNull Application application) {
        super(application);
        mContext = application.getApplicationContext();
        rongIMClient = RongIMClient.getInstance();
        coversationLiveData = new SingleSourceLiveData<>();
        selectedCount.setValue(0);
    }

    public void loadConversation() {
        RongIMClient.getInstance()
                .getConversationList(
                        new RongIMClient.ResultCallback<List<Conversation>>() {
                            @Override
                            public void onSuccess(List<Conversation> conversations) {
                                convert(conversations);
                            }

                            @Override
                            public void onError(RongIMClient.ErrorCode errorCode) {}
                        },
                        Conversation.ConversationType.GROUP,
                        Conversation.ConversationType.PRIVATE);
    }

    /**
     * @param input
     * @return
     */
    private void convert(List<Conversation> input) {
        if (input == null) return;
        SLog.i(TAG, "convert input.size()" + input.size());
        List<CheckableContactModel> output = new ArrayList<>();
        ThreadManager.getInstance()
                .runOnWorkThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                for (Conversation conversation : input) {
                                    // 直接使用 Conversation 对象，不再转换为 GroupEntity 或 FriendShipInfo
                                    CheckableContactModel<Conversation> checkableContactModel =
                                            new CheckableContactModel<>(
                                                    conversation,
                                                    R.layout.select_conversation_item);

                                    // 设置选中状态
                                    if (unCheckedList != null
                                            && unCheckedList.contains(conversation.getTargetId())) {
                                        checkableContactModel.setCheckType(CheckType.UNCHECKED);
                                    }
                                    if (checkedList != null
                                            && checkedList.contains(conversation.getTargetId())) {
                                        checkableContactModel.setCheckType(CheckType.CHECKED);
                                    }
                                    output.add(checkableContactModel);
                                }
                                ThreadManager.getInstance()
                                        .runOnUIThread(
                                                new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        coversationLiveData.setValue(output);
                                                    }
                                                });
                            }
                        });
    }

    /**
     * 点击选取操作
     *
     * @param
     */
    public void onItemClicked(CheckableContactModel model) {
        SLog.i(TAG, "onItemClicked()");
        switch (model.getCheckType()) {
            case CHECKED:
                model.setCheckType(CheckType.NONE);
                break;
            case NONE:
                model.setCheckType(CheckType.CHECKED);
                break;
            default:
                break;
        }
        // 记录选中数
        int size = getCheckedList().size();
        selectedCount.setValue(size);
    }

    public ArrayList<String> getCheckedList() {
        ArrayList<String> strings = new ArrayList<>();
        List<CheckableContactModel> checkableContactModels = coversationLiveData.getValue();
        if (checkableContactModels == null) return strings;
        for (CheckableContactModel model : checkableContactModels) {
            if (model.getCheckType() == CheckType.CHECKED) {
                if (model.getBean() instanceof Conversation) {
                    Conversation conversation = (Conversation) model.getBean();
                    strings.add(conversation.getTargetId());
                }
            }
        }
        return strings;
    }

    public void cancelAllCheck() {
        List<CheckableContactModel> conversationModels = coversationLiveData.getValue();
        for (ContactModel model : conversationModels) {
            CheckableContactModel checkableContactModel = (CheckableContactModel) model;
            checkableContactModel.setCheckType(CheckType.NONE);
        }
        coversationLiveData.setValue(conversationModels);
        selectedCount.setValue(0);
    }

    public void selectAllCheck() {
        List<CheckableContactModel> conversationModels = coversationLiveData.getValue();
        if (conversationModels != null) {
            for (ContactModel model : conversationModels) {
                CheckableContactModel checkableContactModel = (CheckableContactModel) model;
                checkableContactModel.setCheckType(CheckType.CHECKED);
            }
        }
        coversationLiveData.setValue(conversationModels);
        selectedCount.setValue(conversationModels.size());
    }

    /**
     * 获取选择用户的数量
     *
     * @return
     */
    public LiveData<Integer> getSelectedCount() {
        return selectedCount;
    }

    public LiveData<List<CheckableContactModel>> getConersationLiveData() {
        return coversationLiveData;
    }

    public void clearMessage() {
        List<CheckableContactModel> checkableContactModels = coversationLiveData.getValue();
        if (checkableContactModels != null) {
            Iterator<CheckableContactModel> iterator = checkableContactModels.iterator();
            while (iterator.hasNext()) {
                CheckableContactModel model = iterator.next();
                if (model.getCheckType() == CheckType.CHECKED) {
                    if (model.getBean() instanceof Conversation) {
                        Conversation conversation = (Conversation) model.getBean();
                        IMManager.getInstance()
                                .clearConversationAndMessage(
                                        conversation.getTargetId(),
                                        conversation.getConversationType());
                        iterator.remove();
                    }
                }
            }
            coversationLiveData.setValue(checkableContactModels);
            selectedCount.setValue(0);
        }
    }
}
