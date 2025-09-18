package cn.rongcloud.im.ui.test;

import android.os.Bundle;
import android.widget.TextView;
import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.BaseActivity;
import cn.rongcloud.im.utils.BuildVariantUtils;

// import com.umeng.commonsdk.UMConfigure;

/** 设备相关信息，友盟添加测试设备时需要 */
public class DeviceInfoActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);
        TextView deviceInfoTx = findViewById(R.id.rc_device_info);
        StringBuilder sb = new StringBuilder();

        // 仅在Develop版本中使用友盟设备信息获取
        if (!BuildVariantUtils.isPublishStoreBuild()) {
            try {
                // 使用反射调用UMConfigure.getTestDeviceInfo，避免在PublishStore版本中引入依赖
                Class<?> umConfigureClass = Class.forName("com.umeng.commonsdk.UMConfigure");
                String[] deviceInfos =
                        (String[])
                                umConfigureClass
                                        .getMethod(
                                                "getTestDeviceInfo", android.content.Context.class)
                                        .invoke(null, this);

                if (deviceInfos != null) {
                    if (deviceInfos[0] != null) {
                        sb.append("deviceId=" + deviceInfos[0]);
                    }

                    if (deviceInfos[1] != null) {
                        sb.append("mac=" + deviceInfos[1]);
                    }
                }
            } catch (Exception e) {
                sb.append("友盟设备信息获取失败: " + e.getMessage());
            }
        } else {
            sb.append("应用市场版本不显示设备信息");
        }

        deviceInfoTx.setText(sb.toString());
    }
}
