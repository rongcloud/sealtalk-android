package cn.rongcloud.im.ui.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.activity.CombinePreviewActivity;
import cn.rongcloud.im.utils.MessageUtil;
import io.rong.common.FileUtils;
import io.rong.imkit.IMCenter;
import io.rong.imkit.conversation.MessageListAdapter;
import io.rong.imkit.conversation.messgelist.provider.MessageClickType;
import io.rong.imkit.event.actionevent.BaseMessageEvent;
import io.rong.imkit.event.actionevent.DownloadEvent;
import io.rong.imkit.event.actionevent.MessageEventListener;
import io.rong.imkit.feature.destruct.DestructManager;
import io.rong.imkit.manager.AudioPlayManager;
import io.rong.imkit.manager.IAudioPlayListener;
import io.rong.imkit.manager.hqvoicemessage.AutoDownloadEntry;
import io.rong.imkit.manager.hqvoicemessage.HQVoiceMsgDownloadManager;
import io.rong.imkit.model.State;
import io.rong.imkit.model.UiMessage;
import io.rong.imkit.userinfo.RongUserInfoManager;
import io.rong.imkit.utils.RongUtils;
import io.rong.imkit.utils.ToastUtils;
import io.rong.imkit.widget.FixedLinearLayoutManager;
import io.rong.imkit.widget.adapter.IViewProviderListener;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.CombineMsgInfo;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.message.CombineV2Message;
import io.rong.message.HQVoiceMessage;
import io.rong.message.MediaMessageContent;
import io.rong.message.VoiceMessage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 合并转发预览页面
 *
 * @author rongcloud
 * @since 1.0
 */
public class CombinePreviewFragment extends Fragment implements IViewProviderListener<UiMessage> {
    private static final String TAG = CombinePreviewFragment.class.getSimpleName();

    private Message mMessage;
    private ProgressBar mProgress;
    private final List<UiMessage> uiMessages = new ArrayList<>();
    private MessageListAdapter messageListAdapter;
    private RecyclerView recyclerView;

    private final MessageEventListener iMessageEventListener =
            new BaseMessageEvent() {

                @Override
                public void onDownloadMessage(DownloadEvent event) {
                    if (event.getEvent() == DownloadEvent.SUCCESS) {
                        Message message = event.getMessage();
                        for (UiMessage uiMessage : uiMessages) {
                            if (uiMessage.getMessage().getSentTime() == message.getSentTime()) {
                                uiMessage.setState(State.SUCCESS);
                            }
                        }
                        if (messageListAdapter != null) {
                            messageListAdapter.setDataCollection(uiMessages);
                        }
                    }
                }
            };

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View parent = inflater.inflate(R.layout.rc_combine_preview_fragment, null);
        parent.findViewById(R.id.btn_back)
                .setOnClickListener(
                        v -> {
                            if (getActivity() != null) {
                                getActivity().finish();
                            }
                        });
        mProgress = parent.findViewById(R.id.rc_web_progress);

        parent.findViewById(R.id.rc_web_progress).setVisibility(View.VISIBLE);

        IMCenter.getInstance().addMessageEventListener(iMessageEventListener);

