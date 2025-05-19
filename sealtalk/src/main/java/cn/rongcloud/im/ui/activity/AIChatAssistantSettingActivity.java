package cn.rongcloud.im.ui.activity;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.rongcloud.im.R;
import cn.rongcloud.im.model.AIAssistantType;
import cn.rongcloud.im.ui.BaseActivity;
import cn.rongcloud.im.ui.widget.switchbutton.SwitchButton;
import cn.rongcloud.im.viewmodel.AIChatAssistantSettingViewModel;
import java.util.ArrayList;
import java.util.List;

/** AI聊天助手设置页面 */
public class AIChatAssistantSettingActivity extends BaseActivity {

    private RecyclerView recyclerViewStyles;
    private StyleAdapter styleAdapter;
    private SwitchButton switchAccessChatHistory;
    private SwitchButton switchFunctionEnabled;

    private AIChatAssistantSettingViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chat_assistant_setting);

        // 初始化ViewModel
        viewModel = new ViewModelProvider(this).get(AIChatAssistantSettingViewModel.class);

        initView();
        initStyleList();
        initListener();
        observeViewModel();
    }

    private void initView() {
        recyclerViewStyles = findViewById(R.id.recycler_view_styles);
        switchAccessChatHistory = findViewById(R.id.switch_access_chat_history);
        switchFunctionEnabled = findViewById(R.id.switch_function_enabled);
    }

    private void initStyleList() {
        // 设置布局管理器为网格布局，每行显示3个项目
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        layoutManager.setSpanSizeLookup(
                new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        return 1; // 每个项目占用1个网格单元
                    }
                });
        recyclerViewStyles.setLayoutManager(layoutManager);

        // 添加项目间距装饰器
        recyclerViewStyles.addItemDecoration(
                new RecyclerView.ItemDecoration() {
                    @Override
                    public void getItemOffsets(
                            @NonNull Rect outRect,
                            @NonNull View view,
                            @NonNull RecyclerView parent,
                            @NonNull RecyclerView.State state) {
                        int position = parent.getChildAdapterPosition(view);
                        int spacing =
                                getResources()
                                        .getDimensionPixelSize(R.dimen.style_item_vertical_spacing);

                        // 为每个item添加上下间距
                        outRect.top = spacing / 2;
                        outRect.bottom = spacing / 2;
                    }
                });

        // 初始化适配器
        List<AIAssistantType> assistantTypes = viewModel.getAssistantTypes().getValue();
        styleAdapter =
                new StyleAdapter(assistantTypes != null ? assistantTypes : new ArrayList<>());
        recyclerViewStyles.setAdapter(styleAdapter);

        // 从ViewModel获取助手类型列表
        viewModel
                .getAssistantTypes()
                .observe(
                        this,
                        types -> {
                            if (types != null && !types.isEmpty()) {
                                styleAdapter.setStyleItems(types);
                            }
                        });

        // 默认选中存储的风格或第一个
        String savedStyle = viewModel.getChatStyle().getValue();
        if (savedStyle != null) {
            styleAdapter.updateSelectedStyle(savedStyle);
        } else {
            // 如果没有存储的风格，使用默认的第一个风格
            String defaultStyle = getDefaultStyleFromViewModel();
            styleAdapter.updateSelectedStyle(defaultStyle);
            viewModel.setChatStyle(defaultStyle);
        }
    }

    // 从ViewModel获取默认风格
    private String getDefaultStyleFromViewModel() {
        List<AIAssistantType> types = viewModel.getAssistantTypes().getValue();
        if (types != null && !types.isEmpty()) {
            return types.get(0).getStyleCode();
        }
        return "";
    }

    private void initListener() {
        // 开关状态变化监听
        switchAccessChatHistory.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    viewModel.setAccessChatHistory(isChecked);
                });

        switchFunctionEnabled.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    viewModel.setFunctionEnabled(isChecked);
                });
    }

    private void observeViewModel() {
        // 观察聊天风格设置
        viewModel
                .getChatStyle()
                .observe(
                        this,
                        style -> {
                            if (style != null) {
                                styleAdapter.updateSelectedStyle(style);
                            } else {
                                // 如果返回空值，则默认选中第一个
                                String defaultStyle = getDefaultStyleFromViewModel();
                                styleAdapter.updateSelectedStyle(defaultStyle);
                                viewModel.setChatStyle(defaultStyle);
                            }
                        });

        // 观察聊天记录访问权限设置
        viewModel
                .getAccessChatHistory()
                .observe(
                        this,
                        allow -> {
                            switchAccessChatHistory.setCheckedImmediately(allow);
                        });

        // 观察功能开关设置
        viewModel
                .getFunctionEnabled()
                .observe(
                        this,
                        enabled -> {
                            switchFunctionEnabled.setCheckedImmediately(enabled);
                        });

        // 观察agentId的变化
        viewModel
                .getAgentId()
                .observe(
                        this,
                        agentId -> {
                            // 可以在这里做一些与agentId相关的操作
                            // 例如日志记录或其他处理
                        });
    }

    // 风格列表适配器
    class StyleAdapter extends RecyclerView.Adapter<StyleAdapter.StyleViewHolder> {

        private List<AIAssistantType> styleItems;
        private String selectedStyle;

        StyleAdapter(List<AIAssistantType> styleItems) {
            this.styleItems = styleItems;
        }

        void setStyleItems(List<AIAssistantType> items) {
            this.styleItems = items;
            notifyDataSetChanged();
        }

        void updateSelectedStyle(String selectedStyle) {
            this.selectedStyle = selectedStyle;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public StyleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view =
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_chat_style, parent, false);
            return new StyleViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull StyleViewHolder holder, int position) {
            AIAssistantType item = styleItems.get(position);
            holder.tvStyle.setText(item.getStyleName());

            boolean isSelected = item.getStyleCode().equals(selectedStyle);
            if (isSelected) {
                holder.frameLayout.setBackgroundResource(R.drawable.bg_style_card_selected);
                holder.tvStyle.setTextColor(getResources().getColor(android.R.color.white));
            } else {
                holder.frameLayout.setBackgroundResource(R.drawable.bg_style_card_unselected);
                holder.tvStyle.setTextColor(getResources().getColor(R.color.text_gray));
            }

            holder.frameLayout.setOnClickListener(
                    v -> {
                        viewModel.setChatStyle(item.getStyleCode());
                    });
        }

        @Override
        public int getItemCount() {
            return styleItems.size();
        }

        class StyleViewHolder extends RecyclerView.ViewHolder {
            FrameLayout frameLayout;
            TextView tvStyle;

            StyleViewHolder(@NonNull View itemView) {
                super(itemView);
                frameLayout = (FrameLayout) itemView;
                tvStyle = itemView.findViewById(R.id.tv_style);
            }
        }
    }
}
