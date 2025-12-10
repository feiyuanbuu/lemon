// UserRepository.java (添加Widget功能)
package com.bytedance.lemon.recyclerview.repository;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.bytedance.lemon.recyclerview.database.AppDatabase;
import com.bytedance.lemon.recyclerview.database.MessageDao;
import com.bytedance.lemon.recyclerview.database.UserDao;
import com.bytedance.lemon.recyclerview.entity.User;
import com.bytedance.lemon.recyclerview.entity.Usermessage;
import com.bytedance.lemon.recyclerview.utils.AvatarImageUrlList;
import com.bytedance.lemon.recyclerview.widget.MessageAlertWidget;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UserRepository {
    private UserDao userDao;
    private MessageDao messageDao;
    private LiveData<List<User>> allUsers;
    private final ExecutorService executorService;
    private final ScheduledExecutorService scheduledExecutorService;
    private final ScheduledExecutorService operationExcutorService;

    // Widget相关
    private Context appContext;
    private Handler widgetHandler;
    private Runnable widgetAutoClearRunnable;
    private static final long WIDGET_DISPLAY_DURATION = 10000; // Widget显示10秒后自动关闭

    private static final String TAG = "UserRepository";
    private static UserRepository INSTANCE;
    private static boolean isAutoMessagingStarted = false;

    // 选择用于自动发送消息的两个用户ID
    private long[] autoMessageUserIds = new long[2];
    private int currentMessageIndex = 0;

    private static final long MY_USER_ID = 0; // 表示我方用户ID

    private String[] autoMessages = {
            "你好，今天天气不错",
            "在忙什么呢？",
            "吃午饭了吗？",
            "晚上一起吃饭？",
            "这个项目进展如何？",
            "周末有什么计划？",
            "你看过那部新电影了吗？",
            "代码写完了吗？",
            "这个bug修复了",
            "明天开会记得准备"
    };

    public UserRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        userDao = db.userDao();
        messageDao = db.messageDao();
        allUsers = userDao.getAllUsersLive();
        executorService = Executors.newSingleThreadExecutor();
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        operationExcutorService = Executors.newSingleThreadScheduledExecutor();

        // Widget初始化
        appContext = application.getApplicationContext();
        widgetHandler = new Handler(Looper.getMainLooper());

        Log.d(TAG, "UserRepository 实例创建");
    }

    public static synchronized UserRepository getInstance(Application application) {
        if (INSTANCE == null) {
            INSTANCE = new UserRepository(application);
        }
        return INSTANCE;
    }

    // Widget相关方法 ===========================================================

    /**
     * 触发Widget显示新消息提醒
     * @param sender 发送消息的用户
     * @param message 消息内容
     */
    public void triggerWidgetAlert(User sender, String message) {
        if (appContext == null || sender == null) {
            Log.w(TAG, "无法触发Widget: Context或用户为空");
            return;
        }

        executorService.execute(() -> {
            try {
                // 在主线程发送广播更新Widget
                widgetHandler.post(() -> {
                    Intent widgetIntent = new Intent(MessageAlertWidget.ACTION_UPDATE_WIDGET);
                    widgetIntent.putExtra("user_id", sender.getId());
                    widgetIntent.putExtra("user_name", sender.getName());
                    widgetIntent.putExtra("avatar_url", sender.getAvatarUrl());
                    widgetIntent.putExtra("message", message);
                    widgetIntent.putExtra("timestamp", sender.getLastMessageTimestamp());

                    appContext.sendBroadcast(widgetIntent);
                    Log.d(TAG, "Widget触发广播已发送: " + sender.getName() + " - " + message);

                    // 取消之前的自动清除任务（如果存在）
                    if (widgetAutoClearRunnable != null) {
                        widgetHandler.removeCallbacks(widgetAutoClearRunnable);
                    }

                    // 设置10秒后自动关闭Widget
                    widgetAutoClearRunnable = new Runnable() {
                        @Override
                        public void run() {
                            clearWidgetAlert();
                        }
                    };
                    widgetHandler.postDelayed(widgetAutoClearRunnable, WIDGET_DISPLAY_DURATION);
                });
            } catch (Exception e) {
                Log.e(TAG, "触发Widget失败", e);
            }
        });
    }

    /**
     * 清除Widget提醒
     */
    public void clearWidgetAlert() {
        if (appContext == null) return;

        widgetHandler.post(() -> {
            try {
                Intent closeIntent = new Intent(MessageAlertWidget.ACTION_CLOSE_WIDGET);
                appContext.sendBroadcast(closeIntent);
                Log.d(TAG, "Widget已清除");
            } catch (Exception e) {
                Log.e(TAG, "清除Widget失败", e);
            }
        });
    }

    /**
     * 手动测试Widget触达
     */
