package cn.rongcloud.im.utils;

import static com.nostra13.universalimageloader.core.assist.ImageScaleType.IN_SAMPLE_POWER_OF_2;

import android.graphics.Bitmap;
import android.widget.ImageView;
import cn.rongcloud.im.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ImageLoaderUtils {

    private static DisplayImageOptions privateOptions;
    private static DisplayImageOptions groupOptions;
    private static DisplayImageOptions descriptionOptions;

    static {
        privateOptions = createDefaultDisplayOptions(io.rong.imkit.R.drawable.rc_default_portrait);
        groupOptions = createDefaultDisplayOptions(R.drawable.seal_default_group_portrait);
        descriptionOptions = createDefaultDisplayOptions(android.R.color.transparent);
    }

    public static void cancelDisplayTask(ImageView imageView) {
        ImageLoader.getInstance().cancelDisplayTask(imageView);
    }

    public static void displayUserPortraitImage(String uri, ImageView imageView) {
        ImageLoader.getInstance().displayImage(uri, imageView, privateOptions);
        //        ImageLoader.getInstance().displayImage(uri, imageView, privateOptions, null);
    }

    public static void displayGroupPortraitImage(String uri, ImageView imageView) {
        ImageLoader.getInstance().displayImage(uri, imageView, groupOptions, null);
    }

    public static void displayUserDescritpionImage(String uri, ImageView imageView) {
        ImageLoader.getInstance().displayImage(uri, imageView, descriptionOptions, null);
    }

    private static DisplayImageOptions createDefaultDisplayOptions(int defaultImgId) {
        DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
        builder.showImageOnFail(
                        defaultImgId == 0
                                ? io.rong.imkit.R.drawable.rc_default_portrait
                                : defaultImgId) // 设置加载失败的图片
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(IN_SAMPLE_POWER_OF_2)
                .considerExifParams(true)
                .build();
        return builder.build();
    }
}
