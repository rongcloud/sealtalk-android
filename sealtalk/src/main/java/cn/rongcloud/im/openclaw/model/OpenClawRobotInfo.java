package cn.rongcloud.im.openclaw.model;

import android.os.Parcel;
import android.os.Parcelable;

public class OpenClawRobotInfo implements Parcelable {
    private String botId;
    private String name;
    private String portraitUri;
    private OpenClawRobotCreator creator;

    public OpenClawRobotInfo() {}

    protected OpenClawRobotInfo(Parcel in) {
        botId = in.readString();
        name = in.readString();
        portraitUri = in.readString();
    }

    public static final Creator<OpenClawRobotInfo> CREATOR =
            new Creator<OpenClawRobotInfo>() {
                @Override
                public OpenClawRobotInfo createFromParcel(Parcel in) {
                    return new OpenClawRobotInfo(in);
                }

                @Override
                public OpenClawRobotInfo[] newArray(int size) {
                    return new OpenClawRobotInfo[size];
                }
            };

    public String getBotId() {
        return botId;
    }

    public void setBotId(String botId) {
        this.botId = botId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPortraitUri() {
        return portraitUri;
    }

    public void setPortraitUri(String portraitUri) {
        this.portraitUri = portraitUri;
    }

    public OpenClawRobotCreator getCreator() {
        return creator;
    }

    public void setCreator(OpenClawRobotCreator creator) {
        this.creator = creator;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(botId);
        dest.writeString(name);
        dest.writeString(portraitUri);
    }
}
