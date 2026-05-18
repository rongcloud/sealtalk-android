package cn.rongcloud.im.openclaw.detail;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.openclaw.model.OpenClawRobotTokenResult;
import cn.rongcloud.im.openclaw.repository.OpenClawRobotRepository;

public class OpenClawDetailViewModel extends AndroidViewModel {
    private final OpenClawRobotRepository repository;

    public OpenClawDetailViewModel(@NonNull Application application) {
        super(application);
        repository = new OpenClawRobotRepository(application);
    }

    public LiveData<Resource<OpenClawRobotTokenResult>> getRobotInfo(String botId) {
        return repository.getRobotInfo(botId);
    }

    public LiveData<Resource<OpenClawRobotTokenResult>> refreshToken(String botId) {
        return repository.refreshToken(botId);
    }
}
