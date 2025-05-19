package cn.rongcloud.im.utils;

import android.content.Context;
import android.text.TextUtils;
import cn.rongcloud.im.model.AIAssistantType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** AI助手配置管理器 - 用于加载和管理AI助手类型配置 */
public class AIAssistantConfigManager {
    private static final String CONFIG_FILE_PATH = "ai_chat_assistant_types.json";
    private static AIAssistantConfigManager instance;
    private List<AIAssistantType> assistantTypes;
    private Map<String, AIAssistantType> styleCodeMap;

    private AIAssistantConfigManager() {
        assistantTypes = new ArrayList<>();
        styleCodeMap = new HashMap<>();
    }

    public static synchronized AIAssistantConfigManager getInstance() {
        if (instance == null) {
            instance = new AIAssistantConfigManager();
        }
        return instance;
    }

    /**
     * 加载AI助手类型配置
     *
     * @param context 上下文
     * @return 是否加载成功
     */
    public boolean loadConfig(Context context) {
        if (context == null) {
            return false;
        }

        try {
            String jsonString = readJsonFromAssets(context, CONFIG_FILE_PATH);
            if (TextUtils.isEmpty(jsonString)) {
                return false;
            }

            parseJsonConfig(jsonString);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** 从assets目录读取JSON文件内容 */
    private String readJsonFromAssets(Context context, String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream is = context.getAssets().open(fileName);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return stringBuilder.toString();
    }

    /** 解析JSON配置 */
    private void parseJsonConfig(String jsonString) throws JSONException {
        assistantTypes.clear();
        styleCodeMap.clear();

        JSONArray jsonArray = new JSONArray(jsonString);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);

            String styleCode = jsonObject.getString("styleCode");
            String styleName = jsonObject.getString("styleName");
            String agentId = jsonObject.getString("agentId");

            AIAssistantType type = new AIAssistantType(styleCode, styleName, agentId);
            assistantTypes.add(type);
            styleCodeMap.put(styleCode, type);
        }
    }

    /** 获取所有AI助手类型 */
    public List<AIAssistantType> getAllTypes() {
        return assistantTypes;
    }

    /** 根据styleCode获取AI助手类型 */
    public AIAssistantType getTypeByStyleCode(String styleCode) {
        return styleCodeMap.get(styleCode);
    }

    /** 获取默认AI助手类型 */
    public AIAssistantType getDefaultType() {
        if (assistantTypes.isEmpty()) {
            return null;
        }
        return assistantTypes.get(0);
    }

    /** 根据styleCode获取agentId */
    public String getAgentIdByStyleCode(String styleCode) {
        AIAssistantType type = getTypeByStyleCode(styleCode);
        if (type != null) {
            return type.getAgentId();
        }

        // 返回默认类型的agentId
        AIAssistantType defaultType = getDefaultType();
        return defaultType != null ? defaultType.getAgentId() : "";
    }
}