//    public void testWidgetAlert() {
//        executorService.execute(() -> {
//            List<User> allUsers = getAllUsers();
//            if (allUsers != null && allUsers.size() > 1) {
//                List<User> otherUsers = new ArrayList<>();
//                for (User user : allUsers) {
//                    if (user.getId() != 0) { // 排除我自己
//                        otherUsers.add(user);
//                    }
//                }
//
//                if (!otherUsers.isEmpty()) {
//                    Random random = new Random();
//                    User randomUser = otherUsers.get(random.nextInt(otherUsers.size()));
//
//                    String[] testMessages = {
//                            "🎉 您有一条新消息！",
//                            "📱 轻点查看完整对话",
//                            "💬 有新消息，点击回复",
//                            "✨ 好友发来一条消息"
//                    };
//
//                    String testMessage = testMessages[random.nextInt(testMessages.length)];
//
//                    // 触发Widget显示
//                    triggerWidgetAlert(randomUser, testMessage);
//
//                    // 发送一条测试消息到数据库
//                    Usermessage message = new Usermessage(
//                            randomUser.getId(),
//                            testMessage,
//                            System.currentTimeMillis(),
//                            0,
//                            MY_USER_ID
//                    );
//                    sendMessage(message);
//                }
//            }
//        });
//    }

    /**
     * 检查Widget是否已添加到桌面
     */
    public boolean isWidgetActive() {
        if (appContext == null) return false;

        try {
            android.appwidget.AppWidgetManager appWidgetManager =
                    android.appwidget.AppWidgetManager.getInstance(appContext);
            android.content.ComponentName widgetComponent =
                    new android.content.ComponentName(appContext, MessageAlertWidget.class);
            int[] widgetIds = appWidgetManager.getAppWidgetIds(widgetComponent);
            return widgetIds != null && widgetIds.length > 0;
        } catch (Exception e) {
            Log.e(TAG, "检查Widget状态失败", e);
            return false;
        }
    }

    // 自动消息系统（修改部分）=======================================================

    public void initAutoMessagingOnce() {
        if (isAutoMessagingStarted) {
            Log.d(TAG, "自动消息已经启动，跳过");
            return;
        }

        executorService.execute(() -> {
            List<User> users = getAllUsers();
            Log.d(TAG, "获取到的用户数量: " + (users != null ? users.size() : 0));

            if (users != null && users.size() >= 2) {
                selectRandomTwoUsers(users);

                User user1 = getUserById(autoMessageUserIds[0]);
                User user2 = getUserById(autoMessageUserIds[1]);

                Log.d(TAG, "随机选择的两个用户: " +
                        (user1 != null ? user1.getName() : "用户1") + " (ID: " + autoMessageUserIds[0] + ") 和 " +
                        (user2 != null ? user2.getName() : "用户2") + " (ID: " + autoMessageUserIds[1] + ")");

                // 发送初始消息，并触发Widget
                String[] initialMessages = {"你好！这是第一条自动消息", "你好！我也收到自动消息了"};
                for (int i = 0; i < 2; i++) {
                    Usermessage message = new Usermessage(
                            autoMessageUserIds[i],
                            initialMessages[i],
                            System.currentTimeMillis(),
                            0,
                            MY_USER_ID
                    );
                    message.setMessageImageUrl(AvatarImageUrlList.getRandom());
                    sendMessage(message);

                    // 触发Widget显示（只显示第一个用户的）
//                    if (i == 0 && user1 != null) {
//                        triggerWidgetAlert(user1, initialMessages[i]);
//                    }
                }

                // 启动定时任务
                scheduledExecutorService.scheduleAtFixedRate(() -> {
                    sendAutoMessage();
                }, 10, 10, TimeUnit.SECONDS);

                scheduleOperationMessages(users);


                isAutoMessagingStarted = true;
                Log.d(TAG, "自动消息发送已启动，每10秒向随机两个用户发送消息给我方");
            } else if (users != null && users.size() == 1) {
                autoMessageUserIds[0] = users.get(0).getId();
                autoMessageUserIds[1] = users.get(0).getId();

                User user = getUserById(autoMessageUserIds[0]);
                if (user != null) {
                    Log.d(TAG, "只有一个用户: " + user.getName() + "，将向他发送自动消息给我方");

                    // 发送初始消息并触发Widget
                    String initialMessage = "你好！这是自动消息";
                    Usermessage message = new Usermessage(
                            autoMessageUserIds[0],
                            initialMessage,
                            System.currentTimeMillis(),
                            0,
                            MY_USER_ID
                    );
                    sendMessage(message);
//                    triggerWidgetAlert(user, initialMessage);
                }

                scheduledExecutorService.scheduleAtFixedRate(() -> {
                    sendAutoMessage();
                }, 10, 10, TimeUnit.SECONDS);

                isAutoMessagingStarted = true;
            } else {
                Log.w(TAG, "用户数量不足，无法启动自动消息");
            }
        });
    }

    // 自动发送消息（修改为触发Widget）
    private void sendAutoMessage() {
        executorService.execute(() -> {
            // 在两个用户之间轮换
            int userIndex = currentMessageIndex % 2;
            long userId = autoMessageUserIds[userIndex];

            // 获取用户信息
            User user = userDao.getUserById(userId);
            if (user == null) return;

            String userName = user.getName();
            String messageContent = autoMessages[currentMessageIndex % autoMessages.length];

            Log.d(TAG, "向用户 " + userName + " (ID: " + userId + ") 发送消息给我方: " + messageContent);

            // 创建消息对象
            Usermessage message = new Usermessage(
                    userId,
                    messageContent,
                    System.currentTimeMillis(),
                    0,
                    MY_USER_ID
            );

            // 发送消息
            sendMessage(message);

            // 【核心修改】每次自动发送消息时都触发Widget显示
//            triggerWidgetAlert(user, messageContent);

            currentMessageIndex++;
            if (currentMessageIndex > 1000) {
                currentMessageIndex = 0;
            }
        });
    }

    // 发送消息方法（修改为可能触发Widget）
    // 核心，所有消息发送的地方
    public void sendMessage(Usermessage message) {
        executorService.execute(() -> {
            try {
                // 保存消息到数据库
                messageDao.insert(message);

                Log.d(TAG, "已发送消息: " + message.getContent() +
                        " 类型: " + (message.getMessageType() == 1 ? "发送" :
                        message.getMessageType() == 2 ? "运营" : "接收") +
                        " 发送者: " + message.getUserId() +
                        " 接收者: " + message.getReceiverId());

                // 更新相关用户信息
                switch (message.getMessageType()) {
                    case 0: // 接收的消息（对方发送的）
                        if (message.getUserId() != null) {
                            User sender = userDao.getUserById(message.getUserId());
                            if (sender != null) {
                                sender.setNewest_info(message.getContent());
                                sender.setLastMessageTimestamp(message.getTimestamp());
                                sender.incrementUnreadCount();
                                userDao.update(sender);
                                Log.d(TAG, "已更新发送方用户最新消息: " + sender.getName());
                                triggerWidgetAlert(sender, message.getContent());
                                // 【新增】对于接收的消息，可能触发Widget（但自动消息已经在sendAutoMessage中触发了）
                                // 这里主要是为了手动发送的消息
//                                if (!isAutoMessagingStarted) {
//                                    triggerWidgetAlert(sender, message.getContent());
//                                }
                            }
                        }
                        break;

                    case 1: // 发送的消息（我方发送的）
                        User receiver = userDao.getUserById(message.getReceiverId());
                        if (receiver != null) {
                            receiver.setNewest_info(message.getContent());
                            receiver.setLastMessageTimestamp(message.getTimestamp());
                            userDao.update(receiver);
                            Log.d(TAG, "已更新接收方用户最新消息: " + receiver.getName());
                        }
                        break;

                    case 2: // 运营消息
                        User operationSender = userDao.getUserById(message.getUserId());
                        if (operationSender != null) {
                            String operationContent = "运营消息" + message.getContent();
                            operationSender.setNewest_info(operationContent);
                            operationSender.setLastMessageTimestamp(message.getTimestamp());
                            operationSender.incrementUnreadCount();
                            userDao.incrementUnreadInfoCount(operationSender.getId());
                            userDao.update(operationSender);
                            Log.d(TAG, "已更新运营消息接收方用户: " + operationSender.getName());

                            // 【新增】运营消息也触发Widget
                            triggerWidgetAlert(operationSender, message.getContent());
                        }
                        break;
                }

            } catch (Exception e) {
                Log.e(TAG, "发送消息失败: " + e.getMessage(), e);
                if (e.getMessage() != null && e.getMessage().contains("FOREIGN KEY")) {
                    Log.w(TAG, "外键约束错误，尝试使用系统用户发送");
                    if (message.getUserId() == null) {
                        message.setUserId(0);
                        sendMessage(message);
                    }
                }
            }
        });
    }

    // 运营消息发送（修改为触发Widget）
    private void sendOperationMessage(List<User> users) {
        operationExcutorService.execute(() -> {
            // 运营消息模板
            String[] operationTemplates = {
                    "🎉 限时福利！完成任务领取现金红包",
                    "📢 新活动上线，参与即有机会赢取大奖",
                    "🎁 您的专属优惠券已到账，点击领取",
                    "⭐ 每日签到，连续7天领取神秘奖励",
                    "🔥 热门活动：邀请好友得现金奖励"
            };

            String[] buttonTexts = {
                    "立即参与",
                    "查看详情",
                    "领取优惠券",
                    "去签到",
                    "邀请好友"
            };

            Random random = new Random();
            int index = random.nextInt(operationTemplates.length);
            int userIndex = random.nextInt(users.size());

            User selectedUser = users.get(userIndex);

            // 创建运营消息
            Usermessage operationMessage = Usermessage.createOperationMessage(
                    operationTemplates[index],
                    buttonTexts[index],
                    "lemonapp://operation/" + index
            );

            operationMessage.setUserId(selectedUser.getId());

            // 发送消息
            sendMessage(operationMessage);

            // 【新增】运营消息触发Widget
//            triggerWidgetAlert(selectedUser, operationTemplates[index]);

            Log.d(TAG, "用户" + selectedUser.getName() + "已发送运营消息: " + operationTemplates[index]);
        });
    }

    // 停止所有任务（包括Widget相关）
    public void stopAllServices() {
        // 停止自动消息
        stopAutoMessaging();

        // 停止运营消息
        if (operationExcutorService != null && !operationExcutorService.isShutdown()) {
            operationExcutorService.shutdown();
        }

        // 清除Widget定时任务
        if (widgetHandler != null && widgetAutoClearRunnable != null) {
            widgetHandler.removeCallbacks(widgetAutoClearRunnable);
        }

        // 清除Widget显示
        clearWidgetAlert();

        Log.d(TAG, "所有服务已停止");
    }

    // ===========================================================================
    // 以下是你原有的方法，保持不变
    // ===========================================================================

    public LiveData<List<User>> getAllUsersLive() {
        return allUsers;
    }

    public LiveData<List<Usermessage>> getMessagesByUserIdLive(long userId) {
        return messageDao.getMessagesByUserIdLive(userId);
    }

    public List<Usermessage> getMessagesByUserId(long userId) {
        return messageDao.getMessagesByUserId(userId);
    }

    private Random random = new Random();

    private void selectRandomTwoUsers(List<User> users) {
        if (users.size() == 2) {
            autoMessageUserIds[0] = users.get(0).getId();
            autoMessageUserIds[1] = users.get(1).getId();
            return;
        }

        int index1 = random.nextInt(users.size());
        int index2;
        do {
            index2 = random.nextInt(users.size());
        } while (index2 == index1);

        autoMessageUserIds[0] = users.get(index1).getId();
        autoMessageUserIds[1] = users.get(index2).getId();

        Log.d(TAG, "随机选择用户索引: " + index1 + " 和 " + index2);
    }

    public void stopAutoMessaging() {
        scheduledExecutorService.shutdown();
        isAutoMessagingStarted = false;
        Log.d(TAG, "自动消息已停止");
    }

    public boolean isAutoMessagingRunning() {
        return isAutoMessagingStarted && !scheduledExecutorService.isShutdown();
    }

    public void insert(User user) {
        executorService.execute(() -> userDao.insert(user));
    }

    public void insertAll(List<User> users) {
        executorService.execute(() -> userDao.insertAll(users));
    }

    public void update(User user) {
        executorService.execute(() -> userDao.update(user));
    }

    public int getUserCount() {
        Future<Integer> future = executorService.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return userDao.getUserCount();
            }
        });

        try {
            return future.get(2, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void updatePinnedStatus(long userId, boolean isPinned) {
        executorService.execute(() -> {
            User user = userDao.getUserById(userId);
            if (user != null) {
                user.setPinned(isPinned);
                userDao.update(user);
                Log.d(TAG, "用户置顶状态已更新: ID=" + userId + ", isPinned=" + isPinned);
            } else {
                Log.e(TAG, "未找到用户: ID=" + userId);
            }
        });
    }

    public void togglePinnedStatus(long userId) {
        executorService.execute(() -> {
            User user = userDao.getUserById(userId);
            if (user != null) {
                boolean newPinnedState = !user.isPinned();
                user.setPinned(newPinnedState);
                userDao.update(user);
                Log.d(TAG, "用户置顶状态已切换: ID=" + userId + ", 新状态=" + newPinnedState);
            }
        });
    }

    public LiveData<User> getUserByIdLive(long userId) {
        return userDao.getUserByIdLive(userId);
    }

    public void updateUserDesc(long userId, String newDesc) {
        executorService.execute(() -> {
            User userToUpdate = userDao.getUserById(userId);
            if (userToUpdate != null) {
                userToUpdate.setDescription(newDesc);
                userDao.update(userToUpdate);
            } else {
                Log.e("UserRepository", "未找到ID为 " + userId + " 的用户");
            }
        });
    }

    public void deleteAllUsers() {
        executorService.execute(() -> {
            userDao.deleteAllUsers();
            messageDao.deleteAllMessages();
        });
    }

    public LiveData<List<User>> searchUsersLive(String query) {
        return userDao.searchUsersLive(query);
    }

    private List<User> getAllUsers() {
        return userDao.getAllUsers();
    }

    private User getUserById(long userId) {
        return userDao.getUserById(userId);
    }

    public LiveData<List<Usermessage>> getCombinedMessagesLive(long otherUserId, long myUserId) {
        return messageDao.getChatMessagesLive(otherUserId, myUserId);
    }

    public LiveData<List<Usermessage>> getMessagesByUserId(Long userId) {
        return messageDao.getMessagesByUserIdLive(userId);
    }

    public void insertMessage(Usermessage message) {
        executorService.execute(() -> {
            messageDao.insert(message);
        });
    }

    public void insertMessages(List<Usermessage> messages) {
        executorService.execute(() -> {
            for (Usermessage message : messages) {
                messageDao.insert(message);
                if (message.getMessageType() == 0 && !message.isMessage_isRead()) {
                    userDao.incrementUnreadInfoCount(message.getUserId());
                }
            }
        });
    }

    public void markMessageAsRead(Long messageId) {
        executorService.execute(() -> {
            List<Usermessage> messages = messageDao.getMessagesByUserId(messageId);
            if (messages != null && !messages.isEmpty()) {
                for (Usermessage message : messages) {
                    if (message.getId() == messageId && message.getMessageType() == 0 && !message.isMessage_isRead()) {
                        messageDao.markAsRead(messageId);
                        userDao.decrementUnreadInfoCount(message.getUserId());
                        Log.d("MessageDebug", "Decremented unread count for user: " + message.getUserId());
                        break;
                    }
                }
            }
        });
    }

    public void markAllMessagesAsRead(Long userId) {
        executorService.execute(() -> {
            messageDao.markAllAsReadByUserId(userId);
            userDao.resetUnreadInfoCount(userId);
            Log.d("MessageDebug", "Reset unread count for user: " + userId);
        });
    }

    public int getUnreadInfoCount(Long userId) {
        return messageDao.getUnreadInfoCount(userId);
    }

    public void syncAllUsersUnreadCount() {
        executorService.execute(() -> {
            List<MessageDao.UserUnreadCount> unreadCounts = messageDao.getAllUsersUnreadCount();
            for (MessageDao.UserUnreadCount count : unreadCounts) {
                userDao.updateUnreadInfoCount(count.userId, count.unreadCount);
            }
            Log.d("MessageDebug", "Synced unread counts for all users");
        });
    }

    public void insertUserWithUnreadCount(User user) {
        executorService.execute(() -> {
            user.setUnreadInfoCount(0);
            userDao.insert(user);
        });
    }

    public void insertAllUsersWithUnreadCount(List<User> users) {
        executorService.execute(() -> {
            for (User user : users) {
                user.setUnreadInfoCount(0);
            }
            userDao.insertAll(users);
        });
    }

    public void updateMessage(Usermessage message) {
        executorService.execute(() -> {
            messageDao.update(message);
        });
    }

    public void scheduleOperationMessages(List<User> users) {
        operationExcutorService.scheduleAtFixedRate(() -> {
            sendOperationMessage(users);
        }, 10, 15, TimeUnit.SECONDS);
    }
}