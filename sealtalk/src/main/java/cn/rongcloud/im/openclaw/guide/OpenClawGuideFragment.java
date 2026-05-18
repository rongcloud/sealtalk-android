package cn.rongcloud.im.openclaw.guide;

import android.os.Bundle;
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
import cn.rongcloud.im.R;
import cn.rongcloud.im.openclaw.component.OpenClawPageUtils;
import cn.rongcloud.im.openclaw.create.OpenClawCreateActivity;

public class OpenClawGuideFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        LinearLayout root = OpenClawPageUtils.verticalRoot(requireContext());
        OpenClawPageUtils.padding(root, 16, 36, 16, 24);

        ImageView icon = OpenClawPageUtils.robotIcon(requireContext(), 60);
        LinearLayout.LayoutParams iconParams = OpenClawPageUtils.size(requireContext(), 60, 60);
        iconParams.gravity = Gravity.CENTER_HORIZONTAL;
        root.addView(icon, iconParams);

        TextView description =
                OpenClawPageUtils.text(
                        requireContext(), getString(R.string.openclaw_guide_description), 14);
        description.setLineSpacing(0, 1.15f);
        root.addView(description, OpenClawPageUtils.matchWrapWithTop(requireContext(), 40));

        TextView stepTitle =
                OpenClawPageUtils.text(
                        requireContext(), getString(R.string.openclaw_steps_title), 14);
        root.addView(stepTitle, OpenClawPageUtils.matchWrapWithTop(requireContext(), 26));

        LinearLayout card = OpenClawPageUtils.card(requireContext());
        OpenClawPageUtils.padding(card, 16, 10, 16, 10);
        card.addView(stepText(getString(R.string.openclaw_guide_step_create)));
        card.addView(stepText(getString(R.string.openclaw_guide_step_token)));
        card.addView(stepText(getString(R.string.openclaw_guide_step_install)));
        card.addView(stepText(getString(R.string.openclaw_guide_step_chat)));
        root.addView(card, OpenClawPageUtils.matchWrapWithTop(requireContext(), 10));

        View spacer = new View(requireContext());
        root.addView(spacer, new LinearLayout.LayoutParams(-1, 0, 1));

        TextView button =
                OpenClawPageUtils.primaryButton(
                        requireContext(), getString(R.string.openclaw_start));
        button.setOnClickListener(
                v -> startActivity(OpenClawCreateActivity.newIntent(requireContext())));
        root.addView(
                button,
                new LinearLayout.LayoutParams(-1, OpenClawPageUtils.dp(requireContext(), 42)));
        return root;
    }

    private TextView stepText(String text) {
        TextView textView = OpenClawPageUtils.text(requireContext(), text, 14);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        textView.setMinHeight(OpenClawPageUtils.dp(requireContext(), 34));
        return textView;
    }
}
