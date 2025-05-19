package cn.rongcloud.im.viewmodel;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import cn.rongcloud.im.model.AIAssistantType;
import cn.rongcloud.im.utils.AIAssistantConfigManager;
import io.rong.imkit.IMCenter;
import io.rong.imkit.agent.AgentFacadeViewModel;
import io.rong.imkit.userinfo.RongUserInfoManager;
import io.rong.imlib.IRongCoreCallback;
import io.rong.imlib.IRongCoreEnum;
import io.rong.imlib.RongCommonDefine;
import io.rong.imlib.RongCoreClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.UserInfo;
import io.rong.imlib.model.agent.AgentContextMessage;
import io.rong.imlib.model.agent.AgentContextMessageType;
import io.rong.imlib.params.RequestRecommendationParams;
import io.rong.message.TextMessage;
import java.util.ArrayList;
import java.util.List;

public class CustomAgentFacadeViewModel extends AgentFacadeViewModel {
    // 配置管理器
    private AIAssistantConfigManager configManager;

    /**
     * 构造函数
     *
     * @param bundle 初始化参数
     */
    public CustomAgentFacadeViewModel(Bundle bundle) {
        super(bundle);

        // 初始化配置管理器
        configManager = AIAssistantConfigManager.getInstance();
        Context context = IMCenter.getInstance().getContext();
        if (context != null) {
            configManager.loadConfig(context);
        }
    }

    @Override
    public boolean isFeatureEnabled() {
        // 从 SharedPreferences 获取功能开关状态
        Context context = IMCenter.getInstance().getContext();
        if (context != null) {
            SharedPreferences sp =
                    context.getSharedPreferences(
                            AIChatAssistantSettingViewModel.PREFS_NAME, Context.MODE_PRIVATE);
            return sp.getBoolean(AIChatAssistantSettingViewModel.KEY_FUNCTION_ENABLED, false);
        }
        return super.isFeatureEnabled();
    }

    /**
     * 获取用户选择的智能体ID
     *
     * @return 选择的智能体ID，如果没有选择则返回默认值
     */
    @Override
    public String getAgentId() {
        Context context = IMCenter.getInstance().getContext();
        if (context != null) {
            SharedPreferences sp =
                    context.getSharedPreferences(
                            AIChatAssistantSettingViewModel.PREFS_NAME, Context.MODE_PRIVATE);

            // 获取保存的聊天风格
            String style =
                    sp.getString(
                            AIChatAssistantSettingViewModel.KEY_CHAT_STYLE, getDefaultStyleCode());

            // 使用配置管理器根据风格获取AgentId
            String agentId = configManager.getAgentIdByStyleCode(style);
            if (agentId != null && !agentId.isEmpty()) {
                return agentId;
            }
        }

        // 如果出现异常情况，返回默认的 AgentId
        return getDefaultAgentId();
    }

    /** 获取默认的风格编码 */
    private String getDefaultStyleCode() {
        AIAssistantType defaultType = configManager.getDefaultType();
        return defaultType != null ? defaultType.getStyleCode() : "";
    }

    /** 获取默认的AgentId */
    private String getDefaultAgentId() {
        AIAssistantType defaultType = configManager.getDefaultType();
        return defaultType != null ? defaultType.getAgentId() : "";
    }

    @Override
    public boolean onAgentReady(
            RequestRecommendationParams params,
            int count,
            IRongCoreCallback.RecommendationRequestProgressCallback callback) {
        // 检查是否允许访问聊天历史
        boolean canAccessChatHistory = canAccessChatHistory();

        // 如果不允许访问聊天历史，则不执行后续代码，直接调用回调并传递空列表
        if (!canAccessChatHistory) {
            if (callback != null) {
                callback.onReady(new ArrayList<>());
            }
            return true; // 返回true表示已处理，不需要默认实现
        }

        // 允许访问聊天历史，则执行获取历史消息的逻辑
        String targetId = params.getIdentifier().getTargetId();
        Conversation.ConversationType type = params.getIdentifier().getType();
        String channelId = params.getIdentifier().getChannelId();
        List<String> objectNames = new ArrayList<>();
        objectNames.add("RC:TxtMsg");
        List<AgentContextMessage> contextMessages = new ArrayList<>();

        RongCoreClient.getInstance()
                .getHistoryMessages(
                        type,
                        targetId,
                        objectNames,
                        0,
                        count,
                        RongCommonDefine.GetMessageDirection.FRONT,
                        new IRongCoreCallback.ResultCallback<List<Message>>() {
                            @Override
                            public void onSuccess(List<Message> messages) {
                                for (Message message : messages) {
                                    if (message.getContent() instanceof TextMessage) {
                                        TextMessage content = (TextMessage) message.getContent();
                                        AgentContextMessage msg = new AgentContextMessage();
                                        msg.setContent(content.getContent());
                                        msg.setTimestamp(message.getSentTime());
                                        msg.setType(AgentContextMessageType.TEXT);
                                        msg.setMessageId(message.getUId());
                                        UserInfo userInfo =
                                                RongUserInfoManager.getInstance()
                                                        .getUserInfo(message.getSenderUserId());
                                        msg.setUserName(userInfo.getName());
                                        msg.setUserId(message.getSenderUserId());
                                        contextMessages.add(msg);
                                    }
                                }
                                if (callback != null) {
                                    callback.onReady(contextMessages);
                                }
                            }

                            @Override
                            public void onError(IRongCoreEnum.CoreErrorCode e) {
                                if (callback != null) {
                                    callback.onReady(contextMessages);
                                }
                            }
                        });
        return true; // 返回true表示已处理，不需要默认实现
    }

    /**
     * 检查是否允许访问聊天历史
     *
     * @return 是否允许访问聊天历史
     */
    private boolean canAccessChatHistory() {
        Context context = IMCenter.getInstance().getContext();
        if (context != null) {
            SharedPreferences sp =
                    context.getSharedPreferences(
                            AIChatAssistantSettingViewModel.PREFS_NAME, Context.MODE_PRIVATE);
            return sp.getBoolean(AIChatAssistantSettingViewModel.KEY_ACCESS_CHAT_HISTORY, false);
        }
        return false;
    }
}
