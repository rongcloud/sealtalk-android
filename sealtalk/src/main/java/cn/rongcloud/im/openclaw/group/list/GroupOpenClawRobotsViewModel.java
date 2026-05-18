package cn.rongcloud.im.openclaw.group.list;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.openclaw.model.OpenClawRobotInfo;
import cn.rongcloud.im.openclaw.repository.OpenClawRobotRepository;
import java.util.Collections;
import java.util.List;

public class GroupOpenClawRobotsViewModel extends AndroidViewModel {
    private final OpenClawRobotRepository repository;

    public GroupOpenClawRobotsViewModel(@NonNull Application application) {
        super(application);
        repository = new OpenClawRobotRepository(application);
    }

    public LiveData<Resource<List<OpenClawRobotInfo>>> getGroupRobots(String groupId) {
        return repository.getGroupRobots(groupId);
    }

    public LiveData<Resource<Void>> removeRobot(String groupId, String botId) {
        return repository.removeGroupRobots(groupId, Collections.singletonList(botId));
    }
}
