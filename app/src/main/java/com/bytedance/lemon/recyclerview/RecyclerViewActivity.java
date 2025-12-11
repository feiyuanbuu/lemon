package com.bytedance.lemon.recyclerview;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.bytedance.lemon.R;

import com.bytedance.lemon.recyclerview.adapter.UserAdapter;

//监听机制
import com.bytedance.lemon.recyclerview.entity.Usermessage;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.lifecycle.Observer;
import android.os.Looper; // 用于Handler
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bytedance.lemon.recyclerview.entity.User;
import com.bytedance.lemon.recyclerview.repository.UserRepository;


//增加搜索功能
import androidx.appcompat.widget.SearchView;

public class RecyclerViewActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private UserAdapter adapter;
    private RefreshLayout refreshLayout;
    public UserRepository userRepository;

    //增加搜索视图
    private SearchView mSearchView;
    private LiveData<List<User>> mCurrentSearchLiveData;

    //防止搜索抖动
    private final Handler mSearchHandler = new Handler(Looper.getMainLooper());
    private Runnable mSearchRunnable;

//
//    private Button mWidgetTestButton;
//    private TextView mWidgetStatusText;


    private ImageButton btnMenu;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recyclerview);

        // 1. 初始化Repository
        userRepository = new UserRepository(getApplication());

        // 2. 获取组件
        mRecyclerView = findViewById(R.id.recyclerview_id);
        refreshLayout = findViewById(R.id.refreshLayout);
        btnMenu = findViewById(R.id.btn_menu);

        // 3. 设置RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        // 4. 初始化Adapter (注意：你的UserAdapter需要改为支持List<User>)
        adapter = new UserAdapter(RecyclerViewActivity.this, new ArrayList<>()); // 先传空列表
        adapter.setUserRepository(userRepository);
        adapter.setOnUserLongClickListener(new UserAdapter.OnUserLongClickListener() {
            @Override
            public void onUserLongClick(User user, int position, View anchorView) {


                showDeleteUserMenu(user, position, anchorView);
            }
        });


        mRecyclerView.setAdapter(adapter);

        DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(divider);


//        setupWidgetControls();
        setupMenuButton();

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


