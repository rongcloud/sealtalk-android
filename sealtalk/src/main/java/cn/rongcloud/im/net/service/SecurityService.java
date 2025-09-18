package cn.rongcloud.im.net.service;

import androidx.lifecycle.LiveData;
import cn.rongcloud.im.model.Result;
import cn.rongcloud.im.model.SecurityStatusResult;
import cn.rongcloud.im.model.SecurityVerifyResult;
import cn.rongcloud.im.net.SealTalkUrl;
import java.util.Map;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

public interface SecurityService {

    /**
     * 查询数美开关状态
     *
     * @return
     */
    @GET(SealTalkUrl.SECURITY_STATUS)
    LiveData<Result<SecurityStatusResult>> querySecurityStatus();

    /**
     * 数美黑产验证
     *
     * @return
     */
    @GET(SealTalkUrl.SECURITY_VERIFY)
    LiveData<Result<SecurityVerifyResult>> doSecurityVerify(
            @QueryMap(encoded = true) Map<String, String> queryMap);

    /**
     * 举报用户
     *
     * @param body 举报用户的body
     * @return 举报结果
     */
    @POST(SealTalkUrl.REPORT_CUSTOMER)
    LiveData<Result<Void>> reportUser(@Body RequestBody body);
}
