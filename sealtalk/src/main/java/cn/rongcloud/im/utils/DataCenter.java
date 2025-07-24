package cn.rongcloud.im.utils;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import cn.rongcloud.im.sp.UserConfigCache;
import io.rong.imlib.model.InitOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author gusd @Date 2022/03/29
 */
public interface DataCenter {

    Map<String, DataCenter> DATA_CENTER_MAP = new LinkedHashMap<>();

    public static DataCenter getDataCenter(String code) {
        DataCenter center = DATA_CENTER_MAP.get(code);
        if (center == null) {
            for (DataCenter value : DATA_CENTER_MAP.values()) {
                if (value.isDefault()) {
                    return value;
                }
            }
        }
        return center;
    }

    public static void addDataCenter(@NonNull DataCenter dataCenter) {
        DATA_CENTER_MAP.put(dataCenter.getCode(), dataCenter);
    }

    public static List<DataCenter> getDataCenterList() {
        return new ArrayList<>(DATA_CENTER_MAP.values());
    }

    /**
     * 获取过滤后的数据中心列表，根据配置决定是否显示吕布数据中心
     *
     * @param context 上下文
     * @return 过滤后的数据中心列表
     */
    public static List<DataCenter> getFilteredDataCenterList(Context context) {
        List<DataCenter> allCenters = new ArrayList<>(DATA_CENTER_MAP.values());

        if (context == null) {
            return allCenters;
        }

        UserConfigCache configCache = new UserConfigCache(context);
        boolean showSpecialDataCenter = configCache.getSpecialDataCenterVisibility();

        if (!showSpecialDataCenter) {
            // 过滤掉吕布数据中心 (lvbu)
            List<DataCenter> filteredList = new ArrayList<>();
            for (DataCenter center : allCenters) {
                if (!"lvbu".equals(center.getCode())) {
                    filteredList.add(center);
                }
            }
            return filteredList;
        }

        return allCenters;
    }

    public String getNaviUrl();

    @StringRes
    public int getNameId();

    // "north_america" > InitOption.AreaCode.NA
    // "singapore" > InitOption.AreaCode.SG
    // "beijing" > InitOption.AreaCode.BJ
    public String getCode();

    public default InitOption.AreaCode getAreaCode() {
        return InitOption.AreaCode.BJ;
    }

    public String getAppKey();

    public String getAppServer();

    public default boolean isDefault() {
        return false;
    }
}
