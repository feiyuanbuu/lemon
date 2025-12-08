// UserRepository.java (修改导入和类型)
package com.bytedance.lemon.recyclerview.repository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.bytedance.lemon.recyclerview.database.AppDatabase;
import com.bytedance.lemon.recyclerview.database.MessageDao;
import com.bytedance.lemon.recyclerview.database.UserDao;
import com.bytedance.lemon.recyclerview.entity.User;
import com.bytedance.lemon.recyclerview.entity.Usermessage; // 修改导入
import com.bytedance.lemon.recyclerview.utils.AvatarImageUrlList;

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

    private static final String TAG = "UserRepository";
    private static UserRepository INSTANCE;
    private static boolean isAutoMessagingStarted = false; // 标记自动消息是否已启动


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
        Log.d(TAG, "UserRepository 实例创建");


    }

    public static synchronized UserRepository getInstance(Application application) {
        if (INSTANCE == null) {
            INSTANCE = new UserRepository(application);
        }
        return INSTANCE;
    }



    public LiveData<List<User>> getAllUsersLive() {
        return allUsers;
    }

    public void initAutoMessagingOnce() {
        if (isAutoMessagingStarted) {
            Log.d(TAG, "自动消息已经启动，跳过");
            return;
        }

        executorService.execute(() -> {
            // 获取所有用户
            List<User> users = getAllUsers();
            Log.d(TAG, "获取到的用户数量: " + (users != null ? users.size() : 0));

            if (users != null && users.size() >= 2) {
                // 随机选择两个不同的用户
                selectRandomTwoUsers(users);

                // 记录选择的用户
                User user1 = getUserById(autoMessageUserIds[0]);
                User user2 = getUserById(autoMessageUserIds[1]);

                Log.d(TAG, "随机选择的两个用户: " +
                        (user1 != null ? user1.getName() : "用户1") + " (ID: " + autoMessageUserIds[0] + ") 和 " +
                        (user2 != null ? user2.getName() : "用户2") + " (ID: " + autoMessageUserIds[1] + ")");

                // 立即为这两个用户各发送一条初始消息给我方（MY_USER_ID）
                Usermessage message1 = new Usermessage(
                        autoMessageUserIds[0],
                        "你好！这是第一条自动消息",
                        System.currentTimeMillis(),
                        0,  // 0表示对方发送的消息
                        MY_USER_ID  // 接收者是我方
                );
                message1.setMessageImageUrl(AvatarImageUrlList.getRandom());
                sendMessage(message1);


                Usermessage message2 = new Usermessage(
                        autoMessageUserIds[1],
                        "你好！我也收到自动消息了",
                        System.currentTimeMillis(),
                        0,  // 0表示对方发送的消息
                        MY_USER_ID  // 接收者是我方
                );

                sendMessage(message2);
                message2.setMessageImageUrl(AvatarImageUrlList.getRandom());

                // 启动定时任务：每隔10秒发送一条消息
                scheduledExecutorService.scheduleAtFixedRate(() -> {
                    sendAutoMessage();
                }, 10, 10, TimeUnit.SECONDS); // 延迟10秒开始，每10秒发送一次


                scheduleOperationMessages(users);

                isAutoMessagingStarted = true;
                Log.d(TAG, "自动消息发送已启动，每10秒向随机两个用户发送消息给我方");
            } else if (users != null && users.size() == 1) {
                // 只有一个用户的情况
                autoMessageUserIds[0] = users.get(0).getId();
                autoMessageUserIds[1] = users.get(0).getId(); // 同一个用户

                User user = getUserById(autoMessageUserIds[0]);
                if (user != null) {
                    Log.d(TAG, "只有一个用户: " + user.getName() + "，将向他发送自动消息给我方");
                }

                // 发送初始消息
                Usermessage message = new Usermessage(
                        autoMessageUserIds[0],
                        "你好！这是自动消息",
                        System.currentTimeMillis(),
                        0,  // 0表示对方发送的消息
                        MY_USER_ID  // 接收者是我方
                );
                sendMessage(message);

                scheduledExecutorService.scheduleAtFixedRate(() -> {
                    sendAutoMessage();
//                    scheduleOperationMessages(users);
                }, 10, 10, TimeUnit.SECONDS);


//                scheduleOperationMessages(users);

                isAutoMessagingStarted = true;
            } else {
                Log.w(TAG, "用户数量不足，无法启动自动消息");
            }
        });


    }


    // 获取用户的消息历史
    public LiveData<List<Usermessage>> getMessagesByUserIdLive(long userId) {
        return messageDao.getMessagesByUserIdLive(userId);
    }

    // 获取用户的消息历史（同步版本）
    public List<Usermessage> getMessagesByUserId(long userId) {
        return messageDao.getMessagesByUserId(userId);
    }

    //  更新用户的newest_info LastMessageTimestamp& 并保存message

