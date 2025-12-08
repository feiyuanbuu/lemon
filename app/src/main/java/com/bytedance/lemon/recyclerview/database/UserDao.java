// 文件：UserDao.java (建议放在新建的 database 或 dao 包)
package com.bytedance.lemon.recyclerview.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.bytedance.lemon.recyclerview.entity.User;

import java.util.List;

@Dao
public interface UserDao {
    @Insert
    void insert(User user);

    @Insert
    void insertAll(List<User> users);

//    @Update
//    void update(User user);
    @Update
    void update(User user);

    @Delete
    void delete(User user);

    // 查询所有用户，按 pinned 状态优先，然后按最新消息时间戳降序排列
    @Query("SELECT * FROM users ORDER BY is_pined DESC, last_message_timestamp DESC, create_timestamp DESC")
    LiveData<List<User>> getAllUsersLive();

    
    // 清空用户表（用于下拉刷新模拟）
    @Query("DELETE FROM users")
    void deleteAllUsers();

    // 查询用户总数（用于判断是否加载完毕）
    @Query("SELECT COUNT(*) FROM users")
    int getUserCount();

    @Query("SELECT * FROM users WHERE id = :userId")
    User getUserById(long userId);


    @Query("SELECT * FROM users WHERE id = :userId")
    LiveData<User> getUserByIdLive(long userId);


    // 搜索时也要按置顶状态排序
    @Query("SELECT * FROM users WHERE name LIKE '%' || :searchQuery || '%' " +
            "OR user_description LIKE '%' || :searchQuery || '%' " +
            "OR newest_info LIKE '%' || :searchQuery || '%' " +
            "ORDER BY is_pined DESC, last_message_timestamp DESC, create_timestamp DESC")
    LiveData<List<User>> searchUsersLive(String searchQuery);


    // 同步查询所有用户（用于后台线程）按 pinned 状态优先
    @Query("SELECT * FROM users ORDER BY is_pined DESC, last_message_timestamp DESC, create_timestamp DESC")
    List<User> getAllUsers();


    // 新增：更新用户未读计数
    @Query("UPDATE users SET unread_info_count = :count WHERE id = :userId")
    void updateUnreadInfoCount(Long userId, int count);

    // 新增：递增用户未读计数
    @Query("UPDATE users SET unread_info_count = unread_info_count + 1 WHERE id = :userId")
    void incrementUnreadInfoCount(Long userId);

    // 新增：递减用户未读计数
    @Query("UPDATE users SET unread_info_count = unread_info_count - 1 WHERE id = :userId")
    void decrementUnreadInfoCount(Long userId);

    // 新增：重置用户未读计数
    @Query("UPDATE users SET unread_info_count = 0 WHERE id = :userId")
    void resetUnreadInfoCount(Long userId);


    // 新增：更新用户 pinned 状态
    @Query("UPDATE users SET is_pined = :isPinned WHERE id = :userId")
    void updatePinnedStatus(Long userId, boolean isPinned);

    // 新增：查询所有 pinned 的用户
    @Query("SELECT * FROM users WHERE is_pined = 1 ORDER BY last_message_timestamp DESC")
    LiveData<List<User>> getPinnedUsersLive();

    // 新增：查询所有未 pinned 的用户
    @Query("SELECT * FROM users WHERE is_pined = 0 ORDER BY last_message_timestamp DESC")
    LiveData<List<User>> getUnpinnedUsersLive();

    // 新增：切换用户 pinned 状态
    @Query("UPDATE users SET is_pined = CASE WHEN is_pined = 1 THEN 0 ELSE 1 END WHERE id = :userId")
    void togglePinnedStatus(Long userId);



}