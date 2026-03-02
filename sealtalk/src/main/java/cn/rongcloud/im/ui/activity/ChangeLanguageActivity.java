package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.BaseActivity;
import cn.rongcloud.im.ui.view.SettingItemView;
import cn.rongcloud.im.viewmodel.AppViewModel;
import io.rong.imkit.usermanage.component.HeadComponent;
import io.rong.imkit.utils.language.LangUtils;

public class ChangeLanguageActivity extends BaseActivity {

    private HeadComponent headComponent;
    private SettingItemView chineseSiv;
    private SettingItemView englishSiv;
    private SettingItemView arabSiv;
    private AppViewModel appViewModel;
    private LangUtils.RCLocale selectedLocale; // 记录当前选中的语言

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_language);
        initView();
        initViewModel();
    }

    /** 初始化布局 */
    private void initView() {
        // 初始化 HeadComponent
        headComponent = findViewById(R.id.head_component);

        // 添加保存按钮到标题栏右侧
        headComponent.setRightClickListener(
                v -> {
                    // 点击保存按钮时切换语言
                    if (selectedLocale != null) {
                        changeLanguage(selectedLocale);
                        backToSettingActivity();
                    }
                });

        chineseSiv = findViewById(R.id.siv_chinese);
        englishSiv = findViewById(R.id.siv_english);
        arabSiv = findViewById(R.id.siv_arab);

        chineseSiv.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 中文 - 只选中，不立即切换
                        chineseSiv.setSelected(true);
                        englishSiv.setSelected(false);
                        arabSiv.setSelected(false);
                        selectedLocale = LangUtils.RCLocale.LOCALE_CHINA;
                    }
                });
        englishSiv.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 英文 - 只选中，不立即切换
                        chineseSiv.setSelected(false);
                        englishSiv.setSelected(true);
                        arabSiv.setSelected(false);
                        selectedLocale = LangUtils.RCLocale.LOCALE_US;
                    }
                });
        arabSiv.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 阿拉伯语 - 只选中，不立即切换
                        chineseSiv.setSelected(false);
                        englishSiv.setSelected(false);
                        arabSiv.setSelected(true);
                        selectedLocale = LangUtils.RCLocale.LOCALE_ARAB;
                    }
                });
    }

    /** 初始化Viewmodel */
    private void initViewModel() {
        appViewModel = ViewModelProviders.of(this).get(AppViewModel.class);
        // 当前app 语言
        appViewModel
                .getLanguageLocal()
                .observe(
                        this,
                        new Observer<LangUtils.RCLocale>() {
                            @Override
                            public void onChanged(LangUtils.RCLocale rcLocale) {
                                // 初始化时记录当前语言
                                selectedLocale = rcLocale;

                                if (rcLocale == LangUtils.RCLocale.LOCALE_US) {
                                    chineseSiv.setSelected(false);
                                    englishSiv.setSelected(true);
                                    arabSiv.setSelected(false);
                                } else if (rcLocale == LangUtils.RCLocale.LOCALE_CHINA) {
                                    chineseSiv.setSelected(true);
                                    englishSiv.setSelected(false);
                                    arabSiv.setSelected(false);
                                } else if (rcLocale == LangUtils.RCLocale.LOCALE_ARAB) {
                                    chineseSiv.setSelected(false);
                                    englishSiv.setSelected(false);
                                    arabSiv.setSelected(true);
                                }
                            }
                        });
    }

    /**
     * 切换语言
     *
     * @param selectedLocale
     */
    private void changeLanguage(LangUtils.RCLocale selectedLocale) {
        if (appViewModel != null) {
            appViewModel.changeLanguage(selectedLocale);
        }
    }

    private void backToSettingActivity() {
        // 重启主页面以应用最新语言，并跳转到“我的”页
        Intent mainIntent = new Intent(ChangeLanguageActivity.this, MainActivity.class);
        mainIntent.putExtra(MainActivity.PARAMS_TAB_INDEX, MainActivity.ME);
        mainIntent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK); // 清空原任务栈，强制重建界面
        startActivity(mainIntent);
        finish();
    }
}