//    public void sendMessage(long userId, String content) {
//        executorService.execute(() -> {
//            long timestamp = System.currentTimeMillis();
//
//            // 1. 更新用户的newest_info和LastMessageTimestamp
//            User user = userDao.getUserById(userId);
//            if (user != null) {
//                user.setNewest_info(content);
//                user.setLastMessageTimestamp(timestamp);
//                user.setRead(false);
//                userDao.update(user);
//            }
//
//            // 2. 保存消息到历史记录
//            Usermessage message = new Usermessage(userId, content, timestamp);
//            messageDao.insert(message);
//
//
//            if (message.getMessageType() == 0 && !message.isMessage_isRead()) {
//                userDao.incrementUnreadInfoCount(message.getUserId());
//                Log.d("MessageDebug", "Incremented unread count for user: " + message.getUserId());
//            }
//
//        });
//    }

    private Random random = new Random();
    // 新增：随机选择两个不同的用户
    private void selectRandomTwoUsers(List<User> users) {
        if (users.size() == 2) {
            // 如果只有两个用户，直接选择
            autoMessageUserIds[0] = users.get(0).getId();
            autoMessageUserIds[1] = users.get(1).getId();
            return;
        }

        // 随机选择两个不同的索引
        int index1 = random.nextInt(users.size());
        int index2;

        // 确保第二个索引与第一个不同
        do {
            index2 = random.nextInt(users.size());
        } while (index2 == index1);

        // 设置两个随机用户ID
        autoMessageUserIds[0] = users.get(index1).getId();
        autoMessageUserIds[1] = users.get(index2).getId();

        Log.d(TAG, "随机选择用户索引: " + index1 + " 和 " + index2);
    }


    // 自动发送消息
    private void sendAutoMessage() {
        executorService.execute(() -> {
            // 在两个用户之间轮换
            int userIndex = currentMessageIndex % 2;
            long userId = autoMessageUserIds[userIndex];

            // 获取用户信息用于日志
            User user = userDao.getUserById(userId);
            String userName = user != null ? user.getName() : "未知用户";

            // 轮换消息内容
            String messageContent = autoMessages[currentMessageIndex % autoMessages.length];

            Log.d(TAG, "向用户 " + userName + " (ID: " + userId + ") 发送消息给我方: " + messageContent);

            // 创建消息对象，明确接收者是我方
            Usermessage message = new Usermessage(
                    userId,
                    messageContent,
                    System.currentTimeMillis(),
                    0,  // 0表示对方发送的消息
                    MY_USER_ID  // 接收者是我方
            );

//            message.setMessageImageUrl(AvatarImageUrlList.getRandom());

            // 使用 sendMyMessage 发送
            sendMessage(message);

            currentMessageIndex++;

            // 如果currentMessageIndex太大，重置一下
            if (currentMessageIndex > 1000) {
                currentMessageIndex = 0;
            }
        });
    }

    // 停止自动发送消息
    public void stopAutoMessaging() {
        scheduledExecutorService.shutdown();
        isAutoMessagingStarted = false;
        Log.d(TAG, "自动消息已停止");
    }


    // 检查自动消息是否正在运行
    public boolean isAutoMessagingRunning() {
        return isAutoMessagingStarted && !scheduledExecutorService.isShutdown();
    }



    // 原有的方法保持不变
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
            // 设置合理的超时时间，比如2秒
            return future.get(2, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            return -1; // 返回-1表示获取失败
        }
    }


