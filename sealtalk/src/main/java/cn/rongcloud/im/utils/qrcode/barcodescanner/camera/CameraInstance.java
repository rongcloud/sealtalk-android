package cn.rongcloud.im.utils.qrcode.barcodescanner.camera;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import cn.rongcloud.im.R;
import cn.rongcloud.im.utils.qrcode.barcodescanner.Size;
import cn.rongcloud.im.utils.qrcode.barcodescanner.Util;

/**
 * Manage a camera instance using a background thread.
 *
 * <p>All methods must be called from the main thread.
 */
public class CameraInstance {
    private static final String TAG = CameraInstance.class.getSimpleName();

    private CameraThread cameraThread;
    private CameraSurface surface;

    private CameraManager cameraManager;
    private Handler readyHandler;
    private DisplayConfiguration displayConfiguration;
    private boolean open = false;
    private CameraSettings cameraSettings = new CameraSettings();

    /**
     * Construct a new CameraInstance.
     *
     * <p>A new CameraManager is created.
     *
     * @param context the Android Context
     */
    public CameraInstance(Context context) {
        Util.validateMainThread();

        this.cameraThread = CameraThread.getInstance();
        this.cameraManager = new CameraManager(context);
        this.cameraManager.setCameraSettings(cameraSettings);
    }

    /**
     * Construct a new CameraInstance with a specific CameraManager.
     *
     * @param cameraManager the CameraManager to use
     */
    public CameraInstance(CameraManager cameraManager) {
        Util.validateMainThread();

        this.cameraManager = cameraManager;
    }

    public void setDisplayConfiguration(DisplayConfiguration configuration) {
        this.displayConfiguration = configuration;
        cameraManager.setDisplayConfiguration(configuration);
    }

    public DisplayConfiguration getDisplayConfiguration() {
        return displayConfiguration;
    }

    public void setReadyHandler(Handler readyHandler) {
        this.readyHandler = readyHandler;
    }

    public void setSurfaceHolder(SurfaceHolder surfaceHolder) {
        setSurface(new CameraSurface(surfaceHolder));
    }

    public void setSurface(CameraSurface surface) {
        this.surface = surface;
    }

    public CameraSettings getCameraSettings() {
        return cameraSettings;
    }

    /**
     * This only has an effect if the camera is not opened yet.
     *
     * @param cameraSettings the new camera settings
     */
    public void setCameraSettings(CameraSettings cameraSettings) {
        if (!open) {
            this.cameraSettings = cameraSettings;
            this.cameraManager.setCameraSettings(cameraSettings);
        }
    }

    /**
     * Actual preview size in current rotation. null if not determined yet.
     *
     * @return preview size
     */
    private Size getPreviewSize() {
        return cameraManager.getPreviewSize();
    }

    /**
     * @return the camera rotation relative to display rotation, in degrees. Typically 0 if the
     *     display is in landscape orientation.
     */
    public int getCameraRotation() {
        return cameraManager.getCameraRotation();
    }

    public void open() {
        Util.validateMainThread();

        open = true;

        cameraThread.incrementAndEnqueue(opener);
    }

    public void configureCamera() {
        Util.validateMainThread();
        validateOpen();

        cameraThread.enqueue(configure);
    }

    public void startPreview() {
        Util.validateMainThread();
        validateOpen();

        cameraThread.enqueue(previewStarter);
    }

    public void setTorch(final boolean on) {
        Util.validateMainThread();

        if (open) {
            cameraThread.enqueue(
                    new Runnable() {
                        @Override
                        public void run() {
                            cameraManager.setTorch(on);
                        }
                    });
        }
    }

    public void close() {
        Util.validateMainThread();

        if (open) {
            cameraThread.enqueue(closer);
        }

        open = false;
    }

    public boolean isOpen() {
        return open;
    }

    public void requestPreview(final PreviewCallback callback) {
        validateOpen();

        cameraThread.enqueue(
                new Runnable() {
                    @Override
                    public void run() {
                        cameraManager.requestPreviewFrame(callback);
                    }
                });
    }

    private void validateOpen() {
        if (!open) {
            throw new IllegalStateException("CameraInstance is not open");
        }
    }

    private Runnable opener =
            new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.d(TAG, "Opening camera");
                        cameraManager.open();
                    } catch (Exception e) {
                        notifyError(e);
                        Log.e(TAG, "Failed to open camera", e);
                    }
                }
            };

    private Runnable configure =
            new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.d(TAG, "Configuring camera");
                        cameraManager.configure();
                        if (readyHandler != null) {
                            readyHandler
                                    .obtainMessage(R.id.zxing_prewiew_size_ready, getPreviewSize())
                                    .sendToTarget();
                        }
                    } catch (Exception e) {
                        notifyError(e);
                        Log.e(TAG, "Failed to configure camera", e);
                    }
                }
            };

    private Runnable previewStarter =
            new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.d(TAG, "Starting preview");
                        cameraManager.setPreviewDisplay(surface);
                        cameraManager.startPreview();
                    } catch (Exception e) {
                        notifyError(e);
                        Log.e(TAG, "Failed to start preview", e);
                    }
                }
            };

    private Runnable closer =
            new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.d(TAG, "Closing camera");
                        cameraManager.stopPreview();
                        cameraManager.close();
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to close camera", e);
                    }

                    cameraThread.decrementInstances();
                }
            };

    private void notifyError(Exception error) {
        if (readyHandler != null) {
            readyHandler.obtainMessage(R.id.zxing_camera_error, error).sendToTarget();
        }
    }

    /**
     * Returns the CameraManager used to control the camera.
     *
     * <p>The CameraManager is not thread-safe, and must only be used from the CameraThread.
     *
     * @return the CameraManager used
     */
    protected CameraManager getCameraManager() {
        return cameraManager;
    }

    /**
     * @return the CameraThread used to manage the camera
     */
    protected CameraThread getCameraThread() {
        return cameraThread;
    }

    /**
     * @return the surface om which the preview is displayed
     */
    protected CameraSurface getSurface() {
        return surface;
    }
}
