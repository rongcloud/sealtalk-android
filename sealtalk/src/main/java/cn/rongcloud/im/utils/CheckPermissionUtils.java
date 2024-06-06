package cn.rongcloud.im.utils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import cn.rongcloud.im.R;
import io.rong.callkit.util.RongCallPermissionUtil;
import io.rong.callkit.util.permission.PermissionType;
import io.rong.common.rlog.RLog;
import io.rong.imkit.utils.PermissionCheckUtil;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Created by zwfang on 2018/1/29. */
public class CheckPermissionUtils {
    public static final int REQUEST_CODE_PERMISSION = 118;

    /**
     * 检查媒体存储权限（适配 Android13 和 Android14）
     *
     * @param activity Activity
     */
    public static boolean checkMediaStoragePermissions(Activity activity) {
        return PermissionCheckUtil.checkMediaStoragePermissions(activity);
    }

    /**
     * 申请媒体存储权限（适配 Android13 和 Android14）
     *
     * @param activity Activity
     */
    public static void requestMediaStoragePermissions(Activity activity) {
        if (!PermissionCheckUtil.checkMediaStoragePermissions(activity)) {
            String[] permissions = PermissionCheckUtil.getMediaStoragePermissions(activity);
            ActivityCompat.requestPermissions(activity, permissions, REQUEST_CODE_PERMISSION);
        }
    }