//    public void markAsRead(long userId) {
//        executorService.execute(() -> {
//            User user = userDao.getUserById(userId);
//            if (user != null) {
//                user.setRead(true);
//                userDao.update(user);
//            }
//        });
//    }

    public void updatePinnedStatus(long userId, boolean isPinned) {
        executorService.execute(() -> {
            User user = userDao.getUserById(userId);
            if (user != null) {
                user.setPinned(isPinned);
                userDao.update(user);
                Log.d(TAG, "用户置顶状态已更新: ID=" + userId + ", isPinned=" + isPinned);

                // 为了确保UI立即更新，可以发送一个广播或使用回调
                // 但LiveData应该会自动更新，因为数据库已改变
            } else {
                Log.e(TAG, "未找到用户: ID=" + userId);
            }
        });
    }

    // 修改：切换置顶状态（点击时切换）
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
            // 1. 根据ID从数据库查询出完整的用户对象
            User userToUpdate = userDao.getUserById(userId);

            if (userToUpdate != null) {
                // 2. 只更新描述字段，保持其他字段不变
                userToUpdate.setDescription(newDesc); // 假设User实体有setDesc方法

                // 3. 将更新后的对象保存回数据库
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


    // UserRepository.java - 添加同步查询辅助方法
    private List<User> getAllUsers() {
        return userDao.getAllUsers();
    }

    private User getUserById(long userId) {
        return userDao.getUserById(userId);
    }

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

                // 对于所有消息类型，都更新相关用户的 latest_info 和 lastMessageTimestamp
                // 这样主界面的消息列表才能正确显示
                switch (message.getMessageType()) {
                    case 0: // 接收的消息（对方发送的）
                        // 更新发送方用户（对方）的最新消息和时间戳
                        if (message.getUserId() != null) {
                            User sender = userDao.getUserById(message.getUserId());
                            if (sender != null) {
                                sender.setNewest_info(message.getContent());
                                sender.setLastMessageTimestamp(message.getTimestamp());
                                sender.incrementUnreadCount();
                                userDao.update(sender);
                                Log.d(TAG, "已更新发送方用户最新消息: " + sender.getName());
                            }
                        }
                        break;

                    case 1: // 发送的消息（我方发送的）
                        // 更新接收方用户的最新消息和时间戳
                        User receiver = userDao.getUserById(message.getReceiverId());
                        if (receiver != null) {
                            receiver.setNewest_info(message.getContent());
                            receiver.setLastMessageTimestamp(message.getTimestamp());
                            userDao.update(receiver);
                            Log.d(TAG, "已更新接收方用户最新消息: " + receiver.getName());
                        }
                        break;

                    case 2: // 运营消息
                        // 对于运营消息，我们需要更新发送方用户的 latest_info
                        // 这样主界面的消息列表才能显示运营消息
                        User operationSender = userDao.getUserById(message.getUserId());
                        if (operationSender != null) {
                            // 特殊处理：对于运营消息，可以在内容前加上标识
                            String operationContent = "运营消息" + message.getContent();
                            operationSender.setNewest_info(operationContent);
                            operationSender.setLastMessageTimestamp(message.getTimestamp());

                            operationSender.incrementUnreadCount();
                            // 对于运营消息，增加未读计数
                            userDao.incrementUnreadInfoCount(operationSender.getId());

                            userDao.update(operationSender);
                            Log.d(TAG, "已更新运营消息接收方用户: " + operationSender.getName());
                        } else {
                            Log.w(TAG, "运营消息接收方用户不存在: " + message.getReceiverId());
                        }
                        break;
                }

            } catch (Exception e) {
                Log.e(TAG, "发送消息失败: " + e.getMessage(), e);
                // 如果是外键约束错误，可能是 userId 为 null 的问题
                if (e.getMessage() != null && e.getMessage().contains("FOREIGN KEY")) {
                    Log.w(TAG, "外键约束错误，尝试使用系统用户发送");
                    // 尝试使用系统用户 ID
                    if (message.getUserId() == null) {
                        message.setUserId(0); // 使用 0 作为系统用户 ID
                        sendMessage(message); // 重新发送
                    }
                }
            }
        });
    }





    // 新增：获取合并后的消息（对方发送的 + 我方发送的）
    public LiveData<List<Usermessage>> getCombinedMessagesLive(long otherUserId, long myUserId) {
        return messageDao.getChatMessagesLive(otherUserId, myUserId);
    }

    // 获取用户的消息
    public LiveData<List<Usermessage>> getMessagesByUserId(Long userId) {
        return messageDao.getMessagesByUserIdLive(userId);
    }




    //新增message_isread以后method

    public void insertMessage(Usermessage message) {
        executorService.execute(() -> {
            // 插入消息
            messageDao.insert(message);

//            // 如果消息类型为0且未读，更新用户未读计数
//            if (message.getMessageType() == 0 && !message.isMessage_isRead()) {
//                userDao.incrementUnreadInfoCount(message.getUserId());
//                Log.d("MessageDebug", "Incremented unread count for user: " + message.getUserId());
//            }
        });
    }

    // 批量插入消息
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

    // 标记消息为已读并更新用户未读计数
    public void markMessageAsRead(Long messageId) {
        executorService.execute(() -> {
            // 先获取消息
            List<Usermessage> messages = messageDao.getMessagesByUserId(messageId);
            if (messages != null && !messages.isEmpty()) {
                for (Usermessage message : messages) {
                    if (message.getId() == messageId && message.getMessageType() == 0 && !message.isMessage_isRead()) {
                        // 标记消息为已读
                        messageDao.markAsRead(messageId);
                        // 减少用户未读计数
                        userDao.decrementUnreadInfoCount(message.getUserId());
                        Log.d("MessageDebug", "Decremented unread count for user: " + message.getUserId());
                        break;
                    }
                }
            }
        });
    }

    // 标记用户所有消息为已读并重置未读计数
    public void markAllMessagesAsRead(Long userId) {
        executorService.execute(() -> {
            messageDao.markAllAsReadByUserId(userId);
            userDao.resetUnreadInfoCount(userId);
            Log.d("MessageDebug", "Reset unread count for user: " + userId);
        });
    }


    //新增message_isread以后method
    // 获取用户当前的未读计数
    public int getUnreadInfoCount(Long userId) {
        return messageDao.getUnreadInfoCount(userId);
    }

    // 同步所有用户的未读计数（用于初始化或修复数据）
    public void syncAllUsersUnreadCount() {
        executorService.execute(() -> {
            List<MessageDao.UserUnreadCount> unreadCounts = messageDao.getAllUsersUnreadCount();
            for (MessageDao.UserUnreadCount count : unreadCounts) {
                userDao.updateUnreadInfoCount(count.userId, count.unreadCount);
            }
            Log.d("MessageDebug", "Synced unread counts for all users");
        });
    }

    // 在用户创建时初始化未读计数为0
    public void insertUserWithUnreadCount(User user) {
        executorService.execute(() -> {
            user.setUnreadInfoCount(0);
            userDao.insert(user);
        });
    }

    // 批量插入用户时初始化未读计数
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
        }, 0, 5, TimeUnit.SECONDS); // 每30s发送一次运营消息
    }

    private void sendOperationMessage(List<User> users) {
        operationExcutorService.execute(() -> {
            // 随机选择一条运营消息模板
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

            // 创建运营消息
            Usermessage operationMessage = Usermessage.createOperationMessage(
                    operationTemplates[index],
                    buttonTexts[index],
                    "lemonapp://operation/" + index
            );



            int index1 = random.nextInt(users.size());

            operationMessage.setUserId(index1);



            // 发送消息
            sendMessage(operationMessage);

            Log.d(TAG, "用户" + index1 + "已发送运营消息: " + operationTemplates[index]);
        });
    }





}