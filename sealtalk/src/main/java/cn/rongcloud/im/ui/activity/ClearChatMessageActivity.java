package cn.rongcloud.im.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.BaseActivity;
import cn.rongcloud.im.ui.adapter.SelectableConversationAdapter;
import cn.rongcloud.im.ui.adapter.models.CheckableContactModel;
import cn.rongcloud.im.ui.dialog.CommonDialog;
import cn.rongcloud.im.utils.ToastUtils;
import cn.rongcloud.im.utils.log.SLog;
import cn.rongcloud.im.viewmodel.SelectConversationViewModel;
import io.rong.imkit.usermanage.component.CommonListComponent;

public class ClearChatMessageActivity extends BaseActivity implements View.OnClickListener {

    private CommonListComponent commonListComponent;
    private SelectableConversationAdapter adapter;
    private SelectConversationViewModel selectConversationViewModel;
    private TextView tvRemove;
    private TextView tvCount;
    private LinearLayout llSelectAll;
    private LinearLayout llDelete;
    private CheckBox ckSelectAll;
    private int currentChatMessageCount;
    private int currentSelectedCount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clear_chat_message);
        initView();
        initViewModel();
    }

    private void initView() {
        commonListComponent = findViewById(R.id.common_list_component);
        adapter = new SelectableConversationAdapter();
        adapter.setOnItemClickListener(this::onConversationItemClick);
        commonListComponent.setAdapter(adapter);
        tvRemove = findViewById(R.id.tv_remove);
        tvCount = findViewById(R.id.tv_count);
        llDelete = findViewById(R.id.ll_delete);
        llDelete.setOnClickListener(this);
        llSelectAll = findViewById(R.id.ll_select_all);
        llSelectAll.setOnClickListener(this);
        ckSelectAll = findViewById(R.id.cb_select_all);
    }

    private void initViewModel() {
        selectConversationViewModel =
                ViewModelProviders.of(this).get(SelectConversationViewModel.class);
        selectConversationViewModel.loadConversation();

        selectConversationViewModel
                .getConersationLiveData()
                .observe(
                        this,
                        checkableContactModels -> {
                            SLog.i(
                                    "ClearChatMessage",
                                    "checkableContactModels,change**"
                                            + checkableContactModels.size()
                                            + "***"
                                            + currentChatMessageCount);
                            adapter.setData(checkableContactModels);
                            // 记录消息条目数量，判断是否被删除了
                            if (currentChatMessageCount > checkableContactModels.size()) {
                                ToastUtils.showToast(
                                        R.string.seal_clear_chat_message_delete_success);
                            }
                            currentChatMessageCount = checkableContactModels.size();
                        });

        selectConversationViewModel
                .getSelectedCount()
                .observe(
                        this,
                        integer -> {
                            SLog.i("ClearChatMessage", "Selected count: " + integer);
                            currentSelectedCount = integer;
                            updateDeleteButtonStatus(integer);
                        });
    }

    /**
     * 更新删除按钮状态
     *
     * @param count 选中数量
     */
    private void updateDeleteButtonStatus(int count) {
        boolean hasSelection = count > 0;
        llDelete.setClickable(hasSelection);
        llDelete.setEnabled(hasSelection);

        //        // 获取主题颜色
        //        android.content.res.TypedArray ta =
        //                getTheme()
        //                        .obtainStyledAttributes(
        //                                new int[] {
        //                                    io.rong.imkit.R.attr.rc_hint_color,
        //                                    io.rong.imkit.R.attr.rc_disabled_color
        //                                });
        //        int hintColor = ta.getColor(0, getResources().getColor(R.color.read_ff));
        //        int disabledColor = ta.getColor(1,
        // getResources().getColor(android.R.color.darker_gray));
        //        ta.recycle();
        //
        //        // 设置删除按钮颜色
        //        tvRemove.setTextColor(hasSelection ? hintColor : disabledColor);

        // 显示/隐藏数量并设置文本
        if (hasSelection) {
            tvCount.setVisibility(View.VISIBLE);
            tvCount.setText("(" + count + ")");
            //            tvCount.setTextColor(hintColor);
        } else {
            tvCount.setVisibility(View.GONE);
        }

        // 判断全选状态
        ckSelectAll.setChecked(hasSelection && count == currentChatMessageCount);
    }

    /**
     * 会话项点击事件
     *
     * @param conversation 会话模型
     */
    private void onConversationItemClick(CheckableContactModel conversation) {
        selectConversationViewModel.onItemClicked(conversation);
        adapter.notifyDataSetChanged();
    }

    /** 清理消息 */
    private void clearMessage() {
        selectConversationViewModel.clearMessage();
    }

    /** 显示删除确认对话框 */
    private void showDeleteConfirmDialog() {
        CommonDialog.Builder builder = new CommonDialog.Builder();
        // 显示将要删除的会话数量
        String message;
        if (currentSelectedCount > 0) {
            message =
                    getString(
                            R.string.seal_clear_chat_message_delete_confirm_format,
                            currentSelectedCount);
        } else {
            message = getString(R.string.seal_clear_chat_message_delete_dialog);
        }
        builder.setContentMessage(message);
        builder.setDialogButtonClickListener(
                new CommonDialog.OnDialogButtonClickListener() {
                    @Override
                    public void onPositiveClick(View v, Bundle bundle) {
                        clearMessage();
                    }

                    @Override
                    public void onNegativeClick(View v, Bundle bundle) {}
                });
        CommonDialog deleteDialog = builder.build();
        deleteDialog.show(getSupportFragmentManager().beginTransaction(), "DeleteConfirmDialog");
    }

    private void selectAll() {
        if (!ckSelectAll.isChecked()) {
            selectConversationViewModel.selectAllCheck();
            ckSelectAll.setChecked(true);
        } else {
            selectConversationViewModel.cancelAllCheck();
            ckSelectAll.setChecked(false);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ll_delete) {
            if (currentSelectedCount > 0) {
                showDeleteConfirmDialog();
            }
        } else if (id == R.id.ll_select_all) {
            selectAll();
        }
    }
}
