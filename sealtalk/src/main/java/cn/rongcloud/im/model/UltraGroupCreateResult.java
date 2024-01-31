package cn.rongcloud.im.model;

import com.google.gson.annotations.SerializedName;

public class UltraGroupCreateResult {
    @SerializedName("groupId")
    public String groupId;

    @SerializedName("defaultChannelId")
    public String defaultChannelId;

    @SerializedName("defaultChannelName")
    public String defaultChannelName;
}
