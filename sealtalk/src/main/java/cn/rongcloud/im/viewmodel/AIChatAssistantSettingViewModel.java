package cn.rongcloud.im.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import cn.rongcloud.im.model.AIAssistantType;
import cn.rongcloud.im.utils.AIAssistantConfigManager;
import java.util.List;

/** AI聊天助手设置的ViewModel 负责管理AI聊天助手的相关设置项 */
public class AIChatAssistantSettingViewModel extends AndroidViewModel {
    // 共享首选项常量
    public static final String PREFS_NAME = "ai_chat_assistant_settings";
    public static final String KEY_CHAT_STYLE = "chat_style";
    public static final String KEY_ACCESS_CHAT_HISTORY = "access_chat_history";
    public static final String KEY_FUNCTION_ENABLED = "function_enabled";

    // LiveData
    private final MutableLiveData<String> chatStyle = new MutableLiveData<>();
    private final MutableLiveData<Boolean> accessChatHistory = new MutableLiveData<>();
    private final MutableLiveData<Boolean> functionEnabled = new MutableLiveData<>();
    private final MutableLiveData<String> agentId = new MutableLiveData<>();
    private final MutableLiveData<List<AIAssistantType>> assistantTypes = new MutableLiveData<>();

    // 共享首选项
    private final SharedPreferences sharedPreferences;

    // 配置管理器
    private final AIAssistantConfigManager configManager;

    public AIChatAssistantSettingViewModel(@NonNull Application application) {
        super(application);

        // 初始化配置管理器
        configManager = AIAssistantConfigManager.getInstance();
        configManager.loadConfig(application);

        // 设置助手类型列表
        assistantTypes.setValue(configManager.getAllTypes());

        sharedPreferences = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // 初始化LiveData值
        loadSavedSettings();
    }

    /** 获取默认的智能体样式代码 */
    private String getDefaultStyleCode() {
        AIAssistantType defaultType = configManager.getDefaultType();
        return defaultType != null ? defaultType.getStyleCode() : "";
    }

    /** 加载保存的设置 */
    private void loadSavedSettings() {
        // 获取默认样式
        String defaultStyle = getDefaultStyleCode();

        // 加载聊天风格
        String style = sharedPreferences.getString(KEY_CHAT_STYLE, defaultStyle);
        chatStyle.setValue(style);

        // 加载聊天历史访问权限
        boolean allowHistory = sharedPreferences.getBoolean(KEY_ACCESS_CHAT_HISTORY, false);
        accessChatHistory.setValue(allowHistory);

        // 加载功能启用状态
        boolean enabled = sharedPreferences.getBoolean(KEY_FUNCTION_ENABLED, false);
        functionEnabled.setValue(enabled);

        // 根据风格设置AgentID
        updateAgentId(style);
    }

    /**
     * 根据聊天风格更新AgentID
     *
     * @param style 聊天风格
     */
    private void updateAgentId(String style) {
        // 使用配置管理器获取agentId
        String id = configManager.getAgentIdByStyleCode(style);
        agentId.setValue(id);
    }

    /**
     * 设置聊天风格
     *
     * @param style 聊天风格
     */
    public void setChatStyle(String style) {
        chatStyle.setValue(style);
        sharedPreferences.edit().putString(KEY_CHAT_STYLE, style).apply();
        updateAgentId(style);
    }

    /**
     * 设置聊天历史访问权限
     *
     * @param allow 是否允许访问聊天历史
     */
    public void setAccessChatHistory(boolean allow) {
        accessChatHistory.setValue(allow);
        sharedPreferences.edit().putBoolean(KEY_ACCESS_CHAT_HISTORY, allow).apply();
    }

    /**
     * 设置功能是否启用
     *
     * @param enabled 是否启用功能
     */
    public void setFunctionEnabled(boolean enabled) {
        functionEnabled.setValue(enabled);
        sharedPreferences.edit().putBoolean(KEY_FUNCTION_ENABLED, enabled).apply();
    }

    /**
     * 获取聊天风格LiveData
     *
     * @return 聊天风格LiveData
     */
    public LiveData<String> getChatStyle() {
        return chatStyle;
    }

    /**
     * 获取聊天历史访问权限LiveData
     *
     * @return 聊天历史访问权限LiveData
     */
    public LiveData<Boolean> getAccessChatHistory() {
        return accessChatHistory;
    }

    /**
     * 获取功能启用状态LiveData
     *
     * @return 功能启用状态LiveData
     */
    public LiveData<Boolean> getFunctionEnabled() {
        return functionEnabled;
    }

    /**
     * 获取AgentID LiveData
     *
     * @return AgentID LiveData
     */
    public LiveData<String> getAgentId() {
        return agentId;
    }

    /**
     * 获取AI助手类型列表LiveData
     *
     * @return AI助手类型列表LiveData
     */
    public LiveData<List<AIAssistantType>> getAssistantTypes() {
        return assistantTypes;
    }
}
