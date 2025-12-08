// User.java
package com.bytedance.lemon.recyclerview.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.bytedance.lemon.recyclerview.utils.AvatarImageUrlList;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "users")
public class User {
//    @PrimaryKey(autoGenerate = true)
    @PrimaryKey
    private long id;


    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "user_description")
    private String description;

    @ColumnInfo(name = "newest_info")
    private String newest_info;

    @ColumnInfo(name = "is_pined")
    private boolean isPinned;

    @ColumnInfo(name = "create_timestamp")
    private long create_timestamp;

    @ColumnInfo(name = "last_message_timestamp")
    private long lastMessageTimestamp;

    @ColumnInfo(name = "avatar_url")
    private String avatarUrl;

    @ColumnInfo(name = "unread_info_count")
    private int unreadInfoCount;


    // 原有：发送的消息历史（不存储在数据库中）
    @Ignore
    private List<Usermessage> messageHistory = new ArrayList<>();

    // 新增：接收到的消息列表（不存储在数据库中）
    @Ignore
    private List<Usermessage> receivedMessages = new ArrayList<>();

    // 新增：是否是我的消息（用于区分消息方向）
    @Ignore
    private boolean isMine = false;


    @Ignore
    public User(String name, String description, String newest_info, long create_timestamp) {
        this.name = name;
        this.description = description;
//        this.isRead = false;
        this.create_timestamp = create_timestamp;
        this.newest_info = newest_info;
        this.lastMessageTimestamp = create_timestamp;
        this.avatarUrl = AvatarImageUrlList.getRandom();
        this.unreadInfoCount = 0;
        this.isPinned = false;
    }

    public User(long id, String name, String description, String newest_info, long create_timestamp) {
        this.id = id;
        this.name = name;
        this.description = description;
//        this.isRead = false;
        this.create_timestamp = create_timestamp;
        this.newest_info = newest_info;
        this.lastMessageTimestamp = create_timestamp;
        this.avatarUrl = AvatarImageUrlList.getRandom();
        this.unreadInfoCount = 0;
        this.isPinned = false;
    }

    // Getter 和 Setter
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getNewest_info() { return newest_info; }
    public void setNewest_info(String newest_info) { this.newest_info = newest_info; }

//    public boolean isRead() { return isRead; }
//    public void setRead(boolean read) { isRead = read; }

    public long getCreate_timestamp() { return create_timestamp; }
    public void setCreate_timestamp(long timestamp) { this.create_timestamp = timestamp; }

    public long getLastMessageTimestamp() { return lastMessageTimestamp; }
    public void setLastMessageTimestamp(long lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    public String getAvatarUrl() {
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            return AvatarImageUrlList.getByUserId(id);
        }
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    // 消息历史相关方法
    @Ignore
    public List<Usermessage> getMessageHistory() {
        return messageHistory;
    }

    @Ignore
    public void setMessageHistory(List<Usermessage> messageHistory) {
        this.messageHistory = messageHistory;
    }

    @Ignore
    public void addMessage(Usermessage message) {
        if (messageHistory == null) {
            messageHistory = new ArrayList<>();
        }
        messageHistory.add(message);
    }

    // 新增：接收消息相关方法
    @Ignore
    public List<Usermessage> getReceivedMessages() {
        if (receivedMessages == null) {
            receivedMessages = new ArrayList<>();
        }
        return receivedMessages;
    }

    @Ignore
    public void setReceivedMessages(List<Usermessage> receivedMessages) {
        this.receivedMessages = receivedMessages;
    }

    @Ignore
    public void addReceivedMessage(Usermessage message) {
        if (receivedMessages == null) {
            receivedMessages = new ArrayList<>();
        }
        receivedMessages.add(message);
    }

    @Ignore
    public boolean isMine() {
        return isMine;
    }

    @Ignore
    public void setMine(boolean mine) {
        isMine = mine;
    }

    // 新增：获取所有消息（发送+接收）
    @Ignore
    public List<Usermessage> getAllMessages() {
        List<Usermessage> allMessages = new ArrayList<>();
        if (messageHistory != null) {
            allMessages.addAll(messageHistory);
        }
        if (receivedMessages != null) {
            allMessages.addAll(receivedMessages);
        }
        // 按时间戳排序
        allMessages.sort((m1, m2) -> Long.compare(m1.getTimestamp(), m2.getTimestamp()));
        return allMessages;
    }


    // 新增 Getter 和 Setter
    public int getUnreadInfoCount() { return unreadInfoCount; }
    public void setUnreadInfoCount(int unreadInfoCount) { this.unreadInfoCount = unreadInfoCount; }

    // 用于批量更新未读计数的方法
    public void incrementUnreadCount() {
        this.unreadInfoCount++;
    }

    public void decrementUnreadCount() {
        if (this.unreadInfoCount > 0) {
            this.unreadInfoCount--;
        }
    }

    public void resetUnreadCount() {
        this.unreadInfoCount = 0;
    }



    public boolean isPinned() {
        return isPinned;
    }

    public void setPinned(boolean pinned) {
        isPinned = pinned;
    }
}