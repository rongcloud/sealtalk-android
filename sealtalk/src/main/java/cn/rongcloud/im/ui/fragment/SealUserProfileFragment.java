package cn.rongcloud.im.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import cn.rongcloud.im.utils.ToastUtils;
import io.rong.callkit.RongCallAction;
import io.rong.callkit.RongVoIPIntent;
import io.rong.calllib.RongCallClient;
import io.rong.calllib.RongCallCommon;
import io.rong.calllib.RongCallSession;
import io.rong.common.RLog;
import io.rong.imkit.model.UiUserDetail;
import io.rong.imkit.usermanage.friend.user.profile.UserProfileFragment;
import io.rong.imkit.usermanage.friend.user.profile.UserProfileViewModel;
import io.rong.imlib.model.Conversation;
import java.util.Locale;

public class SealUserProfileFragment extends UserProfileFragment {
    private static final String TAG = "SealUserDetailFragment";

    @Override
    protected void onViewReady(@NonNull UserProfileViewModel viewModel) {
        super.onViewReady(viewModel);
        btnStartAudio.setVisibility(View.VISIBLE);
        btnStartVideo.setVisibility(View.VISIBLE);
        btnStartAudio.setOnClickListener(v -> startVoice());
        btnStartVideo.setOnClickListener(v -> startVideo());
    }

    /** 发起音频通话 */
    public void startVoice() {
        UiUserDetail uiUserDetail = getViewModel().getUiUserDetail();
        if (uiUserDetail == null) return;

        // todo
        RongCallSession profile = RongCallClient.getInstance().getCallSession();
        if (profile != null && profile.getStartTime() > 0) {
            ToastUtils.showToast(
                    profile.getMediaType() == RongCallCommon.CallMediaType.AUDIO
                            ? getString(io.rong.callkit.R.string.rc_voip_call_audio_start_fail)
                            : getString(io.rong.callkit.R.string.rc_voip_call_video_start_fail),
                    Toast.LENGTH_SHORT);
            return;
        }
        ConnectivityManager cm =
                (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected() || !networkInfo.isAvailable()) {
            ToastUtils.showToast(
                    getString(io.rong.callkit.R.string.rc_voip_call_network_error),
                    Toast.LENGTH_SHORT);
            return;
        }

        Intent intent = new Intent(RongVoIPIntent.RONG_INTENT_ACTION_VOIP_SINGLEAUDIO);
        intent.putExtra(
                "conversationType",
                Conversation.ConversationType.PRIVATE.getName().toLowerCase(Locale.US));
        intent.putExtra("targetId", uiUserDetail.getUserId());
        intent.putExtra("callAction", RongCallAction.ACTION_OUTGOING_CALL.getName());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage(getContext().getPackageName());
        getContext().getApplicationContext().startActivity(intent);
    }

    /** 发起视频聊天 */
    public void startVideo() {
        UiUserDetail uiUserDetail = getViewModel().getUiUserDetail();
        if (uiUserDetail == null) return;
        if (RongCallClient.getInstance() == null) {
            RLog.e(TAG, "ipc process not init");
            return;
        }
        RongCallSession profile = RongCallClient.getInstance().getCallSession();
        if (profile != null && profile.getStartTime() > 0) {
            ToastUtils.showToast(
                    profile.getMediaType() == RongCallCommon.CallMediaType.AUDIO
                            ? getString(io.rong.callkit.R.string.rc_voip_call_audio_start_fail)
                            : getString(io.rong.callkit.R.string.rc_voip_call_video_start_fail),
                    Toast.LENGTH_SHORT);
            return;
        }
        ConnectivityManager cm =
                (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected() || !networkInfo.isAvailable()) {
            ToastUtils.showToast(
                    getString(io.rong.callkit.R.string.rc_voip_call_network_error),
                    Toast.LENGTH_SHORT);
            return;
        }
        Intent intent = new Intent(RongVoIPIntent.RONG_INTENT_ACTION_VOIP_SINGLEVIDEO);
        intent.putExtra(
                "conversationType",
                Conversation.ConversationType.PRIVATE.getName().toLowerCase(Locale.US));
        intent.putExtra("targetId", uiUserDetail.getUserId());
        intent.putExtra("callAction", RongCallAction.ACTION_OUTGOING_CALL.getName());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage(getContext().getPackageName());
        getContext().getApplicationContext().startActivity(intent);
    }
}
