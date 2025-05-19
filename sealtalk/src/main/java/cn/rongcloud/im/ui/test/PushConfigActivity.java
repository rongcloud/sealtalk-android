package cn.rongcloud.im.ui.test;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import cn.rongcloud.im.R;
import cn.rongcloud.im.im.IMManager;
import cn.rongcloud.im.ui.activity.TitleBaseActivity;
import io.rong.calllib.RongCallClient;
import io.rong.imlib.model.AndroidConfig;
import io.rong.imlib.model.HarmonyConfig;
import io.rong.imlib.model.IOSConfig;
import io.rong.imlib.model.MessagePushConfig;

public class PushConfigActivity extends TitleBaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_config);
        initView();
    }

    private void initView() {
        getTitleBar().setTitle("推送设置");
        findViewById(R.id.btn_set).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_set) {
            showSetDialog();
        }
    }

    private void showSetDialog() {
        final PushConfigDialog pushConfigDialog = new PushConfigDialog(this);
        SharedPreferences sharedPreferences = getSharedPreferences("push_config", MODE_PRIVATE);
        String id = sharedPreferences.getString("id", "");
        String title = sharedPreferences.getString("title", "");
        String content = sharedPreferences.getString("content", "");
        String data = sharedPreferences.getString("data", "");
        String hw = sharedPreferences.getString("hw", "");
        String imageUrlHW = sharedPreferences.getString("imageUrlHW", "");
        String hwImportance = sharedPreferences.getString("importance", "NORMAL").trim();
        String mi = sharedPreferences.getString("mi", "");
        String imageUrlMI = sharedPreferences.getString("imageUrlMi", "");
        String oppo = sharedPreferences.getString("oppo", "");
        String threadId = sharedPreferences.getString("threadId", "");
        String apnsId = sharedPreferences.getString("apnsId", "");
        String category = sharedPreferences.getString("category", "");
        String richMediaUri = sharedPreferences.getString("richMediaUri", "");
        String interruptionLevel = sharedPreferences.getString("interruptionLevel", "");
        String templateId = sharedPreferences.getString("templateId", "");
        String fcm = sharedPreferences.getString("fcm", "");
        String fcmChannelId = sharedPreferences.getString("fcmChannelId", "");
        String imageUrl = sharedPreferences.getString("imageUrl", "");
        boolean vivo = sharedPreferences.getBoolean("vivo", false);
        boolean disableTitle = sharedPreferences.getBoolean("disableTitle", false);
        boolean forceDetail = sharedPreferences.getBoolean("forceDetail", false);
        String ohosCategory = sharedPreferences.getString("ohosCategory", "");
        String ohosImageUrl = sharedPreferences.getString("ohosImageUrl", "");
        pushConfigDialog.getEtId().setText(id);
        pushConfigDialog.getEtTitle().setText(title);
        pushConfigDialog.getEtContent().setText(content);
        pushConfigDialog.getEtData().setText(data);
        pushConfigDialog.getEtHW().setText(hw);
        pushConfigDialog.getEtHWImportance().setText(hwImportance);
        pushConfigDialog.getEtMi().setText(mi);
        pushConfigDialog.getEtOppo().setText(oppo);
        pushConfigDialog.getEtThreadId().setText(threadId);
        pushConfigDialog.getEdFcm().setText(fcm);
        pushConfigDialog.getEdImageUrl().setText(imageUrl);
        pushConfigDialog.getEtApnId().setText(apnsId);
        pushConfigDialog.getEdCategory().setText(category);
        pushConfigDialog.getEdRichMediaUri().setText(richMediaUri);
        pushConfigDialog.getEdInterruptionLevel().setText(interruptionLevel);
        pushConfigDialog.getEdTemplateId().setText(templateId);
        pushConfigDialog.getCbVivo().setChecked(vivo);
        pushConfigDialog.getCbDisableTitle().setChecked(disableTitle);
        pushConfigDialog.getCbForceDetail().setChecked(forceDetail);
        pushConfigDialog.getEtImageUrlHW().setText(imageUrlHW);
        pushConfigDialog.getEtImageUrlMi().setText(imageUrlMI);
        pushConfigDialog.getEtChannelIdFcm().setText(fcmChannelId);
        pushConfigDialog.getEtOhosCategory().setText(ohosCategory);
        pushConfigDialog.getEtOhosImageUrl().setText(ohosImageUrl);

        pushConfigDialog
                .getSureView()
                .setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String id = pushConfigDialog.getEtId().getText().toString();
                                String title = pushConfigDialog.getEtTitle().getText().toString();
                                String content =
                                        pushConfigDialog.getEtContent().getText().toString();
                                String data = pushConfigDialog.getEtData().getText().toString();
                                String hw = pushConfigDialog.getEtHW().getText().toString();
                                String hwImportance =
                                        pushConfigDialog.getEtHWImportance().getText().toString();
                                String mi = pushConfigDialog.getEtMi().getText().toString();
                                String oppo = pushConfigDialog.getEtOppo().getText().toString();
                                String threadId =
                                        pushConfigDialog.getEtThreadId().getText().toString();
                                String apnsId = pushConfigDialog.getEtApnId().getText().toString();
                                String category =
                                        pushConfigDialog.getEdCategory().getText().toString();
                                String richMediaUri =
                                        pushConfigDialog.getEdRichMediaUri().getText().toString();
                                String interruptionLevel =
                                        pushConfigDialog
                                                .getEdInterruptionLevel()
                                                .getText()
                                                .toString();
                                String templateId =
                                        pushConfigDialog
                                                .getEdTemplateId()
                                                .getText()
                                                .toString()
                                                .trim();
                                String imageUrl =
                                        pushConfigDialog
                                                .getEdImageUrl()
                                                .getText()
                                                .toString()
                                                .trim();
                                String fcm =
                                        pushConfigDialog.getEdFcm().getText().toString().trim();
                                String imageUrlHW =
                                        pushConfigDialog.getEtImageUrlHW().getText().toString();
                                String imageUrlMI =
                                        pushConfigDialog.getEtImageUrlMi().getText().toString();
                                String fcmChannelId =
                                        pushConfigDialog.getEtChannelIdFcm().getText().toString();

                                boolean vivo = pushConfigDialog.getCbVivo().isChecked();
                                boolean disableTitle =
                                        pushConfigDialog.getCbDisableTitle().isChecked();
                                boolean forceDetail =
                                        pushConfigDialog.getCbForceDetail().isChecked();
                                String ohosCategory =
                                        pushConfigDialog.getEtOhosCategory().getText().toString();
                                String ohosImageUrl =
                                        pushConfigDialog.getEtOhosImageUrl().getText().toString();
                                SharedPreferences.Editor edit =
                                        getSharedPreferences("push_config", MODE_PRIVATE).edit();
                                edit.putString("id", id);
                                edit.putString("title", title);
                                edit.putString("content", content);
                                edit.putString("data", data);
                                edit.putString("hw", hw);
                                edit.putString("importance", hwImportance);
                                edit.putString("mi", mi);
                                edit.putString("oppo", oppo);
                                edit.putString("threadId", threadId);
                                edit.putString("apnsId", apnsId);
                                edit.putString("category", category);
                                edit.putString("richMediaUri", richMediaUri);
                                edit.putString("interruptionLevel", interruptionLevel);
                                edit.putBoolean("vivo", vivo);
                                edit.putBoolean("disableTitle", disableTitle);
                                edit.putBoolean("forceDetail", forceDetail);
                                edit.putString("templateId", templateId);
                                edit.putString("fcm", fcm);
                                edit.putString("imageUrl", imageUrl);
                                edit.putString("imageUrlHW", imageUrlHW);
                                edit.putString("imageUrlMi", imageUrlMI);
                                edit.putString("fcmChannelId", fcmChannelId);
                                edit.putString("ohosCategory", ohosCategory);
                                edit.putString("ohosImageUrl", ohosImageUrl);
                                IOSConfig iosConfig =
                                        new IOSConfig(threadId, apnsId, category, richMediaUri);
                                iosConfig.setInterruptionLevel(interruptionLevel);
                                HarmonyConfig harmonyConfig =
                                        new HarmonyConfig(ohosCategory, ohosImageUrl);
                                MessagePushConfig startCallMessagePushConfig =
                                        new MessagePushConfig.Builder()
                                                .setPushTitle(title)
                                                .setPushContent(content)
                                                .setPushData(data)
                                                .setForceShowDetailContent(forceDetail)
                                                .setAndroidConfig(
                                                        new AndroidConfig.Builder()
                                                                .setNotificationId(id)
                                                                .setChannelIdHW(hw)
                                                                .setImportanceHW(
                                                                        IMManager.getInstance()
                                                                                .getImportance(
                                                                                        hwImportance
                                                                                                .trim()))
                                                                .setChannelIdMi(mi)
                                                                .setChannelIdOPPO(oppo)
                                                                .setFcmCollapseKey(fcm)
                                                                .setFcmImageUrl(imageUrl)
                                                                .setImageUrlHW(imageUrlHW)
                                                                .setImageUrlMi(imageUrlMI)
                                                                .setChannelIdFCM(fcmChannelId)
                                                                .setTypeVivo(
                                                                        vivo
                                                                                ? AndroidConfig
                                                                                        .SYSTEM
                                                                                : AndroidConfig
                                                                                        .OPERATE)
                                                                .build())
                                                .setTemplateId(templateId)
                                                .setIOSConfig(iosConfig)
                                                .setHarmonyConfig(harmonyConfig)
                                                .build();
                                // SealTalk 发起和挂断的 pushConfig 内容一致，开发者根据实际需求配置
                                MessagePushConfig hangupCallMessagePushConfig =
                                        startCallMessagePushConfig;
                                RongCallClient.setPushConfig(
                                        startCallMessagePushConfig, hangupCallMessagePushConfig);
                                edit.commit();
                                pushConfigDialog.dismiss();
                            }
                        });
        pushConfigDialog.show();
    }
}
