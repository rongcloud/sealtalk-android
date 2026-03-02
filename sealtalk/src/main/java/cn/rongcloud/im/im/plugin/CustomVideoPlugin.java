package cn.rongcloud.im.im.plugin;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import cn.rongcloud.im.R;
import io.rong.callkit.VideoPlugin;
import io.rong.imkit.config.IMKitThemeManager;

public class CustomVideoPlugin extends VideoPlugin {

    @Override
    public Drawable obtainDrawable(Context context) {
        return ContextCompat.getDrawable(
                context,
                IMKitThemeManager.dynamicResource(
                        context,
                        R.attr.rc_conversation_plugin_item_video_img,
                        io.rong.callkit.R.drawable.rc_ic_video_selector));
    }
}
