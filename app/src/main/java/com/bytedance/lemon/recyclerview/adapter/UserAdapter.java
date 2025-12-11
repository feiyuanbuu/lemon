package com.bytedance.lemon.recyclerview.adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bytedance.lemon.ProfileActivity;
import com.bytedance.lemon.R;
import com.bytedance.lemon.recyclerview.RecyclerViewActivity;
import com.bytedance.lemon.recyclerview.entity.User; // 【重要】导入新的User实体
import com.bytedance.lemon.recyclerview.repository.UserRepository; // 【新增】用于更新状态
import com.bytedance.lemon.recyclerview.utils.DateUtil;
import com.bytedance.lemon.recyclerview.utils.TextHighlighter;
import com.bytedance.lemon.recyclerview.viewholder.UserViewHolder;

import com.bytedance.lemon.ChatActivity; // 确保导入ChatActivity


import java.util.ArrayList;
import java.util.List;

/**
 * create by WUzejian on 2025/11/17
 * 修改为支持Room架构和User实体
 */
public class UserAdapter extends RecyclerView.Adapter<UserViewHolder> {
    private List<User> mUserList = new ArrayList<>(); // 【修改】数据类型改为User
    private UserRepository userRepository; // 【新增】用于在点击事件中更新数据库

    private Context mContext; // 新增：保存Context引用

    private String mSearchKeyword = "";

    // 构造方法1：接收数据集
    public UserAdapter(Context context, List<User> userList) {
        this.mContext = context;
        this.mUserList = userList != null ? userList : new ArrayList<>();
    }


    // 【新增】设置Repository（在Activity中初始化Adapter后调用）
    public void setUserRepository(UserRepository repository) {
        this.userRepository = repository;
    }


    // 新增：设置搜索关键词
    public void setSearchKeyword(String keyword) {
        this.mSearchKeyword = keyword != null ? keyword : "";
    }

    // 新增：获取搜索关键词
    public String getSearchKeyword() {
        return mSearchKeyword;
    }

    // 添加长按监听器接口
    public interface OnUserLongClickListener {
        void onUserLongClick(User user, int position, View anchorView);
    }

    private OnUserLongClickListener onUserLongClickListener;

    // 设置长按监听器的方法
    public void setOnUserLongClickListener(OnUserLongClickListener listener) {
        this.onUserLongClickListener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemRootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_user, parent, false);
        return new UserViewHolder(itemRootView);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = mUserList.get(position); // 【修改】获取User对象
        holder.bindData(user); // bindData方法现在接收User对象


        // 如果有关键词，使用高亮文本，否则使用普通文本
        if (mSearchKeyword != null && !mSearchKeyword.trim().isEmpty()) {
            // 高亮显示用户名
            holder.tvName.setText(TextHighlighter.highlight(
                    user.getName(),
                    mSearchKeyword.trim(),
                    mContext
            ));

            // 高亮显示最新消息
            holder.tvNewestinfo.setText(TextHighlighter.highlight(
                    user.getNewest_info(),
                    mSearchKeyword.trim(),
                    mContext
            ));

            // 高亮显示描述（如果有tv_description的话）
            // holder.tvDescription.setText(TextHighlighter.highlight(
            //     user.getDescription(),
            //     mSearchKeyword.trim(),
            //     mContext
            // ));
        } else {
            // 没有搜索关键词，显示普通文本
            holder.tvName.setText(user.getName());
            holder.tvNewestinfo.setText(user.getNewest_info());
            // holder.tvDescription.setText(user.getDescription());
        }

        int unreadCount = user.getUnreadInfoCount();
        if (unreadCount > 0) {
            holder.tvUnreadCount.setVisibility(View.VISIBLE);
            holder.tvUnreadCount.setText(String.valueOf(unreadCount > 99 ? "99+" : unreadCount));
        } else {
            holder.tvUnreadCount.setVisibility(View.GONE);
        }


        // 调试日志
//        long ts = user.getTimestamp();
//        Log.d("TimeDebug", "位置: " + position + ", 时间戳: " + ts + ", 格式化后: " + DateUtil.getTimeString(ts));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPosition = holder.getBindingAdapterPosition();
                if (currentPosition == RecyclerView.NO_POSITION) return;

