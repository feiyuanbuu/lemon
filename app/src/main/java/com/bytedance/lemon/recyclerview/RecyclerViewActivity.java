package com.bytedance.lemon.recyclerview;

import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.bytedance.lemon.R;

import com.bytedance.lemon.recyclerview.adapter.UserAdapter;

//监听机制
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.lifecycle.Observer;
import android.os.Looper; // 用于Handler

import com.bytedance.lemon.recyclerview.entity.User;
import com.bytedance.lemon.recyclerview.repository.UserRepository;


//增加搜索功能
import androidx.appcompat.widget.SearchView;

public class RecyclerViewActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private UserAdapter adapter;
    private RefreshLayout refreshLayout;
    private UserRepository userRepository;

    //增加搜索视图
    private SearchView mSearchView;
    private LiveData<List<User>> mCurrentSearchLiveData;

    //防止搜索抖动
    private final Handler mSearchHandler = new Handler(Looper.getMainLooper());
    private Runnable mSearchRunnable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recyclerview);

        // 1. 初始化Repository
        userRepository = new UserRepository(getApplication());

        // 2. 获取组件
        mRecyclerView = findViewById(R.id.recyclerview_id);
        refreshLayout = findViewById(R.id.refreshLayout);

        // 3. 设置RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        // 4. 初始化Adapter (注意：你的UserAdapter需要改为支持List<User>)
        adapter = new UserAdapter(RecyclerViewActivity.this, new ArrayList<>()); // 先传空列表
        adapter.setUserRepository(userRepository);
        mRecyclerView.setAdapter(adapter);

        DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(divider);


        // 5. 【核心】观察数据库数据变化，自动更新UI
        userRepository.getAllUsersLive().observe(this, new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                // 当数据库中的数据变化时，这里会自动回调
                adapter.setUserList(users); // 你需要为UserAdapter添加setUserList方法
                adapter.notifyDataSetChanged();

                // 检查是否有足够的用户来启动自动消息
                if (users != null && users.size() >= 2 && !userRepository.isAutoMessagingRunning()) {
                    // 只在有足够用户且自动消息未运行时才启动
                    userRepository.initAutoMessagingOnce();
                }

            }
        });

        // 6. 设置下拉刷新监听器
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(final RefreshLayout refreshlayout) {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // 模拟网络刷新：清空旧数据，生成新数据插入数据库
                        // 由于我们观察了LiveData，数据库更新后UI会自动刷新
                        simulateRefreshFromNetwork();
                        refreshlayout.finishRefresh();
                    }
                }, 150);
            }
        });

        // 7. 设置上滑加载更多监听器
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(final RefreshLayout refreshlayout) {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // 模拟加载更多数据到数据库
                        simulateLoadMoreFromNetwork();
                        // 判断是否已无更多数据（例如总数大于50条）
                        if (userRepository.getUserCount() >= 50) {
                            refreshlayout.finishLoadMoreWithNoMoreData();
                        } else {
                            refreshlayout.finishLoadMore();
                        }
                    }
                }, 150);
            }
        });

        // 搜索
        mSearchView = findViewById(R.id.search_view);
        setupSearchView();




    }

    // 模拟从网络获取刷新数据
    private void simulateRefreshFromNetwork() {
        // 在后台线程执行数据库操作
        new Thread(() -> {
            // 1. 清空数据库
            userRepository.deleteAllUsers();

            // 2. 生成新的模拟数据
            List<User> newUsers = new ArrayList<>();
            Random random = new Random();
            String[] familyNames = {"张", "李", "王", "赵", "刘", "陈", "杨", "黄", "周", "吴"};
            String[] givenNames = {"明", "华", "强", "伟", "芳", "丽", "敏", "军", "杰", "娜"};
            String[] descTemplates = {
                    "喜欢旅行和摄影",
                    "热爱生活的程序员",
                    "美食爱好者，擅长烘焙",
                    "健身达人，每周运动5次",
                    "音乐迷，收藏了1000+首歌",
                    "阅读是最大的爱好",
                    "职场新人，努力提升中",
                    "宠物博主，家有一只猫"
            };

            String[] newest_info_Templates = {
                    "消息：你好",
                    "消息：我想回家了",
                    "消息：妈妈我爱你",
                    "消息：今天好累了",
                    "消息：想家了～～",
                    "消息：宝， 吃饭了么？",
                    "消息：凭什么我在这个垃圾学校",
                    "消息：真实个混蛋，让我毕业不了"
            };

            for (int i = 1; i <= 20; i++) {
                String name = familyNames[random.nextInt(familyNames.length)] +
                        givenNames[random.nextInt(givenNames.length)];

                String randomDesc = descTemplates[random.nextInt(descTemplates.length)];
                String randomNewestinfo = newest_info_Templates[random.nextInt(newest_info_Templates.length)];
                long current_time = System.currentTimeMillis();
//                User user = new User(name, randomDesc,randomNewestinfo, current_time);
//
//                newUsers.add(user);
                // 使用带ID的构造函数
                User user = new User(
                        (long)i,        // 明确指定ID
                        name,           // 名称
                        randomDesc,     // 描述
                        randomNewestinfo, // 最新消息
                        current_time    // 创建时间
                );
                user.setUnreadInfoCount(1);
                newUsers.add(user);

            }

            // 3. 将新数据插入数据库（观察者模式会自动更新UI）
            userRepository.insertAll(newUsers);

            //添加“我”用户
            User user_me = new User(0, "Me", "It's me","lemon", System.currentTimeMillis());
            user_me.setAvatarUrl("https://img95.699pic.com/photo/50136/1351.jpg_wh300.jpg");
            userRepository.insert(user_me);

        }).start();
    }

    // 模拟从网络加载更多数据
    private void simulateLoadMoreFromNetwork() {
        new Thread(() -> {
            List<User> moreUsers = new ArrayList<>();
            Random random = new Random();
            String[] familyNames = {"Curry", "Dawson", "Aron", "James"};
            String[] givenNames = {"Stephen", "Lebron", "Leo", "Jane"};
            String[] descTemplates = {
                    "Love traveling and photography",
                    "Passionate programmer",
                    "Food enthusiast, skilled in baking.",
                    "Fitness enthusiast, exercises 5 times a week",
                    "Music lover, has a collection of 1000+ songs.",
                    "Reading is my greatest hobby.",
                    "New professional, striving to improve.",
                    "Pet blogger with one cat."
            };

            String[] newest_info_Templates = {
                    "Info:hello.",
                    "Info:I want to go home.",
                    "Info:Mom, I love you.",
                    "Info:I'm so tired today.",
                    "Info:Miss my family.",
                    "Info:Honey, do you have breakfast?",
                    "Info:Why I have to stay in this donkey school???",
                    "Info:He is a donkey guy, and he don't let me graduate."
            };

            int currentSize = userRepository.getUserCount();

            for (int i = currentSize+1; i < currentSize+5; i++) {
                String name = familyNames[random.nextInt(familyNames.length)] +
                        givenNames[random.nextInt(givenNames.length)];
                // 注意：User的构造方法参数
                // 生成随机描述（从模板中随机选择）
                String randomDesc = descTemplates[random.nextInt(descTemplates.length)];
                String randomNewestinfo = newest_info_Templates[random.nextInt(newest_info_Templates.length)];
                long current_time_1 = System.currentTimeMillis();
                User user = new User(i, "加载项_" + i + "_" + name, randomDesc, randomNewestinfo, current_time_1);
                user.setUnreadInfoCount(1);
                moreUsers.add(user);
            }
            userRepository.insertAll(moreUsers);
        }).start();
    }




    private void setupSearchView() {
        mSearchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // 当用户提交搜索（如按下键盘上的搜索按钮）时触发
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // 移除之前未执行的搜索任务
                if (mSearchRunnable != null) {
                    mSearchHandler.removeCallbacks(mSearchRunnable);
                }
                // 延迟 300 毫秒执行搜索，避免频繁查询数据库
                mSearchRunnable = new Runnable() {
                    @Override
                    public void run() {
                        performSearch(newText);
                    }
                };
                mSearchHandler.postDelayed(mSearchRunnable, 100);
                return true;
            }

        });

        // 可选：监听搜索框的关闭事件，显示恢复全部列表
        mSearchView.setOnCloseListener(new androidx.appcompat.widget.SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                mSearchView.setQuery("", false);
                // 恢复全部列表
                performSearch("");
                return false;
            }
        });
    }



    private void performSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            // 如果搜索框为空，则显示全部用户（无高亮）
            observeUserList(userRepository.getAllUsersLive(), null);
        } else {
            // 否则，执行搜索并传递关键词用于高亮
            observeUserList(userRepository.searchUsersLive(query.trim()), query.trim());
        }
    }

    private void observeUserList(LiveData<List<User>> userLiveData, String query) {
        // 如果之前有观察其他 LiveData，先移除观察
        if (mCurrentSearchLiveData != null) {
            mCurrentSearchLiveData.removeObservers(this);
        }

        // 观察新的 LiveData
        mCurrentSearchLiveData = userLiveData;
        mCurrentSearchLiveData.observe(this, new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                // 当数据变化时，更新 Adapter 并传递搜索关键词
                if (adapter != null) {
                    if (query != null && !query.trim().isEmpty()) {
                        // 有搜索关键词，使用带高亮的方法
                        adapter.updateSearchResults(users, query.trim());
                    } else {
                        // 没有搜索关键词，使用普通方法
                        adapter.setUserList(users);
                    }
                }
            }
        });
    }

}