//        setupWidgetControls();

    }



    // 模拟从网络获取刷新数据
    private void simulateRefreshFromNetwork() {
        // 在后台线程执行数据库操作
        new Thread(() -> {
            // 1. 清空数据库
            userRepository.deleteAllUsers();

            // 2. 生成新的模拟数据
//            List<User> newUsers = new ArrayList<>();
            Random random = new Random();
            String[] familyNames = {"张", "李", "王", "赵", "刘", "陈", "杨", "黄", "周", "吴", "武","任","范","程", "黄", "高","郭"};
            String[] givenNames = {"明", "华", "强", "伟", "芳", "丽", "敏", "军", "杰", "娜", "晨", "琰", "越", "小雨", "家童", "一帆", "晓"};
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
                    "消息：今天好累",
                    "消息：想家了～～",
                    "消息：宝， 吃饭了么？",
                    "消息：just right now, 即刻上场",
                    "消息：掌上珊瑚怜不得，却叫移作上阳花"
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
//                newUsers.add(user);
                userRepository.insert(user);
//                userRepository.userDao.update(user);


                Usermessage message = new Usermessage(
                        (long)i,
                        randomNewestinfo,
                        current_time,
                        0,  // 接收的消息类型
                        0   // 未读状态
                );
                userRepository.insertMessage(message);


            }

            // 3. 将新数据插入数据库（观察者模式会自动更新UI）
//            userRepository.insertAll(newUsers);

            //添加“我”用户
            User user_me = new User(0, "Me", "It's me","lemon", System.currentTimeMillis());
            user_me.setAvatarUrl("https://img95.699pic.com/photo/50136/1351.jpg_wh300.jpg");
            userRepository.insert(user_me);

        }).start();
    }

    // 模拟从网络加载更多数据
    private void simulateLoadMoreFromNetwork() {
        new Thread(() -> {
//            List<User> moreUsers = new ArrayList<>();
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
                    "Pet blogger with one cat.",
                    "Palmed coral, too precious to keep in hand,\n" +
                            "Yet ended up as flowers in Shangyang Palace—transplanted."
            };

            String[] newest_info_Templates = {
                    "Info:hello.",
                    "Info:I want to go home.",
                    "Info:Mom, I love you.",
                    "Info:I'm so tired today.",
                    "Info:Miss my family.",
                    "Info:Honey, do you have breakfast?",
                    "Info:Why I have to stay in this donkey school???",
                    "Info:He is a donkey guy, and he don't let me graduate.",
                    "You are never too old to set another goal or to dream a new dream.",
                    "It's okay to have setbacks, just don't give up.",
                    "Your potential is endless.",
                    "The only way to do great work is to love what you do."
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
//                moreUsers.add(user);
                userRepository.insert(user);

                Usermessage message = new Usermessage(
                        (long)i,
                        randomNewestinfo,
                        current_time_1,
                        0,  // 接收的消息类型
                        0   // 未读状态
                );
                userRepository.insertMessage(message);


//                userRepository.userDao.update(user);
            }
//            userRepository.insertAll(moreUsers);
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


    private void showDeleteUserMenu(User user, int position, View anchorView) {
        PopupMenu popupMenu = new PopupMenu(this, anchorView);
        popupMenu.getMenuInflater().inflate(R.menu.menu_user_item, popupMenu.getMenu());

        // 设置菜单标题显示用户名
        MenuItem titleItem = popupMenu.getMenu().findItem(R.id.menu_title);
        if (titleItem != null) {
            SpannableString title = new SpannableString(user.getName());
            title.setSpan(new StyleSpan(Typeface.BOLD), 0, title.length(), 0);
            titleItem.setTitle(title);
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.menu_delete) {
                    // 确认删除对话框
                    showDeleteConfirmationDialog(user, position);
                    return true;
                } else if (itemId == R.id.menu_view_profile) {
                    // 查看用户资料
                    viewUserProfile(user);
                    return true;
                } else if (itemId == R.id.menu_mark_all_read) {
                    // 标记所有消息为已读
                    userRepository.markAllMessagesAsRead(user.getId());
                    Toast.makeText(RecyclerViewActivity.this,
                            "已将 " + user.getName() + " 的所有消息标记为已读",
                            Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.menu_copy_user_id) {
                    // 复制用户ID到剪贴板
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("user_id", String.valueOf(user.getId()));
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(RecyclerViewActivity.this,
                            "已复制用户ID: " + user.getId(),
                            Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        });

        popupMenu.show();
    }


    private void showDeleteConfirmationDialog(User user, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("确认删除");
        builder.setMessage("确定要删除用户 \"" + user.getName() + "\" 吗？\n\n" +
                "此操作将删除该用户的所有聊天记录，且无法恢复。");

        builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 从数据库中删除用户
                userRepository.deleteUser(user.getId());

                // 从列表中移除（可选，因为LiveData会自动更新）
                // adapter.removeUser(position);

                dialog.dismiss();
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // 添加中立按钮：仅删除聊天记录
        builder.setNeutralButton("仅删除聊天记录", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showDeleteMessagesOnlyDialog(user);
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        // 为删除按钮设置红色文本
        Button deleteButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (deleteButton != null) {
            deleteButton.setTextColor(Color.RED);
        }
    }

    private void showDeleteMessagesOnlyDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("删除聊天记录");
        builder.setMessage("确定要删除与 \"" + user.getName() + "\" 的所有聊天记录吗？\n\n" +
                "此操作不会删除用户本身，仅删除聊天消息。");

        builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 仅删除该用户的所有消息
                userRepository.executorService.execute(() -> {
                    List<Usermessage> userMessages = userRepository.messageDao.getMessagesByUserId(user.getId());
                    if (userMessages != null && !userMessages.isEmpty()) {
                        for (Usermessage message : userMessages) {
                            userRepository.messageDao.delete(message);
                        }

                        // 重置未读计数
                        userRepository.userDao.resetUnreadInfoCount(user.getId());

                        // 更新用户的最新消息和时间戳
                        User updatedUser = userRepository.userDao.getUserById(user.getId());
                        if (updatedUser != null) {
                            updatedUser.setNewest_info("暂无消息");
                            updatedUser.setLastMessageTimestamp(System.currentTimeMillis());
                            userRepository.userDao.update(updatedUser);
                        }

                        runOnUiThread(() -> {
                            Toast.makeText(RecyclerViewActivity.this,
                                    "已删除 " + userMessages.size() + " 条聊天记录",
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                });
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("取消", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void viewUserProfile(User user) {
        // 这里可以跳转到用户资料页面，或者显示一个简化的资料对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("用户资料");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_user_profile, null);
        builder.setView(dialogView);

        // 设置用户信息
        TextView tvUserId = dialogView.findViewById(R.id.tv_user_id);
        TextView tvUserName = dialogView.findViewById(R.id.tv_user_name);
        TextView tvUserDesc = dialogView.findViewById(R.id.tv_user_desc);
        ImageView ivUserAvatar = dialogView.findViewById(R.id.iv_user_avatar);

        tvUserId.setText("ID: " + user.getId());
        tvUserName.setText(user.getName());
        tvUserDesc.setText(user.getDescription());

        Glide.with(this)
                .load(user.getAvatarUrl())
                .placeholder(R.drawable.avator_1)
                .circleCrop()
                .into(ivUserAvatar);

        builder.setPositiveButton("关闭", null);

        // 添加额外按钮
        builder.setNeutralButton("发送测试消息", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendTestMessage(user);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void sendTestMessage(User user) {
        String[] testMessages = {
                "你好！这是一条测试消息",
                "测试消息已收到",
                "今天天气不错",
                "最近在忙什么呢？",
                "测试消息发送成功"
        };

        Random random = new Random();
        String testMessage = testMessages[random.nextInt(testMessages.length)];

        Usermessage message = new Usermessage(
                user.getId(),
                testMessage,
                System.currentTimeMillis(),
                0,
                0
        );

        userRepository.sendMessage(message);
        Toast.makeText(this, "已向 " + user.getName() + " 发送测试消息", Toast.LENGTH_SHORT).show();
    }




    private void setupMenuButton() {
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMainMenu(v);
            }
        });
    }

    private void showMainMenu(View anchorView) {
        PopupMenu popupMenu = new PopupMenu(this, anchorView);
        popupMenu.getMenuInflater().inflate(R.menu.menu_main_actions, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.menu_mark_all_read) {
                    // 全部标记为已读
                    markAllMessagesAsRead();
                    return true;
                } else if (itemId == R.id.menu_message_stats) {
                    // 当前维度消息数量统计
                    showMessageStatistics();
                    return true;
                }
//                } else if (itemId == R.id.menu_clear_all_messages) {
//                    // 清空所有聊天记录
//                    showClearAllMessagesDialog();
//                    return true;
//                } else if (itemId == R.id.menu_refresh_data) {
//                    // 刷新数据
//                    refreshLayout.autoRefresh();
//                    return true;
//                }
                return false;
            }
        });

        popupMenu.show();
    }


    private void markAllMessagesAsRead() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("确认操作");
        builder.setMessage("确定要将所有用户的所有消息标记为已读吗？");

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 执行标记所有消息为已读的操作
                userRepository.memuService.execute(() -> {
                    try {
                        // 1. 获取所有用户
                        List<User> allUsers = userRepository.userDao.getAllUsers();

                        // 2. 为每个用户标记所有消息为已读
                        int totalUsers = 0;
                        int totalMessages = 0;

                        for (User user : allUsers) {
                            if (user.getId() != 0) { // 排除"我"自己
                                // 获取用户未读消息数量
                                int unreadCount = userRepository.getUnreadInfoCount(user.getId());

                                if (unreadCount > 0) {

                                    totalUsers++;
                                    totalMessages += unreadCount;
                                    // 标记该用户的所有消息为已读
//                                    userRepository.messageDao.markAllAsReadByUserId(user.getId());
                                    userRepository.markAllMessagesAsRead(user.getId());
                                    // 重置用户的未读计数
                                    userRepository.userDao.resetUnreadInfoCount(user.getId());
//
//                                    totalUsers++;
//                                    totalMessages += unreadCount;
                                }
                            }
                        }

                        // 3. 更新UI显示结果
                        final int finalTotalUsers = totalUsers;
                        final int finalTotalMessages = totalMessages;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String message;
                                if (finalTotalMessages > 0) {
                                    message = String.format("已标记 %d 个用户的 %d 条消息为已读",
                                            finalTotalUsers, finalTotalMessages);
                                } else {
                                    message = "所有消息已标记为已读";
                                }

                                Toast.makeText(RecyclerViewActivity.this,
                                        message, Toast.LENGTH_LONG).show();

                                // 刷新列表显示
                                adapter.notifyDataSetChanged();
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(RecyclerViewActivity.this,
                                        "操作失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                dialog.dismiss();
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * 显示消息统计
     */
    private void showMessageStatistics() {
        // 显示加载对话框
        AlertDialog loadingDialog = new AlertDialog.Builder(this)
                .setTitle("统计中...")
                .setMessage("正在统计消息数据，请稍候")
                .setCancelable(false)
                .create();
        loadingDialog.show();

        // 在后台线程执行统计
        userRepository.executorService.execute(() -> {
            try {
                // 1. 获取所有用户
                List<User> allUsers = userRepository.userDao.getAllUsers();

                // 2. 统计各种消息
                int totalUsers = 0;
                int totalMessages = 0;
                int totalUnreadMessages = 0;
                int totalSentMessages = 0;
                int totalReceivedMessages = 0;
                int totalOperationMessages = 0;

                for (User user : allUsers) {
//                    if (user.getId() != 0) { // 排除"我"自己
                        totalUsers++;

                        // 获取该用户的所有消息
                        List<Usermessage> userMessages = userRepository.messageDao.getMessagesByUserId(user.getId());

                        if (userMessages != null) {
                            totalMessages += userMessages.size();

                            // 分类统计
                            for (Usermessage message : userMessages) {
                                switch (message.getMessageType()) {
                                    case 0: // 接收的消息
                                        totalReceivedMessages++;
                                        if (!message.isMessage_isRead()) {
                                            totalUnreadMessages++;
                                        }
                                        break;
                                    case 1: // 发送的消息
                                        totalSentMessages++;
                                        break;
                                    case 2: // 运营消息
                                        totalOperationMessages++;
                                        if (!message.isMessage_isRead()) {
                                            totalUnreadMessages++;
                                        }
                                        break;
                                }
                            }
                        }

                }

                // 3. 构建统计信息文本
                final String statsText = String.format(
                        "📊 消息统计报告\n\n" +
                                "用户总数: %d 人\n" +
                                "消息总数: %d 条\n" +
                                "未读消息: %d 条\n\n" +
                                "📤 发送消息: %d 条\n" +
                                "📥 接收消息: %d 条\n" +
                                "🎯 运营消息: %d 条\n\n" +
                                "平均每人消息: %.1f 条",
                        totalUsers,
                        totalMessages,
                        totalUnreadMessages,
                        totalSentMessages,
                        totalReceivedMessages,
                        totalOperationMessages,
                        totalUsers > 0 ? (float) totalMessages / totalUsers : 0
                );

                // 4. 在UI线程显示结果
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.dismiss();

                        AlertDialog.Builder resultBuilder = new AlertDialog.Builder(RecyclerViewActivity.this);
                        resultBuilder.setTitle("当前消息数量统计");
                        resultBuilder.setMessage(statsText);

                        resultBuilder.setPositiveButton("确定", null);

                        // 添加额外按钮：导出统计
                        resultBuilder.setNeutralButton("复制统计", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("消息统计", statsText);
                                clipboard.setPrimaryClip(clip);
                                Toast.makeText(RecyclerViewActivity.this,
                                        "已复制统计信息到剪贴板", Toast.LENGTH_SHORT).show();
                            }
                        });

                        AlertDialog resultDialog = resultBuilder.create();
                        resultDialog.show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.dismiss();
                        Toast.makeText(RecyclerViewActivity.this,
                                "统计失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * 显示清空所有聊天记录的确认对话框
     */
    private void showClearAllMessagesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("警告");
        builder.setMessage("确定要清空所有用户的聊天记录吗？\n\n" +
                "此操作将删除所有聊天消息，但会保留用户信息。\n" +
                "此操作不可恢复！");

        builder.setPositiveButton("清空所有", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                userRepository.executorService.execute(() -> {
                    try {
                        // 清空所有消息
                        userRepository.messageDao.deleteAllMessages();

                        // 重置所有用户的未读计数和最新消息
                        List<User> allUsers = userRepository.userDao.getAllUsers();
                        for (User user : allUsers) {
                            user.setUnreadInfoCount(0);
                            user.setNewest_info("暂无消息");
                            user.setLastMessageTimestamp(System.currentTimeMillis());
                            userRepository.userDao.update(user);
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(RecyclerViewActivity.this,
                                        "已清空所有聊天记录", Toast.LENGTH_LONG).show();
                                adapter.notifyDataSetChanged();
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(RecyclerViewActivity.this,
                                        "清空失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("取消", null);

        // 为清空按钮设置红色文本
        AlertDialog dialog = builder.create();
        dialog.show();
        Button clearButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (clearButton != null) {
            clearButton.setTextColor(Color.RED);
        }
    }

}

