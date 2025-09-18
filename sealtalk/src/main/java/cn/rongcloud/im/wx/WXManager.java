package cn.rongcloud.im.wx;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import cn.rongcloud.im.BuildConfig;
import cn.rongcloud.im.R;
import cn.rongcloud.im.qrcode.QRCodeManager;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import io.rong.imlib.RongIMClient;

public class WXManager {
    private static final String APP_ID = BuildConfig.WECHAT_APP_ID; // 从BuildConfig获取
    private static final int THUMB_SIZE = 150;

    private static WXManager sInstance;
    private IWXAPI api;
    private WXBroadcastReceiver receiver;

    private WXManager() {}

    public static WXManager getInstance() {
        if (sInstance == null) {
            synchronized (WXManager.class) {
                if (sInstance == null) {
                    sInstance = new WXManager();
                }
            }
        }
        return sInstance;
    }

    public void init(Context context) {
        api = WXAPIFactory.createWXAPI(context, APP_ID, false);
        api.registerApp(APP_ID);

        receiver = new WXBroadcastReceiver(api);
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.tencent.mm.plugin.openapi.Intent.ACTION_REFRESH_WXAPP");
        context.registerReceiver(receiver, filter);
    }

    public boolean isWXAppInstalled() {
        return api != null && api.isWXAppInstalled();
    }

    public void shareToWechat(Context context) {
        if (!isWXAppInstalled()) {
            return;
        }

        WXWebpageObject webpage = new WXWebpageObject();
        QRCodeManager qrCodeManager = new QRCodeManager(context);
        webpage.webpageUrl =
                qrCodeManager.generateUserQRCodeContent(
                        RongIMClient.getInstance().getCurrentUserId());

        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = context.getString(R.string.wx_share_invite_friend_title);
        msg.description = context.getString(R.string.wx_share_invite_friend_content);

        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.seal_app_logo);
        Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true);
        bmp.recycle();
        msg.thumbData = WXUtils.bmpToByteArray(thumbBmp, true);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = WXUtils.buildTransaction("webpage");
        req.message = msg;
        req.scene = SendMessageToWX.Req.WXSceneSession;
        api.sendReq(req);
    }

    public IWXAPI getWXAPI() {
        return api;
    }

    public static class WXBroadcastReceiver extends BroadcastReceiver {
        private final IWXAPI api;

        private WXBroadcastReceiver(IWXAPI api) {
            this.api = api;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            // 将该app注册到微信
            api.registerApp(APP_ID);
        }
    }
}
