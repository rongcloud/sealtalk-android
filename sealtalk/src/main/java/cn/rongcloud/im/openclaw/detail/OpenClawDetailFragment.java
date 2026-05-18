package cn.rongcloud.im.openclaw.detail;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import cn.rongcloud.im.openclaw.model.OpenClawRobotInfo;
import cn.rongcloud.im.openclaw.model.OpenClawRobotRegistry;
import cn.rongcloud.im.openclaw.model.OpenClawRobotTokenResult;
import cn.rongcloud.im.utils.ToastUtils;
import io.rong.imkit.RongIM;
import io.rong.imkit.config.RongConfigCenter;

public class OpenClawDetailFragment extends Fragment {
    private OpenClawDetailViewModel viewModel;
    private OpenClawRobotInfo robot;
    private String botId;
    private String token;
    private ImageView avatarView;
    private TextView nameView;
    private TextView tokenView;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(OpenClawDetailViewModel.class);
        if (getArguments() != null) {
            robot = getArguments().getParcelable(OpenClawDetailActivity.EXTRA_ROBOT);
            token = getArguments().getString(OpenClawDetailActivity.EXTRA_TOKEN);
            if (robot != null) {
                botId = robot.getBotId();
            }
            if (!TextUtils.isEmpty(botId) && !TextUtils.isEmpty(token)) {
                OpenClawRobotRegistry.registerToken(requireContext(), botId, token);
            }
        }

        LinearLayout root = OpenClawPageUtils.verticalRoot(requireContext());
        OpenClawPageUtils.padding(root, 16, 16, 16, 24);
        root.addView(createHeader(), OpenClawPageUtils.matchWrap());
        root.addView(createTokenCard(), OpenClawPageUtils.matchWrapWithTop(requireContext(), 20));

        TextView hint =
                OpenClawPageUtils.secondaryText(
                        requireContext(), getString(R.string.openclaw_token_hint), 12);
        hint.setGravity(Gravity.CENTER);
        root.addView(hint, OpenClawPageUtils.matchWrapWithTop(requireContext(), 10));

        TextView refresh =
                OpenClawPageUtils.primaryButton(
                        requireContext(), getString(R.string.openclaw_reset_token));
        refresh.setOnClickListener(v -> refreshToken());
        root.addView(refresh, buttonParams(36));

