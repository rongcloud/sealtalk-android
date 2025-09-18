package cn.rongcloud.im.ui.fragment;

import android.view.View;
import android.widget.Toast;
import io.rong.imkit.usermanage.friend.my.profile.MyProfileFragment;

public class CustomMyProfileFragment extends MyProfileFragment {
    @Override
    protected void onUserHeaderClick(View view) {
        Toast.makeText(getContext(), "自定义上传头像逻辑", Toast.LENGTH_SHORT).show();
    }
}
