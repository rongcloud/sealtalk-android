package cn.rongcloud.im.task;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Result;
import cn.rongcloud.im.model.SecurityStatusResult;
import cn.rongcloud.im.model.SecurityVerifyResult;
import cn.rongcloud.im.model.UserCacheInfo;
import cn.rongcloud.im.net.RetrofitUtil;
import cn.rongcloud.im.net.proxy.RetrofitProxyServiceCreator;
import cn.rongcloud.im.net.service.SecurityService;
import cn.rongcloud.im.sp.UserCache;
import cn.rongcloud.im.utils.NetworkOnlyResource;
import java.util.HashMap;

public class SecurityTask {

    SecurityService securityService;

    UserCache userCache;

    public SecurityTask(Context context) {
        securityService =
                RetrofitProxyServiceCreator.getRetrofitService(context, SecurityService.class);
        userCache = new UserCache(context);
    }

    public LiveData<Resource<SecurityStatusResult>> querySecurityStatus() {
        return new NetworkOnlyResource<SecurityStatusResult, Result<SecurityStatusResult>>() {

            @NonNull
            @Override
            protected LiveData<Result<SecurityStatusResult>> createCall() {
                return securityService.querySecurityStatus();
            }
        }.asLiveData();
    }

    public LiveData<Resource<SecurityVerifyResult>> doSecurityVerify(String deviceId) {
        return new NetworkOnlyResource<SecurityVerifyResult, Result<SecurityVerifyResult>>() {

            @NonNull
            @Override
            protected LiveData<Result<SecurityVerifyResult>> createCall() {
                HashMap<String, String> queryMap = new HashMap<>();
                UserCacheInfo user = userCache.getUserCache();
                queryMap.put("deviceId", deviceId);
                queryMap.put("region", user.getRegion());
                queryMap.put("phone", user.getPhoneNumber());
                queryMap.put("os", "android");
                return securityService.doSecurityVerify(queryMap);
            }
        }.asLiveData();
    }

    public LiveData<Resource<Void>> reportUser(
            int conversationType,
            String targetId,
            String levelF,
            String levelS,
            String[] pics,
            String content) {
        return new NetworkOnlyResource<Void, Result<Void>>() {

            @NonNull
            @Override
            protected LiveData<Result<Void>> createCall() {
                HashMap<String, Object> paramMap = new HashMap<>();
                paramMap.put("channelType", conversationType);
                paramMap.put("targetId", targetId);
                paramMap.put("levelF", levelF);
                paramMap.put("levelS", levelS);
                paramMap.put("pics", pics);
                paramMap.put("content", content);
                return securityService.reportUser(RetrofitUtil.createJsonRequest(paramMap));
            }
        }.asLiveData();
    }
}
