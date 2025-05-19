package cn.rongcloud.im.ui.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.activity.AIChatAssistantSettingActivity;
import cn.rongcloud.im.ui.test.CustomConversationFragment;
import cn.rongcloud.im.utils.ToastUtils;
import cn.rongcloud.im.viewmodel.CustomAgentFacadeViewModel;
import io.rong.imkit.IMCenter;
import io.rong.imkit.agent.AgentFacadePage;
import io.rong.imkit.agent.AgentFacadeViewModel;
import io.rong.imkit.conversation.extension.InputMode;
import io.rong.imkit.conversation.extension.RongExtensionViewModel;
import io.rong.imkit.userinfo.RongUserInfoManager;
import io.rong.imkit.usermanage.ViewModelFactory;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.ConversationIdentifier;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.UserInfo;
import io.rong.imlib.model.agent.AgentRecommendation;
import io.rong.message.TextMessage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomAgentFacadePage extends AgentFacadePage {

    private boolean isNeedReload = true;
    // 自定义禁用状态视图容器
    private View disabledStateView;

    public CustomAgentFacadePage(@NonNull Fragment fragment, ConversationIdentifier identifier) {
        super(fragment, identifier);
    }

    @NonNull
    @Override
    public View onCreateView(
            @NonNull Context context,
            @NonNull LayoutInflater inflater,
            @NonNull ViewGroup container,
            @NonNull Bundle args) {
        View view = super.onCreateView(context, inflater, container, args);
        // 获取内容容器FrameLayout
        FrameLayout contentContainer =
                view.findViewById(io.rong.imkit.R.id.rc_agent_content_container);

        // 动态加载禁用状态容器
        if (contentContainer != null) {
            // 从自定义布局中加载禁用状态视图
            disabledStateView =
                    inflater.inflate(R.layout.rc_agent_disabled_container, contentContainer, false);
            contentContainer.addView(disabledStateView);

            // 为禁用状态中的启用按钮设置点击事件
            Button enableButton = disabledStateView.findViewById(R.id.rc_agent_enable_button);
            if (enableButton != null) {
                enableButton.setOnClickListener(this::onEnableButtonClick);
            }

            // 初始时隐藏禁用容器
            disabledStateView.setVisibility(View.GONE);
        }

        return view;
    }

    @Override
    protected AgentFacadeViewModel onCreateViewModel(@NonNull Bundle bundle) {
        return new ViewModelProvider(this.fragment, new ViewModelFactory(bundle))
                .get(CustomAgentFacadeViewModel.class);
    }

    @Override
    protected void onViewReady(@NonNull AgentFacadeViewModel viewModel) {
        super.onViewReady(viewModel);

        viewModel
                .getRecommendationsLiveData()
                .observe(
                        fragment,
                        recommendations -> {
                            if (recommendations != null && !recommendations.isEmpty()) {
                                isNeedReload = false;
                            }
                        });

        viewModel
                .getErrorEventLiveData()
                .observe(
                        fragment,
                        errorEvent -> {
                            if (errorEvent != null && errorEvent.getError() != null) {
                                ToastUtils.showToast(
                                        String.valueOf(errorEvent.getError().getCode()));
                            }
                        });

        setAgentRecommendationListener(
                new AgentFacadePage.AgentRecommendationListener() {
                    @Override
                    public void onItemEdit(List<AgentRecommendation> recommendations) {
                        if (recommendations != null && recommendations.size() == 1) {
                            String content = recommendations.get(0).getContent();
                            isNeedReload = true;
                            if (!TextUtils.isEmpty(content)) {
                                if (fragment instanceof CustomConversationFragment) {
                                    CustomConversationFragment customConversationFragment =
                                            (CustomConversationFragment) fragment;
                                    EditText inputEditText =
                                            customConversationFragment
                                                    .getRongExtension()
                                                    .getInputEditText();
                                    inputEditText.setText(content);
                                    inputEditText.setSelection(content.length());
                                }
                            }
                        }
                    }

                    @Override
                    public void onItemClick(List<AgentRecommendation> recommendations) {
                        if (recommendations != null && !recommendations.isEmpty()) {

                            if (fragment instanceof CustomConversationFragment) {
                                CustomConversationFragment customConversationFragment =
                                        (CustomConversationFragment) fragment;
                                customConversationFragment
                                        .getRongExtension()
                                        .getInputEditText()
                                        .requestFocus();
                                RongExtensionViewModel mExtensionViewModel =
                                        new ViewModelProvider(fragment)
                                                .get(RongExtensionViewModel.class);
                                mExtensionViewModel
                                        .getInputModeLiveData()
                                        .setValue(InputMode.TextInput);
                            }

                            ConversationIdentifier identifier =
                                    CustomAgentFacadePage.this.identifier;

                            if (recommendations.size() == 1) {
                                // 单条推荐，直接发送
                                String content = recommendations.get(0).getContent();
                                sendTextMessage(identifier, content);
                            } else {
                                // 多条推荐，每条间隔1秒发送
                                sendMultipleMessages(identifier, recommendations);
                            }

                            isNeedReload = true;
                        }
                    }
                });
    }

    @Override
    public void onResume() {
        if (isNeedReload) {
            requestRecommendationParams();
        } else {
            super.onResume();
        }
    }

    @Override
    protected void showDisabledView() {
        super.showDisabledView();
        // 显示禁用状态视图
        if (disabledStateView != null) {
            disabledStateView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void showLoadingView() {
        super.showLoadingView();

        // 确保我们的自定义禁用容器是隐藏的
        if (disabledStateView != null) {
            disabledStateView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void showRecommendations(
            List<io.rong.imlib.model.agent.SuperAgentRecommendation> recommendations) {
        super.showRecommendations(recommendations);

        // 确保我们的自定义禁用容器是隐藏的
        if (disabledStateView != null) {
            disabledStateView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void showEmptyView() {
        super.showEmptyView();

        // 确保我们的自定义禁用容器是隐藏的
        if (disabledStateView != null) {
            disabledStateView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void showErrorView() {
        super.showErrorView();

        // 确保我们的自定义禁用容器是隐藏的
        if (disabledStateView != null) {
            disabledStateView.setVisibility(View.GONE);
        }
    }

    /** 启用按钮点击处理方法 */
    protected void onEnableButtonClick(View v) {
        openAIChatAssistantSettingActivity();
    }

    /** 发送单条文本消息 */
    private void sendTextMessage(ConversationIdentifier identifier, String content) {
        if (TextUtils.isEmpty(content)) return;

        TextMessage textMessage = TextMessage.obtain(content);
        IMCenter.getInstance()
                .sendMessage(
                        identifier.getType(),
                        identifier.getTargetId(),
                        textMessage,
                        null,
                        null,
                        new IRongCallback.ISendMessageCallback() {
                            @Override
                            public void onAttached(Message message) {}

                            @Override
                            public void onSuccess(Message message) {}

                            @Override
                            public void onError(
                                    Message message, RongIMClient.ErrorCode errorCode) {}
                        });
    }

    /** 发送多条文本消息，每条间隔1秒 */
    private void sendMultipleMessages(
            ConversationIdentifier identifier, List<AgentRecommendation> recommendations) {
        if (recommendations == null || recommendations.isEmpty()) return;

        new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                for (int i = 0; i < recommendations.size(); i++) {
                                    final int index = i;
                                    String content = recommendations.get(i).getContent();

                                    if (CustomAgentFacadePage.this.fragment != null
                                            && CustomAgentFacadePage.this.fragment.getActivity()
                                                    != null) {
                                        CustomAgentFacadePage.this
                                                .fragment
                                                .getActivity()
                                                .runOnUiThread(
                                                        new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                sendTextMessage(
                                                                        identifier, content);
                                                            }
                                                        });
                                    }

                                    // 每条消息之间间隔1秒，最后一条不需要延迟
                                    if (i < recommendations.size() - 1) {
                                        try {
                                            Thread.sleep(1000);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        })
                .start();
    }

    @Override
    protected Map<String, Object> getCustomInfo() {
        UserInfo targetUserInfo =
                RongUserInfoManager.getInstance().getUserInfo(identifier.getTargetId());
        UserInfo userInfo = RongUserInfoManager.getInstance().getCurrentUserInfo();
        Map<String, Object> customInfo = new HashMap<>(2);
        if (userInfo != null) {
            customInfo.put("user_name", userInfo.getName());
        }
        if (targetUserInfo != null) {
            customInfo.put("target_name", targetUserInfo.getName());
        }
        return customInfo;
    }

    @Override
    protected void onSettingClick(View view) {
        super.onSettingClick(view);
        // 启动 AI 聊天助手设置页面
        openAIChatAssistantSettingActivity();
    }

    private void openAIChatAssistantSettingActivity() {
        // 启动 AI 聊天助手设置页面
        Intent intent = new Intent(fragment.getContext(), AIChatAssistantSettingActivity.class);
        fragment.startActivity(intent);
    }
}
