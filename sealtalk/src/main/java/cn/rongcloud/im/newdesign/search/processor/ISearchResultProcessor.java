package cn.rongcloud.im.newdesign.search.processor;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import cn.rongcloud.im.newdesign.search.SearchViewModel;
import cn.rongcloud.im.ui.adapter.models.SearchModel;
import java.util.List;

/**
 * 搜索结果处理器接口
 *
 * <p>每种搜索类型（好友、群组、会话、消息）都有一个对应的实现类
 *
 * <p>负责观察 ViewModel、数据转换、生成展示 Section
 *
 * @author rongcloud
 * @since 5.12.2
 */
public interface ISearchResultProcessor {

    /**
     * 获取搜索类型
     *
     * @return 搜索类型（SearchType.FRIEND、SearchType.GROUP 等）
     */
    int getSearchType();

    /**
     * 初始化 Processor，开始观察 ViewModel
     *
     * @param viewModel SearchViewModel
     * @param callback 结果变更回调
     */
    void initialize(@NonNull SearchViewModel viewModel, @NonNull OnResultChangedCallback callback);

    /**
     * 设置 LifecycleOwner（用于观察 LiveData）
     *
     * @param owner LifecycleOwner
     */
    void setLifecycleOwner(@NonNull LifecycleOwner owner);

    /**
     * 生成展示的 SearchModel 列表
     *
     * @param showAll 是否显示全部结果（true：全部显示，false：最多3条+查看更多）
     * @return 展示的 SearchModel 列表
     */
    @NonNull
    List<SearchModel> generateDisplayModels(boolean showAll);

    /**
     * 检查是否有搜索结果
     *
     * @return true：有结果，false：无结果
     */
    boolean hasResults();

    /** 清除搜索结果 */
    void clearResults();

    /**
     * 获取当前搜索关键字
     *
     * @return 搜索关键字
     */
    String getCurrentQuery();

    /** 销毁 Processor，释放资源 */
    void destroy();

    /** 结果变更回调 */
    interface OnResultChangedCallback {
        /** 当搜索结果发生变化时调用 */
        void onResultChanged();
    }
}
