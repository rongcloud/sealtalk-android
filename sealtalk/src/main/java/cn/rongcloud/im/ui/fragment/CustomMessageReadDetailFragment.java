package cn.rongcloud.im.ui.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import cn.rongcloud.im.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import io.rong.imkit.IMCenter;
import io.rong.imkit.conversation.readreceipt.MessageReadDetailFragment;
import io.rong.imkit.conversation.readreceipt.MessageReadDetailViewModel;
import io.rong.imkit.feature.forward.CombineMessage;
import io.rong.imkit.picture.tools.ScreenUtils;
import io.rong.imkit.userinfo.RongUserInfoManager;
import io.rong.imkit.utils.FileTypeUtils;
import io.rong.imkit.utils.RongDateUtils;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.location.message.LocationMessage;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.UserInfo;
import io.rong.message.CombineV2Message;
import io.rong.message.FileMessage;
import io.rong.message.GIFMessage;
import io.rong.message.HQVoiceMessage;
import io.rong.message.ImageMessage;
import io.rong.message.SightMessage;
import io.rong.message.TextMessage;

public class CustomMessageReadDetailFragment extends MessageReadDetailFragment {

    private TextView nickName;
    private TextView txtMessageContent;
    private ImageView imageMessageContent;
    private TextView fileName;
    private TextView fileSize;
    private TextView messageTime;

    @NonNull
    @Override
    public View onCreateView(
            @NonNull Context context,
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle args) {
        View view = super.onCreateView(context, inflater, container, args);
        view.findViewById(R.id.message_content_container).setVisibility(View.VISIBLE);
        nickName = view.findViewById(R.id.tv_nick_name);
        txtMessageContent = view.findViewById(R.id.tv_message_content);
        imageMessageContent = view.findViewById(R.id.iv_message_content);
        fileName = view.findViewById(R.id.tv_file_name);
        fileSize = view.findViewById(R.id.tv_file_size);
        messageTime = view.findViewById(R.id.tv_message_time);
        return view;
    }

    @Override
    protected void onViewReady(@NonNull MessageReadDetailViewModel viewModel) {
        super.onViewReady(viewModel);
        Message message = viewModel.getMessage();
        // nickName
        UserInfo user = RongUserInfoManager.getInstance().getUserInfo(message.getSenderUserId());
        nickName.setText(user != null ? user.getName() : message.getSenderUserId());
        // 消息体
        setMessageContentView(message);
        // time
        long time = message.getSentTime();
        if (time > 0) {
            messageTime.setText(RongDateUtils.getConversationFormatDate(time, this.getActivity()));
        }
    }

    /**
     * 展示消息内容 - 文本消息：最多展示两行，展示不下的部分在最后用“…”标识
     *
     * <p>- 图片 视频消息：展示缩略图，固定按60*60宽高上限锁定比例缩放
     *
     * <p>- 文件消息：展示文件缩略图和文件名称，文件缩略图固定60*60
     *
     * <p>- 位置消息：展示为【位置】
     *
     * <p>- 语音消息：展示为【语音】
     *
     * <p>- 合并转发内容：展示为【聊天记录】
     *
     * <p>
     *
     * @param message Message
     */
    private void setMessageContentView(Message message) {
        Context context = getContext();
        if (context == null) {
            return;
        }
        txtMessageContent.setVisibility(View.VISIBLE);
        if (message.getContent() instanceof TextMessage) {
            String content = ((TextMessage) message.getContent()).getContent();
            txtMessageContent.setText(content);
        } else if (message.getContent() instanceof ImageMessage) {
            Uri thumbUri = ((ImageMessage) message.getContent()).getThumUri();
            loadImageThumbUri(context, thumbUri);
        } else if (message.getContent() instanceof GIFMessage) {
            GIFMessage gifMessage = (GIFMessage) message.getContent();
            if (gifMessage.getLocalPath() != null) {
                loadImageThumbUri(context, gifMessage.getLocalPath());
            } else {
                IMCenter.getInstance()
                        .downloadMediaMessage(
                                message,
                                new IRongCallback.IDownloadMediaMessageCallback() {
                                    @Override
                                    public void onSuccess(Message message) {
                                        Uri thumbUri =
                                                ((GIFMessage) message.getContent()).getLocalUri();
                                        loadImageThumbUri(context, thumbUri);
                                    }

                                    @Override
                                    public void onProgress(Message message, int progress) {}

                                    @Override
                                    public void onError(
                                            Message message, RongIMClient.ErrorCode code) {}

                                    @Override
                                    public void onCanceled(Message message) {}
                                });
            }
        } else if (message.getContent() instanceof SightMessage) {
            Uri thumbUri = ((SightMessage) message.getContent()).getThumbUri();
            loadImageThumbUri(context, thumbUri);
        } else if (message.getContent() instanceof FileMessage) {
            loadFileIcon(context, (FileMessage) message.getContent());
        } else if (message.getContent() instanceof LocationMessage) {
            txtMessageContent.setText(io.rong.imkit.R.string.rc_message_content_location);
        } else if (message.getContent() instanceof HQVoiceMessage) {
            txtMessageContent.setText(io.rong.imkit.R.string.rc_message_content_voice);
        } else if (message.getContent() instanceof CombineMessage
                || message.getContent() instanceof CombineV2Message) {
            txtMessageContent.setText(io.rong.imkit.R.string.rc_message_content_combine);
        } else {
            txtMessageContent.setText(R.string.seal_message_content_others);
        }
    }

    private void loadImageThumbUri(Context context, Uri thumbUri) {
        txtMessageContent.setVisibility(View.GONE);
        imageMessageContent.setVisibility(View.VISIBLE);
        if (thumbUri != null && thumbUri.getPath() != null) {
            Glide.with(imageMessageContent)
                    .load(thumbUri.getPath())
                    .optionalCenterCrop()
                    .apply(
                            RequestOptions.bitmapTransform(
                                    new RoundedCorners(ScreenUtils.dip2px(context, 6))))
                    .into(imageMessageContent);
        }
    }

    private void loadFileIcon(Context context, FileMessage fileMessage) {
        txtMessageContent.setVisibility(View.GONE);
        imageMessageContent.setVisibility(View.VISIBLE);
        fileName.setVisibility(View.VISIBLE);
        fileSize.setVisibility(View.VISIBLE);
        fileName.setText(fileMessage.getName());
        fileSize.setText(FileTypeUtils.formatFileSize(fileMessage.getSize()));
        imageMessageContent.setImageResource(
                FileTypeUtils.fileTypeImageId(getActivity(), fileMessage.getName()));
    }
}
