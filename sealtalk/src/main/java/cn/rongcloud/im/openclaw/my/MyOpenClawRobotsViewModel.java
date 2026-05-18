package cn.rongcloud.im.openclaw.my;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.openclaw.model.OpenClawRobotInfo;
import cn.rongcloud.im.openclaw.repository.OpenClawRobotRepository;
import java.util.List;

public class MyOpenClawRobotsViewModel extends AndroidViewModel {
    private final OpenClawRobotRepository repository;

    public MyOpenClawRobotsViewModel(@NonNull Application application) {
        super(application);
        repository = new OpenClawRobotRepository(application);
    }

    public LiveData<Resource<List<OpenClawRobotInfo>>> getMyRobots() {
        return repository.getMyRobots();
    }
}
