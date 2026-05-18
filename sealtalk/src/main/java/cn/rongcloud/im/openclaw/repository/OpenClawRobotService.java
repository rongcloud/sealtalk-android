package cn.rongcloud.im.openclaw.repository;

import androidx.lifecycle.LiveData;
import cn.rongcloud.im.model.Result;
import cn.rongcloud.im.net.SealTalkUrl;
import cn.rongcloud.im.openclaw.model.OpenClawRobotInfo;
import cn.rongcloud.im.openclaw.model.OpenClawRobotTokenResult;
import java.util.List;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface OpenClawRobotService {
    @POST(SealTalkUrl.OPENCLAW_BOT_CREATE)
    LiveData<Result<OpenClawRobotTokenResult>> createRobot(@Body RequestBody body);

    @POST(SealTalkUrl.OPENCLAW_BOT_REFRESH_TOKEN)
    LiveData<Result<OpenClawRobotTokenResult>> refreshToken(@Body RequestBody body);

    @GET(SealTalkUrl.OPENCLAW_USER_BOT)
    LiveData<Result<List<OpenClawRobotInfo>>> getMyRobots();

    @GET(SealTalkUrl.OPENCLAW_USER_BOT_DETAIL)
    LiveData<Result<OpenClawRobotTokenResult>> getRobotInfo(@Query("botId") String botId);

    @GET(SealTalkUrl.OPENCLAW_GROUP_BOT)
    LiveData<Result<List<OpenClawRobotInfo>>> getGroupRobots(@Query("groupId") String groupId);

    @POST(SealTalkUrl.OPENCLAW_GROUP_BOT_ADD)
    LiveData<Result<Void>> addGroupRobots(@Query("groupId") String groupId, @Body RequestBody body);

    @POST(SealTalkUrl.OPENCLAW_GROUP_BOT_REMOVE)
    LiveData<Result<Void>> removeGroupRobots(
            @Query("groupId") String groupId, @Body RequestBody body);
}