        LinearLayout chat = createChatButton();
        chat.setOnClickListener(v -> startChat());
        root.addView(chat, buttonParams(10));
        loadRobotDetail();
        return root;
    }

    private LinearLayout createChatButton() {
        LinearLayout button = new LinearLayout(requireContext());
        button.setGravity(Gravity.CENTER);
        button.setOrientation(LinearLayout.HORIZONTAL);
        button.setClickable(true);
        button.setFocusable(true);
        button.setBackground(
                OpenClawPageUtils.roundRect(
                        OpenClawPageUtils.cardColor(requireContext()),
                        OpenClawPageUtils.dp(requireContext(), 6)));

        ImageView icon = new ImageView(requireContext());
        icon.setImageResource(R.drawable.openclaw_ic_chat);
        button.addView(icon, OpenClawPageUtils.size(requireContext(), 20, 20));

        TextView text =
                OpenClawPageUtils.text(
                        requireContext(), getString(R.string.openclaw_start_chat), 17);
        text.setTextColor(OpenClawPageUtils.primaryColor(requireContext()));
        text.setIncludeFontPadding(false);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(-2, -2);
        textParams.leftMargin = OpenClawPageUtils.dp(requireContext(), 6);
        button.addView(text, textParams);
        return button;
    }

    private LinearLayout createHeader() {
        LinearLayout header = new LinearLayout(requireContext());
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        avatarView = OpenClawPageUtils.robotIcon(requireContext(), 60);
        updateRobotAvatar();
        header.addView(avatarView, OpenClawPageUtils.size(requireContext(), 60, 60));

        nameView = OpenClawPageUtils.titleText(requireContext(), getRobotName(), 20);
        nameView.setSingleLine(true);
        nameView.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(0, -2, 1);
        nameParams.leftMargin = OpenClawPageUtils.dp(requireContext(), 20);
        header.addView(nameView, nameParams);
        return header;
    }

    private LinearLayout createTokenCard() {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setBackground(
                OpenClawPageUtils.roundRect(OpenClawPageUtils.cardColor(requireContext()), 0));
        OpenClawPageUtils.padding(card, 16, 0, 16, 0);

        tokenView = OpenClawPageUtils.text(requireContext(), formatToken(), 17);
        tokenView.setSingleLine(true);
        tokenView.setGravity(Gravity.CENTER_VERTICAL);
        tokenView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
        card.addView(
                tokenView,
                new LinearLayout.LayoutParams(0, OpenClawPageUtils.dp(requireContext(), 54), 1));

        ImageView copy = new ImageView(requireContext());
        copy.setImageResource(R.drawable.openclaw_ic_copy);
        copy.setPadding(
                OpenClawPageUtils.dp(requireContext(), 4),
                OpenClawPageUtils.dp(requireContext(), 4),
                OpenClawPageUtils.dp(requireContext(), 4),
                OpenClawPageUtils.dp(requireContext(), 4));
        copy.setOnClickListener(v -> copyToken());
        card.addView(copy, OpenClawPageUtils.size(requireContext(), 28, 28));
        return card;
    }

    private LinearLayout.LayoutParams buttonParams(int topMarginDp) {
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(-1, OpenClawPageUtils.dp(requireContext(), 42));
        params.topMargin = OpenClawPageUtils.dp(requireContext(), topMarginDp);
        return params;
    }

    private String getRobotName() {
        return robot == null || TextUtils.isEmpty(robot.getName())
                ? getString(R.string.openclaw_default_robot_name)
                : robot.getName();
    }

    private String getRobotPortraitUri() {
        return robot == null ? null : robot.getPortraitUri();
    }

    private String formatToken() {
        return TextUtils.isEmpty(token)
                ? getString(R.string.openclaw_token_prefix)
                : getString(R.string.openclaw_token_prefix) + token;
    }

    private void loadRobotDetail() {
        if (TextUtils.isEmpty(botId)) {
            return;
        }
        viewModel
                .getRobotInfo(botId)
                .observe(
                        getViewLifecycleOwner(),
                        resource -> {
                            if (resource.status == Status.SUCCESS && resource.data != null) {
                                updateRobotInfo(resource.data);
                                updateToken(resource.data.getToken());
                            } else if (resource.status == Status.ERROR) {
                                ToastUtils.showErrorToast(resource.code);
                            }
                        });
    }

    private void updateRobotInfo(OpenClawRobotInfo latest) {
        if (latest == null) {
            return;
        }
        if (TextUtils.isEmpty(latest.getBotId())) {
            latest.setBotId(botId);
        } else {
            botId = latest.getBotId();
        }
        robot = latest;
        if (nameView != null) {
            nameView.setText(getRobotName());
        }
        updateRobotAvatar();
    }

    private void updateRobotAvatar() {
        if (avatarView == null) {
            return;
        }
        avatarView.setImageResource(R.drawable.openclaw_ic_ai_robot_avatar);
        String portraitUri = getRobotPortraitUri();
        if (!OpenClawRobotRegistry.shouldUseDefaultPortrait(portraitUri)) {
            RongConfigCenter.featureConfig()
                    .getKitImageEngine()
                    .loadUserPortrait(requireContext(), portraitUri, avatarView);
        }
    }

    private void updateToken(String latestToken) {
        if (TextUtils.isEmpty(latestToken)) {
            return;
        }
        token = latestToken;
        OpenClawRobotRegistry.registerToken(requireContext(), botId, token);
        if (tokenView != null) {
            tokenView.setText(formatToken());
        }
    }

    private void copyToken() {
        if (TextUtils.isEmpty(token)) {
            ToastUtils.showToast(getString(R.string.openclaw_reset_token_first));
            return;
        }
        ClipboardManager manager =
                (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        manager.setPrimaryClip(ClipData.newPlainText("OpenClaw Token", token));
        ToastUtils.showToast(getString(R.string.openclaw_copy_success));
    }

    private void refreshToken() {
        if (TextUtils.isEmpty(botId)) {
            return;
        }
        viewModel
                .refreshToken(botId)
                .observe(
                        getViewLifecycleOwner(),
                        resource -> {
                            if (resource.status == Status.SUCCESS) {
                                OpenClawRobotTokenResult result = resource.data;
                                if (result != null) {
                                    updateRobotInfo(result);
                                    updateToken(result.getToken());
                                    ToastUtils.showToast(
                                            getString(R.string.openclaw_token_success_title));
                                }
                            } else if (resource.status == Status.ERROR) {
                                ToastUtils.showErrorToast(resource.code);
                            }
                        });
    }

    private void startChat() {
        if (TextUtils.isEmpty(botId)) {
            return;
        }
        if (!TextUtils.isEmpty(token)) {
            OpenClawRobotRegistry.registerToken(requireContext(), botId, token);
        }
        RongIM.getInstance().startPrivateChat(requireContext(), botId, getRobotName());
    }
}
