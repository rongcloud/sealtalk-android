package cn.rongcloud.im.openclaw.create;

import android.app.Application;
import android.net.Uri;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.openclaw.model.OpenClawRobotTokenResult;
import cn.rongcloud.im.openclaw.repository.OpenClawRobotRepository;
import cn.rongcloud.im.task.UserTask;

public class OpenClawCreateViewModel extends AndroidViewModel {
    private static final String DEFAULT_PORTRAIT_URI =
            "https://static.rongcloud.cn/avatar/claw.png";
    private final OpenClawRobotRepository repository;
    private final UserTask userTask;

    public OpenClawCreateViewModel(@NonNull Application application) {
        super(application);
        repository = new OpenClawRobotRepository(application);
        userTask = new UserTask(application);
    }

    public LiveData<Resource<String>> uploadPortrait(Uri imageUri) {
        return userTask.uploadPortraitImage(imageUri);
    }

    public LiveData<Resource<OpenClawRobotTokenResult>> createRobot(
            String name, String portraitUri) {
        return repository.createRobot(
                name, TextUtils.isEmpty(portraitUri) ? DEFAULT_PORTRAIT_URI : portraitUri);
    }
}
