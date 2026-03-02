package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import cn.rongcloud.im.R;
import cn.rongcloud.im.task.AppTask;
import cn.rongcloud.im.utils.DataCenter;

/**
 * 数据中心选择页面
 *
 * @author gusd @Date 2022/03/29
 */
public class SelectDataCenterActivity extends TitleBaseActivity {
    private static final String TAG = "SelectDataCenterActivity";

    private DataCenterAdapter adapter;
    private String currentDataCenterCode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_data_center);
        getTitleBar().setTitle(R.string.data_center);
        initViews();
    }

    private void initViews() {
        // 获取当前选中的数据中心
        AppTask appTask = new AppTask(getApplicationContext());
        DataCenter currentDataCenter = appTask.getCurrentDataCenter();
        currentDataCenterCode = currentDataCenter != null ? currentDataCenter.getCode() : null;

        RecyclerView dataCenterList = findViewById(R.id.rv_data_center_list);
        adapter = new DataCenterAdapter();
        dataCenterList.setAdapter(adapter);
    }

    /** 数据中心列表适配器 */
    private class DataCenterAdapter extends RecyclerView.Adapter<DataCenterViewHolder> {

        @NonNull
        @Override
        public DataCenterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view =
                    LayoutInflater.from(SelectDataCenterActivity.this)
                            .inflate(R.layout.layout_data_center_item, parent, false);
            return new DataCenterViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DataCenterViewHolder holder, int position) {
            DataCenter dataCenter =
                    DataCenter.getFilteredDataCenterList(SelectDataCenterActivity.this)
                            .get(position);

            // 设置数据中心名称
            holder.dataCenter.setText(dataCenter.getNameId());

            // 根据当前选中状态显示/隐藏对勾图标
            boolean isSelected = dataCenter.getCode().equals(currentDataCenterCode);
            holder.checkIcon.setVisibility(isSelected ? View.VISIBLE : View.GONE);

            // 最后一项隐藏分割线
            boolean isLastItem = position == getItemCount() - 1;
            holder.divider.setVisibility(isLastItem ? View.GONE : View.VISIBLE);

            // 设置点击事件
            holder.itemView.setOnClickListener(
                    v -> {
                        // 返回选中的数据中心代码
                        Intent intent = new Intent();
                        intent.putExtra("code", dataCenter.getCode());
                        setResult(AppCompatActivity.RESULT_OK, intent);
                        finish();
                    });
        }

        @Override
        public int getItemCount() {
            return DataCenter.getFilteredDataCenterList(SelectDataCenterActivity.this).size();
        }
    }

    /** 数据中心列表项ViewHolder */
    private static class DataCenterViewHolder extends RecyclerView.ViewHolder {
        public final TextView dataCenter;
        public final ImageView checkIcon;
        public final View divider;

        public DataCenterViewHolder(@NonNull View itemView) {
            super(itemView);
            dataCenter = itemView.findViewById(R.id.item_data_center);
            checkIcon = itemView.findViewById(R.id.iv_check_icon);
            divider = itemView.findViewById(R.id.divider);
        }
    }
}
