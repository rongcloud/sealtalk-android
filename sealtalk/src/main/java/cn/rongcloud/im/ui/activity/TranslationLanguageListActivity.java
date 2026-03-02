package cn.rongcloud.im.ui.activity;

import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import cn.rongcloud.im.R;
import cn.rongcloud.im.task.AppTask;
import cn.rongcloud.im.ui.BaseActivity;
import io.rong.imkit.config.RongConfigCenter;
import io.rong.imkit.usermanage.component.HeadComponent;

/**
 * @author gusd
 */
public class TranslationLanguageListActivity extends BaseActivity {
    private static final String TAG = "TranslationLanguageListActivity";
    private HeadComponent headComponent;
    private String selectedLanguageCode; // 当前选中的语言代码（临时选择，未保存）
    private String currentLanguageCode; // 当前已保存的语言代码
    private LanguageAdapter languageAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translation_language_list);

        initView();
        initData();
    }

    private void initView() {
        // 初始化 HeadComponent
        headComponent = findViewById(R.id.head_component);

        // 获取当前已保存的语言
        String type = getIntent().getStringExtra("type");
        if ("src".equals(type)) {
            currentLanguageCode = RongConfigCenter.featureConfig().rc_translation_src_language;
        } else {
            currentLanguageCode = RongConfigCenter.featureConfig().rc_translation_target_language;
        }
        // 初始化时，选中的语言等于当前语言
        selectedLanguageCode = currentLanguageCode;

        // 添加保存按钮点击事件
        headComponent.setRightClickListener(
                v -> {
                    // 点击保存按钮时才真正保存
                    if (selectedLanguageCode != null) {
                        saveLanguage();
                        finish();
                    }
                });
    }

    private void initData() {
        RecyclerView rcLanguage = findViewById(R.id.rv_language_list);

        languageAdapter = new LanguageAdapter();
        rcLanguage.setAdapter(languageAdapter);
    }

    /** 保存选中的语言 */
    private void saveLanguage() {
        final AppTask appTask = new AppTask(this);
        final String type = getIntent().getStringExtra("type");

        if ("src".equals(type)) {
            appTask.setTranslationSrcLanguage(selectedLanguageCode);
            RongConfigCenter.featureConfig().rc_translation_src_language = selectedLanguageCode;
        } else {
            appTask.setTranslationTargetLanguage(selectedLanguageCode);
            RongConfigCenter.featureConfig().rc_translation_target_language = selectedLanguageCode;
        }
    }

    /** Adapter 用于语言列表 */
    private class LanguageAdapter extends RecyclerView.Adapter<LanguageViewHolder> {
        @NonNull
        @Override
        public LanguageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view =
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_translation_language, parent, false);
            return new LanguageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull LanguageViewHolder holder, int position) {
            final Pair<String, String> languageItem =
                    TranslationSettingActivity.LANGUAGE_LIST.get(position);

            holder.tvLanguage.setText(languageItem.second);

            // 显示选中状态
            boolean isSelected = languageItem.first.equals(selectedLanguageCode);
            holder.ivSelected.setVisibility(isSelected ? View.VISIBLE : View.GONE);

            // 隐藏第一个item的分割线
            holder.divider.setVisibility(position == 0 ? View.GONE : View.VISIBLE);

            holder.itemView.setOnClickListener(
                    v -> {
                        // 只更新选中状态，不保存
                        selectedLanguageCode = languageItem.first;
                        notifyDataSetChanged(); // 刷新列表以显示新的选中状态
                    });
        }

        @Override
        public int getItemCount() {
            return TranslationSettingActivity.LANGUAGE_LIST.size();
        }
    }

    /** ViewHolder 用于语言列表项 */
    private static class LanguageViewHolder extends RecyclerView.ViewHolder {
        TextView tvLanguage;
        ImageView ivSelected;
        View divider;

        LanguageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLanguage = itemView.findViewById(R.id.tv_translation_language);
            ivSelected = itemView.findViewById(R.id.iv_selected_indicator);
            divider = itemView.findViewById(R.id.divider_line);
        }
    }
}
