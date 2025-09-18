package cn.rongcloud.im.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import io.rong.imkit.activity.RongBaseActivity;
import io.rong.imlib.model.Conversation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 举报页面
 *
 * @author rongcloud
 */
public class ReportCategoryActivity extends RongBaseActivity {
    private Map<String, List<String>> reportCategories;

    public static Intent newIntent(
            Context context, Conversation.ConversationType conversationType, String targetId) {
        Intent intent = new Intent(context, ReportCategoryActivity.class);
        intent.putExtra(IntentExtra.SERIA_CONVERSATION_TYPE, conversationType.getValue());
        intent.putExtra(IntentExtra.STR_TARGET_ID, targetId);
        return intent;
    }

    private BroadcastReceiver closeActivitiesReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    finish();
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_category);
        if (mTitleBar != null) {
            mTitleBar.setTitle(getString(R.string.report_title));
            mTitleBar.setRightVisible(false);
        }
        // 初始化数据
        initData();
        // 显示一级分类列表
        displayCategoryList();

        // 注册广播接收器
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(closeActivitiesReceiver, new IntentFilter("CLOSE_ALL"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注销广播接收器
        LocalBroadcastManager.getInstance(this).unregisterReceiver(closeActivitiesReceiver);
    }

    private void initData() {
        reportCategories = new LinkedHashMap<>();

        // 添加一级分类和对应的二级分类
        reportCategories.put(
                getString(R.string.report_category_pornographic_info),
                Arrays.asList(
                        getString(R.string.report_subcategory_pornographic),
                        getString(R.string.report_subcategory_illegal_crime),
                        getString(R.string.report_subcategory_gambling),
                        getString(R.string.report_subcategory_political_rumors),
                        getString(R.string.report_subcategory_violence_bloodshed),
                        getString(R.string.report_subcategory_other)));

        reportCategories.put(
                getString(R.string.report_category_fraudulent_activities),
                Arrays.asList(
                        getString(R.string.report_subcategory_financial_fraud),
                        getString(R.string.report_subcategory_online_job_scam),
                        getString(R.string.report_subcategory_online_dating_scam),
                        getString(R.string.report_subcategory_fake_investment_scam),
                        getString(R.string.report_subcategory_gambling_scam),
                        getString(R.string.report_subcategory_other_fraud)));

        reportCategories.put(getString(R.string.report_category_minor_infringement), null);
        reportCategories.put(getString(R.string.report_category_spam_advertising), null);
        reportCategories.put(getString(R.string.report_category_cyber_violence), null);
        reportCategories.put(getString(R.string.report_category_other_violations), null);
    }

    private void displayCategoryList() {
        ListView listView = findViewById(R.id.categoryListView);
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        R.layout.list_item_category,
                        R.id.categoryTextView,
                        new ArrayList<>(reportCategories.keySet()));
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(
                (parent, view, position, id) -> {
                    String selectedCategory = adapter.getItem(position);
                    List<String> subCategories = reportCategories.get(selectedCategory);

                    if (subCategories != null && !subCategories.isEmpty()) {
                        // 显示二级分类
                        displaySubCategoryList(selectedCategory, subCategories);
                    } else {
                        // 直接进入举报提交界面
                        submitReport(selectedCategory, null);
                    }
                });
    }

    private void displaySubCategoryList(String categoryName, List<String> subCategories) {
        Intent intent =
                ReportSubCategoryActivity.newIntent(
                        this,
                        getIntentExtraConversationType(),
                        getIntentExtraTargetId(),
                        categoryName,
                        new ArrayList<>(subCategories));
        startActivity(intent);
    }

    private void submitReport(String categoryName, String subCategoryName) {
        Intent intent =
                ReportDetailActivity.newIntent(
                        this,
                        getIntentExtraConversationType(),
                        getIntentExtraTargetId(),
                        categoryName,
                        subCategoryName);
        startActivity(intent);
    }

    private int getIntentExtraConversationType() {
        return getIntent().getIntExtra(IntentExtra.SERIA_CONVERSATION_TYPE, 1);
    }

    private String getIntentExtraTargetId() {
        return getIntent().getStringExtra(IntentExtra.STR_TARGET_ID);
    }
}
