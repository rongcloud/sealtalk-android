package cn.rongcloud.im.openclaw.group.select;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.openclaw.model.OpenClawRobotInfo;
import cn.rongcloud.im.openclaw.repository.OpenClawRobotRepository;
import java.util.Collections;
import java.util.List;

public class SelectOpenClawRobotViewModel extends AndroidViewModel {
    private final OpenClawRobotRepository repository;

    public SelectOpenClawRobotViewModel(@NonNull Application application) {
        super(application);
        repository = new OpenClawRobotRepository(application);
    }

    public LiveData<Resource<List<OpenClawRobotInfo>>> getMyRobots() {
        return repository.getMyRobots();
    }

    public LiveData<Resource<List<OpenClawRobotInfo>>> getGroupRobots(String groupId) {
        return repository.getGroupRobots(groupId);
    }

    public LiveData<Resource<Void>> addRobot(String groupId, String botId) {
        return addRobots(groupId, Collections.singletonList(botId));
    }

    public LiveData<Resource<Void>> addRobots(String groupId, List<String> botIds) {
        return repository.addGroupRobots(groupId, botIds);
    }
}
