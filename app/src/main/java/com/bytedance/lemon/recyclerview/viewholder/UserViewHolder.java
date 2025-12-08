package com.bytedance.lemon.recyclerview.viewholder;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bytedance.lemon.R;
import com.bytedance.lemon.recyclerview.entity.User;
import com.bytedance.lemon.recyclerview.utils.DateUtil;

/**
 * create by WUzejian on 2025/11/17
 */
public class UserViewHolder extends RecyclerView.ViewHolder {
    public ImageView ivAvatar;
    public TextView tvName;
    public TextView tvNewestinfo;

//    public ImageView noselect;
    public ImageView btnPin ;

    public TextView tvTime;
    public CardView cardView; // 添加这个引用


    public TextView tvUnreadCount; // 新增


    public UserViewHolder(@NonNull View itemView) {
        super(itemView);
        // 绑定视图ID
        ivAvatar = itemView.findViewById(R.id.iv_avatar);
        tvName = itemView.findViewById(R.id.tv_name);
        tvNewestinfo = itemView.findViewById(R.id.tv_newest_info);
//        noselect = itemView.findViewById(R.id.no_select);
        btnPin  = itemView.findViewById(R.id.iv_Pinned);
        tvTime = itemView.findViewById(R.id.tv_time);
        cardView = itemView.findViewById(R.id.card_view); // 初始化cardView
        tvUnreadCount = itemView.findViewById(R.id.tv_unread_count);
    }

    // 绑定数据到视图
    public void bindData(User user) {
        // 设置头像（实际开发中推荐用Glide/Picasso加载网络图片）
//        ivAvatar.setImageResource(user.getAvatarResId());
        String avatarUrl = user.getAvatarUrl();
//        Log.e("UserRepository", "用户的 avatarUrl" + avatarUrl);
        Glide.with(itemView.getContext())
                .load(avatarUrl)
                .placeholder(R.drawable.avator_1)
                .error(R.drawable.avator_19)
                .circleCrop()
                .into(ivAvatar);

        // 设置用户名
        tvName.setText(user.getName());
        // 设置最新消息
        tvNewestinfo.setText(user.getNewest_info());

//        btnPin.setImageResource();
//        if(user.isRead()){
//            noselect.setVisibility(View.GONE);
//        }
//        else{
//            noselect.setVisibility(View.VISIBLE);
//        }

        tvTime.setText(DateUtil.getTimeString(user.getLastMessageTimestamp()));
    }


}
