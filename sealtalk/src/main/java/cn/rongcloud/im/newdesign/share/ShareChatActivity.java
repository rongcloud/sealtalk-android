package cn.rongcloud.im.newdesign.share;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import cn.rongcloud.im.ui.BaseActivity;
import io.rong.imkit.feature.forward.ForwardClickActions;
import io.rong.imkit.utils.RouteUtils;
import java.util.ArrayList;

/**
 * 分享/转发统一入口 Activity
 *
 * <p>该 Activity 作为消息转发和图片分享的统一入口，通过不同的 Intent 参数支持两种工作模式：
 *
 * <p><b>模式 1: 图片分享模式</b>
 *
 * <ul>
 *   <li>传入参数：{@link #EXTRA_IMAGE_URI}
 *   <li>工作流程：用户选择会话 → 直接发送图片消息
 *   <li>创建方法：{@link #newIntent(Context, Uri)}
 * </ul>
 *
 * <p><b>模式 2: 消息转发模式</b>
 *
 * <ul>
 *   <li>传入参数：{@link RouteUtils#FORWARD_TYPE} 和 {@link RouteUtils#MESSAGE_IDS}
 *   <li>工作流程：用户选择会话 → 返回结果给 ConversationFragment → SDK 执行转发
 *   <li>创建方法：{@link #newForwardIntent(Context, ForwardClickActions.ForwardType, ArrayList)}
 * </ul>
 *
 * <p><b>Activity 注册：</b>
 *
 * <p>该 Activity 在 {@code IMManager} 中注册为 {@code ForwardSelectConversationActivity} 的替换：
 *
 * <pre>
 * RouteUtils.registerActivity(
 *     RouteUtils.RongActivityType.ForwardSelectConversationActivity,
 *     ShareChatActivity.class);
 * </pre>
 *
 * <p>这样当 SDK 调用 {@code RouteUtils.routeToForwardSelectConversationActivity()} 时， 会自动启动此 Activity
 * 而非默认的 {@code ForwardSelectConversationActivity}。
 *
 * @see SelectChatFragment
 * @see io.rong.imkit.utils.RouteUtils#routeToForwardSelectConversationActivity(Fragment,
 *     ForwardClickActions.ForwardType, ArrayList)
 */
public class ShareChatActivity extends BaseActivity {

    /** 图片分享模式：图片 Uri 参数 Key */
    public static final String EXTRA_IMAGE_URI = "extra_image_uri";

    private Fragment fragment;

    /**
     * 创建图片分享 Intent（无参数，用于外部调用）
     *
     * @param context 上下文
     * @return Intent 实例
     */
    @NonNull
    public static Intent newIntent(@NonNull Context context) {
        return new Intent(context, ShareChatActivity.class);
    }

    /**
     * 创建图片分享 Intent
     *
     * @param context 上下文
     * @param imageUri 图片 Uri
     * @return 配置好的 Intent，包含 EXTRA_IMAGE_URI 参数
     */
    @NonNull
    public static Intent newIntent(@NonNull Context context, @NonNull Uri imageUri) {
        Intent intent = new Intent(context, ShareChatActivity.class);
        intent.putExtra(EXTRA_IMAGE_URI, imageUri);
        return intent;
    }

    /**
     * 创建消息转发 Intent
     *
     * <p>注意：通常不需要手动调用此方法，而是由 SDK 的 {@link RouteUtils#routeToForwardSelectConversationActivity}
     * 自动调用。
     *
     * @param context 上下文
     * @param forwardType 转发类型 {@link ForwardClickActions.ForwardType#SINGLE} 单条消息转发 {@link
     *     ForwardClickActions.ForwardType#MULTI} 多条消息合并转发
     * @param messageIds 要转发的消息 ID 列表
     * @return 配置好的 Intent，包含 FORWARD_TYPE 和 MESSAGE_IDS 参数
     */
    @NonNull
    public static Intent newForwardIntent(
            @NonNull Context context,
            @NonNull ForwardClickActions.ForwardType forwardType,
            @NonNull ArrayList<Integer> messageIds) {
        Intent intent = new Intent(context, ShareChatActivity.class);
        intent.putExtra(RouteUtils.FORWARD_TYPE, forwardType.getValue());
        intent.putIntegerArrayListExtra(RouteUtils.MESSAGE_IDS, messageIds);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(io.rong.imkit.R.layout.rc_activity);

        fragment = new SelectChatFragment();
        FragmentManager manager = getSupportFragmentManager();
        manager.popBackStack();
        manager.beginTransaction()
                .replace(io.rong.imkit.R.id.fl_fragment_container, fragment)
                .commit();
    }
}
