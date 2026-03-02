package cn.rongcloud.im.newdesign.search.processor;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import cn.rongcloud.im.R;
import cn.rongcloud.im.newdesign.search.SearchViewModel;
import cn.rongcloud.im.ui.adapter.models.SearchModel;
import java.util.ArrayList;
import java.util.List;

/**
 * 搜索结果处理器基类
 *
 * <p>提供通用实现，子类只需关注数据转换和展示逻辑
 *
 * @author rongcloud
 * @since 5.12.2
 */
public abstract class BaseSearchResultProcessor<T> implements ISearchResultProcessor {

    protected SearchViewModel viewModel;
    protected OnResultChangedCallback callback;
    protected LifecycleOwner lifecycleOwner;
    protected String currentQuery = "";

    /** 当前搜索结果的原始数据 */
    protected final List<T> rawResults = new ArrayList<>();

    @Override
    public void initialize(
            @NonNull SearchViewModel viewModel, @NonNull OnResultChangedCallback callback) {
        this.viewModel = viewModel;
        this.callback = callback;
    }

    /**
     * 设置 LifecycleOwner（用于观察 LiveData）
     *
     * @param owner LifecycleOwner
     */
    public void setLifecycleOwner(@NonNull LifecycleOwner owner) {
        this.lifecycleOwner = owner;
        observeLiveData();
    }

    /** 观察 LiveData（子类实现） */
    protected abstract void observeLiveData();

    /**
     * 将原始数据转换为 SearchModel
     *
     * @param data 原始数据
     * @param query 搜索关键字
     * @return SearchModel
     */
    protected abstract SearchModel convertToSearchModel(T data, String query);

    /**
     * 创建标题 Model
     *
     * @return 标题 SearchModel
     */
    protected abstract SearchModel createTitleModel();

    /**
     * 创建"查看更多" Model
     *
     * @return "查看更多" SearchModel
     */
    protected abstract SearchModel createShowMoreModel();

    /**
     * 创建分隔线 Model
     *
     * @return 分隔线 SearchModel
     */
    protected SearchModel createDividerModel() {
        return new SearchModel(null, R.layout.search_fragment_recycler_div_layout);
    }

    @Override
    @NonNull
    public List<SearchModel> generateDisplayModels(boolean showAll) {
        List<SearchModel> result = new ArrayList<>();

        if (rawResults.isEmpty()) {
            return result;
        }

        // 1. 添加标题
        result.add(createTitleModel());

        // 2. 添加结果
        if (showAll) {
            // 显示全部结果
            for (T data : rawResults) {
                result.add(convertToSearchModel(data, currentQuery));
            }
        } else {
            // 最多显示 3 条
            int count = Math.min(3, rawResults.size());
            for (int i = 0; i < count; i++) {
                result.add(convertToSearchModel(rawResults.get(i), currentQuery));
            }

            // 如果超过 3 条，添加"查看更多"
            if (rawResults.size() > 3) {
                result.add(createShowMoreModel());
            }
        }

        // 3. 添加分隔线
        result.add(createDividerModel());

        return result;
    }

    @Override
    public boolean hasResults() {
        return !rawResults.isEmpty();
    }

    @Override
    public void clearResults() {
        rawResults.clear();
    }

    @Override
    public String getCurrentQuery() {
        return currentQuery;
    }

    @Override
    public void destroy() {
        rawResults.clear();
        viewModel = null;
        callback = null;
        lifecycleOwner = null;
    }

    /**
     * 更新搜索结果
     *
     * @param results 新的搜索结果
     */
    protected void updateResults(List<T> results) {
        rawResults.clear();
        if (results != null && !results.isEmpty()) {
            rawResults.addAll(results);
            currentQuery = viewModel.getCurrentQuery();
        }
        if (callback != null) {
            callback.onResultChanged();
        }
    }
}
