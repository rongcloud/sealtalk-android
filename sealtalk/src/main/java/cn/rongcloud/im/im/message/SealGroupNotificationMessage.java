package cn.rongcloud.im.im.message;

import android.os.Parcel;
import android.text.TextUtils;
import io.rong.common.rlog.RLog;
import io.rong.imlib.MessageTag;
import io.rong.message.GroupNotificationMessage;
import java.nio.charset.StandardCharsets;
import org.json.JSONException;
import org.json.JSONObject;

@MessageTag(value = "ST:GrpNtf", flag = MessageTag.ISPERSISTED)
public class SealGroupNotificationMessage extends GroupNotificationMessage {

    private static final String TAG = "SealGroupNotificationMessage";

    private SealGroupNotificationMessage() {}

    public static SealGroupNotificationMessage obtain(String operatorUserId, String operation) {
        SealGroupNotificationMessage obj = new SealGroupNotificationMessage();
        obj.setOperatorUserId(operatorUserId);
        obj.setOperation(operation);
        return obj;
    }

    public static SealGroupNotificationMessage obtain(
            String operatorUserId, String operation, String data) {
        SealGroupNotificationMessage obj = new SealGroupNotificationMessage();
        obj.setOperatorUserId(operatorUserId);
        obj.setOperation(operation);
        obj.setData(data);
        return obj;
    }

    public SealGroupNotificationMessage(byte[] data) {
        super(data);
    }

    @Override
    public byte[] encode() {
        JSONObject jsonObj = super.getBaseJsonObject();
        try {
            jsonObj.put("operatorUserId", getOperatorUserId());
            jsonObj.put("operation", getOperation());
            if (!TextUtils.isEmpty(getMessage())) {
                jsonObj.put("message", getMessage());
            }
            if (!TextUtils.isEmpty(getData())) {
                JSONObject dataObject = null;
                try {
                    dataObject = new JSONObject(getData());
                } catch (JSONException e) {
                    RLog.e(TAG, "JSONException " + e.getMessage());
                }
                jsonObj.put("data", dataObject != null ? dataObject : getData());
            }
        } catch (JSONException e) {
            RLog.e(TAG, "JSONException " + e.getMessage());
        }
        return jsonObj.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    public SealGroupNotificationMessage(Parcel in) {
        super(in);
    }

    public static final Creator<SealGroupNotificationMessage> CREATOR =
            new Creator<SealGroupNotificationMessage>() {

                @Override
                public SealGroupNotificationMessage createFromParcel(Parcel source) {
                    return new SealGroupNotificationMessage(source);
                }

                @Override
                public SealGroupNotificationMessage[] newArray(int size) {
                    return new SealGroupNotificationMessage[size];
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }
}