        try {
            Bundle bundle = getArguments();
            mMessage = bundle.getParcelable(CombinePreviewActivity.KEY_MESSAGE);

            if (mMessage == null || !(mMessage.getContent() instanceof CombineV2Message)) {
                return parent;
            }

            CombineV2Message combineV2Message = (CombineV2Message) mMessage.getContent();
            boolean isNeedDownload = isCombineV2MessageNeedDownload(combineV2Message);

            if (isNeedDownload) {
                IMCenter.getInstance()
                        .downloadMediaMessage(
                                mMessage,
                                new IRongCallback.IDownloadMediaMessageCallback() {
                                    @Override
                                    public void onSuccess(Message message) {
                                        if (isFragmentAlive()) {
                                            initView(
                                                    parent,
                                                    (CombineV2Message) message.getContent());
                                        }
                                    }

                                    @Override
                                    public void onProgress(Message message, int progress) {
                                        if (isFragmentAlive() && mProgress != null) {
                                            mProgress.setVisibility(View.VISIBLE);
                                        }
                                    }

                                    @Override
                                    public void onError(
                                            Message message, RongIMClient.ErrorCode code) {
                                        Context context = IMCenter.getInstance().getContext();
                                        ToastUtils.show(
                                                context,
                                                context.getString(
                                                        io.rong
                                                                .imkit
                                                                .R
                                                                .string
                                                                .rc_ac_file_preview_download_error),
                                                Toast.LENGTH_SHORT);
                                    }

                                    @Override
                                    public void onCanceled(Message message) {}
                                });
            } else {
                initView(parent, combineV2Message);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return parent;
    }

    private void initView(View parent, CombineV2Message combineV2Message) {
        List<CombineMsgInfo> msgList = combineV2Message.getMsgList();
        for (CombineMsgInfo combineMsgInfo : msgList) {
            Message message =
                    Message.obtain(
                            combineMsgInfo.getTargetId(),
                            Conversation.ConversationType.GROUP,
                            combineMsgInfo.getContent());
            boolean isSender =
                    combineMsgInfo
                            .getFromUserId()
                            .equals(
                                    RongUserInfoManager.getInstance()
                                            .getCurrentUserInfo()
                                            .getUserId());
            message.setMessageDirection(
                    isSender ? Message.MessageDirection.SEND : Message.MessageDirection.RECEIVE);
            message.setSentStatus(Message.SentStatus.READ);
            message.setSentTime(combineMsgInfo.getTimestamp());
            message.setObjectName(combineMsgInfo.getObjectName());

            Message.ReceivedStatus receivedStatus = new Message.ReceivedStatus(0);
            receivedStatus.setRead();
            receivedStatus.setListened();
            message.setReceivedStatus(receivedStatus);
            message.setSenderUserId(combineMsgInfo.getFromUserId());

            // 将合并转发的消息UID,设置给预览页面构建的消息, 用于合并转发中媒体消息在预览页面 被撤回时的能找到对应的消息
            if (mMessage != null) {
                message.setUId(mMessage.getUId());
            }

            UiMessage uiMessage = new UiMessage(message);
            boolean isNeedDownload = false;
            if (combineMsgInfo.getContent() instanceof MediaMessageContent) {
                MediaMessageContent mediaMessageContent =
                        (MediaMessageContent) combineMsgInfo.getContent();
                isNeedDownload =
                        (mediaMessageContent.getLocalPath() == null
                                || !FileUtils.isFileExistsWithUri(
                                        getContext(), mediaMessageContent.getLocalPath()));
            }

            uiMessage.setState(isNeedDownload ? State.NORMAL : State.SUCCESS);
            uiMessages.add(uiMessage);

            if (isMediaNeedDownLoadFromCombineForward(uiMessage.getMessage())) {
                // 高清语言消息,自动下载
                if (TextUtils.equals(message.getObjectName(), "RC:HQVCMsg")) {
                    HQVoiceMsgDownloadManager.getInstance()
                            .enqueue(
                                    CombinePreviewFragment.this,
                                    new AutoDownloadEntry(
                                            message, AutoDownloadEntry.DownloadPriority.NORMAL));
                }
            }
        }

        recyclerView = parent.findViewById(io.rong.imkit.R.id.rc_message_list);
        LinearLayoutManager linearLayoutManager = new FixedLinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(null);
        messageListAdapter = new MessageListAdapter(this);
        recyclerView.setAdapter(messageListAdapter);
        messageListAdapter.setDataCollection(uiMessages);
        recyclerView.postDelayed(() -> recyclerView.scrollToPosition(0), 50);
        recyclerView.postDelayed(() -> recyclerView.scrollToPosition(0), 150);

        TextView tvTitle = parent.findViewById(R.id.tv_title);
        tvTitle.setText(MessageUtil.getTitle(getContext(), combineV2Message));

        if (mProgress != null) {
            mProgress.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        IMCenter.getInstance().removeMessageEventListener(iMessageEventListener);
    }

    private boolean isFragmentAlive() {
        boolean isDeactivated = !isAdded() || isRemoving() || isDetached() || getContext() == null;
        return !isDeactivated;
    }

    /**
     * @param clickType 区分点击事件的标记位
     * @param data 传递的数据源
     */
    @Override
    public void onViewClick(int clickType, UiMessage data) {
        if (clickType == MessageClickType.AUDIO_CLICK) {
            onAudioClick(data);
        }
    }

    /**
     * @param clickType 区分点击事件的标记位
     * @param data 传递的数据源
     */
    @Override
    public boolean onViewLongClick(int clickType, UiMessage data) {
        return false;
    }

    private void onAudioClick(UiMessage uiMessage) {
        // 处理暂停逻辑
        MessageContent messageContent = uiMessage.getMessage().getContent();
        Context context = IMCenter.getInstance().getContext();
        if (messageContent instanceof HQVoiceMessage) {
            if (AudioPlayManager.getInstance().isPlaying()) {
                Uri playingUri = AudioPlayManager.getInstance().getPlayingUri();
                AudioPlayManager.getInstance().stopPlay();
                // 暂停的是当前播放的 Uri
                if (playingUri.equals(((HQVoiceMessage) messageContent).getLocalPath())) {
                    return;
                }
            }
            // 如果被 voip 占用通道，则不播放，弹提示框
            if (AudioPlayManager.getInstance().isInVOIPMode(context)) {
                ToastUtils.show(
                        context,
                        context.getString(io.rong.imkit.R.string.rc_voip_occupying),
                        Toast.LENGTH_SHORT);
                return;
            }
            playOrDownloadHQVoiceMsg(
                    context, (HQVoiceMessage) uiMessage.getMessage().getContent(), uiMessage);
        } else if (messageContent instanceof VoiceMessage) {
            if (AudioPlayManager.getInstance().isPlaying()) {
                Uri playingUri = AudioPlayManager.getInstance().getPlayingUri();
                AudioPlayManager.getInstance().stopPlay();
                // 暂停的是当前播放的 Uri
                if (playingUri.equals(((VoiceMessage) messageContent).getUri())) {
                    return;
                }
            }
            // 如果被 voip 占用通道，则不播放，弹提示框
            if (AudioPlayManager.getInstance().isInVOIPMode(context)) {
                ToastUtils.show(
                        context,
                        context.getString(io.rong.imkit.R.string.rc_voip_occupying),
                        Toast.LENGTH_SHORT);
                return;
            }
            playVoiceMessage(context, uiMessage);
        }
    }

    private void playOrDownloadHQVoiceMsg(
            Context context, HQVoiceMessage hqVoiceMessage, UiMessage uiMessage) {
        boolean ifDownloadHQVoiceMsg =
                (hqVoiceMessage.getLocalPath() == null
                        || TextUtils.isEmpty(hqVoiceMessage.getLocalPath().toString())
                        || !FileUtils.isFileExistsWithUri(context, hqVoiceMessage.getLocalPath()));
        if (ifDownloadHQVoiceMsg) {
            downloadHQVoiceMsg(context, uiMessage);
        } else {
            playVoiceMessage(context, uiMessage);
        }
    }

    private void downloadHQVoiceMsg(Context context, final UiMessage uiMessage) {
        RongIMClient.getInstance()
                .downloadMediaMessage(
                        uiMessage.getMessage(),
                        new IRongCallback.IDownloadMediaMessageCallback() {
                            @Override
                            public void onSuccess(Message message) {
                                uiMessage.setState(State.NORMAL);
                                refreshSingleMessage(uiMessage);
                                playVoiceMessage(context, uiMessage);
                            }

                            @Override
                            public void onProgress(Message message, int progress) {
                                uiMessage.setState(State.PROGRESS);
                                uiMessage.setProgress(progress);
                                refreshSingleMessage(uiMessage);
                            }

                            @Override
                            public void onError(Message message, RongIMClient.ErrorCode code) {
                                uiMessage.setState(State.ERROR);
                                refreshSingleMessage(uiMessage);
                            }

                            @Override
                            public void onCanceled(Message message) {
                                uiMessage.setState(State.CANCEL);
                                refreshSingleMessage(uiMessage);
                            }
                        });
    }

    private void playVoiceMessage(Context context, final UiMessage uiMessage) {
        final MessageContent content = uiMessage.getMessage().getContent();
        Uri voicePath = null;
        if (content instanceof HQVoiceMessage) {
            voicePath = ((HQVoiceMessage) content).getLocalPath();
        } else if (content instanceof VoiceMessage) {
            voicePath = ((VoiceMessage) content).getUri();
        }
        if (voicePath != null) {
            AudioPlayManager.getInstance()
                    .startPlay(
                            context,
                            voicePath,
                            new IAudioPlayListener() {
                                @Override
                                public void onStart(Uri uri) {
                                    uiMessage.setPlaying(true);
                                    Message message = uiMessage.getMessage();
                                    message.getReceivedStatus().setListened();

                                    if (message.getContent().isDestruct()
                                            && message.getMessageDirection()
                                                    .equals(Message.MessageDirection.RECEIVE)) {
                                        uiMessage.setReadTime(0);
                                        DestructManager.getInstance()
                                                .stopDestruct(uiMessage.getMessage());
                                    }
                                    refreshSingleMessage(uiMessage);
                                }

                                @Override
                                public void onStop(Uri uri) {
                                    uiMessage.setPlaying(false);
                                    refreshSingleMessage(uiMessage);
                                }

                                @Override
                                public void onComplete(Uri uri) {
                                    uiMessage.setPlaying(false);
                                    refreshSingleMessage(uiMessage);
                                }
                            });
        }
    }

    public void refreshSingleMessage(UiMessage uiMessage) {
        int position = findPositionByMessageId(uiMessage.getMessage().getMessageId());
        if (position != -1) {
            uiMessage.setChange(true);
            refreshList(uiMessages);
        }
    }

    private void refreshList(final List<UiMessage> data) {
        if (!recyclerView.isComputingLayout()
                && recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
            messageListAdapter.setDataCollection(data);
        }
    }

    private int findPositionByMessageId(int messageId) {
        int position = -1;
        for (int i = 0; i < uiMessages.size(); i++) {
            UiMessage item = uiMessages.get(i);
            if (item.getMessage().getMessageId() == messageId) {
                position = i;
                break;
            }
        }
        return position;
    }

    public boolean isCombineV2MessageNeedDownload(CombineV2Message combineV2Message) {
        boolean isMsgListEmpty =
                combineV2Message.getMsgList() == null || combineV2Message.getMsgList().size() == 0;
        boolean isNeedDownload =
                !TextUtils.isEmpty(combineV2Message.getJsonMsgKey())
                        && (isMsgListEmpty
                                || (combineV2Message.getLocalPath() == null
                                        || !FileUtils.isFileExistsWithUri(
                                                IMCenter.getInstance().getContext(),
                                                combineV2Message.getLocalPath())));
        return isNeedDownload;
    }

    private boolean isMediaNeedDownLoadFromCombineForward(Message message) {
        // 合并转发预览
        if (message != null
                && message.getMessageId() <= 0
                && message.getContent() instanceof MediaMessageContent) {
            MediaMessageContent messageContent = (MediaMessageContent) message.getContent();
            String fileName = getCombineV2FilePath(messageContent, message.getObjectName());
            if (FileUtils.isFileExistsWithUri(
                    IMCenter.getInstance().getContext(),
                    Uri.parse(new File(fileName).toString()))) {
                messageContent.setLocalPath(Uri.parse(new File(fileName).toString()));
                return false;
            }
        }
        return true;
    }

    private static final String COMBINE_FORWARD_PATH = "combineForward";

    private String getCombineV2FilePath(
            MediaMessageContent mediaMessageContent, String objectName) {
        switch (objectName) {
            case "RC:GIFMsg":
                return FileUtils.getCachePath(IMCenter.getInstance().getContext())
                        + File.separator
                        + COMBINE_FORWARD_PATH
                        + File.separator
                        + RongUtils.md5(mediaMessageContent.getMediaUrl().toString())
                        + ".gif";
            case "RC:SightMsg":
                return FileUtils.getCachePath(IMCenter.getInstance().getContext())
                        + File.separator
                        + COMBINE_FORWARD_PATH
                        + File.separator
                        + RongUtils.md5(mediaMessageContent.getMediaUrl().toString())
                        + ".mp4";
            case "RC:ImgMsg":
                return FileUtils.getCachePath(IMCenter.getInstance().getContext())
                        + File.separator
                        + COMBINE_FORWARD_PATH
                        + File.separator
                        + RongUtils.md5(mediaMessageContent.getMediaUrl().toString())
                        + ".jpg";
            case "RC:HQVCMsg":
                return FileUtils.getCachePath(IMCenter.getInstance().getContext())
                        + File.separator
                        + COMBINE_FORWARD_PATH
                        + File.separator
                        + RongUtils.md5(mediaMessageContent.getMediaUrl().toString())
                        + ".acc";
            default:
                return FileUtils.getCachePath(IMCenter.getInstance().getContext())
                        + File.separator
                        + COMBINE_FORWARD_PATH
                        + File.separator
                        + mediaMessageContent.getName();
        }
    }
}
