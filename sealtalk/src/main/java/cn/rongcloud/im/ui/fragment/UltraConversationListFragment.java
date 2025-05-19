package cn.rongcloud.im.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.im.IMManager;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.model.UltraChannelInfo;
import cn.rongcloud.im.model.UltraGroupInfo;
import cn.rongcloud.im.ui.activity.ConversationActivity;
import cn.rongcloud.im.ui.activity.CreateChannelActivity;
import cn.rongcloud.im.ui.activity.CreateUltraGroupActivity;
import cn.rongcloud.im.ui.activity.SealTalkDebugTestActivity;
import cn.rongcloud.im.ui.activity.SelectUltraCreateGroupActivity;
import cn.rongcloud.im.ui.activity.UltraSettingActivity;
import cn.rongcloud.im.ui.adapter.UltraConversationListAdapterEx;
import cn.rongcloud.im.ui.adapter.UltraListAdapter;
import cn.rongcloud.im.ultraGroup.UltraGroupManager;
import cn.rongcloud.im.utils.NetworkUtils;
import cn.rongcloud.im.viewmodel.UltraGroupViewModel;
import com.google.gson.Gson;
import io.rong.common.RLog;
import io.rong.imkit.conversationlist.model.BaseUiConversation;
import io.rong.imkit.conversationlist.model.GatheredConversation;
import io.rong.imkit.userinfo.RongUserInfoManager;
import io.rong.imkit.userinfo.model.GroupUserInfo;
import io.rong.imkit.utils.RouteUtils;
import io.rong.imkit.widget.TitleBar;
import io.rong.imkit.widget.adapter.BaseAdapter;
import io.rong.imkit.widget.adapter.ViewHolder;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.ConversationIdentifier;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.UserInfo;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class UltraConversationListFragment extends Fragment
        implements View.OnClickListener,
                RongUserInfoManager.UserDataObserver,
                BaseAdapter.OnItemClickListener {
    private static final String TAG = "UltraConversationListFragment";
    private UltraListAdapter ultraListAdapter;
    private List<UltraGroupInfo> dataList = new ArrayList<>();
    protected ListView mList;
    private ImageView channelImageView;
    private SharedPreferences sharedPreferences;
    private String currentId;
    private String currentName;
    private String currentCreatorId;
    protected UltraGroupViewModel mConversationListViewModel;
    private TextView textView;
    private UltraConversationListAdapterEx mAdapter;
    private TitleBar mTitleBar;
    private LinearLayout linearLayout;
    private TextView invite;
    private RecyclerView recyclerView;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    {
        mAdapter = onResolveAdapter();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UltraGroupManager.getInstance()
                .addGroupChangeListener(
                        new UltraGroupManager.GroupChangeListener() {
                            @Override
                            public void onGroupChange() {
                                getUltraGroupMemberList();
                            }

                            @Override
                            public void onGroupCreate(UltraGroupInfo ultraGroupInfo) {
                                dataList.add(0, ultraGroupInfo);
                                ultraListAdapter.setList(dataList);
                            }
                        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData(view);
        subscribeUi();
    }

    private UltraConversationListAdapterEx onResolveAdapter() {
        mAdapter = new UltraConversationListAdapterEx();
        mAdapter.setEmptyView(io.rong.imkit.R.layout.rc_conversationlist_empty_view);
        return mAdapter;
    }

    private void initData(View view) {
        if (getActivity() == null) {
            return;
        }

        recyclerView = view.findViewById(R.id.rc_conversation_list);
        // mRefreshLayout = view.findViewById(io.rong.imkit.R.id.rc_refresh);

        mAdapter.setContext(getContext());
        mAdapter.setItemClickListener(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mAdapter);

        linearLayout = view.findViewById(R.id.ll_right_icon);
        sharedPreferences = getActivity().getSharedPreferences("ultra", Context.MODE_PRIVATE);
        mList = view.findViewById(R.id.ultraList);
        ImageView imageView = view.findViewById(R.id.imageView);
        imageView.setOnClickListener(this);
        if (IMManager.getInstance().getAppTask().isDebugMode()
                && IMManager.getInstance().getAppTask().isUltraGroupDebugMode()) {
            ImageView imageViewSetting = view.findViewById(R.id.imageViewSetting);
            imageViewSetting.setVisibility(View.VISIBLE);
            imageViewSetting.setOnClickListener(
                    v -> startActivity(new Intent(getActivity(), SealTalkDebugTestActivity.class)));
        }
        channelImageView = view.findViewById(R.id.channelImageView);
        channelImageView.setOnClickListener(this);
        invite = view.findViewById(R.id.invite);
        invite.setOnClickListener(this);
        textView = view.findViewById(R.id.tv_right);
        mTitleBar = view.findViewById(R.id.rc_ultra_bar);
        mTitleBar.getLeftView().setVisibility(View.GONE);

        mTitleBar.setOnRightIconClickListener(
                v -> {
                    Intent intent = new Intent(getActivity(), UltraSettingActivity.class);
                    intent.putExtra(
                            IntentExtra.SERIA_CONVERSATION_IDENTIFIER,
                            ConversationIdentifier.obtainUltraGroup(currentId, ""));
                    startActivity(intent);
                });
        ultraListAdapter = new UltraListAdapter(getActivity(), new ArrayList<>());
        mList.setAdapter(ultraListAdapter);
        mList.setOnItemClickListener(
                (parent, view1, position, id) -> {
                    UltraGroupInfo ultraUserInfo = dataList.get(position);
                    if (ultraUserInfo != null) {
                        currentId = ultraUserInfo.groupId;
                        currentName = ultraUserInfo.groupName;
                        sharedPreferences
                                .edit()
                                .putString("creatorId", ultraUserInfo.creatorId)
                                .apply();
                        sharedPreferences.edit().putString("group_id", currentId).apply();
                        sharedPreferences.edit().putString("name", currentName).apply();
                        mConversationListViewModel.getConversationList(currentId, false, false);
                        getUltraGroupChannelList(currentId);
                        textView.setText(ultraUserInfo.groupName);
                        if (RongIMClient.getInstance()
                                .getCurrentUserId()
                                .equals(ultraUserInfo.creatorId)) {
                            channelImageView.setVisibility(View.VISIBLE);
                        } else {
                            channelImageView.setVisibility(View.GONE);
                        }
                    }
                });
    }

    // 获取当前用户所属群组列表get
    private void getUltraGroupMemberList() {
        mConversationListViewModel.getUltraGroupMemberList();
    }

    // 获取当前用户所属群组列表
    private void getUltraGroupChannelList(String groupId) {
        mConversationListViewModel.getUltraGroupChannelList(groupId);
    }

    @SuppressLint("InflateParams")
    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.rc_ultra_conversationlist_fragment, null, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RouteUtils.registerActivity(
                RouteUtils.RongActivityType.ConversationActivity, ConversationActivity.class);
        RongUserInfoManager.getInstance().removeUserDataObserver(this);
        if (mConversationListViewModel != null
                && mConversationListViewModel.getUltraGroupMemberListResult() != null) {
            mConversationListViewModel.getUltraGroupMemberListResult().removeObserver(observer);
        }
        if (mConversationListViewModel != null
                && mConversationListViewModel.getUltraGroupChannelListResult() != null) {
            mConversationListViewModel
                    .getUltraGroupChannelListResult()
                    .removeObserver(channelObserver);
        }

        UltraGroupManager.getInstance().clearGroupChangeListener();
    }

    protected void subscribeUi() {
        if (getActivity() == null) {
            return;
        }
        mConversationListViewModel = new ViewModelProvider(this).get(UltraGroupViewModel.class);
        RongUserInfoManager.getInstance().addUserDataObserver(this);
        mConversationListViewModel.getUltraGroupMemberListResult().observeForever(observer);
        mConversationListViewModel.getUltraGroupChannelListResult().observeForever(channelObserver);
        getUltraGroupMemberList();

        mConversationListViewModel
                .getConversationListLiveData()
                .observe(
                        getViewLifecycleOwner(),
                        uiConversations -> {
                            ArrayList<BaseUiConversation> data = new ArrayList<>();
                            HashSet<String> channelIds = new HashSet<>();
                            for (BaseUiConversation uiConversation : uiConversations) {
                                if (!Conversation.ConversationType.ULTRA_GROUP.equals(
                                                uiConversation.mCore.getConversationType())
                                        && !uiConversation.mCore.getTargetId().equals(currentId)) {
                                    continue;
                                }
                                data.add(uiConversation);
                                channelIds.add(uiConversation.mCore.getChannelId());
                            }
                            mAdapter.setDataCollection(data);
                            // 存储当前的 ChannelId
                            if (!channelIds.isEmpty()) {
                                sharedPreferences
                                        .edit()
                                        .putStringSet("channel_ids", channelIds)
                                        .apply();
                            }
                        });
    }

    Observer<Resource<List<UltraGroupInfo>>> observer =
            new Observer<Resource<List<UltraGroupInfo>>>() {
                @Override
                public void onChanged(Resource<List<UltraGroupInfo>> listResource) {
                    if (listResource.status == Status.LOADING) {
                        return;
                    }
                    if (listResource.status == Status.ERROR) {
                        if (NetworkUtils.isNetWorkAvailable(getActivity())) {
                            mainHandler.postDelayed(() -> getUltraGroupMemberList(), 1000);
                        }
                        return;
                    }
                    if (listResource.status == Status.SUCCESS) {
                        if (!dataList.isEmpty()) {
                            dataList.clear();
                        }
                        if (!listResource.data.isEmpty()) {
                            dataList.addAll(listResource.data);
                        }
                        ultraListAdapter.setList(dataList);
                        Gson gson = new Gson();
                        String userJson = gson.toJson(listResource.data);
                        sharedPreferences.edit().putString("member_list", userJson).apply();
                    }
                    // 处理超级群下频道列表相关逻辑
                    if (!dataList.isEmpty()) {
                        String id = sharedPreferences.getString("group_id", "");
                        String name = sharedPreferences.getString("name", "");
                        String creatorId = sharedPreferences.getString("creatorId", "");
                        UltraGroupInfo info = dataList.get(0);
                        currentId = TextUtils.isEmpty(id) ? info.groupId : id;
                        currentName = TextUtils.isEmpty(name) ? info.groupName : name;
                        currentCreatorId =
                                TextUtils.isEmpty(creatorId) ? info.creatorId : creatorId;
                        boolean isCreator =
                                RongIMClient.getInstance()
                                        .getCurrentUserId()
                                        .equals(currentCreatorId);

                        sharedPreferences.edit().putString("creatorId", currentCreatorId).apply();
                        mConversationListViewModel.getConversationList(currentId, false, false);
                        getUltraGroupChannelList(currentId);

                        // 处理TitleBar等UI
                        mTitleBar.getRightView().setVisibility(View.VISIBLE);
                        channelImageView.setVisibility(isCreator ? View.VISIBLE : View.GONE);
                        linearLayout.setVisibility(View.VISIBLE);
                        invite.setVisibility(View.VISIBLE);

                        textView.setText(currentName);
                    } else {
                        mAdapter.setDataCollection(new ArrayList<>());

                        // 处理TitleBar等UI
                        mTitleBar.getRightView().setVisibility(View.GONE);
                        channelImageView.setVisibility(View.GONE);
                        linearLayout.setVisibility(View.GONE);
                        invite.setVisibility(View.GONE);
                        textView.setText("");
                    }
                }
            };

    Observer<Resource<List<UltraChannelInfo>>> channelObserver =
            new Observer<Resource<List<UltraChannelInfo>>>() {
                @Override
                public void onChanged(Resource<List<UltraChannelInfo>> listResource) {
                    if (listResource.status == Status.LOADING) {
                        return;
                    }
                    if (listResource.status == Status.SUCCESS) {
                        if (listResource.data == null) {
                            return;
                        }
                        Gson gson = new Gson();
                        String userJson = gson.toJson(listResource.data);
                        sharedPreferences.edit().putString(currentId, userJson).apply();
                        UltraGroupManager.getInstance()
                                .refreshUltraChannelInfo(
                                        getContext(), currentId, listResource.data);
                    } else if (listResource.status == Status.ERROR) {
                        Toast.makeText(getActivity(), "获取频道列表失败", Toast.LENGTH_LONG).show();
                    }
                }
            };

    @Override
    public void onItemClick(View view, ViewHolder holder, int position) {
        if (position < 0) {
            return;
        }
        BaseUiConversation baseUiConversation = mAdapter.getItem(position);
        if (baseUiConversation != null && baseUiConversation.mCore != null) {
            if (baseUiConversation instanceof GatheredConversation) {
                RouteUtils.routeToSubConversationListActivity(
                        view.getContext(),
                        ((GatheredConversation) baseUiConversation).mGatheredType,
                        baseUiConversation.mCore.getConversationTitle());
            } else {
                RouteUtils.routeToConversationActivity(
                        view.getContext(), baseUiConversation.getConversationIdentifier());
            }
        } else {
            RLog.e(TAG, "invalid conversation.");
        }

        sharedPreferences.edit().putString("group_id", currentId).apply();
        sharedPreferences.edit().putString("name", currentName).apply();
        sharedPreferences.edit().putString("creatorId", currentCreatorId).apply();
        if (baseUiConversation != null) {
            mConversationListViewModel.setChannelId(baseUiConversation.mCore.getChannelId());
        }
    }

    public boolean onItemLongClick(View view, ViewHolder holder, int position) {
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        getUltraGroupMemberList();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.imageView) {
            Intent intent = new Intent(this.getActivity(), CreateUltraGroupActivity.class);
            startActivity(intent);
        } else if (id == R.id.invite) {
            Intent contactIntent =
                    new Intent(this.getActivity(), SelectUltraCreateGroupActivity.class);
            contactIntent.putExtra("groupId", currentId);
            startActivity(contactIntent);
        } else if (id == R.id.channelImageView) {
            Intent channelIntent = new Intent(this.getActivity(), CreateChannelActivity.class);
            channelIntent.putExtra("groupId", currentId);
            startActivity(channelIntent);
        }
    }

    @Override
    public void onUserUpdate(UserInfo info) {}

    @Override
    public void onGroupUpdate(Group group) {}

    @Override
    public void onGroupUserInfoUpdate(GroupUserInfo groupUserInfo) {}
}