    public static boolean requestPermissions(
            Activity activity, String[] permissions, int requestCode) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (permissions.length == 0) {
            return true;
        }
        if (lacksPermissions(activity, permissions)) {
            ActivityCompat.requestPermissions(activity, permissions, requestCode);
            return false;
        }
        return true;
    }

    public static boolean allPermissionGranted(int... grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void showPermissionAlert(
            Context context, String content, DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(context, android.R.style.Theme_Material_Light_Dialog_Alert)
                .setMessage(content)
                .setPositiveButton(R.string.common_confirm, listener)
                .setNegativeButton(R.string.common_cancel, listener)
                .setCancelable(false)
                .create()
                .show();
    }

    public static String getNotGrantedPermissionMsg(Context context, List<String> permissions) {
        Set<String> permissionsValue = new HashSet<>();
        String permissionValue;
        for (String permission : permissions) {
            permissionValue =
                    context.getApplicationContext()
                            .getString(
                                    context.getResources()
                                            .getIdentifier(
                                                    "rc_" + permission,
                                                    "string",
                                                    context.getPackageName()),
                                    0);
            permissionsValue.add(permissionValue);
        }

        String result = "(";
        for (String value : permissionsValue) {
            result += (value + " ");
        }
        result = result.trim() + ")";
        return result;
    }

    private static boolean lacksPermissions(Activity activity, String... permissions) {
        for (String permission : permissions) {
            try {
                if (ContextCompat.checkSelfPermission(activity, permission)
                        == PackageManager.PERMISSION_DENIED) {
                    return true;
                }
            } catch (Exception e) {
                return true;
            }
        }
        return false;
    }

    /** 跳转设置界面 */
    public static void startAppSetting(Context context) {
        Intent localIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        localIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
        context.startActivity(localIntent);
    }

    /**
     * 跳转到权限设置界面
     *
     * @param context
     */
    public static void toPermissionSetting(Context context) {
        String brand = Build.BRAND;
        if (TextUtils.equals(brand.toLowerCase(), "redmi")
                || TextUtils.equals(brand.toLowerCase(), "xiaomi")) {
            gotoMiuiPermission(context);
        } else if (TextUtils.equals(brand.toLowerCase(), "meizu")) {
            gotoMeizuPermission(context);
        } else if (TextUtils.equals(brand.toLowerCase(), "huawei")
                || TextUtils.equals(brand.toLowerCase(), "honor")) {
            gotoHuaweiPermission(context);
        } else {
            startAppSetting(context);
        }
    }

    /** 跳转到miui的权限管理页面 */
    private static void gotoMiuiPermission(Context context) {
        try { // MIUI 8
            Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
            localIntent.setClassName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.permissions.PermissionsEditorActivity");
            localIntent.putExtra("extra_pkgname", context.getPackageName());
            context.startActivity(localIntent);
        } catch (Exception e) {
            Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
            localIntent.setClassName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
            localIntent.putExtra("extra_pkgname", context.getPackageName());
            context.startActivity(localIntent);
        }
    }

    /** 跳转到魅族的权限管理系统 */
    private static void gotoMeizuPermission(Context context) {
        Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra("packageName", context.getPackageName());
        context.startActivity(intent);
    }

    /** 华为的权限管理页面 */
    private static void gotoHuaweiPermission(Context context) {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ComponentName comp =
                new ComponentName(
                        "com.huawei.systemmanager",
                        "com.huawei.permissionmanager.ui.MainActivity"); // 华为权限管理
        intent.setComponent(comp);
        context.startActivity(intent);
    }

    public static void setPermissionRequestListener(Context context) {
        PermissionCheckUtil.setRequestPermissionListListener(
                new PermissionCheckUtil.IRequestPermissionListListener() {
                    @Override
                    public void onRequestPermissionList(
                            Context activity,
                            List<String> permissionsNotGranted,
                            PermissionCheckUtil.IPermissionEventCallback callback) {

                        String notGrantedPermissionMsg =
                                getNotGrantedPermissionMsg(
                                        context, permissionsNotGranted.toArray(new String[0]));
                        StringBuilder messageBuilder =
                                new StringBuilder(notGrantedPermissionMsg).append("，才能正常使用");

                        if (containsPermissions(
                                permissionsNotGranted,
                                PermissionCheckUtil.getMediaStoragePermissions(context))) {
                            messageBuilder.append("图片、");
                        }

                        if (containsPermissions(
                                permissionsNotGranted,
                                new String[] {
                                    Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO
                                })) {
                            messageBuilder.append("拍照、小视频、");
                        }

                        if (containsPermissions(
                                permissionsNotGranted,
                                new String[] {
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_NETWORK_STATE,
                                    Manifest.permission.READ_PHONE_STATE
                                })) {
                            messageBuilder.append("位置消息、位置共享、");
                        }

                        if (containsPermissions(
                                permissionsNotGranted,
                                getCallPermissions(
                                        RongCallPermissionUtil.getAudioCallPermissions(context)))) {
                            messageBuilder.append("语音通话、");
                        }

                        if (containsPermissions(
                                permissionsNotGranted,
                                getCallPermissions(
                                        RongCallPermissionUtil.getVideoCallPermissions(context)))) {
                            messageBuilder.append("视频通话、");
                        }

                        if (permissionsNotGranted.contains(Manifest.permission.RECORD_AUDIO)) {
                            messageBuilder.append("语音输入、");
                        }

                        // 删除最后一个多余的逗号
                        if (messageBuilder.charAt(messageBuilder.length() - 1) == '、') {
                            messageBuilder.deleteCharAt(messageBuilder.length() - 1);
                        }

                        messageBuilder.append("等相关功能");
                        String message = messageBuilder.toString().trim();

                        new AlertDialog.Builder(
                                        activity,
                                        android.R.style.Theme_DeviceDefault_Light_Dialog_Alert)
                                .setTitle("权限说明")
                                .setMessage(message)
                                .setPositiveButton(
                                        "去申请",
                                        (dialog, which) -> {
                                            dialog.dismiss();
                                            callback.confirmed();
                                        })
                                .setNegativeButton(
                                        "取消",
                                        (dialog, which) -> {
                                            dialog.dismiss();
                                            callback.cancelled();
                                        })
                                .show();
                    }
                });
    }

    private static boolean containsPermissions(
            List<String> permissionsNotGranted, String[] requiredPermissions) {
        for (String permission : requiredPermissions) {
            if (permissionsNotGranted.contains(permission)) {
                return true;
            }
        }
        return false;
    }

    private static String[] getCallPermissions(PermissionType[] permissionTypes) {
        String[] permissions = new String[permissionTypes.length];
        for (int i = 0; i < permissionTypes.length; i++) {
            permissions[i] = permissionTypes[i].getPermissionName();
        }
        return permissions;
    }

    private static String getNotGrantedPermissionMsg(Context context, String[] permissions) {
        List<String> permissionNameList = new ArrayList<>(permissions.length);
        for (String permission : permissions) {
            try {
                String permissionName =
                        context.getString(
                                context.getResources()
                                        .getIdentifier(
                                                "rc_" + permission,
                                                "string",
                                                context.getPackageName()),
                                0);
                if (!permissionNameList.contains(permissionName)) {
                    permissionNameList.add(permissionName);
                }
            } catch (Resources.NotFoundException e) {
                RLog.e(
                        "PermissionUtils",
                        "One of the permissions is not recognized by SDK: " + permission);
            }
        }
        return "需要开启(" + TextUtils.join(" ", permissionNameList) + ")权限";
    }
}