                User currentUser = mUserList.get(currentPosition);
//                if (!currentUser.isRead()) {
//                    // --- 【优化】先立即更新UI，提供即时视觉反馈 ---
//                    currentUser.setRead(true);
//                    notifyItemChanged(currentPosition); // 立即刷新当前项，红点瞬间消失
//                    // --- 核心：后续仍要持久化到数据库 ---
//                    if (userRepository != null) {
//                        userRepository.markAsRead(currentUser.getId());
//                    }
//                    // 注意：这里不用担心 `LiveData` 之后会覆盖导致“闪烁”，
//                    // 因为数据库更新后的新数据中，该用户的 `isRead` 状态同样是 true。
//                }


                    if (userRepository != null) {
                        userRepository.markAllMessagesAsRead(currentUser.getId());
                    }


            }
        });

        holder.itemView.findViewById(R.id.iv_avatar).setOnClickListener(v -> {

            ImageView avatarView = holder.itemView.findViewById(R.id.iv_avatar);
            CardView cardView = holder.itemView.findViewById(R.id.card_view); // 需要给item布局中的CardView添加id

            // 设置共享元素的过渡名称
            String avatarTransitionName = "avatar_" + user.getId();
            String cardTransitionName = "card_" + user.getId();

            avatarView.setTransitionName(avatarTransitionName);
            cardView.setTransitionName(cardTransitionName);

            // 使用成员变量 mContext
            Intent intent = new Intent(mContext, ProfileActivity.class);
            // 关键：传递用户ID
            intent.putExtra("EXTRA_USER_ID", user.getId()); // user是当前列表项对应的User对象

            // 添加共享元素参数
            Bundle options = ActivityOptions.makeSceneTransitionAnimation(
                    (Activity) mContext,
                    Pair.create(avatarView, avatarTransitionName),
                    Pair.create(cardView, cardTransitionName)
            ).toBundle();


//            mContext.startActivity(intent);

            mContext.startActivity(intent, options);



        });

        holder.tvNewestinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPosition = holder.getBindingAdapterPosition();
                if (currentPosition == RecyclerView.NO_POSITION) return;

                User currentUser = mUserList.get(currentPosition);
//                currentUser.setRead(true);
//                userRepository.markAsRead(currentUser.getId());
                userRepository.markAllMessagesAsRead(currentUser.getId());
//                notifyItemChanged(currentPosition);
                // 跳转到聊天页面
                Intent intent = new Intent(mContext, ChatActivity.class);
                intent.putExtra("USER_ID", currentUser.getId());
                mContext.startActivity(intent);
            }
        });

//        holder.is_pinned.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                int currentPosition = holder.getBindingAdapterPosition();
//                if (currentPosition == RecyclerView.NO_POSITION) return;
//
//                User currentUser = mUserList.get(currentPosition);
//                currentUser.setPinned(true);
//                userRepository.markAsPinned(currentUser.getId());
//
//                notifyDataSetChanged();
//
//            }
//        });
        updatePinnedUI(holder, user);

        holder.btnPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPosition = holder.getBindingAdapterPosition();
                if (currentPosition == RecyclerView.NO_POSITION) return;

                User currentUser = mUserList.get(currentPosition);

                // 切换置顶状态
                boolean newPinnedState = !currentUser.isPinned();

                // 1. 先更新内存中的状态，提供即时反馈
                currentUser.setPinned(newPinnedState);
                updatePinnedUI(holder, currentUser);

                // 2. 显示Toast提示
                String message = newPinnedState ? "已置顶" : "已取消置顶";
                Toast.makeText(mContext, message + ": " + currentUser.getName(),
                        Toast.LENGTH_SHORT).show();

                // 3. 更新数据库
                if (userRepository != null) {
                    userRepository.updatePinnedStatus(currentUser.getId(), newPinnedState);
                }

                // 4. 重新排序列表（重要！）
