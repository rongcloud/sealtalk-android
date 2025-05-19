package cn.rongcloud.im.model;

/** AI聊天助手类型模型 */
public class AIAssistantType {
    private String styleCode;
    private String styleName;
    private String agentId;

    public AIAssistantType() {}

    public AIAssistantType(String styleCode, String styleName, String agentId) {
        this.styleCode = styleCode;
        this.styleName = styleName;
        this.agentId = agentId;
    }

    public String getStyleCode() {
        return styleCode;
    }

    public void setStyleCode(String styleCode) {
        this.styleCode = styleCode;
    }

    public String getStyleName() {
        return styleName;
    }

    public void setStyleName(String styleName) {
        this.styleName = styleName;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }
}
