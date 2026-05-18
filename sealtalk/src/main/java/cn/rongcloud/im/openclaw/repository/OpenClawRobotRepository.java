package cn.rongcloud.im.openclaw.repository;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Result;
import cn.rongcloud.im.net.RetrofitUtil;
import cn.rongcloud.im.net.proxy.RetrofitProxyServiceCreator;
import cn.rongcloud.im.openclaw.model.OpenClawRobotInfo;
import cn.rongcloud.im.openclaw.model.OpenClawRobotTokenResult;
import cn.rongcloud.im.utils.NetworkOnlyResource;
import java.util.HashMap;
import java.util.List;
import okhttp3.RequestBody;

public class OpenClawRobotRepository {
    private final OpenClawRobotService service;

    public OpenClawRobotRepository(Context context) {
        service =
                RetrofitProxyServiceCreator.getRetrofitService(context, OpenClawRobotService.class);
    }

    public LiveData<Resource<OpenClawRobotTokenResult>> createRobot(
            String name, String portraitUri) {
        return new NetworkOnlyResource<
                OpenClawRobotTokenResult, Result<OpenClawRobotTokenResult>>() {
            @NonNull
            @Override
            protected LiveData<Result<OpenClawRobotTokenResult>> createCall() {
                HashMap<String, Object> params = new HashMap<>();
                params.put("name", name);
                params.put("portraitUri", portraitUri);
                return service.createRobot(RetrofitUtil.createJsonRequest(params));
            }
        }.asLiveData();
    }

    public LiveData<Resource<OpenClawRobotTokenResult>> refreshToken(String botId) {
        return new NetworkOnlyResource<
                OpenClawRobotTokenResult, Result<OpenClawRobotTokenResult>>() {
            @NonNull
            @Override
            protected LiveData<Result<OpenClawRobotTokenResult>> createCall() {
                HashMap<String, Object> params = new HashMap<>();
                params.put("botId", botId);
                return service.refreshToken(RetrofitUtil.createJsonRequest(params));
            }
        }.asLiveData();
    }

    public LiveData<Resource<List<OpenClawRobotInfo>>> getMyRobots() {
        return new NetworkOnlyResource<List<OpenClawRobotInfo>, Result<List<OpenClawRobotInfo>>>() {
            @NonNull
            @Override
            protected LiveData<Result<List<OpenClawRobotInfo>>> createCall() {
                return service.getMyRobots();
            }
        }.asLiveData();
    }

    public LiveData<Resource<OpenClawRobotTokenResult>> getRobotInfo(String botId) {
        return new NetworkOnlyResource<
                OpenClawRobotTokenResult, Result<OpenClawRobotTokenResult>>() {
            @NonNull
            @Override
            protected LiveData<Result<OpenClawRobotTokenResult>> createCall() {
                return service.getRobotInfo(botId);
            }
        }.asLiveData();
    }

    public LiveData<Resource<List<OpenClawRobotInfo>>> getGroupRobots(String groupId) {
        return new NetworkOnlyResource<List<OpenClawRobotInfo>, Result<List<OpenClawRobotInfo>>>() {
            @NonNull
            @Override
            protected LiveData<Result<List<OpenClawRobotInfo>>> createCall() {
                return service.getGroupRobots(groupId);
            }
        }.asLiveData();
    }

    public LiveData<Resource<Void>> addGroupRobots(String groupId, List<String> botIds) {
        return updateGroupRobots(groupId, botIds, true);
    }

    public LiveData<Resource<Void>> removeGroupRobots(String groupId, List<String> botIds) {
        return updateGroupRobots(groupId, botIds, false);
    }

    private LiveData<Resource<Void>> updateGroupRobots(
            String groupId, List<String> botIds, boolean add) {
        return new NetworkOnlyResource<Void, Result<Void>>() {
            @NonNull
            @Override
            protected LiveData<Result<Void>> createCall() {
                HashMap<String, Object> params = new HashMap<>();
                params.put("groupId", groupId);
                params.put("botIds", botIds);
                RequestBody body = RetrofitUtil.createJsonRequest(params);
                return add
                        ? service.addGroupRobots(groupId, body)
                        : service.removeGroupRobots(groupId, body);
            }
        }.asLiveData();
    }
}
