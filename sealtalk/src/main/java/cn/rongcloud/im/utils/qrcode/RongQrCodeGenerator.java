package cn.rongcloud.im.utils.qrcode;

import android.graphics.Bitmap;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.util.HashMap;
import java.util.Map;

/**
 * 融云二维码生成器实现
 *
 * <p>使用 ZXing 库生成二维码
 *
 * @author rongcloud
 * @since 5.12.0
 */
public class RongQrCodeGenerator {

    private static final int DEFAULT_SIZE = 400;
    private static final int BLACK = 0xFF000000;
    private static final int WHITE = 0xFFFFFFFF;

    private static volatile RongQrCodeGenerator instance;

    /**
     * 获取单例实例
     *
     * @return RongQrCodeGenerator 实例
     */
    public static RongQrCodeGenerator getInstance() {
        if (instance == null) {
            synchronized (RongQrCodeGenerator.class) {
                if (instance == null) {
                    instance = new RongQrCodeGenerator();
                }
            }
        }
        return instance;
    }

    private RongQrCodeGenerator() {}

    public Bitmap generateQRCode(@NonNull String content, int width, int height) {
        if (TextUtils.isEmpty(content)) {
            return null;
        }

        if (width <= 0) {
            width = DEFAULT_SIZE;
        }
        if (height <= 0) {
            height = DEFAULT_SIZE;
        }

        try {
            // 配置二维码参数
            Map<EncodeHintType, Object> hints = new HashMap<>();
            // 设置字符编码
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            // 设置容错级别（L < M < Q < H）
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            // 设置边距
            hints.put(EncodeHintType.MARGIN, 1);

            // 生成二维码矩阵
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix =
                    writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints);

            // 将矩阵转换为 Bitmap
            return bitMatrixToBitmap(bitMatrix);
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 生成带 Logo 的二维码
     *
     * @param content 二维码内容
     * @param width 二维码宽度
     * @param height 二维码高度
     * @param logo Logo 图片
     * @return 二维码 Bitmap，如果生成失败返回 null
     */
    public Bitmap generateQRCodeWithLogo(String content, int width, int height, Bitmap logo) {
        if (TextUtils.isEmpty(content)) {
            return null;
        }

        if (width <= 0) {
            width = DEFAULT_SIZE;
        }
        if (height <= 0) {
            height = DEFAULT_SIZE;
        }

        try {
            // 配置二维码参数
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            // 使用较高的容错级别以支持 Logo 覆盖
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 1);

            // 生成二维码矩阵
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix =
                    writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints);

            // 将矩阵转换为 Bitmap
            Bitmap qrBitmap = bitMatrixToBitmap(bitMatrix);

            // 如果有 Logo，将 Logo 绘制到二维码中心
            if (logo != null && qrBitmap != null) {
                return addLogoToQRCode(qrBitmap, logo);
            }

            return qrBitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将 BitMatrix 转换为 Bitmap
     *
     * @param matrix 二维码矩阵
     * @return Bitmap
     */
    private Bitmap bitMatrixToBitmap(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int[] pixels = new int[width * height];

        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = matrix.get(x, y) ? BLACK : WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    /**
     * 在二维码中心添加 Logo
     *
     * @param qrBitmap 二维码 Bitmap
     * @param logo Logo Bitmap
     * @return 带 Logo 的二维码 Bitmap
     */
    private Bitmap addLogoToQRCode(Bitmap qrBitmap, Bitmap logo) {
        if (qrBitmap == null || logo == null) {
            return qrBitmap;
        }

        int qrWidth = qrBitmap.getWidth();
        int qrHeight = qrBitmap.getHeight();
        int logoWidth = logo.getWidth();
        int logoHeight = logo.getHeight();

        // Logo 大小不应超过二维码的 1/5
        if (logoWidth > qrWidth / 5 || logoHeight > qrHeight / 5) {
            float scaleWidth = (qrWidth / 5.0f) / logoWidth;
            float scaleHeight = (qrHeight / 5.0f) / logoHeight;
            float scale = Math.min(scaleWidth, scaleHeight);
            logoWidth = (int) (logoWidth * scale);
            logoHeight = (int) (logoHeight * scale);
            logo = Bitmap.createScaledBitmap(logo, logoWidth, logoHeight, true);
        }

        // 创建新的 Bitmap 用于绘制
        Bitmap bitmap = Bitmap.createBitmap(qrWidth, qrHeight, Bitmap.Config.ARGB_8888);
        android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);

        // 绘制二维码
        canvas.drawBitmap(qrBitmap, 0, 0, null);

        // 在中心绘制 Logo
        int left = (qrWidth - logoWidth) / 2;
        int top = (qrHeight - logoHeight) / 2;
        canvas.drawBitmap(logo, left, top, null);

        canvas.save();
        canvas.restore();

        return bitmap;
    }
}
