package cn.rongcloud.im.utils;

import android.content.Context;
import android.text.Spannable;
import android.text.TextUtils;
import androidx.annotation.Nullable;
import cn.rongcloud.im.R;
import io.rong.common.RLog;
import io.rong.imkit.IMCenter;
import io.rong.imkit.config.RongConfigCenter;
import io.rong.imkit.userinfo.RongUserInfoManager;
import io.rong.imkit.userinfo.model.GroupUserInfo;
import io.rong.imlib.model.CombineMsgInfo;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.imlib.model.UserInfo;
import io.rong.message.CombineV2Message;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 消息工具栏
 *
 * @author rongcloud
 * @since 1.0
 */
public class MessageUtil {
    private static final String TAG = MessageUtil.class.getSimpleName();
    private static final String PREFIX_FILE = "file";

    /** 合并消息最多存储四条消息的文本信息 */
    private static final int SUMMARY_MAX_SIZE = 1000;

    private MessageUtil() {}

    public static CombineV2Message convertMessage2CombineV2Message(
            Conversation.ConversationType curConversationType,
            @Nullable List<Message> messageList) {
        if (messageList == null || messageList.size() == 0) {
            return null;
        }

        List<CombineMsgInfo> combineMsgInfos = new ArrayList<>();
        for (Message message : messageList) {
            CombineMsgInfo combineMsgInfo =
                    CombineMsgInfo.obtain(
                            message.getSenderUserId(),
                            message.getTargetId(),
                            message.getSentTime(),
                            message.getContent());
            combineMsgInfos.add(combineMsgInfo);
        }
        Collections.sort(messageList, (o1, o2) -> (int) (o1.getSentTime() - o2.getSentTime()));

        return CombineV2Message.obtain(
                IMCenter.getInstance().getContext(),
                curConversationType,
                getNameList(messageList, curConversationType),
                getSummaryList(messageList),
                combineMsgInfos);
    }

    private static List<String> getNameList(
            List<Message> messages, Conversation.ConversationType type) {
        List<String> names = new ArrayList<>();
        if ((Conversation.ConversationType.GROUP).equals(type)) {
            Group group =
                    RongUserInfoManager.getInstance().getGroupInfo(messages.get(0).getTargetId());
            if (group != null) {
                String name = group.getName();
                if (!TextUtils.isEmpty(name) && !names.contains(name)) {
                    names.add(name);
                }
            }
        } else {
            for (Message msg : messages) {
                if (names.size() == 2) {
                    return names;
                }

                UserInfo info =
                        RongUserInfoManager.getInstance().getUserInfo(msg.getSenderUserId());
                if (info == null) {
                    RLog.d(TAG, "getNameList name is null, msg:" + msg);
                    break;
                }

                String name = info.getName();
                if (!TextUtils.isEmpty(name) && !names.contains(name)) {
                    names.add(name);
                }
            }
        }
        return names;
    }

    private static List<String> getSummaryList(List<Message> messages) {
        List<String> summaryList = new ArrayList<>();
        Conversation.ConversationType type = messages.get(0).getConversationType();
        for (int i = 0; i < messages.size() && i < SUMMARY_MAX_SIZE; i++) {
            Message message = messages.get(i);
            MessageContent content = message.getContent();
            UserInfo userInfo =
                    RongUserInfoManager.getInstance().getUserInfo(message.getSenderUserId());
            String userName = "";
            if (type.equals(Conversation.ConversationType.GROUP)) {
                GroupUserInfo groupUserInfo =
                        RongUserInfoManager.getInstance()
                                .getGroupUserInfo(message.getTargetId(), message.getSenderUserId());
                if (groupUserInfo != null) {
                    userName = groupUserInfo.getNickname();
                }
            }

            if (TextUtils.isEmpty(userName) && userInfo != null) {
                userName = userInfo.getName();
            }

            Spannable spannable =
                    RongConfigCenter.conversationConfig()
                            .getMessageSummary(IMCenter.getInstance().getContext(), content);
            String text = spannable.toString();
            summaryList.add(userName + " : " + text);
        }
        return summaryList;
    }

    public static String getTitle(Context context, CombineV2Message combineV2Message) {
        String title = "";
        if (context == null || combineV2Message == null) {
            return title;
        }
        if (Conversation.ConversationType.GROUP.equals(combineV2Message.getConversationType())) {
            title = context.getString(R.string.rc_combine_group_chat);
        } else {
            List<String> nameList = combineV2Message.getNameList();
            if (nameList == null) {
                return title;
            }

            if (nameList.size() == 1) {
                title =
                        String.format(
                                context.getString(R.string.rc_combine_the_group_chat_of),
                                nameList.get(0));
            } else if (nameList.size() == 2) {
                title =
                        String.format(
                                context.getString(R.string.rc_combine_the_group_chat_of),
                                nameList.get(0)
                                        + " "
                                        + context.getString(R.string.rc_combine_and)
                                        + " "
                                        + nameList.get(1));
            }
        }
        return title;
    }
}
