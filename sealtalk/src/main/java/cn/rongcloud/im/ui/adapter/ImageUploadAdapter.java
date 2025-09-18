package cn.rongcloud.im.ui.adapter;

import android.net.Uri;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import cn.rongcloud.im.R;
import com.bumptech.glide.Glide;
import java.util.List;

public class ImageUploadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_IMAGE = 1;
    private static final int VIEW_TYPE_ADD_BUTTON = 2;

    private List<Pair<Uri, UploadStatus>> imageUploads;
    private OnRetryClickListener onRetryClickListener;
    private OnImageClickListener onImageClickListener;
    private OnAddImageClickListener onAddImageClickListener;
    private boolean isAddButtonVisible = true;

    public interface OnRetryClickListener {
        void onRetry(Pair<Uri, UploadStatus> imageUpload);
    }

    public interface OnImageClickListener {
        void onImageClick(int position);
    }

    public interface OnAddImageClickListener {
        void onAddImageClick();
    }

    public ImageUploadAdapter(
            List<Pair<Uri, UploadStatus>> imageUploads,
            OnRetryClickListener onRetryClickListener,
            OnImageClickListener onImageClickListener,
            OnAddImageClickListener onAddImageClickListener) {
        this.imageUploads = imageUploads;
        this.onRetryClickListener = onRetryClickListener;
        this.onImageClickListener = onImageClickListener;
        this.onAddImageClickListener = onAddImageClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == imageUploads.size() && isAddButtonVisible) {
            return VIEW_TYPE_ADD_BUTTON;
        } else {
            return VIEW_TYPE_IMAGE;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ADD_BUTTON) {
            View view =
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.add_image_layout, parent, false);
            return new AddButtonViewHolder(view);
        } else {
            View view =
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_image_upload, parent, false);
            return new ImageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == VIEW_TYPE_IMAGE) {
            ImageViewHolder imageHolder = (ImageViewHolder) holder;
            Pair<Uri, UploadStatus> imageUpload = imageUploads.get(position);

            // 使用 Glide 加载图片
            Glide.with(imageHolder.itemView.getContext())
                    .load(imageUpload.first)
                    .into(imageHolder.imageView);

            switch (imageUpload.second) {
                case UPLOADING:
                    imageHolder.statusTextView.setText(R.string.upload_status_uploading);
                    imageHolder.retryButton.setVisibility(View.GONE);
                    break;
                case SUCCESS:
                    imageHolder.statusTextView.setText(R.string.upload_status_success);
                    imageHolder.retryButton.setVisibility(View.GONE);
                    break;
                case FAILED:
                    imageHolder.statusTextView.setText(R.string.upload_status_failed);
                    imageHolder.retryButton.setVisibility(View.VISIBLE);
                    imageHolder.retryButton.setOnClickListener(
                            v -> onRetryClickListener.onRetry(imageUpload));
                    break;
                default:
                    break;
            }

            imageHolder.itemView.setOnClickListener(
                    v -> onImageClickListener.onImageClick(holder.getAdapterPosition()));

        } else if (holder.getItemViewType() == VIEW_TYPE_ADD_BUTTON) {
            AddButtonViewHolder addButtonHolder = (AddButtonViewHolder) holder;
            addButtonHolder.addButton.setOnClickListener(
                    v -> onAddImageClickListener.onAddImageClick());
        }
    }

    @Override
    public int getItemCount() {
        return imageUploads.size() + (isAddButtonVisible ? 1 : 0);
    }

    public boolean isAddButtonVisible() {
        return isAddButtonVisible;
    }

    public void setAddButtonVisible(boolean isVisible) {
        this.isAddButtonVisible = isVisible;
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView statusTextView;
        Button retryButton;

        ImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            retryButton = itemView.findViewById(R.id.retryButton);
        }
    }

    static class AddButtonViewHolder extends RecyclerView.ViewHolder {
        ImageView addButton;

        AddButtonViewHolder(View itemView) {
            super(itemView);
            addButton = itemView.findViewById(R.id.addButton);
        }
    }

    public enum UploadStatus {
        PENDING,
        UPLOADING,
        SUCCESS,
        FAILED
    }
}
