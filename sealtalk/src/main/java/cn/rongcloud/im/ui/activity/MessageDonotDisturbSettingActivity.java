package cn.rongcloud.im.ui.activity;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import cn.rongcloud.im.R;
import cn.rongcloud.im.model.QuietHours;
import cn.rongcloud.im.viewmodel.NewMessageViewModel;
import io.rong.imkit.widget.SettingItemView;

/** 消息免打扰设置 */
public class MessageDonotDisturbSettingActivity extends TitleBaseActivity
        implements View.OnClickListener {
    private SettingItemView donotDistrabSiv;
    private SettingItemView startTimeSiv;
    private SettingItemView endTimeSiv;
    private LinearLayout timeContainer;
    private NewMessageViewModel newMessageViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_donot_disturb_setting);

        initView();
        initViewModel();
    }

    /** 初始化布局 */
    private void initView() {
        getTitleBar().setTitle(R.string.seal_new_message_donot_disturb);

        timeContainer = findViewById(R.id.ll_time_container);
        donotDistrabSiv = findViewById(R.id.siv_donot_distrab);
        startTimeSiv = findViewById(R.id.siv_start_time);
        endTimeSiv = findViewById(R.id.siv_end_time);

        donotDistrabSiv.setSwitchCheckListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            timeContainer.setVisibility(View.VISIBLE);
                            int spanMinutes = 0;
                            String startTime = "";
                            String endTime = "";
                            QuietHours value = getQuietHours();
                            if (value != null) {
                                startTime = value.getStartTimeFormat();
                                if (TextUtils.isEmpty(startTime)) {
                                    startTime = "23:59:59";
                                }

                                endTime = value.getEndTimeFormat();

                                if (TextUtils.isEmpty(endTime)) {
                                    endTime = "07:00:00";
                                }

                                spanMinutes = value.spanMinutes;

                                if (spanMinutes <= 0) {
                                    spanMinutes = 7 * 60;
                                }
                            } else {
                                startTime = "23:59:59";
                                endTime = "07:00:00";
                                spanMinutes = 7 * 60;
                            }
                            startTimeSiv.setValue(startTime);
                            endTimeSiv.setValue(endTime);
                            setNotificationQuietHours(startTimeSiv.getValue(), spanMinutes);

                        } else {
                            timeContainer.setVisibility(View.GONE);
                            removeNotificationQuietHours();
                        }
                    }
                });

        startTimeSiv.setOnClickListener(this);
        endTimeSiv.setOnClickListener(this);
    }

    /** 初始化ViewModel */
    private void initViewModel() {
        newMessageViewModel = ViewModelProviders.of(this).get(NewMessageViewModel.class);
        newMessageViewModel
                .getDonotDistrabStatus()
                .observe(
                        this,
                        new Observer<QuietHours>() {
                            @Override
                            public void onChanged(QuietHours quietHours) {
                                if (quietHours != null) {
                                    donotDistrabSiv.setChecked(quietHours.isDonotDistrab);
                                }
                            }
                        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.siv_start_time) {
            showSelectStartTime();
        } else if (v.getId() == R.id.siv_end_time) {
            showSelectEndTime();
        }
    }

    /** 开始时间选择 */
    private void showSelectStartTime() {
        int hours = 0;
        int minutes = 0;
        QuietHours value = getQuietHours();
        if (value != null) {
            hours = QuietHours.getHours(value.getStartTime());
            minutes = QuietHours.getMinutes(value.getStartTime());
        }

        TimePickerDialog timePickerDialog =
                new TimePickerDialog(
                        this,
                        getTimePickerDialogTheme(),
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                                String startTime = QuietHours.getFormatTime(hourOfDay, minute);
                                startTimeSiv.setValue(startTime);

                                String endTimeValue = endTimeSiv.getValue();
                                QuietHours quietHours = getQuietHours();
                                int spanMinutes = quietHours.spanMinutes;
                                // 开始时间变化， 重写计算时间间隔
                                if (!TextUtils.isEmpty(endTimeValue)) {
                                    spanMinutes =
                                            QuietHours.getSpanMinutes(startTime, endTimeValue);
                                }
                                setNotificationQuietHours(startTime, spanMinutes);
                            }
                        },
                        hours,
                        minutes,
                        true);
        timePickerDialog.show();
    }

    /** 结束时间选择 */
    private void showSelectEndTime() {
        int hours = 0;
        int minutes = 0;
        QuietHours value = getQuietHours();
        if (value != null) {
            hours = QuietHours.getHours(value.getEndTime());
            minutes = QuietHours.getMinutes(value.getEndTime());
        }

        TimePickerDialog timePickerDialog =
                new TimePickerDialog(
                        this,
                        getTimePickerDialogTheme(),
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                                String endTime = QuietHours.getFormatTime(hourOfDay, minute);
                                endTimeSiv.setValue(endTime);
                                QuietHours quietHours = getQuietHours();
                                int spanMinutes =
                                        QuietHours.getSpanMinutes(
                                                quietHours.getStartTimeFormat(), endTime);
                                setNotificationQuietHours(
                                        quietHours.getStartTimeFormat(), spanMinutes);
                            }
                        },
                        hours,
                        minutes,
                        true);
        timePickerDialog.show();
    }

    /** 移除消息免打扰时间 */
    public void removeNotificationQuietHours() {
        if (newMessageViewModel != null) {
            newMessageViewModel.removeNotificationQuietHours();
        }
    }

    /**
     * 设置会话通知免打扰时间。
     *
     * @param startTime 起始时间 格式 HH:MM:SS。
     * @param spanMinutes 间隔分钟数大于 0 小于 1440。
     */
    public void setNotificationQuietHours(String startTime, int spanMinutes) {
        if (newMessageViewModel != null) {
            newMessageViewModel.setNotificationQuietHours(startTime, spanMinutes);
        }
    }

    /**
     * 获取设置时间
     *
     * @return
     */
    public QuietHours getQuietHours() {
        if (newMessageViewModel == null) {
            return null;
        }
        return newMessageViewModel.getDonotDistrabStatus().getValue();
    }

    /**
     * 根据当前深浅色模式获取合适的 TimePickerDialog 主题
     *
     * @return 主题资源 ID
     */
    private int getTimePickerDialogTheme() {
        int nightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightMode == Configuration.UI_MODE_NIGHT_YES) {
            return AlertDialog.THEME_HOLO_DARK;
        } else {
            return AlertDialog.THEME_HOLO_LIGHT;
        }
    }
}
