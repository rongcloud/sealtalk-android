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
import java.util.ArrayList;
import java.util.List;

/**
 * 举报二级分类页面
 *
 * @author rongcloud
 */
public class ReportSubCategoryActivity extends RongBaseActivity {

    private static final String CATEGORY_NAME = "categoryName";
    private static final String SUB_CATEGORIES = "subCategories";

    public static Intent newIntent(
            Context context,
            int conversationType,
            String targetId,
            String categoryName,
            ArrayList<String> subCategoryNames) {
        Intent intent = new Intent(context, ReportSubCategoryActivity.class);
        intent.putExtra(CATEGORY_NAME, categoryName);
        intent.putExtra(SUB_CATEGORIES, subCategoryNames);
        intent.putExtra(IntentExtra.SERIA_CONVERSATION_TYPE, conversationType);
        intent.putExtra(IntentExtra.STR_TARGET_ID, targetId);
        return intent;
    }

    private BroadcastReceiver closeActivitiesReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    finish(); // 关闭当前 Activity
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_sub_category);
        if (mTitleBar != null) {
            mTitleBar.setTitle(getString(R.string.report_title));
            mTitleBar.setRightVisible(false);
        }

        // 获取传递过来的分类数据
        List<String> subCategories = getIntent().getStringArrayListExtra(SUB_CATEGORIES);
        String categoryName = getIntent().getStringExtra(CATEGORY_NAME);

        // 显示二级分类列表
        ListView listView = findViewById(R.id.subCategoryListView);
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this, R.layout.list_item_category, R.id.categoryTextView, subCategories);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(
                (parent, view, position, id) -> {
                    String selectedSubCategory = subCategories.get(position);
                    submitReport(categoryName, selectedSubCategory);
                });

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

    private void submitReport(String categoryName, String subCategoryName) {
        // 在这里处理举报提交逻辑
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
