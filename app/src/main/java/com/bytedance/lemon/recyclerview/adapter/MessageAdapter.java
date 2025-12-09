// MessageAdapter.java - 完整修改版本
package com.bytedance.lemon.recyclerview.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bytedance.lemon.R;
import com.bytedance.lemon.recyclerview.entity.Usermessage;
import com.bytedance.lemon.recyclerview.utils.DateUtil;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Usermessage> messages = new ArrayList<>();
    private long myUserId;
    private OnOperationButtonClickListener operationButtonClickListener;

    // 头像缓存
    private Map<Long, String> avatarMap = new HashMap<>();

    // 消息类型常量
    private static final int TYPE_LEFT = 0;
    private static final int TYPE_RIGHT = 1;
    private static final int TYPE_OPERATION = 2;

    public interface OnOperationButtonClickListener {
        void onOperationButtonClick(Usermessage message, String actionUrl);
    }

    public void setMyUserId(long myUserId) {
        this.myUserId = myUserId;
    }

    public void setMessages(List<Usermessage> messages) {
        this.messages = messages != null ? messages : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addUserAvatar(long userId, String avatarUrl) {
        avatarMap.put(userId, avatarUrl);
    }

    public void setOnOperationButtonClickListener(OnOperationButtonClickListener listener) {
        this.operationButtonClickListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case TYPE_LEFT:
                View leftView = inflater.inflate(R.layout.item_message_left, parent, false);
                return new LeftMessageViewHolder(leftView);

            case TYPE_RIGHT:
                View rightView = inflater.inflate(R.layout.item_message_right, parent, false);
                return new RightMessageViewHolder(rightView);

            case TYPE_OPERATION:
                View operationView = inflater.inflate(R.layout.item_message_operation, parent, false);
                return new OperationMessageViewHolder(operationView);

            default:
                View defaultView = inflater.inflate(R.layout.item_message_left, parent, false);
                return new LeftMessageViewHolder(defaultView);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Usermessage message = messages.get(position);

        if (message.isOperationMessage()) {
            return TYPE_OPERATION;
        } else if (message.getUserId() == myUserId || message.getMessageType() == 1) {
            return TYPE_RIGHT;
        } else {
            return TYPE_LEFT;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Usermessage message = messages.get(position);

        switch (holder.getItemViewType()) {
            case TYPE_LEFT:
                ((LeftMessageViewHolder) holder).bind(message);
                break;

            case TYPE_RIGHT:
                ((RightMessageViewHolder) holder).bind(message);
                break;

            case TYPE_OPERATION:
                ((OperationMessageViewHolder) holder).bind(message);
                break;
        }

        // 添加调试日志
        Log.d("MessageAdapter", "绑定消息位置: " + position +
                ", 类型: " + holder.getItemViewType() +
                ", 内容: " + message.getContent() +
                ", 消息类型: " + message.getMessageType());

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // 左消息ViewHolder（对方消息）
    class LeftMessageViewHolder extends RecyclerView.ViewHolder {
        ImageView ivMessageAvatar;
        TextView tvMessageContent;
        TextView tvMessageTime;
        ImageView ivMessageImage;

        public LeftMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMessageAvatar = itemView.findViewById(R.id.iv_message_avatar);
            tvMessageContent = itemView.findViewById(R.id.tv_message_content);
            tvMessageTime = itemView.findViewById(R.id.tv_message_time);
            ivMessageImage = itemView.findViewById(R.id.iv_message_Image);
        }

        public void bind(Usermessage message) {
            tvMessageContent.setText(message.getContent());
            tvMessageTime.setText(DateUtil.getTimeString(message.getTimestamp()));


            // 加载头像 - 处理 null userId
            Long userId = message.getUserId();
            if (userId != null) {
                String avatarUrl = avatarMap.get(userId);
                if (avatarUrl != null && ivMessageAvatar != null) {
                    Glide.with(itemView.getContext())
                            .load(avatarUrl)
                            .placeholder(R.drawable.avator_15)
                            .into(ivMessageAvatar);
                } else if (ivMessageAvatar != null) {
                    // 加载默认头像
                    Glide.with(itemView.getContext())
                            .load(R.drawable.avator_15)
                            .into(ivMessageAvatar);
                }
            }

            // 加载消息图像
            String messageImageUrl = message.getMessageImageUrl();
            if (messageImageUrl != null && !messageImageUrl.isEmpty() && ivMessageImage != null) {
                ivMessageImage.setVisibility(View.VISIBLE);
                Glide.with(itemView.getContext())
                        .load(messageImageUrl)
                        .placeholder(R.drawable.avator_12)
                        .into(ivMessageImage);
            } else {
                ivMessageImage.setVisibility(View.GONE);
            }
        }
    }

    // 右消息ViewHolder（我方消息）
    class RightMessageViewHolder extends RecyclerView.ViewHolder {
        ImageView ivMessageAvatar_me;
        TextView tvMessageContent_me;
        TextView tvMessageTime_me;

        public RightMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMessageAvatar_me = itemView.findViewById(R.id.iv_message_avatar_me);
            tvMessageContent_me = itemView.findViewById(R.id.tv_message_content_me);
            tvMessageTime_me = itemView.findViewById(R.id.tv_message_time_me);
        }

        public void bind(Usermessage message) {
            tvMessageContent_me.setText(message.getContent());
            tvMessageTime_me.setText(DateUtil.getTimeString(message.getTimestamp()));

            // 加载我方头像
            String avatarUrl = "https://img95.699pic.com/photo/50136/1351.jpg_wh300.jpg";
            Glide.with(itemView.getContext())
                    .load(avatarUrl)
                    .placeholder(R.drawable.avator_15)
                    .into(ivMessageAvatar_me);
        }
    }

    // 运营消息ViewHolder
    class OperationMessageViewHolder extends RecyclerView.ViewHolder {
        ImageView operation_avatar;
        ImageView ivOperationIcon;
        TextView tvOperationContent;
        Button btnOperationAction;
        TextView tvOperationTime;

        private static final String TAG = "OperationViewHolder";

        public OperationMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            operation_avatar = itemView.findViewById(R.id.iv_operation_avatar);
            ivOperationIcon = itemView.findViewById(R.id.iv_operation_icon);
            tvOperationContent = itemView.findViewById(R.id.tv_operation_content);
            btnOperationAction = itemView.findViewById(R.id.btn_operation_action);
            tvOperationTime = itemView.findViewById(R.id.tv_operation_time);
            Log.d(TAG, "OperationMessageViewHolder 初始化完成");
        }

        public void bind(Usermessage message) {

            tvOperationContent.setText(message.getContent());
            tvOperationTime.setText(DateUtil.getTimeString(message.getTimestamp()));

            Long userId = message.getUserId();
            if (userId != null) {
                String avatarUrl = avatarMap.get(userId);
                if (avatarUrl != null && operation_avatar != null) {
                    Glide.with(itemView.getContext())
                            .load(avatarUrl)
                            .placeholder(R.drawable.avator_15)
                            .into(operation_avatar);
                } else if (operation_avatar != null) {
                    // 加载默认头像
                    Glide.with(itemView.getContext())
                            .load(R.drawable.avator_15)
                            .into(operation_avatar);
                }
            }


            // 解析运营数据
            JSONObject operationData = message.getOperationDataJson();
            if (operationData != null) {
                try {
                    String buttonText = operationData.optString("buttonText", "领取奖励");
                    final String actionUrl = operationData.optString("actionUrl", "");

                    btnOperationAction.setText(buttonText);
                    btnOperationAction.setOnClickListener(v -> {
                        Log.d(TAG, "运营按钮被点击: " + buttonText + ", URL: " + actionUrl);

                        if (operationButtonClickListener != null) {
                            operationButtonClickListener.onOperationButtonClick(message, actionUrl);
                        } else {
                            // 默认处理：显示Toast并跳转
                            Toast.makeText(
                                    itemView.getContext(),
                                    "领取成功！",
                                    Toast.LENGTH_SHORT
                            ).show();

                            // 这里可以处理跳转逻辑
                            if (!actionUrl.isEmpty()) {
                                // handleActionUrl(actionUrl);
                            }
                        }
                    });
                    btnOperationAction.setVisibility(View.VISIBLE);
                    Log.d(TAG, "设置按钮文字: " + buttonText);

                } catch (Exception e) {
                    e.printStackTrace();
                    btnOperationAction.setVisibility(View.GONE);
                }
            } else {
                btnOperationAction.setVisibility(View.GONE);
                Log.d(TAG, "operationData 为 null");
            }
        }
    }
}