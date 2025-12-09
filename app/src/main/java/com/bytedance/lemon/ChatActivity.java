package com.bytedance.lemon;

import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bytedance.lemon.recyclerview.adapter.MessageAdapter;
import com.bytedance.lemon.recyclerview.entity.User;
import com.bytedance.lemon.recyclerview.entity.Usermessage;
import com.bytedance.lemon.recyclerview.viewmodel.UserViewModel;

import org.json.JSONObject;

import java.util.List;

public class ChatActivity extends AppCompatActivity implements MessageAdapter.OnOperationButtonClickListener{

    private ImageView ivAvatar;
    private TextView tvUserName;
    private RecyclerView rvMessages;
    private EditText etMessageInput;
    private Button btnSend;

    private MessageAdapter messageAdapter;
    private UserViewModel userViewModel;
    private long currentUserId;

    // 我方信息（固定）
    private static final long MY_USER_ID = 0; // 固定ID表示我方
    private static final String MY_USER_NAME = "我";
    private static final int DRAWABLE_MY_AVATAR = R.drawable.avator_19; // 我方头像资源

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initViews();
        initData();
        setupViewModel();
        setupInputListeners();


        // 测试：发送一条运营消息（实际中可能由后台触发）
//        sendTestOperationMessage();

//        addTestButton();

    }

    private void initViews() {
        ivAvatar = findViewById(R.id.iv_chat_avatar);
        tvUserName = findViewById(R.id.tv_chat_user_name);
        rvMessages = findViewById(R.id.rv_messages);
        etMessageInput = findViewById(R.id.et_message_input);
        btnSend = findViewById(R.id.btn_send);

        // 设置消息列表适配器
        messageAdapter = new MessageAdapter();
        messageAdapter.setMyUserId(MY_USER_ID); // 设置我方ID用于区分消息方向
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(messageAdapter);

        // 返回按钮
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        messageAdapter.setOnOperationButtonClickListener(this); // 设置监听器


    }


    private void initData() {
        currentUserId = getIntent().getLongExtra("USER_ID", -1);
        if (currentUserId == -1) {
            finish();
        }
    }

    private void setupInputListeners() {
        // 监听输入框变化
        etMessageInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnSend.setEnabled(s.toString().trim().length() > 0);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // 发送按钮点击事件
        btnSend.setOnClickListener(v -> sendMessage());

        // 软键盘发送键监听
        etMessageInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });
    }

    private void sendMessage() {
        String content = etMessageInput.getText().toString().trim();
        if (content.isEmpty()) {
            return;
        }

        long timestamp = System.currentTimeMillis();

        // 创建我方发送的消息
        Usermessage myMessage = new Usermessage(
                MY_USER_ID,
                content,
                timestamp,
                1, // 消息类型：1=发送的消息
                currentUserId // 接收者ID
        );

        // 发送消息到数据库
        userViewModel.sendMessage(myMessage);

        Log.d("ChatActivity", "我已发送消息: " + content + " 给用户ID: " + currentUserId);

        // 清空输入框
        etMessageInput.setText("");

        // 关闭软键盘
        android.view.inputmethod.InputMethodManager imm =
                (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(etMessageInput.getWindowToken(), 0);
        }
    }

    private void setupViewModel() {
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // 观察对方用户信息
        userViewModel.getUserByIdLive(currentUserId).observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                if (user != null) {
                    updateUserInfo(user);

                    // 将对方用户头像添加到适配器
                    messageAdapter.addUserAvatar(user.getId(), user.getAvatarUrl());

                    // 将我方用户头像也添加到适配器
                    messageAdapter.addUserAvatar(MY_USER_ID, "drawable://" + DRAWABLE_MY_AVATAR);

                    // 添加系统头像（用于运营消息）
//                    messageAdapter.addUserAvatar(user.getId(), user.getAvatarUrl());

                }
            }
        });

        // 观察聊天消息（合并对方发送的消息和我方发送的消息和运营消息）
        userViewModel.getCombinedMessagesLive(currentUserId, MY_USER_ID).observe(this,
                new Observer<List<Usermessage>>() {
                    @Override
                    public void onChanged(List<Usermessage> messages) {
                        if (messages != null) {
                            Log.d("ChatActivity", "收到消息列表，数量: " + messages.size());

                            // 打印每条消息详情用于调试
                            for (Usermessage msg : messages) {
                                String type;
                                if (msg.getMessageType() == 1) {
                                    type = "发送";
                                } else if (msg.getMessageType() == 2) {
                                    type = "运营";
                                } else {
                                    type = "接收";
                                }

                                Log.d("ChatActivity", "消息: " + msg.getContent() +
                                        " 类型: " + type +
                                        " 发送者: " + msg.getUserId() +
                                        " 接收者: " + msg.getReceiverId() +
                                        " 时间: " + msg.getTimestamp());
                            }

                            messageAdapter.setMessages(messages);

                            // 滚动到底部
                            if (!messages.isEmpty()) {
                                rvMessages.smoothScrollToPosition(messages.size() - 1);
                            }
                        } else {
                            Log.d("ChatActivity", "消息列表为空");
                        }
                    }
                });
    }



    private void updateUserInfo(User user) {
        tvUserName.setText(user.getName());

        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            Glide.with(this)
                    .load(user.getAvatarUrl())
                    .placeholder(R.drawable.avator_15)
                    .into(ivAvatar);
        }
    }


    // 发送测试运营消息
    private void sendTestOperationMessage() {
        // 创建运营消息 - 现在 userId 为 null
        Usermessage operationMessage = Usermessage.createOperationMessage(
                "🎁 恭喜！您获得了一份专属奖励",
                "立即领取",
                "lemonapp://reward/detail?id=123"
        );

//         设置发送者为当前聊天用户
        operationMessage.setUserId(currentUserId);
        operationMessage.setReceiverId(currentUserId);

        // 发送到数据库
        userViewModel.sendMessage(operationMessage);

        Log.d("ChatActivity", "用户: " + currentUserId +"已发送运营消息给我(0)");
    }


    @Override
    public void onOperationButtonClick(Usermessage message, String actionUrl) {
        Log.d("feiyuan", "运营按钮点击: " + actionUrl);

        // 根据actionUrl处理不同的操作
        if (actionUrl.startsWith("lemonapp://reward/")) {
            showRewardDialog(message);
        } else if (actionUrl.startsWith("lemonapp://activity/")) {
            openActivity(actionUrl);
        } else if (actionUrl.startsWith("http")) {
            openWebView(actionUrl);
        }
        else {

            showRewardDialog(message);
        }
    }

    private void showRewardDialog(Usermessage message) {
        // 显示奖励领取对话框
        RewardDialogFragment dialog = RewardDialogFragment.newInstance(message.getContent());
        dialog.show(getSupportFragmentManager(), "RewardDialog");

        // 更新消息状态（已领取）
        try {
            JSONObject operationData = message.getOperationDataJson();
            if (operationData != null) {
                operationData.put("claimed", true);
                operationData.put("claimedTime", System.currentTimeMillis());
                message.setOperationData(operationData.toString());
                userViewModel.updateMessage(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void openActivity(String actionUrl) {
        // 解析URL并打开对应Activity
        // ...
    }

    private void openWebView(String url) {
        // 打开WebView显示网页
        // ...
    }



    private void addTestButton() {
        // 在右上角添加一个测试按钮
        Button btnTest = new Button(this);
        btnTest.setText("测试运营消息");
        btnTest.setBackgroundColor(0xFF4CAF50);
        btnTest.setTextColor(Color.WHITE);

        // 添加到布局
        ViewGroup rootView = findViewById(android.R.id.content);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.END | Gravity.TOP;
        params.topMargin = 100;
        params.rightMargin = 20;
        rootView.addView(btnTest, params);

        // 设置点击事件
        btnTest.setOnClickListener(v -> {
            sendTestOperationMessage();
            Toast.makeText(this, "已发送测试运营消息", Toast.LENGTH_SHORT).show();
        });
    }


}