//                reorderUserList();

                // 5. 通知适配器数据已更改
                notifyDataSetChanged();

                Log.d("UserAdapter", "用户 " + currentUser.getName() +
                        " 置顶状态已切换为: " + newPinnedState);
            }
        });


        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int currentPosition = holder.getBindingAdapterPosition();
                if (currentPosition == RecyclerView.NO_POSITION) return false;

                User currentUser = mUserList.get(currentPosition);

                if (onUserLongClickListener != null) {
                    // 传递用户、位置和锚点视图（用于显示菜单）
                    onUserLongClickListener.onUserLongClick(currentUser, currentPosition, holder.itemView);
                    return true;
                }
                return false;
            }
        });


    }

    public void removeUser(int position) {
        if (position >= 0 && position < mUserList.size()) {
            mUserList.remove(position);
            notifyItemRemoved(position);
            // 通知范围更改，确保后续项位置正确
            notifyItemRangeChanged(position, mUserList.size() - position);
        }
    }


    public void removeUserById(long userId) {
        for (int i = 0; i < mUserList.size(); i++) {
            if (mUserList.get(i).getId() == userId) {
                removeUser(i);
                break;
            }
        }
    }

    private void updatePinnedUI(UserViewHolder holder, User user) {
        if (holder.btnPin != null) {
            // 设置按钮图标
            if (user.isPinned()) {
                holder.btnPin.setImageResource(R.drawable.ic_pinned);
                holder.btnPin.setColorFilter(Color.parseColor("#FF5722")); // 橙色
                // 可以改变item背景色
                holder.itemView.setBackgroundColor(Color.parseColor("#FFF8E1")); // 浅黄色背景
            } else {
                holder.btnPin.setImageResource(R.drawable.ic_unpinned);
                holder.btnPin.setColorFilter(Color.parseColor("#757575")); // 灰色
                holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            }
        }

//        // 如果布局中有置顶标识文本，也更新
//        if (holder.tvPinnedIndicator != null) {
//            holder.tvPinnedIndicator.setVisibility(user.isPinned() ? View.VISIBLE : View.GONE);
//        }
    }

//    private void reorderUserList() {
//        // 将用户列表按置顶状态和时间戳排序
//        mUserList.sort((u1, u2) -> {
//            // 首先按置顶状态排序：置顶的在前
//            if (u1.isPinned() && !u2.isPinned()) {
//                return -1; // u1在前
//            } else if (!u1.isPinned() && u2.isPinned()) {
//                return 1; // u2在前
//            } else {
//                // 如果置顶状态相同，按最新消息时间戳降序排序
//                return Long.compare(u2.getLastMessageTimestamp(), u1.getLastMessageTimestamp());
//            }
//        });
//    }

    @Override
    public int getItemCount() {
        return mUserList.size(); // 已确保mUserList不为null
    }

    // 【核心新增】供LiveData观察者调用的方法
    public void setUserList(List<User> newUserList) {
        this.mUserList = newUserList != null ? newUserList : new ArrayList<User>();
        mSearchKeyword = ""; // 清除搜索关键词
        notifyDataSetChanged(); // 通知列表整体刷新
    }

    public void setUserListWithKeyword(List<User> newUserList, String keyword) {
        this.mUserList = newUserList != null ? newUserList : new ArrayList<User>();
        this.mSearchKeyword = keyword != null ? keyword : "";
        notifyDataSetChanged();
    }

    // 新增：供搜索功能使用的方法
    public void updateSearchResults(List<User> searchResults, String keyword) {
        setUserListWithKeyword(searchResults, keyword);
        notifyDataSetChanged();
    }




    // 保留旧的updateData方法，但改为接收User列表（兼容旧代码）
    public void updateData(List<User> newUserList) {
        setUserList(newUserList); // 直接调用新方法
    }

    // 【可选】用于“加载更多”时追加数据的方法
    public void addUserList(List<User> newUsers) {
        if (newUsers != null && !newUsers.isEmpty()) {
            int startPosition = mUserList.size();
            mUserList.addAll(newUsers);
            notifyItemRangeInserted(startPosition, newUsers.size());
        }
    }

    // 【可选】获取指定位置用户
    public User getUserAt(int position) {
        if (position >= 0 && position < mUserList.size()) {
            return mUserList.get(position);
        }
        return null;
    }
}