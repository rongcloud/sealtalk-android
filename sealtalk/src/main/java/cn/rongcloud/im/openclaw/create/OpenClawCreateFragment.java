package cn.rongcloud.im.openclaw.create;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import cn.rongcloud.im.R;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.openclaw.component.OpenClawPageUtils;
import cn.rongcloud.im.openclaw.detail.OpenClawDetailActivity;
import cn.rongcloud.im.openclaw.model.OpenClawRobotTokenResult;
import cn.rongcloud.im.ui.dialog.SelectPictureBottomDialog;
import cn.rongcloud.im.utils.ToastUtils;
import com.bumptech.glide.Glide;
import io.rong.imkit.picture.config.PictureConfig;
import java.lang.ref.WeakReference;

public class OpenClawCreateFragment extends Fragment {
    private static final String DEFAULT_PORTRAIT_URI =
            "https://static.rongcloud.cn/avatar/claw.png";
    private OpenClawCreateViewModel viewModel;
    private EditText nameInput;
    private ImageView avatarView;
    private SelectPictureBottomDialog pictureDialog;
    private Uri selectedPortraitUri;
    private String portraitUri = DEFAULT_PORTRAIT_URI;
    private boolean portraitUploading;
    private int portraitUploadRequestId;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(OpenClawCreateViewModel.class);
        LinearLayout root = OpenClawPageUtils.verticalRoot(requireContext());
        OpenClawPageUtils.padding(root, 16, 36, 16, 24);

        avatarView = OpenClawPageUtils.robotIcon(requireContext(), 60);
        avatarView.setClickable(true);
        avatarView.setFocusable(true);
        avatarView.setOnClickListener(v -> showSelectPictureDialog());
        LinearLayout.LayoutParams iconParams = OpenClawPageUtils.size(requireContext(), 60, 60);
        iconParams.gravity = Gravity.CENTER_HORIZONTAL;
        root.addView(avatarView, iconParams);

        TextView avatarHint =
                OpenClawPageUtils.secondaryText(
                        requireContext(), getString(R.string.openclaw_upload_avatar_hint), 12);
        avatarHint.setGravity(Gravity.CENTER);
        avatarHint.setOnClickListener(v -> showSelectPictureDialog());
        root.addView(avatarHint, OpenClawPageUtils.matchWrapWithTop(requireContext(), 8));

        TextView label =
                OpenClawPageUtils.text(
                        requireContext(), getString(R.string.openclaw_robot_name), 14);
        root.addView(label, OpenClawPageUtils.matchWrapWithTop(requireContext(), 28));

        nameInput =
                OpenClawPageUtils.input(
                        requireContext(), getString(R.string.openclaw_robot_name_hint));
        nameInput.setFilters(new InputFilter[] {new InputFilter.LengthFilter(10)});
        root.addView(nameInput, OpenClawPageUtils.matchWrapWithTop(requireContext(), 10));
        ViewGroup.LayoutParams inputParams = nameInput.getLayoutParams();
        inputParams.height = OpenClawPageUtils.dp(requireContext(), 38);
        nameInput.setLayoutParams(inputParams);

        View spacer = new View(requireContext());
        root.addView(spacer, new LinearLayout.LayoutParams(-1, 0, 1));

        TextView button =
                OpenClawPageUtils.primaryButton(
                        requireContext(), getString(R.string.openclaw_create_robot));
        button.setOnClickListener(v -> createRobot());
        root.addView(
                button,
                new LinearLayout.LayoutParams(-1, OpenClawPageUtils.dp(requireContext(), 42)));
        return root;
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PictureConfig.APPLY_CAMERA_PERMISSIONS_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && pictureDialog != null) {
                pictureDialog.takePicture();
            }
        } else if (requestCode == PictureConfig.APPLY_STORAGE_PERMISSIONS_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && pictureDialog != null) {
                pictureDialog.selectPicture();
            }
        }
    }

    private void showSelectPictureDialog() {
        SelectPictureBottomDialog.Builder builder = new SelectPictureBottomDialog.Builder();
        builder.setOnSelectPictureListener(new AvatarSelectListener(this));
        pictureDialog = builder.build();
        pictureDialog.show(getChildFragmentManager(), "openclaw_select_avatar_dialog");
    }

    private void onAvatarSelected(Uri uri) {
        if (uri == null) {
            return;
        }
        selectedPortraitUri = uri;
        portraitUri = null;
        Glide.with(this).load(uri).circleCrop().into(avatarView);
        uploadPortrait(uri, null);
    }

    private void createRobot() {
        String name = nameInput.getText().toString().trim();
        if (TextUtils.isEmpty(name) || name.length() < 2 || name.length() > 10) {
            ToastUtils.showToast(getString(R.string.openclaw_robot_name_invalid));
            return;
        }
        if (portraitUploading) {
            ToastUtils.showToast(getString(R.string.profile_uploading_portrait));
            return;
        }
        if (selectedPortraitUri != null && TextUtils.isEmpty(portraitUri)) {
            uploadPortrait(selectedPortraitUri, () -> submitCreateRobot(name));
            return;
        }
        submitCreateRobot(name);
    }

    private void uploadPortrait(Uri uri, @Nullable Runnable afterSuccess) {
        if (uri == null) {
            return;
        }
        int requestId = ++portraitUploadRequestId;
        portraitUploading = true;
        ToastUtils.showToast(getString(R.string.profile_uploading_portrait));
        viewModel
                .uploadPortrait(uri)
                .observe(
                        getViewLifecycleOwner(),
                        resource -> {
                            if (resource.status == Status.LOADING) {
                                return;
                            }
                            if (requestId != portraitUploadRequestId) {
                                return;
                            }
                            portraitUploading = false;
                            if (resource.status == Status.SUCCESS
                                    && !TextUtils.isEmpty(resource.data)) {
                                portraitUri = resource.data;
                                ToastUtils.showToast(
                                        getString(R.string.profile_update_portrait_success));
                                if (afterSuccess != null) {
                                    afterSuccess.run();
                                }
                            } else if (resource.status == Status.ERROR) {
                                ToastUtils.showToast(
                                        getString(R.string.profile_upload_portrait_failed));
                            }
                        });
    }

    private void submitCreateRobot(String name) {
        viewModel
                .createRobot(name, portraitUri)
                .observe(
                        getViewLifecycleOwner(),
                        resource -> {
                            if (resource.status == Status.SUCCESS) {
                                OpenClawRobotTokenResult robot = resource.data;
                                if (robot != null) {
                                    startActivity(
                                            OpenClawDetailActivity.newCreateResultIntent(
                                                    requireContext(), robot, robot.getToken()));
                                    requireActivity().finish();
                                }
                            } else if (resource.status == Status.ERROR) {
                                ToastUtils.showToast(getString(R.string.openclaw_robot_limit));
                            }
                        });
    }

    private static class AvatarSelectListener
            implements SelectPictureBottomDialog.OnSelectPictureListener {
        private final WeakReference<OpenClawCreateFragment> fragmentRef;

        AvatarSelectListener(OpenClawCreateFragment fragment) {
            fragmentRef = new WeakReference<>(fragment);
        }

        @Override
        public void onSelectPicture(Uri uri) {
            OpenClawCreateFragment fragment = fragmentRef.get();
            if (fragment != null) {
                fragment.onAvatarSelected(uri);
            }
        }
    }
}
