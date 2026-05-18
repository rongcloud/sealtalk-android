package cn.rongcloud.im.openclaw.my;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import cn.rongcloud.im.R;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.openclaw.adapter.OpenClawRobotAdapter;
import cn.rongcloud.im.openclaw.component.OpenClawPageUtils;
import cn.rongcloud.im.openclaw.detail.OpenClawDetailActivity;
import cn.rongcloud.im.openclaw.model.OpenClawRobotInfo;
import cn.rongcloud.im.utils.ToastUtils;
import java.util.List;

public class MyOpenClawRobotsFragment extends Fragment {
    private OpenClawRobotAdapter adapter;
    private TextView emptyView;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        LinearLayout root = OpenClawPageUtils.verticalRoot(requireContext());
        OpenClawPageUtils.padding(root, 16, 8, 16, 0);

        EditText search = OpenClawPageUtils.searchInput(requireContext());
        root.addView(
                search,
                new LinearLayout.LayoutParams(-1, OpenClawPageUtils.dp(requireContext(), 36)));

        RecyclerView recyclerView = OpenClawPageUtils.recyclerView(requireContext());
        LinearLayout.LayoutParams recyclerParams = new LinearLayout.LayoutParams(-1, 0, 1);
        recyclerParams.topMargin = OpenClawPageUtils.dp(requireContext(), 20);
        emptyView =
                OpenClawPageUtils.empty(
                        requireContext(), getString(R.string.openclaw_empty_my_robots));
        adapter = new OpenClawRobotAdapter(null);
        adapter.setOnItemClickListener(
                robot ->
                        startActivity(
                                OpenClawDetailActivity.newIntent(requireContext(), robot, null)));
        recyclerView.setAdapter(adapter);
        root.addView(recyclerView, recyclerParams);
        root.addView(emptyView, new LinearLayout.LayoutParams(-1, -2));
        search.addTextChangedListener(
                new SimpleTextWatcher() {
                    @Override
                    public void afterTextChanged(Editable s) {
                        adapter.filter(s == null ? null : s.toString());
                        updateEmpty();
                    }
                });
        loadData();
        return root;
    }

    private void loadData() {
        new ViewModelProvider(this)
                .get(MyOpenClawRobotsViewModel.class)
                .getMyRobots()
                .observe(
                        getViewLifecycleOwner(),
                        resource -> {
                            if (resource.status == Status.SUCCESS) {
                                List<OpenClawRobotInfo> robots = resource.data;
                                adapter.setData(robots);
                                updateEmpty();
                            } else if (resource.status == Status.ERROR) {
                                ToastUtils.showErrorToast(resource.code);
                            }
                        });
    }

    private void updateEmpty() {
        emptyView.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }
}
