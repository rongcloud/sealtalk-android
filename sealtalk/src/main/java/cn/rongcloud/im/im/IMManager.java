package cn.rongcloud.im.im;

import static android.content.Context.MODE_PRIVATE;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import cn.rongcloud.im.BuildConfig;
import cn.rongcloud.im.R;
import cn.rongcloud.im.SealApp;
import cn.rongcloud.im.common.ErrorCode;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.common.LogTag;
import cn.rongcloud.im.common.NetConstant;
import cn.rongcloud.im.common.ResultCallback;
import cn.rongcloud.im.db.DBManager;
import cn.rongcloud.im.im.message.GroupApplyMessage;
import cn.rongcloud.im.im.message.GroupClearMessage;
import cn.rongcloud.im.im.message.PokeMessage;
import cn.rongcloud.im.im.message.SealContactNotificationMessage;
import cn.rongcloud.im.im.message.SealGroupConNtfMessage;
import cn.rongcloud.im.im.message.SealGroupNotificationMessage;
import cn.rongcloud.im.im.message.SealUltraGroupNotificationMessage;
import cn.rongcloud.im.im.plugin.ImageTextModule;
import cn.rongcloud.im.im.plugin.InsertMessageModule;
import cn.rongcloud.im.im.plugin.MentionAllModule;
import cn.rongcloud.im.im.plugin.PokeExtensionModule;
import cn.rongcloud.im.im.plugin.SendKVMessageModule;
import cn.rongcloud.im.im.provider.ContactNotificationMessageProvider;
import cn.rongcloud.im.im.provider.GroupApplyMessageItemProvider;
import cn.rongcloud.im.im.provider.PokeMessageItemProvider;
import cn.rongcloud.im.im.provider.SealGroupConNtfMessageProvider;
import cn.rongcloud.im.im.provider.SealGroupNotificationMessageItemProvider;
import cn.rongcloud.im.model.ChatRoomAction;
import cn.rongcloud.im.model.ConversationRecord;
import cn.rongcloud.im.model.LoginResult;
import cn.rongcloud.im.model.ProxyModel;
import cn.rongcloud.im.model.QuietHours;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.TranslationTokenResult;
import cn.rongcloud.im.model.UserCacheInfo;
import cn.rongcloud.im.net.AppProxyManager;
import cn.rongcloud.im.net.CallBackWrapper;
import cn.rongcloud.im.net.SealTalkUrl;
import cn.rongcloud.im.net.proxy.RetrofitProxyServiceCreator;
import cn.rongcloud.im.net.service.UserService;
import cn.rongcloud.im.sp.UserCache;
import cn.rongcloud.im.sp.UserConfigCache;
import cn.rongcloud.im.task.AppTask;
import cn.rongcloud.im.task.UserTask;
import cn.rongcloud.im.ui.activity.ConversationActivity;
import cn.rongcloud.im.ui.activity.ForwardActivity;
import cn.rongcloud.im.ui.activity.GroupNoticeListActivity;
import cn.rongcloud.im.ui.activity.GroupReadReceiptDetailActivity;
import cn.rongcloud.im.ui.activity.MainActivity;
import cn.rongcloud.im.ui.activity.MemberMentionedExActivity;
import cn.rongcloud.im.ui.activity.NewFriendListActivity;
import cn.rongcloud.im.ui.activity.PokeInviteChatActivity;
import cn.rongcloud.im.ui.activity.SplashActivity;
import cn.rongcloud.im.ui.activity.SubConversationListActivity;
import cn.rongcloud.im.ui.activity.UserDetailActivity;
import cn.rongcloud.im.ui.adapter.CombineV2MessageItemProvider;
import cn.rongcloud.im.utils.DataCenter;
import cn.rongcloud.im.utils.log.SLog;
import com.google.gson.Gson;
import io.rong.common.RLog;
import io.rong.contactcard.ContactCardExtensionModule;
import io.rong.contactcard.IContactCardInfoProvider;
import io.rong.imkit.IMCenter;
import io.rong.imkit.MessageInterceptor;
import io.rong.imkit.config.ConversationClickListener;
import io.rong.imkit.config.ConversationListBehaviorListener;
import io.rong.imkit.config.DataProcessor;
import io.rong.imkit.config.RongConfigCenter;
import io.rong.imkit.conversation.extension.RongExtensionManager;
import io.rong.imkit.conversation.messgelist.provider.GroupNotificationMessageItemProvider;
import io.rong.imkit.conversationlist.model.BaseUiConversation;
import io.rong.imkit.feature.mention.IExtensionEventWatcher;
import io.rong.imkit.feature.mention.IMentionedInputListener;
import io.rong.imkit.feature.mention.RongMentionManager;
import io.rong.imkit.feature.quickreply.IQuickReplyProvider;
import io.rong.imkit.manager.UnReadMessageManager;
import io.rong.imkit.model.GroupNotificationMessageData;
import io.rong.imkit.notification.RongNotificationManager;
import io.rong.imkit.streammessage.StreamMessageItemProvider;
import io.rong.imkit.userinfo.RongUserInfoManager;
import io.rong.imkit.userinfo.model.GroupUserInfo;
import io.rong.imkit.utils.RouteUtils;
import io.rong.imlib.ChannelClient;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.IRongCoreCallback;
import io.rong.imlib.IRongCoreEnum;
import io.rong.imlib.IRongCoreListener;
import io.rong.imlib.RongCoreClient;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.chatroom.base.RongChatRoomClient;
import io.rong.imlib.common.DeviceUtils;
import io.rong.imlib.cs.CustomServiceConfig;
import io.rong.imlib.cs.CustomServiceManager;
import io.rong.imlib.model.AndroidConfig;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.ConversationIdentifier;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.HarmonyConfig;
import io.rong.imlib.model.IOSConfig;
import io.rong.imlib.model.InitOption;
import io.rong.imlib.model.MentionedInfo;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageAuditInfo;
import io.rong.imlib.model.MessageAuditType;
import io.rong.imlib.model.MessageConfig;
import io.rong.imlib.model.MessageContent;
import io.rong.imlib.model.MessagePushConfig;
import io.rong.imlib.model.RCIMProxy;
import io.rong.imlib.model.RCTranslationResult;
import io.rong.imlib.model.UltraGroupTypingStatusInfo;
import io.rong.imlib.model.UserInfo;
import io.rong.imlib.publicservice.model.PublicServiceProfile;
import io.rong.imlib.translation.TranslationClient;
import io.rong.imlib.translation.TranslationResultListener;
import io.rong.message.CombineV2Message;
import io.rong.message.ContactNotificationMessage;
import io.rong.message.GroupNotificationMessage;
import io.rong.push.PushEventListener;
import io.rong.push.PushType;
import io.rong.push.RongPushClient;
import io.rong.push.RongPushPlugin;
import io.rong.push.notification.PushNotificationMessage;
import io.rong.recognizer.RecognizeExtensionModule;
import io.rong.sight.SightExtensionModule;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class IMManager {
    private static volatile IMManager instance;
    private final String TAG = IMManager.class.getSimpleName();
    private MutableLiveData<ChatRoomAction> chatRoomActionLiveData = new MutableLiveData<>();
    private Context context;
    private AppTask appTask;

    private UserConfigCache configCache;
    private UserCache userCache;

    private MutableLiveData<Boolean> autologinResult = new MutableLiveData<>();
    private MutableLiveData<Message> messageRouter = new MutableLiveData<>();
    private MutableLiveData<Boolean> kickedOffline = new MutableLiveData<>();
    private MutableLiveData<Boolean> userIsAbandon = new MutableLiveData<>();
    private MutableLiveData<Boolean> userIsBlocked = new MutableLiveData<>();

    /** 接收戳一下消息 */
    private volatile Boolean isReceivePokeMessage = null;

    private IMInfoProvider imInfoProvider;
    private ConversationRecord lastConversationRecord;

    private List<IRongCoreListener.UltraGroupMessageChangeListener>
            mUltraGroupMessageChangeListener = new ArrayList<>();
    private CopyOnWriteArrayList<IRongCoreListener.UserGroupStatusListener>
            mUserGroupStatusListeners = new CopyOnWriteArrayList<>();

    private IMManager() {
        RongMentionManager.getInstance()
                .setMentionedInputListener(
                        new IMentionedInputListener() {
                            @Override
                            public boolean onMentionedInput(
                                    Conversation.ConversationType conversationType,
                                    String targetId) {
                                Intent intent =
                                        new Intent(getContext(), MemberMentionedExActivity.class);
                                intent.putExtra(
                                        RouteUtils.CONVERSATION_TYPE, conversationType.getValue());
                                intent.putExtra(RouteUtils.TARGET_ID, targetId);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);
                                return true;
                            }
                        });
    }

    public static IMManager getInstance() {
        if (instance == null) {
            synchronized (IMManager.class) {
                if (instance == null) {
                    instance = new IMManager();
                }
            }
        }
        return instance;
    }

    public Context getContext() {
        return context;
    }

    /**
     * @param application
     */
    public void init(Application application) {
        RongCoreClient.getInstance().setAppVer(BuildConfig.VERSION_NAME);
        this.context = application.getApplicationContext();
        appTask = new AppTask(this.context);

        // 初始化 IM 相关缓存
        initIMCache();

        // 初始化推送
        initPush();

        initPhrase();

        initIMConfig();

        // 调用 RongIM 初始化
        initRongIM(application);

        initDebug();

        initInterceptor();

        // 初始化用户和群组信息内容提供者
        initInfoProvider(context);

        // 初始化自定义消息和消息模版
        initMessageAndTemplate();

        // 初始化扩展模块
        initExtensionModules(context);

        // 初始化连接状态变化监听
        initConnectStateChangeListener();

        // 初始化消息监听
        initOnReceiveMessage(context);

        // 初始化聊天室监听
        initChatRoomActionListener();

        // 初始化超级群相关监听
        initUltraGroup();

        // 初始化翻译监听
        initTranslationListener();

        // 缓存连接
        cacheConnectIM();
        RongExtensionManager.getInstance()
                .addExtensionEventWatcher(
                        new IExtensionEventWatcher() {
                            @Override
                            public void onTextChanged(
                                    Context context,
                                    Conversation.ConversationType type,
                                    String targetId,
                                    int cursorPos,
                                    int count,
                                    String text) {}

                            @Override
                            public void onSendToggleClick(Message message) {
                                if (message != null
                                        && message.getContent() != null
                                        && message.getContent().getMentionedInfo() != null
                                        && message.getContent()
                                                        .getMentionedInfo()
                                                        .getMentionedUserIdList()
                                                != null
                                        && message.getContent()
                                                        .getMentionedInfo()
                                                        .getMentionedUserIdList()
                                                        .size()
                                                > 0
                                        && message.getContent()
                                                .getMentionedInfo()
                                                .getMentionedUserIdList()
                                                .get(0)
                                                .equals(String.valueOf(-1))) {
                                    message.getContent()
                                            .getMentionedInfo()
                                            .setType(MentionedInfo.MentionedType.ALL);
                                }
                                sendUltraGroupTypingStatus(message);
                            }

                            @Override
                            public void onDeleteClick(
                                    Conversation.ConversationType type,
                                    String targetId,
                                    EditText editText,
                                    int cursorPos) {}

                            @Override
                            public void onDestroy(
                                    Conversation.ConversationType type, String targetId) {}
                        });
    }

    private void initInterceptor() {
        IMCenter.getInstance()
                .setMessageInterceptor(
                        new MessageInterceptor() {
                            @Override
                            public boolean interceptReceivedMessage(
                                    Message message,
                                    int left,
                                    boolean hasPackage,
                                    boolean offline) {
                                return false;
                            }

                            @Override
                            public boolean interceptOnSendMessage(Message message) {
                                // 推送配置测试相关
                                SharedPreferences sharedPreferences =
                                        context.getSharedPreferences("push_config", MODE_PRIVATE);
                                String id = sharedPreferences.getString("id", "");
                                String title = sharedPreferences.getString("title", "");
                                String content = sharedPreferences.getString("content", "");
                                String data = sharedPreferences.getString("data", "");
                                String hw = sharedPreferences.getString("hw", "");
                                String hwImportance =
                                        sharedPreferences.getString("importance", "NORMAL");
                                String mi = sharedPreferences.getString("mi", "");
                                String oppo = sharedPreferences.getString("oppo", "");
                                String threadId = sharedPreferences.getString("threadId", "");
                                String apnsId = sharedPreferences.getString("apnsId", "");
                                String category = sharedPreferences.getString("category", "");
                                String richMediaUri =
                                        sharedPreferences.getString("richMediaUri", "");
                                String interruptionLevel =
                                        sharedPreferences.getString("interruptionLevel", "");
                                String fcm = sharedPreferences.getString("fcm", "");
                                String imageUrl = sharedPreferences.getString("imageUrl", "");
                                String hwCategory = sharedPreferences.getString("hwCategory", null);
                                String vivoCategory =
                                        sharedPreferences.getString("vivoCategory", null);
                                boolean vivo = sharedPreferences.getBoolean("vivo", true);
                                boolean disableTitle =
                                        sharedPreferences.getBoolean("disableTitle", false);
                                String templateId = sharedPreferences.getString("templateId", "");
                                boolean forceDetail =
                                        sharedPreferences.getBoolean("forceDetail", false);
                                String imageUrlHW = sharedPreferences.getString("imageUrlHW", "");
                                String imageUrlMi = sharedPreferences.getString("imageUrlMi", "");
                                String fcmChannelId =
                                        sharedPreferences.getString("fcmChannelId", "");
                                String imageUrlHonor =
                                        sharedPreferences.getString("imageUrlHonor", "");
                                String importanceHonor =
                                        sharedPreferences.getString("importanceHonor", "NORMAL");
                                String ohosCategory =
                                        sharedPreferences.getString("ohosCategory", "");
                                String ohosImageUrl =
                                        sharedPreferences.getString("ohosImageUrl", "");
                                IOSConfig iosConfig =
                                        new IOSConfig(threadId, apnsId, category, richMediaUri);
                                iosConfig.setInterruptionLevel(interruptionLevel);
                                HarmonyConfig harmonyConfig =
                                        new HarmonyConfig(ohosCategory, ohosImageUrl);
                                MessagePushConfig messagePushConfig =
                                        new MessagePushConfig.Builder()
                                                .setPushTitle(title)
                                                .setPushContent(content)
                                                .setPushData(data)
                                                .setForceShowDetailContent(forceDetail)
                                                .setDisablePushTitle(disableTitle)
                                                .setTemplateId(templateId)
                                                .setAndroidConfig(
                                                        new AndroidConfig.Builder()
                                                                .setNotificationId(id)
                                                                .setChannelIdHW(hw)
                                                                .setChannelIdMi(mi)
                                                                .setChannelIdOPPO(oppo)
                                                                .setTypeVivo(
                                                                        vivo
                                                                                ? AndroidConfig
                                                                                        .SYSTEM
                                                                                : AndroidConfig
                                                                                        .OPERATE)
                                                                .setFcmCollapseKey(fcm)
                                                                .setFcmImageUrl(imageUrl)
                                                                .setImportanceHW(
                                                                        getImportance(hwImportance))
                                                                .setChannelIdFCM(fcmChannelId)
                                                                .setImageUrlMi(imageUrlMi)
                                                                .setImageUrlHW(imageUrlHW)
                                                                .setCategoryHW(hwCategory)
                                                                .setCategoryVivo(vivoCategory)
                                                                .setImportanceHonor(
                                                                        AndroidConfig
                                                                                .ImportanceHonor
                                                                                .getImportanceHonor(
                                                                                        importanceHonor))
                                                                .setImageUrlHonor(imageUrlHonor)
                                                                .build())
                                                .setIOSConfig(iosConfig)
                                                .setHarmonyConfig(harmonyConfig)
                                                .build();
                                message.setMessagePushConfig(messagePushConfig);
                                SharedPreferences sharedPreferencesPush =
                                        context.getSharedPreferences("MessageConfig", MODE_PRIVATE);
                                boolean disableNotification =
                                        sharedPreferencesPush.getBoolean(
                                                "disableNotification", false);
                                message.setMessageConfig(
                                        new MessageConfig.Builder()
                                                .setDisableNotification(disableNotification)
                                                .build());
                                message.getContent().setAuditInfo(getTestAuditInfo());
                                return false;
                            }

                            private MessageAuditInfo getTestAuditInfo() {
                                SharedPreferences sharedPreferences =
                                        context.getSharedPreferences("audit_config", MODE_PRIVATE);
                                int switchInt = sharedPreferences.getInt("switch", 0);
                                String projectText = sharedPreferences.getString("project", "");
                                String strategyText = sharedPreferences.getString("strategy", "");
                                MessageAuditInfo messageAuditInfo =
                                        new MessageAuditInfo(
                                                MessageAuditType.valueOf(switchInt),
                                                projectText,
                                                strategyText);
                                return messageAuditInfo;
                            }

                            @Override
                            public boolean interceptOnSentMessage(Message message) {
                                if (message != null) {
                                    message.setMessageConfig(null);
                                }
                                return false;
                            }

                            @Override
                            public boolean interceptOnInsertOutgoingMessage(
                                    Conversation.ConversationType type,
                                    String targetId,
                                    Message.SentStatus sentStatus,
                                    MessageContent content,
                                    long time) {
                                return false;
                            }

                            @Override
                            public boolean interceptOnInsertIncomingMessage(
                                    Conversation.ConversationType type,
                                    String targetId,
                                    String senderId,
                                    Message.ReceivedStatus receivedStatus,
                                    MessageContent content,
                                    long time) {
                                return false;
                            }
                        });
    }

    private void initTranslationListener() {
        if (TranslationClient.getInstance().isTextTranslationSupported()) {
            TranslationClient.getInstance()
                    .addTranslationResultListener(
                            new TranslationResultListener() {
                                @Override
                                public void onTranslationResult(
                                        int code, RCTranslationResult result) {
                                    handleTranslationFailedEvent(code, result);
                                }
                            });
        }
    }

    private void handleTranslationFailedEvent(int code, RCTranslationResult result) {
        if (code == IRongCoreEnum.CoreErrorCode.RC_TRANSLATION_CODE_INVALID_AUTH_TOKEN.code
                || code == IRongCoreEnum.CoreErrorCode.RC_TRANSLATION_CODE_AUTH_FAILED.code
                || code
                        == IRongCoreEnum.CoreErrorCode.RC_TRANSLATION_CODE_SERVER_AUTH_FAILED
                                .code) {
            updateJwtToken();
        }
    }

    private void initUltraGroupMessageChangeListener() {
        ChannelClient.getInstance()
                .setUltraGroupMessageChangeListener(
                        new IRongCoreListener.UltraGroupMessageChangeListener() {
                            @Override
                            public void onUltraGroupMessageExpansionUpdated(
                                    List<Message> messages) {
                                if (messages == null) {
                                    return;
                                }
                                StringBuilder stringBuilder = new StringBuilder();
                                stringBuilder.append("扩展更新: ");
                                for (Message message : messages) {
                                    stringBuilder.append(message.getUId()).append(",");
                                }

                                new Handler(Looper.getMainLooper())
                                        .post(
                                                new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        // 此时已经回到主线程
                                                        Toast.makeText(
                                                                        context,
                                                                        stringBuilder.toString(),
                                                                        Toast.LENGTH_LONG)
                                                                .show();
                                                    }
                                                });
                                RLog.e(TAG, "onUltraGroupMessageExpansionUpdated:" + stringBuilder);
                                for (IRongCoreListener.UltraGroupMessageChangeListener l :
                                        mUltraGroupMessageChangeListener) {
                                    l.onUltraGroupMessageExpansionUpdated(messages);
                                }
                            }

                            @Override
                            public void onUltraGroupMessageModified(List<Message> messages) {
                                if (messages == null) {
                                    return;
                                }
                                StringBuilder stringBuilder = new StringBuilder();
                                stringBuilder.append("消息修改：");
                                for (Message message : messages) {
                                    stringBuilder.append(message.getUId()).append(",");
                                }

                                new Handler(Looper.getMainLooper())
                                        .post(
                                                new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        // 此时已经回到主线程
                                                        Toast.makeText(
                                                                        context,
                                                                        stringBuilder.toString(),
                                                                        Toast.LENGTH_LONG)
                                                                .show();
                                                    }
                                                });
                                RLog.e(TAG, "onUltraGroupMessageModified:" + stringBuilder);
                                for (IRongCoreListener.UltraGroupMessageChangeListener l :
                                        mUltraGroupMessageChangeListener) {
                                    l.onUltraGroupMessageModified(messages);
                                }
                            }

                            @Override
                            public void onUltraGroupMessageRecalled(List<Message> messages) {
                                if (messages == null) {
                                    return;
                                }

                                StringBuilder stringBuilder = new StringBuilder();
                                stringBuilder.append("消息撤回：");
                                for (Message message : messages) {
                                    stringBuilder.append(message.getUId()).append(",");
                                }

                                new Handler(Looper.getMainLooper())
                                        .post(
                                                new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        // 此时已经回到主线程
                                                        Toast.makeText(
                                                                        context,
                                                                        stringBuilder.toString(),
                                                                        Toast.LENGTH_LONG)
                                                                .show();
                                                    }
                                                });
                                RLog.e(TAG, "onUltraGroupMessageRecalled:" + stringBuilder);
                                for (IRongCoreListener.UltraGroupMessageChangeListener l :
                                        mUltraGroupMessageChangeListener) {
                                    l.onUltraGroupMessageRecalled(messages);
                                }
                            }
                        });
    }

    private void initUserGroupEventListener() {
        ChannelClient.getInstance()
                .setUserGroupStatusListener(
                        new IRongCoreListener.UserGroupStatusListener() {
                            @Override
                            public void userGroupDisbandFrom(
                                    ConversationIdentifier identifier, String[] userGroupIds) {
                                String users = Arrays.toString(userGroupIds);
                                RLog.d(TAG, "userGroupDisbandFrom: " + identifier + "," + users);
                                for (IRongCoreListener.UserGroupStatusListener listener :
                                        mUserGroupStatusListeners) {
                                    try {
                                        listener.userGroupDisbandFrom(identifier, userGroupIds);
                                    } catch (Exception e) {
                                        RLog.e(TAG, "userGroupDisbandFrom e: " + e);
                                    }
                                }
                            }

                            @Override
                            public void userAddedTo(
                                    ConversationIdentifier identifier, String[] userGroupIds) {
                                String users = Arrays.toString(userGroupIds);
                                RLog.d(TAG, "userAddedTo: " + identifier + "," + users);
                                for (IRongCoreListener.UserGroupStatusListener listener :
                                        mUserGroupStatusListeners) {
                                    try {
                                        listener.userAddedTo(identifier, userGroupIds);
                                    } catch (Exception e) {
                                        RLog.e(TAG, "userAddedTo e: " + e);
                                    }
                                }
                            }

                            @Override
                            public void userRemovedFrom(
                                    ConversationIdentifier identifier, String[] userGroupIds) {
                                String users = Arrays.toString(userGroupIds);
                                RLog.d(TAG, "userRemovedFrom: " + identifier + "," + users);
                                for (IRongCoreListener.UserGroupStatusListener listener :
                                        mUserGroupStatusListeners) {
                                    try {
                                        listener.userRemovedFrom(identifier, userGroupIds);
                                    } catch (Exception e) {
                                        RLog.e(TAG, "userRemovedFrom e: " + e);
                                    }
                                }
                            }

                            @Override
                            public void userGroupBindTo(
                                    ConversationIdentifier identifier,
                                    IRongCoreEnum.UltraGroupChannelType channelType,
                                    String[] userGroupIds) {
                                String users = Arrays.toString(userGroupIds);
                                RLog.d(
                                        TAG,
                                        "userGroupBindTo: "
                                                + identifier
                                                + ","
                                                + channelType
                                                + ","
                                                + users);
                                for (IRongCoreListener.UserGroupStatusListener listener :
                                        mUserGroupStatusListeners) {
                                    try {
                                        listener.userGroupBindTo(
                                                identifier, channelType, userGroupIds);
                                    } catch (Exception e) {
                                        RLog.e(TAG, "userGroupBindTo e: " + e);
                                    }
                                }
                            }

                            @Override
                            public void userGroupUnbindFrom(
                                    ConversationIdentifier identifier,
                                    IRongCoreEnum.UltraGroupChannelType channelType,
                                    String[] userGroupIds) {
                                String users = Arrays.toString(userGroupIds);
                                RLog.d(
                                        TAG,
                                        "userGroupUnbindFrom: "
                                                + identifier
                                                + ","
                                                + channelType
                                                + ","
                                                + users);
                                for (IRongCoreListener.UserGroupStatusListener listener :
                                        mUserGroupStatusListeners) {
                                    try {
                                        listener.userGroupUnbindFrom(
                                                identifier, channelType, userGroupIds);
                                    } catch (Exception e) {
                                        RLog.e(TAG, "userGroupUnbindFrom e: " + e);
                                    }
                                }
                            }
                        });
    }

    private void initUltraGroupConversationListener() {
        ChannelClient.getInstance()
                .setUltraGroupConversationListener(
                        new IRongCoreListener.UltraGroupConversationListener() {
                            @Override
                            public void ultraGroupConversationListDidSync() {
                                RLog.e(TAG, "ultraGroupConversationListDidSync");
                            }
                        });
    }

    public void addUltraGroupMessageChangeListener(
            IRongCoreListener.UltraGroupMessageChangeListener listener) {
        this.mUltraGroupMessageChangeListener.add(listener);
    }

    public void removeUltraGroupMessageChangeListener(
            IRongCoreListener.UltraGroupMessageChangeListener listener) {
        this.mUltraGroupMessageChangeListener.remove(listener);
    }

    public void addUserGroupStatusListener(IRongCoreListener.UserGroupStatusListener listener) {
        this.mUserGroupStatusListeners.add(listener);
    }

    public void removeUserGroupStatusListener(IRongCoreListener.UserGroupStatusListener listener) {
        this.mUserGroupStatusListeners.remove(listener);
    }

    private void initUltraGroupTyingStatusListener() {
        ChannelClient.getInstance()
                .setUltraGroupTypingStatusListener(
                        infoList -> {
                            if (infoList == null || infoList.isEmpty()) {
                                return;
                            }
                            RLog.e(TAG, "onUltraGroupTypingStatusChanged: " + infoList.size());
                            for (UltraGroupTypingStatusInfo info : infoList) {
                                StringBuilder infoString = new StringBuilder();
                                infoString
                                        .append(info.getTargetId())
                                        .append(",")
                                        .append(info.getChannelId())
                                        .append(",")
                                        .append(info.getUserId());

                                RLog.e(TAG, "onUltraGroupTypingStatusChanged: " + infoString);
                            }
                        });
    }

    private void initUltraGroup() {
        // 初始化超级群已读状态监听
        initUltraGroupReadStatusListener();

        // 初始化超级群输入状态监听
        initUltraGroupTyingStatusListener();

        // 设置超级群消息变化监听
        initUltraGroupMessageChangeListener();

        // 初始化超级群会话监听
        initUltraGroupConversationListener();

        // 初始化用户组监听
        initUserGroupEventListener();
    }

    private void initUltraGroupReadStatusListener() {
        ChannelClient.getInstance()
                .setUltraGroupReadTimeListener(
                        (targetId, channelId, time) -> {
                            RLog.e(
                                    TAG,
                                    "onUltraGroupReadTimeReceived: "
                                            + targetId
                                            + ","
                                            + channelId
                                            + ","
                                            + time);
                            ChannelClient.getInstance()
                                    .syncUltraGroupReadStatus(
                                            targetId,
                                            channelId,
                                            time,
                                            new IRongCoreCallback.OperationCallback() {
                                                @Override
                                                public void onSuccess() {
                                                    RLog.e(
                                                            TAG,
                                                            "syncUltraGroupReadStatus success "
                                                                    + targetId
                                                                    + ","
                                                                    + channelId
                                                                    + ","
                                                                    + time);
                                                }

                                                @Override
                                                public void onError(
                                                        IRongCoreEnum.CoreErrorCode coreErrorCode) {
                                                    RLog.e(
                                                            TAG,
                                                            "syncUltraGroupReadStatus "
                                                                    + targetId
                                                                    + ","
                                                                    + channelId
                                                                    + ","
                                                                    + time
                                                                    + ",e:"
                                                                    + coreErrorCode);
                                                }
                                            });
                        });
    }

    /** 缓存登录 */
    public void cacheConnectIM() {
        if (RongIMClient.getInstance().getCurrentConnectionStatus()
                == RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED) {
            autologinResult.setValue(true);
            return;
        }

        UserCacheInfo userCache = this.userCache.getUserCache();
        if (userCache == null) {
            autologinResult.setValue(false);
            return;
        }

        String loginToken = this.userCache.getUserCache().getLoginToken();
        if (TextUtils.isEmpty(loginToken)) {
            autologinResult.setValue(false);
            return;
        }

        connectIM(
                loginToken,
                true,
                true,
                new ResultCallback<String>() {
                    @Override
                    public void onSuccess(String s) {
                        autologinResult.postValue(true);
                    }

                    @Override
                    public void onFail(int errorCode) {
                        RLog.e(TAG, "cacheConnectIM errorCode:" + errorCode);
                        // 缓存登录时可以认为缓存了之前连接成功过的结果，所以当再次连接失败时可以通过 SDK 自动重连连接成功
                        if (ErrorCode.USER_ABANDON.getCode() == errorCode
                                || ErrorCode.USER_BLOCKED.getCode() == errorCode) {
                            autologinResult.postValue(false);
                        } else {
                            autologinResult.postValue(true);
                        }
                    }
                });
    }

    public void evaluateCustomService(
            String targetId,
            int stars,
            CustomServiceConfig.CSEvaSolveStatus resolveStatus,
            String lables,
            String suggestion,
            String dialogId) {
        RongIMClient.getInstance()
                .evaluateCustomService(
                        targetId, stars, resolveStatus, lables, suggestion, dialogId, null);
    }

    /**
     * 设置人工评价监听 当人工评价有标签等配置时，在回调中返回配置
     *
     * @param listener
     */
    public void setCustomServiceHumanEvaluateListener(
            CustomServiceManager.OnHumanEvaluateListener listener) {
        RongIMClient.getInstance().setCustomServiceHumanEvaluateListener(listener);
    }

    /**
     * 设置正在输入消息状态
     *
     * @param listener
     */
    public void setTypingStatusListener(RongIMClient.TypingStatusListener listener) {
        RongIMClient.setTypingStatusListener(listener);
    }

    /**
     * 获取从公众号信息
     *
     * @param type
     * @param targetId
     * @param callback
     */
    public void getPublicServiceProfile(
            Conversation.PublicServiceType type,
            String targetId,
            RongIMClient.ResultCallback<PublicServiceProfile> callback) {
        RongIMClient.getInstance()
                .getPublicServiceProfile(
                        type,
                        targetId,
                        new RongIMClient.ResultCallback<PublicServiceProfile>() {
                            @Override
                            public void onSuccess(PublicServiceProfile publicServiceProfile) {
                                if (callback != null) {
                                    callback.onSuccess(publicServiceProfile);
                                }
                            }

                            @Override
                            public void onError(RongIMClient.ErrorCode e) {
                                if (callback != null) {
                                    callback.onError(RongIMClient.ErrorCode.valueOf(e.getValue()));
                                }
                            }
                        });
    }

    /** 初始化会话相关 */
    private void initConversation() {
        RongConfigCenter.conversationConfig()
                .setConversationClickListener(
                        new ConversationClickListener() {
                            @Override
                            public boolean onUserPortraitClick(
                                    Context context,
                                    Conversation.ConversationType conversationType,
                                    UserInfo user,
                                    String targetId) {
                                if (conversationType
                                        != Conversation.ConversationType.CUSTOMER_SERVICE) {
                                    Intent intent = new Intent(context, UserDetailActivity.class);
                                    intent.putExtra(IntentExtra.STR_TARGET_ID, user.getUserId());
                                    if (conversationType == Conversation.ConversationType.GROUP) {
                                        Group groupInfo =
                                                RongUserInfoManager.getInstance()
                                                        .getGroupInfo(targetId);
                                        if (groupInfo != null) {
                                            intent.putExtra(
                                                    IntentExtra.GROUP_ID, groupInfo.getId());
                                            intent.putExtra(
                                                    IntentExtra.STR_GROUP_NAME,
                                                    groupInfo.getName());
                                        }
                                    }
                                    context.startActivity(intent);
                                }
                                return true;
                            }

                            @Override
                            public boolean onUserPortraitLongClick(
                                    Context context,
                                    Conversation.ConversationType conversationType,
                                    UserInfo user,
                                    String targetId) {
                                return false;
                            }

                            @Override
                            public boolean onMessageClick(
                                    Context context, View view, Message message) {
                                // todo 二维码相关功能
                                //                if (message.getContent() instanceof ImageMessage)
                                // {
                                //                    Intent intent = new Intent(view.getContext(),
                                // SealPicturePagerActivity.class);
                                //
                                // intent.setPackage(view.getContext().getPackageName());
                                //                    intent.putExtra("message", message);
                                //                    view.getContext().startActivity(intent);
                                //                    return true;
                                //                }
                                return false;
                            }

                            @Override
                            public boolean onMessageLongClick(
                                    Context context, View view, Message message) {
                                return false;
                            }

                            @Override
                            public boolean onMessageLinkClick(
                                    Context context, String link, Message message) {
                                return false;
                            }

                            @Override
                            public boolean onReadReceiptStateClick(
                                    Context context, Message message) {
                                if (message.getConversationType()
                                        == Conversation.ConversationType.GROUP) { // 目前只适配了群组会话
                                    // 群组显示未读消息的人的信息
                                    Intent intent =
                                            new Intent(
                                                    context, GroupReadReceiptDetailActivity.class);
                                    intent.putExtra(IntentExtra.PARCEL_MESSAGE, message);
                                    context.startActivity(intent);
                                    return true;
                                }
                                return false;
                            }
                        });
    }

    /** 初始化 IM 相关缓存 */
    private void initIMCache() {
        // 用户设置缓存 sp
        configCache = new UserConfigCache(context.getApplicationContext());
        userCache = new UserCache(context.getApplicationContext());
    }

    /** 初始化会话列表相关事件 */
    private void initConversationList() {
        Conversation.ConversationType[] supportedTypes = {
            Conversation.ConversationType.PRIVATE,
            Conversation.ConversationType.GROUP,
            Conversation.ConversationType.SYSTEM,
            Conversation.ConversationType.APP_PUBLIC_SERVICE,
            Conversation.ConversationType.PUBLIC_SERVICE,
        };
        RouteUtils.registerActivity(
                RouteUtils.RongActivityType.ConversationActivity, ConversationActivity.class);
        RouteUtils.registerActivity(
                RouteUtils.RongActivityType.ForwardSelectConversationActivity,
                ForwardActivity.class);
        RouteUtils.registerActivity(
                RouteUtils.RongActivityType.SubConversationListActivity,
                SubConversationListActivity.class);
        RongConfigCenter.conversationListConfig()
                .setDataProcessor(
                        new DataProcessor<Conversation>() {
                            @Override
                            public Conversation.ConversationType[] supportedTypes() {
                                return supportedTypes;
                            }

                            @Override
                            public List<Conversation> filtered(List<Conversation> data) {
                                return data;
                            }

                            @Override
                            public boolean isGathered(Conversation.ConversationType type) {
                                if (type.equals(Conversation.ConversationType.SYSTEM)) {
                                    return true;
                                } else {
                                    return false;
                                }
                            }
                        });
        RongConfigCenter.conversationListConfig()
                .setBehaviorListener(
                        new ConversationListBehaviorListener() {
                            @Override
                            public boolean onConversationPortraitClick(
                                    Context context,
                                    Conversation.ConversationType conversationType,
                                    String targetId) {
                                // 如果是群通知，点击头像进入群通知页面
                                if (targetId.equals("__group_apply__")) {
                                    Intent noticeListIntent =
                                            new Intent(context, GroupNoticeListActivity.class);
                                    context.startActivity(noticeListIntent);
                                    return true;
                                }
                                return false;
                            }

                            @Override
                            public boolean onConversationPortraitLongClick(
                                    Context context,
                                    Conversation.ConversationType conversationType,
                                    String targetId) {
                                return false;
                            }

                            @Override
                            public boolean onConversationLongClick(
                                    Context context, View view, BaseUiConversation conversation) {
                                return false;
                            }

                            @Override
                            public boolean onConversationClick(
                                    Context context, View view, BaseUiConversation conversation) {
                                /*
                                 * 当点击会话列表中通知添加好友消息时，判断是否已成为好友
                                 * 已成为好友时，跳转到私聊界面
                                 * 非好友时跳转到新的朋友界面查看添加好友状态
                                 */
                                //                        MessageContent messageContent =
                                // conversation.mCore.getLatestMessage();
                                //                        if (messageContent instanceof
                                // ContactNotificationMessage) {
                                //                            ContactNotificationMessage
                                // contactNotificationMessage = (ContactNotificationMessage)
                                // messageContent;
                                //                            if
                                // (contactNotificationMessage.getOperation().equals("AcceptResponse")) {
                                //                                // 被加方同意请求后
                                //                                if
                                // (contactNotificationMessage.getExtra() != null) {
                                //                                    ContactNotificationMessageData
                                // bean = null;
                                //                                    try {
                                //                                        Gson gson = new Gson();
                                //                                        bean =
                                // gson.fromJson(contactNotificationMessage.getExtra(),
                                // ContactNotificationMessageData.class);
                                //                                    } catch (Exception e) {
                                //                                        e.printStackTrace();
                                //                                    }
                                //                                    Bundle bundle = new Bundle();
                                //
                                // bundle.putString(RouteUtils.TITLE, bean.getSourceUserNickname());
                                //
                                // RouteUtils.routeToConversationActivity(context,
                                // conversation.mCore.getConversationType(),
                                // conversation.mCore.getTargetId(), bundle);
                                //                                }
                                //                            } else {
                                //                                context.startActivity(new
                                // Intent(context, NewFriendListActivity.class));
                                //                            }
                                //                            return true;
                                //                        } else if (messageContent instanceof
                                // GroupApplyMessage) {
                                //                            Intent noticeListIntent = new
                                // Intent(context, GroupNoticeListActivity.class);
                                //
                                // context.startActivity(noticeListIntent);
                                //                            return true;
                                //                        }
                                return false;
                            }
                        });
        RongConfigCenter.gatheredConversationConfig()
                .setConversationTitle(
                        Conversation.ConversationType.SYSTEM,
                        R.string.seal_conversation_title_system);
    }

    /**
     * 更新 IMKit 显示用用户信息
     *
     * @param userId
     * @param userName
     * @param portraitUri
     */
    public void updateUserInfoCache(String userId, String userName, Uri portraitUri, String alias) {

        UserInfo oldUserInfo = RongUserInfoManager.getInstance().getUserInfo(userId);
        if (oldUserInfo == null
                || (!oldUserInfo.getName().equals(userName)
                        || oldUserInfo.getPortraitUri() == null
                        || !oldUserInfo.getPortraitUri().equals(portraitUri))
                || !TextUtils.equals(oldUserInfo.getAlias(), alias)) {
            UserInfo userInfo = new UserInfo(userId, userName, portraitUri);
            userInfo.setAlias(alias);
            RongUserInfoManager.getInstance().refreshUserInfoCache(userInfo);
        }
    }

    /**
     * 更新 IMKit 显示用群组信息
     *
     * @param groupId
     * @param groupName
     * @param portraitUri
     */
    public void updateGroupInfoCache(String groupId, String groupName, Uri portraitUri) {
        Group oldGroup = RongUserInfoManager.getInstance().getGroupInfo(groupId);
        if (oldGroup == null
                || (!oldGroup.getName().equals(groupName)
                        || oldGroup.getPortraitUri() == null
                        || !oldGroup.getPortraitUri().equals(portraitUri))) {
            Group group = new Group(groupId, groupName, portraitUri);
            RongUserInfoManager.getInstance().refreshGroupInfoCache(group);
        }
    }

    /**
     * 更新 IMKit 显示用群组成员信息
     *
     * @param groupId
     * @param userId
     * @param nickName
     */
    public void updateGroupMemberInfoCache(String groupId, String userId, String nickName) {
        GroupUserInfo oldGroupUserInfo =
                RongUserInfoManager.getInstance().getGroupUserInfo(groupId, userId);
        if (oldGroupUserInfo == null || (!oldGroupUserInfo.getNickname().equals(nickName))) {
            GroupUserInfo groupMemberInfo = new GroupUserInfo(groupId, userId, nickName);
            RongUserInfoManager.getInstance().refreshGroupUserInfoCache(groupMemberInfo);
        }
    }

    /**
     * 清除会话及消息
     *
     * @param targetId
     * @param conversationType
     */
    public void clearConversationAndMessage(
            String targetId, Conversation.ConversationType conversationType) {
        RongIMClient.getInstance()
                .getConversation(
                        conversationType,
                        targetId,
                        new RongIMClient.ResultCallback<Conversation>() {
                            @Override
                            public void onSuccess(Conversation conversation) {
                                IMCenter.getInstance()
                                        .cleanHistoryMessages(
                                                ConversationIdentifier.obtain(
                                                        conversationType, targetId, ""),
                                                0,
                                                false,
                                                new RongIMClient.OperationCallback() {
                                                    @Override
                                                    public void onSuccess() {
                                                        IMCenter.getInstance()
                                                                .removeConversation(
                                                                        conversationType,
                                                                        targetId,
                                                                        new RongIMClient
                                                                                        .ResultCallback<
                                                                                Boolean>() {
                                                                            @Override
                                                                            public void onSuccess(
                                                                                    Boolean
                                                                                            aBoolean) {}

                                                                            @Override
                                                                            public void onError(
                                                                                    RongIMClient
                                                                                                    .ErrorCode
                                                                                            errorCode) {}
                                                                        });
                                                    }

                                                    @Override
                                                    public void onError(
                                                            RongIMClient.ErrorCode errorCode) {}
                                                });
                            }

                            @Override
                            public void onError(RongIMClient.ErrorCode errorCode) {}
                        });
    }

    /** 初始化推送 */
    private void initPush() {
        /*
         * 配置 融云 IM 消息推送
         * 根据需求配置各个平台的推送
         * 配置推送需要在初始化 融云 SDK 之前
         */
        RongPushPlugin.init(getContext());
        RongPushClient.setPushEventListener(
                new PushEventListener() {
                    @Override
                    public boolean preNotificationMessageArrived(
                            Context context,
                            PushType pushType,
                            PushNotificationMessage notificationMessage) {
                        RLog.d(TAG, "preNotificationMessageArrived");
                        RongPushClient.recordPushArriveEvent(
                                context, pushType, notificationMessage);
                        return false;
                    }

                    @Override
                    public void afterNotificationMessageArrived(
                            Context context,
                            PushType pushType,
                            PushNotificationMessage notificationMessage) {
                        RLog.d(TAG, "afterNotificationMessageArrived");
                    }

                    @Override
                    public boolean onNotificationMessageClicked(
                            Context context,
                            PushType pushType,
                            PushNotificationMessage notificationMessage) {
                        RLog.d(TAG, "onNotificationMessageClicked");
                        if (!notificationMessage
                                .getSourceType()
                                .equals(PushNotificationMessage.PushSourceType.FROM_ADMIN)) {
                            String targetId = notificationMessage.getTargetId();
                            // 10000 为 Demo Server 加好友的 id，若 targetId 为 10000，则为加好友消息，默认跳转到
                            // NewFriendListActivity
                            if (targetId != null && targetId.equals("10000")) {
                                Intent intentMain =
                                        new Intent(context, NewFriendListActivity.class);
                                intentMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                Intent intentNewFriend = new Intent(context, MainActivity.class);
                                intentNewFriend.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                Intent[] intents = new Intent[] {};
                                intents[0] = intentMain;
                                intents[1] = intentNewFriend;
                                context.startActivities(intents);
                                return true;
                            } else {
                                Intent intentMain = new Intent(context, SplashActivity.class);
                                intentMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intentMain);
                                return true;
                            }
                        }
                        return false;
                    }

                    @Override
                    public void onThirdPartyPushState(
                            PushType pushType, String action, long resultCode) {}

                    @Override
                    public void onTokenReceived(PushType pushType, String token) {
                        RLog.d(
                                TAG,
                                "onTokenReceived: pushType = " + pushType + ",token = " + token);
                    }

                    @Override
                    public void onTokenReportResult(
                            PushType reportType, int code, PushType finalType, String finalToken) {
                        RLog.d(
                                TAG,
                                "onTokenReportResult: reportType = "
                                        + reportType
                                        + ",code = "
                                        + code
                                        + ",finalType = "
                                        + finalType
                                        + ",finalToken = "
                                        + finalToken);
                    }
                });
    }

    /** IM 配置 */
    private void initIMConfig() {
        // 将私聊，群组加入消息已读回执
        Conversation.ConversationType[] types =
                new Conversation.ConversationType[] {
                    Conversation.ConversationType.PRIVATE,
                    Conversation.ConversationType.GROUP,
                    Conversation.ConversationType.ENCRYPTED
                };
        RongConfigCenter.featureConfig().enableReadReceipt(types);

        // 配置会话列表界面相关内容
        initConversationList();
        // 配置会话界面
        initConversation();

        RongConfigCenter.featureConfig().enableDestruct(BuildConfig.DEBUG);
    }

    private void initPhrase() {
        if (appTask.isDebugMode()) {

            List<String> phraseList = new ArrayList<>();
            phraseList.add(
                    "1为了研究编译器的实现原理，我们需要使用 clang 命令。clang 命令可以将 Objetive-C 的源码改写成 C / C++ 语言的，借此可以研究 block 中各个特性的源码实现方式。该命令是为了研究编译器的实现原理，我们需要使用 clang ");
            phraseList.add(
                    "clang 命令可以将 Objetive-C 的源码改写成 C / C++ 语言的，借此可以研究 block 中各个特性的源码实现方式。该命令是\",@\"2为了研究编译器的实现原理，我们需要使用 clang 命令");
            phraseList.add(
                    "clang 命令可以将 Objetive-C 的源码改写成 C / C++ 语言的，借此可以研究 block 中各个特性的源码实现方式\", @\"33333333\", @\"4超瓷晶的最大优势就是缺点很少但硬度很高\", @\"5苹果iPhone 12采用了一种新的保护玻璃涂层，名叫超瓷晶（Ceramic Shield）。官方称新机的防摔能力增强4倍，光学性能更加出色，更加防刮。考虑到之前iPhone的玻璃保护做得并不算好，如果这次真如苹果所说那么好，进步还是很明显的");
            phraseList.add("6超瓷晶技术由康宁开发，利用超高温结晶制造工艺，他们将微小陶瓷纳米晶嵌入玻璃基体。晶体的连锁结构有助于让裂缝偏转。");
            phraseList.add("45263573475");
            phraseList.add("随后，康宁再用离子交换技术强化玻璃，本质上就是扩大离子尺寸，让结构更牢固");
            RongConfigCenter.featureConfig()
                    .enableQuickReply(
                            new IQuickReplyProvider() {
                                @Override
                                public List<String> getPhraseList(
                                        Conversation.ConversationType type) {
                                    return phraseList;
                                }
                            });
        }
    }

    /** 注册消息及消息模版 */
    public void initMessageAndTemplate() {
        SLog.d("ss_register_message", "initMessageAndTemplate");
        ArrayList<Class<? extends MessageContent>> myMessages = new ArrayList<>();
        myMessages.add(SealGroupNotificationMessage.class);
        myMessages.add(SealUltraGroupNotificationMessage.class);
        myMessages.add(SealContactNotificationMessage.class);
        myMessages.add(SealGroupConNtfMessage.class);
        myMessages.add(GroupApplyMessage.class);
        myMessages.add(GroupClearMessage.class);
        myMessages.add(CombineV2Message.class);
        myMessages.add(PokeMessage.class);
        RongIMClient.registerMessageType(myMessages);
        RongConfigCenter.conversationConfig()
                .addMessageProvider(new ContactNotificationMessageProvider());
        RongConfigCenter.conversationConfig().addMessageProvider(new PokeMessageItemProvider());
        RongConfigCenter.conversationConfig()
                .addMessageProvider(new CombineV2MessageItemProvider());
        RongConfigCenter.conversationConfig()
                .addMessageProvider(new SealGroupConNtfMessageProvider());
        RongConfigCenter.conversationConfig()
                .addMessageProvider(new GroupApplyMessageItemProvider());
        RongConfigCenter.conversationConfig().addMessageProvider(new StreamMessageItemProvider());
        RongConfigCenter.conversationConfig()
                .replaceMessageProvider(
                        GroupNotificationMessageItemProvider.class,
                        new SealGroupNotificationMessageItemProvider());
    }

    /**
     * 初始化扩展模块
     *
     * @param context
     */
    public void initExtensionModules(Context context) {
        RongExtensionManager.getInstance().setExtensionConfig(new SealExtensionConfig());
        // 语音输入
        RongExtensionManager.getInstance().registerExtensionModule(new RecognizeExtensionModule());

        // 个人名片
        RongExtensionManager.getInstance()
                .registerExtensionModule(createContactCardExtensionModule());

        // 小视频 为了调整位置将此注册放入 SealExtensionModule 中进行，无此需求可直接在此注册
        RongExtensionManager.getInstance().registerExtensionModule(new SightExtensionModule());
        // 戳一下
        RongExtensionManager.getInstance().registerExtensionModule(new PokeExtensionModule());
        if (appTask.isDebugMode()) {
            // 图文消息
            RongExtensionManager.getInstance().registerExtensionModule(new ImageTextModule());
            // @所有人
            RongExtensionManager.getInstance().registerExtensionModule(new MentionAllModule());
            // 插入一条消息
            RongExtensionManager.getInstance().registerExtensionModule(new InsertMessageModule());
            // 发送一条KV消息
            RongExtensionManager.getInstance().registerExtensionModule(new SendKVMessageModule());
        }
    }

    /**
     * 调用初始化 RongIM
     *
     * @param application
     */
    private void initRongIM(Application application) {
        /*
         * 如果是连接到私有云需要在此配置服务器地址
         * 如果是公有云则不需要调用此方法
         */
        //        RongIMClient.getInstance()
        //                .setServerInfo(BuildConfig.SEALTALK_NAVI_SERVER,
        // BuildConfig.SEALTALK_FILE_SERVER);

        ProxyModel proxy = appTask.getProxy();
        if (proxy != null) {
            AppProxyManager.getInstance()
                    .setProxy(
                            new RCIMProxy(
                                    proxy.getProxyHost(),
                                    proxy.getPort(),
                                    proxy.getUserName(),
                                    proxy.getPassword()));
        }

        /*
         * 初始化 SDK，在整个应用程序全局，只需要调用一次。建议在 Application 继承类中调用。
         */

        /* 若直接调用init方法请在 IMLib 模块中的 AndroidManifest.xml 中, 找到 <meta-data> 中 android:name 为 RONG_CLOUD_APP_KEY的标签，
         * 将 android:value 替换为融云 IM 申请的APP KEY
         */
        // RongIM.init(this);

        RongCoreClient.getInstance().setReconnectKickEnable(true);

        // 可在初始 SDK 时直接带入融云 IM 申请的APP KEY
        DataCenter currentDataCenter = appTask.getCurrentDataCenter();
        String appkey = currentDataCenter.getAppKey();

        InitOption initOption =
                new InitOption.Builder()
                        .setNaviServer(currentDataCenter.getNaviUrl())
                        .setFileServer(BuildConfig.SEALTALK_FILE_SERVER)
                        .enablePush(true)
                        .setAreaCode(currentDataCenter.getAreaCode())
                        .build();
        IMCenter.init(application, appkey, initOption);
        SealTalkUrl.DOMAIN = currentDataCenter.getAppServer();
    }

    /** 初始化信息提供者，包括用户信息，群组信息，群主成员信息 */
    private void initInfoProvider(Context context) {
        imInfoProvider = new IMInfoProvider();
        imInfoProvider.init(context);
    }

    /**
     * 创建个人名片模块
     *
     * @return
     */
    private ContactCardExtensionModule createContactCardExtensionModule() {
        return new ContactCardExtensionModule(
                new IContactCardInfoProvider() {
                    /**
                     * 获取所有通讯录用户
                     *
                     * @param contactInfoCallback
                     */
                    @Override
                    public void getContactAllInfoProvider(
                            IContactCardInfoCallback contactInfoCallback) {
                        imInfoProvider.getAllContactUserInfo(contactInfoCallback);
                    }

                    /**
                     * 获取单一用户
                     *
                     * @param userId
                     * @param name
                     * @param portrait
                     * @param contactInfoCallback
                     */
                    @Override
                    public void getContactAppointedInfoProvider(
                            String userId,
                            String name,
                            String portrait,
                            IContactCardInfoProvider.IContactCardInfoCallback contactInfoCallback) {
                        imInfoProvider.getContactUserInfo(userId, contactInfoCallback);
                    }
                },
                (view, content) -> {
                    Context activityContext = view.getContext();
                    // 点击名片进入到个人详细界面
                    Intent intent = new Intent(activityContext, UserDetailActivity.class);
                    intent.putExtra(IntentExtra.STR_TARGET_ID, content.getId());
                    activityContext.startActivity(intent);
                });
    }

    /** 初始化连接状态监听 */
    private void initConnectStateChangeListener() {
        IMCenter.getInstance()
                .addConnectionStatusListener(
                        new RongIMClient.ConnectionStatusListener() {
                            @Override
                            public void onChanged(ConnectionStatus connectionStatus) {
                                SLog.d(
                                        LogTag.IM,
                                        "ConnectionStatus onChanged = "
                                                + connectionStatus.getMessage());
                                if (connectionStatus.equals(
                                        ConnectionStatus.KICKED_OFFLINE_BY_OTHER_CLIENT)) {
                                    // 被其他提出时，需要返回登录界面
                                    kickedOffline.postValue(true);
                                } else if (connectionStatus == ConnectionStatus.TOKEN_INCORRECT) {
                                    // TODO token 错误时，重新登录
                                } else if (connectionStatus.equals(ConnectionStatus.USER_LOGOUT)) {
                                    userIsAbandon.postValue(true);
                                } else if (connectionStatus.equals(
                                        ConnectionStatus.CONN_USER_BLOCKED)) {
                                    userIsBlocked.postValue(true);
                                }
                            }
                        });
    }

    private void initDebug() {
        if (appTask.isDebugMode()) {
            RongConfigCenter.featureConfig()
                    .enableQuickReply(
                            type -> {
                                List<String> phraseList = new ArrayList<>();
                                phraseList.add(
                                        "1为了研究编译器的实现原理，我们需要使用 clang 命令。clang 命令可以将 Objetive-C 的源码改写成 C / C++ 语言的，借此可以研究 block 中各个特性的源码实现方式。该命令是为了研究编译器的实现原理，我们需要使用 clang ");
                                phraseList.add(
                                        "clang 命令可以将 Objetive-C 的源码改写成 C / C++ 语言的，借此可以研究 block 中各个特性的源码实现方式。该命令是\",@\"2为了研究编译器的实现原理，我们需要使用 clang 命令");
                                phraseList.add(
                                        "clang 命令可以将 Objetive-C 的源码改写成 C / C++ 语言的，借此可以研究 block 中各个特性的源码实现方式\", @\"33333333\", @\"4超瓷晶的最大优势就是缺点很少但硬度很高\", @\"5苹果iPhone 12采用了一种新的保护玻璃涂层，名叫超瓷晶（Ceramic Shield）。官方称新机的防摔能力增强4倍，光学性能更加出色，更加防刮。考虑到之前iPhone的玻璃保护做得并不算好，如果这次真如苹果所说那么好，进步还是很明显的");
                                phraseList.add(
                                        "6超瓷晶技术由康宁开发，利用超高温结晶制造工艺，他们将微小陶瓷纳米晶嵌入玻璃基体。晶体的连锁结构有助于让裂缝偏转。");
                                phraseList.add("45263573475");
                                phraseList.add("随后，康宁再用离子交换技术强化玻璃，本质上就是扩大离子尺寸，让结构更牢固");
                                return phraseList;
                            });
        }
    }

    /** 初始化消息监听 */
    private void initOnReceiveMessage(Context context) {
        IMCenter.getInstance()
                .addAsyncOnReceiveMessageListener(
                        new RongIMClient.OnReceiveMessageWrapperListener() {
                            @Override
                            public boolean onReceived(
                                    Message message, int i, boolean hasPackage, boolean isOffline) {
                                messageRouter.postValue(message);
                                MessageContent messageContent = message.getContent();
                                String targetId = message.getTargetId();
                                if (messageContent
                                        instanceof ContactNotificationMessage) { // 添加好友状态信息
                                    ContactNotificationMessage contactNotificationMessage =
                                            (ContactNotificationMessage) messageContent;
                                    if (contactNotificationMessage
                                            .getOperation()
                                            .equals("Request")) {

                                    } else if (contactNotificationMessage
                                            .getOperation()
                                            .equals("AcceptResponse")) {
                                        // 根据好友 id 进行获取好友信息并刷新
                                        String sourceUserId =
                                                contactNotificationMessage.getSourceUserId();
                                        imInfoProvider.updateFriendInfo(sourceUserId);
                                    }
                                    return true;
                                } else if (messageContent
                                        instanceof GroupNotificationMessage) { // 群组通知消息
                                    GroupNotificationMessage groupNotificationMessage =
                                            (GroupNotificationMessage) messageContent;
                                    SLog.d(
                                            LogTag.IM,
                                            "onReceived GroupNotificationMessage:"
                                                    + groupNotificationMessage.getMessage());
                                    String groupID = targetId;
                                    GroupNotificationMessageData data = null;
                                    try {
                                        String currentID =
                                                RongIMClient.getInstance().getCurrentUserId();
                                        try {
                                            Gson gson = new Gson();
                                            data =
                                                    gson.fromJson(
                                                            groupNotificationMessage.getData(),
                                                            GroupNotificationMessageData.class);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        if (groupNotificationMessage
                                                .getOperation()
                                                .equals("Create")) {
                                            // 创建群组,获取群组信息
                                            imInfoProvider.updateGroupInfo(groupID);
                                            imInfoProvider.updateGroupMember(groupID);
                                        } else if (groupNotificationMessage
                                                .getOperation()
                                                .equals("Dismiss")) {
                                            // 被踢出群组，删除群组会话和消息
                                            clearConversationAndMessage(
                                                    groupID, Conversation.ConversationType.GROUP);
                                            imInfoProvider.deleteGroupInfoInDb(groupID);
                                        } else if (groupNotificationMessage
                                                .getOperation()
                                                .equals("Kicked")) {
                                            // 群组踢人
                                            boolean isKicked = false;
                                            if (data != null) {
                                                List<String> memberIdList = data.getTargetUserIds();
                                                if (memberIdList != null) {
                                                    for (String userId : memberIdList) {
                                                        if (currentID.equals(userId)) {
                                                            // 被踢出群组，删除群组会话和消息
                                                            clearConversationAndMessage(
                                                                    groupID,
                                                                    Conversation.ConversationType
                                                                            .GROUP);
                                                            imInfoProvider.deleteGroupInfoInDb(
                                                                    groupID);
                                                            isKicked = true;
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                            // 如果未被提出，则更新群组信息和群成员
                                            if (!isKicked) {
                                                imInfoProvider.updateGroupInfo(groupID);
                                                imInfoProvider.updateGroupMember(groupID);
                                            }
                                        } else if (groupNotificationMessage
                                                .getOperation()
                                                .equals("Add")) {
                                            // 群组添加人员
                                            imInfoProvider.updateGroupInfo(groupID);
                                            imInfoProvider.updateGroupMember(groupID);
                                        } else if (groupNotificationMessage
                                                .getOperation()
                                                .equals("Quit")) {
                                            // 刷新退群列表
                                            imInfoProvider.refreshGroupExitedInfo(groupID);
                                            // 退出群组，当非自己退出室刷新群组信息
                                            if (!currentID.equals(
                                                    groupNotificationMessage.getOperatorUserId())) {
                                                imInfoProvider.updateGroupInfo(groupID);
                                                imInfoProvider.updateGroupMember(groupID);
                                            }
                                        } else if (groupNotificationMessage
                                                .getOperation()
                                                .equals("Rename")) {
                                            // 群组重命名,更新群信息
                                            if (data != null) {
                                                // 更新数据库中群组名称
                                                String targetGroupName = data.getTargetGroupName();
                                                imInfoProvider.updateGroupNameInDb(
                                                        groupID, targetGroupName);
                                            }
                                        } else if (groupNotificationMessage
                                                .getOperation()
                                                .equals("Transfer")) {
                                            // 转移群主，获取群组信息
                                            imInfoProvider.updateGroupInfo(groupID);
                                            imInfoProvider.updateGroupMember(groupID);
                                        } else if (groupNotificationMessage
                                                .getOperation()
                                                .equals("SetManager")) {
                                            // 设置管理员，获取群组信息
                                            imInfoProvider.updateGroupInfo(groupID);
                                            imInfoProvider.updateGroupMember(groupID);
                                        } else if (groupNotificationMessage
                                                .getOperation()
                                                .equals("RemoveManager")) {
                                            // 移除管理员，获取群组信息
                                            imInfoProvider.updateGroupInfo(groupID);
                                            imInfoProvider.updateGroupMember(groupID);
                                        }
                                    } catch (Exception e) {
                                        SLog.d(
                                                LogTag.IM,
                                                "onReceived process GroupNotificationMessage catch exception:"
                                                        + e.getMessage());
                                        e.printStackTrace();
                                    }
                                    return true;
                                } else if (messageContent instanceof GroupApplyMessage) {
                                    imInfoProvider.refreshGroupNotideInfo();
                                    return true;
                                } else if (messageContent instanceof PokeMessage) {
                                    PokeMessage pokeMessage = (PokeMessage) messageContent;
                                    if (getReceivePokeMessageStatus()) {
                                        // 显示戳一下界面
                                        // 判断当前是否在目标的会话界面中
                                        boolean isInConversation = false;
                                        ConversationRecord lastConversationRecord =
                                                IMManager.getInstance().getLastConversationRecord();
                                        if (lastConversationRecord != null
                                                && targetId.equals(
                                                        lastConversationRecord.targetId)) {
                                            isInConversation = true;
                                        }
                                        // 当戳一下的目标不在会话界面且在前台时显示戳一下界面
                                        if (!isInConversation) {
                                            Intent showPokeIntent =
                                                    new Intent(
                                                            context, PokeInviteChatActivity.class);
                                            showPokeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            showPokeIntent.addFlags(
                                                    Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                            showPokeIntent.putExtra(
                                                    IntentExtra.START_FROM_ID,
                                                    message.getSenderUserId());
                                            showPokeIntent.putExtra(
                                                    IntentExtra.STR_POKE_MESSAGE,
                                                    pokeMessage.getContent());
                                            if (message.getConversationType()
                                                    == Conversation.ConversationType.GROUP) {
                                                Group groupInfo =
                                                        RongUserInfoManager.getInstance()
                                                                .getGroupInfo(targetId);
                                                if (groupInfo != null) {
                                                    showPokeIntent.putExtra(
                                                            IntentExtra.STR_GROUP_NAME,
                                                            groupInfo.getName());
                                                }
                                            }
                                            showPokeIntent.putExtra(
                                                    IntentExtra.SERIA_CONVERSATION_TYPE,
                                                    message.getConversationType());
                                            showPokeIntent.putExtra(
                                                    IntentExtra.STR_TARGET_ID, targetId);
                                            /*
                                             * 判断是否在在前台，如果不在前台则下次进入 app 时进行弹出
                                             * 再判断是否已进入到了主界面，反正拉取离线消息时再未进入主界面前弹出戳一下界面
                                             */
                                            if (SealApp.getApplication().isAppInForeground()
                                                    && SealApp.getApplication()
                                                            .isMainActivityCreated()) {
                                                context.startActivity(showPokeIntent);
                                            } else {
                                                // 若之前有未启动的戳一下消息则默认启动第一个戳一下消息
                                                Intent lastIntent =
                                                        SealApp.getApplication()
                                                                .getLastOnAppForegroundStartIntent();
                                                if (lastIntent == null
                                                        || (lastIntent.getComponent() != null
                                                                && !lastIntent
                                                                        .getComponent()
                                                                        .getClassName()
                                                                        .equals(
                                                                                PokeInviteChatActivity
                                                                                        .class
                                                                                        .getName()))) {
                                                    SealApp.getApplication()
                                                            .setOnAppForegroundStartIntent(
                                                                    showPokeIntent);
                                                }
                                            }
                                        }
                                        return true;
                                    } else {
                                        // 如果不接受戳一下消息则什么也不做
                                        return true;
                                    }
                                } else if (messageContent instanceof GroupClearMessage) {
                                    GroupClearMessage groupClearMessage =
                                            (GroupClearMessage) messageContent;
                                    SLog.i(
                                            "GroupClearMessage",
                                            groupClearMessage.toString()
                                                    + "***"
                                                    + message.getTargetId());
                                    if (groupClearMessage.getClearTime() > 0) {
                                        IMCenter.getInstance()
                                                .cleanHistoryMessages(
                                                        ConversationIdentifier.obtain(message),
                                                        groupClearMessage.getClearTime(),
                                                        true,
                                                        null);
                                    }
                                    return true;
                                }
                                return true;
                            }
                        });
    }

    /**
     * 设置通知消息免打扰
     *
     * @param startTime
     * @param spanMinutes
     * @return
     */
    public MutableLiveData<Resource<QuietHours>> setNotificationQuietHours(
            final String startTime, final int spanMinutes, boolean isCache) {
        MutableLiveData<Resource<QuietHours>> result = new MutableLiveData<>();
        RongIMClient.getInstance()
                .setNotificationQuietHours(
                        startTime,
                        spanMinutes,
                        new RongIMClient.OperationCallback() {
                            @Override
                            public void onSuccess() {
                                RongNotificationManager.getInstance()
                                        .setNotificationQuietHours(startTime, spanMinutes, null);
                                // 设置用户消息免打扰缓存状态
                                if (isCache) {
                                    configCache.setNotifiDonotDistrabStatus(getCurrentId(), true);
                                    configCache.setNotifiQuietHours(
                                            getCurrentId(), startTime, spanMinutes);
                                    result.setValue(
                                            Resource.success(
                                                    configCache.getNotifiQUietHours(
                                                            getCurrentId())));
                                }
                            }

                            @Override
                            public void onError(RongIMClient.ErrorCode errorCode) {
                                result.postValue(
                                        Resource.error(ErrorCode.IM_ERROR.getCode(), null));
                            }
                        });
        return result;
    }

    /**
     * 获取当前用户 id
     *
     * @return
     */
    public String getCurrentId() {
        return RongIMClient.getInstance().getCurrentUserId();
    }

    /**
     * 获取通知时间
     *
     * @return
     */
    public MutableLiveData<Resource<QuietHours>> getNotificationQuietHours() {
        MutableLiveData<Resource<QuietHours>> result = new MutableLiveData<>();
        RongNotificationManager.getInstance()
                .getNotificationQuietHours(
                        new RongIMClient.GetNotificationQuietHoursCallback() {
                            @Override
                            public void onSuccess(String s, int i) {
                                QuietHours quietHours = new QuietHours();
                                quietHours.startTime = s;
                                quietHours.spanMinutes = i;
                                result.postValue(Resource.success(quietHours));
                            }

                            @Override
                            public void onError(RongIMClient.ErrorCode errorCode) {
                                result.postValue(
                                        Resource.error(ErrorCode.IM_ERROR.getCode(), null));
                            }
                        });
        return result;
    }

    /**
     * 获取消息通知设置
     *
     * @return
     */
    public boolean getRemindStatus() {
        return configCache.getNewMessageRemind(getCurrentId());
    }

    /**
     * 新消息通知设置
     *
     * @param status
     */
    public void setRemindStatus(boolean status) {
        if (status) {
            boolean donotDistrabStatus = configCache.getNotifiDonotDistrabStatus(getCurrentId());
            if (!donotDistrabStatus) {
                removeNotificationQuietHours();
            }
        } else {
            RongNotificationManager.getInstance().setNotificationQuietHours("00:00:00", 1439, null);
        }
        configCache.setNewMessageRemind(getCurrentId(), status);
    }

    /**
     * 清理通知免打扰
     *
     * @return
     */
    public MutableLiveData<Resource<Boolean>> removeNotificationQuietHours() {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        RongNotificationManager.getInstance()
                .removeNotificationQuietHours(
                        new RongIMClient.OperationCallback() {
                            @Override
                            public void onSuccess() {
                                // 设置用户消息免打扰缓存状态
                                configCache.setNotifiDonotDistrabStatus(getCurrentId(), false);
                                result.postValue(Resource.success(true));
                            }

                            @Override
                            public void onError(RongIMClient.ErrorCode errorCode) {
                                result.postValue(
                                        Resource.error(ErrorCode.IM_ERROR.getCode(), false));
                            }
                        });

        return result;
    }

    /**
     * 获取通知设置消息
     *
     * @return
     */
    public QuietHours getNotifiQuietHours() {
        return configCache.getNotifiQUietHours(getCurrentId());
    }

    /** 初始化聊天室监听 */
    public void initChatRoomActionListener() {
        RongChatRoomClient.setChatRoomAdvancedActionListener(
                new RongChatRoomClient.ChatRoomAdvancedActionListener() {
                    @Override
                    public void onJoining(String roomId) {
                        chatRoomActionLiveData.postValue(ChatRoomAction.joining(roomId));
                    }

                    @Override
                    public void onJoined(String roomId) {
                        chatRoomActionLiveData.postValue(ChatRoomAction.joined(roomId));
                    }

                    @Override
                    public void onReset(String roomId) {
                        chatRoomActionLiveData.postValue(ChatRoomAction.reset(roomId));
                    }

                    @Override
                    public void onQuited(String roomId) {
                        chatRoomActionLiveData.postValue(ChatRoomAction.quited(roomId));
                    }

                    @Override
                    public void onDestroyed(
                            String chatRoomId, IRongCoreEnum.ChatRoomDestroyType type) {
                        chatRoomActionLiveData.postValue(ChatRoomAction.destroyed(chatRoomId));
                    }

                    @Override
                    public void onError(String chatRoomId, IRongCoreEnum.CoreErrorCode code) {
                        chatRoomActionLiveData.postValue(ChatRoomAction.error(chatRoomId, code));
                    }
                });
    }

    /**
     * 获取聊天室行为状态
     *
     * @return
     */
    public LiveData<ChatRoomAction> getChatRoomAction() {
        return chatRoomActionLiveData;
    }

    /** 监听未读消息状态 */
    public void addUnReadMessageCountChangedObserver(
            UnReadMessageManager.IUnReadMessageObserver observer,
            Conversation.ConversationType[] conversationTypes) {
        UnReadMessageManager.getInstance().addForeverObserver(conversationTypes, observer);
    }

    /**
     * 移除未读消息监听
     *
     * @param observer
     */
    public void removeUnReadMessageCountChangedObserver(
            UnReadMessageManager.IUnReadMessageObserver observer) {
        UnReadMessageManager.getInstance().removeForeverObserver(observer);
    }

    /**
     * 清理未读消息状态
     *
     * @param conversationTypes 指定清理的会话类型
     */
    public void clearMessageUnreadStatus(Conversation.ConversationType[] conversationTypes) {
        RongIMClient.getInstance()
                .getConversationList(
                        new RongIMClient.ResultCallback<List<Conversation>>() {
                            @Override
                            public void onSuccess(List<Conversation> conversations) {
                                if (conversations != null && conversations.size() > 0) {
                                    for (Conversation c : conversations) {
                                        if (c.getUnreadMessageCount() <= 0) {
                                            continue;
                                        }
                                        IMCenter.getInstance()
                                                .clearMessagesUnreadStatus(
                                                        ConversationIdentifier.obtain(c), null);
                                        if (c.getConversationType()
                                                        == Conversation.ConversationType.PRIVATE
                                                || c.getConversationType()
                                                        == Conversation.ConversationType
                                                                .ENCRYPTED) {
                                            RongIMClient.getInstance()
                                                    .sendReadReceiptMessage(
                                                            c.getConversationType(),
                                                            c.getTargetId(),
                                                            c.getSentTime(),
                                                            new IRongCallback
                                                                    .ISendMessageCallback() {

                                                                @Override
                                                                public void onAttached(
                                                                        Message message) {}

                                                                @Override
                                                                public void onSuccess(
                                                                        Message message) {}

                                                                @Override
                                                                public void onError(
                                                                        Message message,
                                                                        RongIMClient.ErrorCode
                                                                                errorCode) {}
                                                            });
                                        }
                                        RongIMClient.getInstance()
                                                .syncConversationReadStatus(
                                                        c.getConversationType(),
                                                        c.getTargetId(),
                                                        c.getSentTime(),
                                                        null);
                                    }
                                }
                            }

                            @Override
                            public void onError(RongIMClient.ErrorCode e) {}
                        },
                        conversationTypes);
    }

    private String getSavedReadReceiptTimeName(
            String targetId, Conversation.ConversationType conversationType) {
        String savedId =
                DeviceUtils.ShortMD5(
                        RongIMClient.getInstance().getCurrentUserId(),
                        targetId,
                        conversationType.getName());
        return "ReadReceipt" + savedId + "Time";
    }

    /**
     * 自动重连结果
     *
     * @return
     */
    public LiveData<Boolean> getAutoLoginResult() {
        return autologinResult;
    }

    public MutableLiveData<Message> getMessageRouter() {
        return messageRouter;
    }

    /** 退出 */
    public void logout() {
        IMCenter.getInstance().logout();
    }

    /** 退出并清除缓存,等同于手动logout */
    public void logoutAndClear() {
        IMCenter.getInstance().logout();
        UserTask userTask = new UserTask(context);
        userTask.logout();
    }

    /**
     * 是否登录状态
     *
     * @return
     */
    public boolean userIsLoginState() {
        UserCache userCache = new UserCache(context);
        return userCache.getUserCache() != null;
    }

    /**
     * 连接 IM 服务
     *
     * @param token
     * @param autoConnect 是否在连接失败时无限时间自动重连
     * @param cacheConnect 是否为缓存登录而做的IM连接 （true：只连接IM；false：主动操作，会在业务登录成功后再连接im）
     * @param callback
     */
    public void connectIM(
            String token,
            boolean autoConnect,
            boolean cacheConnect,
            ResultCallback<String> callback) {
        if (autoConnect) {
            connectIM(token, cacheConnect, 0, callback);
        } else {
            connectIM(token, cacheConnect, NetConstant.API_IM_CONNECT_TIME_OUT, callback);
        }
    }

    /**
     * 连接 IM 服务
     *
     * @param token
     * @param cacheConnect 是否为缓存登录而做的IM连接 （true：只连接IM；false：主动操作，会在业务登录成功后再连接im）
     * @param timeOut 自动重连超时时间。
     * @param callback
     */
    public void connectIM(
            String token, boolean cacheConnect, int timeOut, ResultCallback<String> callback) {
        /*
         *  考虑到会有后台调用此方法，所以不采用 LiveData 做返回值
         */
        IMCenter.getInstance()
                .connect(
                        token,
                        timeOut,
                        new RongIMClient.ConnectCallback() {
                            @Override
                            public void onSuccess(String s) {
                                // 连接 IM 成功后，初始化数据库
                                DBManager.getInstance(context).openDb(s);
                                updateJwtToken();
                                // 如果是非缓存连接，既主动登录触发的连接，onSuccess才抛给上层
                                if (!cacheConnect) {
                                    if (callback != null) {
                                        callback.onSuccess(s);
                                    }
                                }
                            }

                            public void onError(RongIMClient.ConnectionErrorCode errorCode) {
                                SLog.e(LogTag.IM, "connect error - code:" + errorCode.getValue());
                                RLog.e(TAG, "connectIM errorCode:" + errorCode);
                                if (errorCode
                                        == RongIMClient.ConnectionErrorCode
                                                .RC_CONN_TOKEN_INCORRECT) {
                                    if (callback != null) {
                                        callback.onFail(ErrorCode.IM_ERROR.getCode());
                                    }
                                } else if (errorCode
                                        == RongIMClient.ConnectionErrorCode.RC_CONN_USER_ABANDON) {
                                    callback.onFail(ErrorCode.USER_ABANDON.getCode());
                                } else if (errorCode
                                        == RongIMClient.ConnectionErrorCode.RC_CONN_USER_BLOCKED) {
                                    callback.onFail(ErrorCode.USER_BLOCKED.getCode());
                                } else {
                                    if (callback != null) {
                                        callback.onFail(ErrorCode.IM_ERROR.getCode());
                                    } else {
                                        // do nothing
                                    }
                                }
                            }

                            @Override
                            public void onDatabaseOpened(
                                    RongIMClient.DatabaseOpenStatus databaseOpenStatus) {
                                String currentUserId =
                                        RongIMClient.getInstance().getCurrentUserId();
                                DBManager.getInstance(context).openDb(currentUserId);
                                // 如果是缓存连接，在onDatabaseOpened抛给上层
                                if (cacheConnect) {
                                    if (callback != null) {
                                        callback.onSuccess(currentUserId);
                                    }
                                }
                            }
                        });
    }

    private void updateJwtToken() {
        RLog.d(TAG, "updateJwtToken: ");
        if (!RongCoreClient.getInstance().isTextTranslationSupported()) {
            RLog.d(TAG, "updateJwtToken: not support translation");
            return;
        }
        UserService service =
                RetrofitProxyServiceCreator.getRetrofitService(context, UserService.class);
        service.getTranslationToken(RongIMClient.getInstance().getCurrentUserId())
                .observeForever(
                        new Observer<TranslationTokenResult>() {
                            @Override
                            public void onChanged(TranslationTokenResult translationTokenResult) {
                                if (translationTokenResult == null) {
                                    RLog.d(TAG, "updateJwtToken: translationTokenResult is null");
                                    return;
                                }
                                RLog.d(
                                        TAG,
                                        "updateJwtToken: jwtToken = "
                                                + translationTokenResult.getToken());
                                TranslationClient.getInstance()
                                        .updateAuthToken(translationTokenResult.getToken());
                            }
                        });
        RongConfigCenter.featureConfig().rc_translation_src_language =
                appTask.getTranslationSrcLanguage();
        RongConfigCenter.featureConfig().rc_translation_target_language =
                appTask.getTranslationTargetLanguage();
    }

    /**
     * 获取用户 IM token 此接口需要在登录成功后可调用，用于在 IM 提示 token 失效时刷新 token 使用
     *
     * @param callback
     */
    private void getToken(ResultCallback<LoginResult> callback) {
        UserService userService =
                RetrofitProxyServiceCreator.getRetrofitService(context, UserService.class);
        /*
         *  考虑到会有后台调用此方法，所以不采用 LiveData 做返回值
         */
        userService.getToken().enqueue(new CallBackWrapper<>(callback));
    }

    /**
     * 获取连接状态
     *
     * @return
     */
    public RongIMClient.ConnectionStatusListener.ConnectionStatus getConnectStatus() {
        return RongIMClient.getInstance().getCurrentConnectionStatus();
    }

    /**
     * 被踢监听, true 为当前为被提出状态， false 为不需要处理踢出状态
     *
     * @return
     */
    public LiveData<Boolean> getKickedOffline() {
        return kickedOffline;
    }

    /**
     * 被踢销户, true 为当前为被销户状态， false 为不需要处理
     *
     * @return
     */
    public LiveData<Boolean> getUserIsAbandon() {
        return userIsAbandon;
    }

    /**
     * 封禁状态, true 为当前为被封禁状态， false 为不需要处理
     *
     * @return
     */
    public LiveData<Boolean> getUserIsBlocked() {
        return userIsBlocked;
    }

    /** 重置被提出状态为 false */
    public void resetKickedOfflineState() {
        resetState(kickedOffline);
    }

    /** 重置销户状态 */
    public void resetUserLogoutState() {
        resetState(userIsAbandon);
    }

    /** 重置封禁状态 */
    public void resetUserBlockedState() {
        resetState(userIsBlocked);
    }

    /** 登录后重置状态 */
    public void resetAfterLogin() {
        resetUserLogoutState();
        resetUserBlockedState();
        saveLoginDataCenter();
    }

    private void saveLoginDataCenter() {
        appTask.saveDataCenter(appTask.getCurrentDataCenter());
    }

    public void resetState(MutableLiveData<Boolean> state) {
        if (Looper.getMainLooper().getThread().equals(Thread.currentThread())) {
            state.setValue(false);
        } else {
            state.postValue(false);
        }
    }

    /**
     * 发送图片消息
     *
     * @param conversationType
     * @param targetId
     * @param imageList
     * @param origin
     */
    public void sendImageMessage(
            Conversation.ConversationType conversationType,
            String targetId,
            List<Uri> imageList,
            boolean origin) {
        // Todo
        //        SendImageManager.getInstance().sendImages(conversationType, targetId, imageList,
        // origin);
    }

    /**
     * 获取是否接受戳一下消息
     *
     * @return
     */
    public boolean getReceivePokeMessageStatus() {
        if (isReceivePokeMessage == null) {
            // 第一次获取时
            boolean receivePokeMessageStatus =
                    configCache.getReceivePokeMessageStatus(getCurrentId());
            imInfoProvider.refreshReceivePokeMessageStatus();
            isReceivePokeMessage = receivePokeMessageStatus;
        }

        return isReceivePokeMessage;
    }

    /**
     * 更新是否接受戳一下消息状态
     *
     * @param isReceive
     */
    public void updateReceivePokeMessageStatus(boolean isReceive) {
        if (isReceivePokeMessage == null || isReceivePokeMessage != isReceive) {
            isReceivePokeMessage = isReceive;
            configCache.setReceivePokeMessageStatus(getCurrentId(), isReceive);
        }
    }

    /** 发送戳一下消息给单人 */
    public void sendPokeMessageToPrivate(
            String targetId, String content, IRongCallback.ISendMessageCallback callback) {
        PokeMessage pokeMessage = PokeMessage.obtain(content);
        Message message =
                Message.obtain(targetId, Conversation.ConversationType.PRIVATE, pokeMessage);
        IMCenter.getInstance().sendMessage(message, null, null, callback);
    }

    /**
     * 发送戳一下消息给群组
     *
     * @param targetId
     * @param content
     * @param userIds 发送目标用户 id，当为 null 时发给群内所有人
     * @param callback
     */
    public void sendPokeMessageToGroup(
            String targetId,
            String content,
            String[] userIds,
            IRongCallback.ISendMessageCallback callback) {
        PokeMessage pokeMessage = PokeMessage.obtain(content);
        if (userIds != null && userIds.length > 0) {
            IMCenter.getInstance()
                    .sendDirectionalMessage(
                            Conversation.ConversationType.GROUP,
                            targetId,
                            pokeMessage,
                            userIds,
                            null,
                            null,
                            callback);
        } else {
            Message message =
                    Message.obtain(targetId, Conversation.ConversationType.GROUP, pokeMessage);
            IMCenter.getInstance().sendMessage(message, null, null, callback);
        }
    }

    /**
     * 记录最新的会话信息
     *
     * @param targetId
     * @param conversationType
     */
    public void setLastConversationRecord(
            String targetId, Conversation.ConversationType conversationType) {
        ConversationRecord record = new ConversationRecord();
        record.targetId = targetId;
        record.conversationType = conversationType;
        lastConversationRecord = record;
    }

    /** 清除最后的会话信息 */
    public void clearConversationRecord(String targetId) {
        if (lastConversationRecord != null && lastConversationRecord.targetId.equals(targetId)) {
            lastConversationRecord = null;
        }
    }

    /**
     * 获取最后的会话信息
     *
     * @return
     */
    public ConversationRecord getLastConversationRecord() {
        return lastConversationRecord;
    }

    /**
     * 设置推送消息通知是否显示信息
     *
     * @param isDetail 是否显示通知详情
     */
    public LiveData<Resource<Boolean>> setPushDetailContentStatus(boolean isDetail) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        RongIMClient.getInstance()
                .setPushContentShowStatus(
                        isDetail,
                        new RongIMClient.OperationCallback() {
                            @Override
                            public void onSuccess() {
                                result.postValue(Resource.success(isDetail));
                            }

                            @Override
                            public void onError(RongIMClient.ErrorCode errorCode) {
                                int errCode = ErrorCode.IM_ERROR.getCode();
                                if (errorCode == RongIMClient.ErrorCode.RC_NET_CHANNEL_INVALID
                                        || errorCode == RongIMClient.ErrorCode.RC_NET_UNAVAILABLE
                                        || errorCode
                                                == RongIMClient.ErrorCode
                                                        .RC_NETWORK_IS_DOWN_OR_UNREACHABLE) {
                                    errCode = ErrorCode.NETWORK_ERROR.getCode();
                                }
                                result.postValue(Resource.error(errCode, !isDetail));
                            }
                        });

        return result;
    }

    /**
     * 获取推送消息通知详情状态
     *
     * @return Resource 结果为 success 时，data 值为当前是否为显示消息通知详情。
     */
    public LiveData<Resource<Boolean>> getPushDetailContentStatus() {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        RongIMClient.getInstance()
                .getPushContentShowStatus(
                        new RongIMClient.ResultCallback<Boolean>() {
                            @Override
                            public void onSuccess(Boolean isDetail) {
                                result.postValue(Resource.success(isDetail));
                            }

                            @Override
                            public void onError(RongIMClient.ErrorCode errorCode) {
                                int errCode = ErrorCode.IM_ERROR.getCode();
                                if (errorCode == RongIMClient.ErrorCode.RC_NET_CHANNEL_INVALID
                                        || errorCode == RongIMClient.ErrorCode.RC_NET_UNAVAILABLE
                                        || errorCode
                                                == RongIMClient.ErrorCode
                                                        .RC_NETWORK_IS_DOWN_OR_UNREACHABLE) {
                                    errCode = ErrorCode.NETWORK_ERROR.getCode();
                                }
                                result.postValue(Resource.error(errCode, false));
                            }
                        });

        return result;
    }

    public AndroidConfig.ImportanceHW getImportance(String hwImportance) {
        if (!TextUtils.isEmpty(hwImportance) && TextUtils.equals(hwImportance, "LOW")) {
            return AndroidConfig.ImportanceHW.LOW;
        }
        return AndroidConfig.ImportanceHW.NORMAL;
    }

    public AppTask getAppTask() {
        return appTask;
    }

    private void sendUltraGroupTypingStatus(Message message) {
        if (appTask.isDebugMode()) {
            // 超级群测试代码
            if (Conversation.ConversationType.ULTRA_GROUP.equals(message.getConversationType())) {
                ChannelClient.getInstance()
                        .sendUltraGroupTypingStatus(
                                message.getTargetId(),
                                message.getChannelId(),
                                IRongCoreEnum.UltraGroupTypingStatus.ULTRA_GROUP_TYPING_STATUS_TEXT,
                                new IRongCoreCallback.OperationCallback() {
                                    @Override
                                    public void onSuccess() {
                                        new Handler(Looper.getMainLooper())
                                                .post(
                                                        () -> {
                                                            // 此时已经回到主线程
                                                            Toast.makeText(
                                                                            getContext(),
                                                                            "发送超级群 Typing 状态成功",
                                                                            Toast.LENGTH_LONG)
                                                                    .show();
                                                        });
                                    }

                                    @Override
                                    public void onError(IRongCoreEnum.CoreErrorCode coreErrorCode) {
                                        new Handler(Looper.getMainLooper())
                                                .post(
                                                        () -> {
                                                            // 此时已经回到主线程
                                                            Toast.makeText(
                                                                            getContext(),
                                                                            "发送超级群 Typing 状态错误"
                                                                                    + coreErrorCode
                                                                                            .getValue(),
                                                                            Toast.LENGTH_LONG)
                                                                    .show();
                                                        });
                                    }
                                });
            }
        }
    }
}
