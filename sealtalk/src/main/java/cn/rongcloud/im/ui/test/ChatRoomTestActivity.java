package cn.rongcloud.im.ui.test;

import static cn.rongcloud.im.ui.activity.SealTalkDebugTestActivity.SP_PERMISSION_NAME;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.activity.TitleBaseActivity;
import cn.rongcloud.im.ui.view.SettingItemView;
import io.rong.imlib.chatroom.base.RongChatRoomClient;

public class ChatRoomTestActivity extends TitleBaseActivity implements View.OnClickListener {

    private SettingItemView sivChatRoomKv;
    private SettingItemView sivChatRoomListenerTest;

    private static final String SP_CHAT_ROOM_DUPLICATE_MSG = "chat_room_duplicate_msg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room_test);
        initView();
    }

    private void initView() {
        SharedPreferences permissionConfigSP =
                getSharedPreferences(SP_PERMISSION_NAME, MODE_PRIVATE);

        SettingItemView chatRoomDuplicateMsg = findViewById(R.id.siv_chat_room_duplicate_msg);
        chatRoomDuplicateMsg.setChecked(
                permissionConfigSP.getBoolean(SP_CHAT_ROOM_DUPLICATE_MSG, true));
        chatRoomDuplicateMsg.setSwitchCheckListener(
                (buttonView, isChecked) -> {
                    permissionConfigSP
                            .edit()
                            .putBoolean(SP_CHAT_ROOM_DUPLICATE_MSG, isChecked)
                            .commit();
                    RongChatRoomClient.getInstance().setCheckChatRoomDuplicateMessage(isChecked);
                });

        sivChatRoomKv = (SettingItemView) findViewById(R.id.siv_chat_room_kv);
        sivChatRoomListenerTest = (SettingItemView) findViewById(R.id.siv_chat_room_listener_test);

        sivChatRoomKv.setOnClickListener(this);
        sivChatRoomListenerTest.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.siv_chat_room_kv:
                Intent intent = new Intent(this, ChatRoomStatusActivity.class);
                startActivity(intent);
                break;
            case R.id.siv_chat_room_listener_test:
                Intent intent1 = new Intent(this, ChatRoomListenerTestActivity.class);
                startActivity(intent1);
                break;
            default:
                break;
        }
    }
}
