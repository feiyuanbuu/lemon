package com.bytedance.lemon.recyclerview.database;


import androidx.lifecycle.LiveData;
import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.bytedance.lemon.recyclerview.entity.Usermessage;

import java.util.List;


import androidx.room.Update; // 添加这个导入




@Dao
public interface MessageDao {

    @Update
    void update(Usermessage message);

    @Insert
    void insert(Usermessage message);

    @Insert
    void insertAll(List<Usermessage> messages);

    // 查询指定用户的所有消息，按时间升序排列
    @Query("SELECT * FROM user_messages WHERE user_id = :userId ORDER BY timestamp ASC")
    LiveData<List<Usermessage>> getMessagesByUserIdLive(long userId);

    // 查询指定用户的所有消息（非LiveData版本）
    @Query("SELECT * FROM user_messages WHERE user_id = :userId ORDER BY timestamp ASC")
    List<Usermessage> getMessagesByUserId(long userId);

    // 清空指定用户的消息
    @Query("DELETE FROM user_messages WHERE user_id = :userId")
    void deleteMessagesByUserId(long userId);

    // 清空所有消息
    @Query("DELETE FROM user_messages")
    void deleteAllMessages();

    // 获取指定用户的最新消息
    @Query("SELECT * FROM user_messages WHERE user_id = :userId ORDER BY timestamp DESC LIMIT 1")
    Usermessage getLatestMessageByUserId(long userId);

    // 获取所有用户的最新消息（用于主列表显示）
    @Query("SELECT * FROM user_messages WHERE id IN (SELECT MAX(id) FROM user_messages GROUP BY user_id)")
    LiveData<List<Usermessage>> getAllLatestMessagesLive();


    // 修改：获取合并消息
    @Query("SELECT * FROM user_messages WHERE " +
            "(user_id = :otherUserId AND message_type = 0) OR " +  // 对方发送的消息
            "(user_id = :myUserId AND receiver_id = :otherUserId AND message_type = 1) " +  // 我方发送的消息
            "ORDER BY timestamp ASC")
    LiveData<List<Usermessage>> getCombinedMessagesLive(long otherUserId, long myUserId);

    // 新增：根据接收者ID查询消息
    @Query("SELECT * FROM user_messages WHERE receiver_id = :receiverId ORDER BY timestamp ASC")
    LiveData<List<Usermessage>> getMessagesByReceiverIdLive(long receiverId);

    // 获取两个用户之间的所有消息（包括发送和接收）
    // 修改：获取两个用户之间的所有消息（包括发送、接收和运营消息）
    @Query("SELECT * FROM user_messages WHERE " +
            "(user_id = :userId AND receiver_id = :otherUserId) OR " +
            "(user_id = :otherUserId AND receiver_id = :userId) OR " +
            "(message_type = 2 AND receiver_id = :userId) " + // 增加：接收方是该用户的运营消息
            "ORDER BY timestamp ASC")
    LiveData<List<Usermessage>> getChatMessagesLive(long userId, long otherUserId);

    // 标记消息为已读
    @Query("UPDATE user_messages SET message_is_read = 1 WHERE id = :messageId")
    void markAsRead(Long messageId);

    // 标记用户的所有消息为已读
    @Query("UPDATE user_messages SET message_is_read= 1 WHERE user_id = :userId")
    void markAllAsReadByUserId(Long userId);

    // 获取用户未读消息数量（messageType = 0且未读）
    @Query("SELECT COUNT(*) FROM user_messages WHERE user_id = :userId AND message_type = 0 AND message_is_read = 0")
    int getUnreadInfoCount(Long userId);

    // 获取所有用户的未读消息数量（messageType = 0且未读）
    @Query("SELECT user_id, COUNT(*) as unread_count FROM user_messages WHERE message_type = 0 AND message_is_read = 0 GROUP BY user_id")
    List<UserUnreadCount> getAllUsersUnreadCount();

    // 新增：获取运营消息
    @Query("SELECT * FROM user_messages WHERE message_type = 2 ORDER BY timestamp DESC")
    LiveData<List<Usermessage>> getOperationMessagesLive();


    // 用于查询结果的临时类
    class UserUnreadCount {
        @ColumnInfo(name = "user_id")
        public Long userId;
        @ColumnInfo(name = "unread_count")  // 添加这个注解
        public int unreadCount;

        public int getUnreadCount() {
            return unreadCount;
        }


        public void setUnreadCount(int unreadCount) {
            this.unreadCount = unreadCount;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }
    }






}