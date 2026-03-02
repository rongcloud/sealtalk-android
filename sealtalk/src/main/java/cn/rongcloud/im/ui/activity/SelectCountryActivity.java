package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import cn.rongcloud.im.R;
import cn.rongcloud.im.model.CountryInfo;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.ui.adapter.CountryAdapter;
import cn.rongcloud.im.ui.widget.SideBar;
import cn.rongcloud.im.viewmodel.CountryViewModel;
import io.rong.imkit.usermanage.component.SearchComponent;
import java.util.List;

public class SelectCountryActivity extends TitleBaseActivity {
    private static final String TAG = SelectCountryActivity.class.getSimpleName();
    public static final String RESULT_PARAMS_COUNTRY_INFO = "result_params_country_info";
    private ListView listView;
    private CountryAdapter adapter;
    private CountryViewModel countryViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity_select_country_new);
        initView();
        initViewModel();
        loadCountryDatas(null);
    }

    /** 初始化布局 */
    public void initView() {
        // 初始化视图
        SearchComponent searchComponent = findViewById(R.id.search_component);
        listView = findViewById(R.id.lv_select_country);
        SideBar sideBar = findViewById(R.id.sidrbar);
        getTitleBar().setTitle(R.string.seal_select_country_region);
        getTitleBar()
                .setOnBtnLeftClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                hideInputKeyboard();
                                finish();
                            }
                        });

        // 设置搜索提示
        searchComponent.setSearchHint(R.string.seal_search_country_hint);

        // 设置搜索监听
        searchComponent.setSearchQueryListener(
                new SearchComponent.OnSearchQueryListener() {
                    @Override
                    public void onSearch(String query) {
                        loadCountryDatas(query);
                    }
                });

        // 设置右侧触摸监听
        sideBar.setOnTouchingLetterChangedListener(
                new SideBar.OnTouchingLetterChangedListener() {
                    @Override
                    public void onTouchingLetterChanged(String s) {
                        // 该字母首次出现的位置
                        int position = adapter.getPositionForSection(s.charAt(0));
                        if (position != -1) {
                            listView.setSelection(position);
                        }
                    }
                });

        adapter = new CountryAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(
                            AdapterView<?> parent, View view, int position, long id) {
                        Object item = parent.getAdapter().getItem(position);
                        if (item != null && item instanceof CountryInfo) {
                            CountryInfo info = (CountryInfo) item;
                            Intent intent = new Intent();
                            intent.putExtra(RESULT_PARAMS_COUNTRY_INFO, info);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }
                });
    }

    /** ViewModel */
    private void initViewModel() {
        countryViewModel = ViewModelProviders.of(this).get(CountryViewModel.class);
        // 监听返回数据， 并展示在 Adapter 中
        countryViewModel
                .getFilterCountryList()
                .observe(
                        this,
                        new Observer<Resource<List<CountryInfo>>>() {
                            @Override
                            public void onChanged(Resource<List<CountryInfo>> listResource) {
                                if (listResource.status == Status.SUCCESS) {
                                    adapter.updateList(listResource.data);
                                }
                            }
                        });
    }

    /**
     * 请求加载数据信息，包括过滤信息
     *
     * @param str 过滤条件， 如果为空， 则加载全部
     */
    private void loadCountryDatas(String str) {
        countryViewModel.loadCountryDatas(str);
    }
}
