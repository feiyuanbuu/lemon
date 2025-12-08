// Usermessage.java
package com.bytedance.lemon.recyclerview.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.ColumnInfo;

import com.bytedance.lemon.recyclerview.utils.AvatarImageUrlList;

import org.json.JSONObject;
import org.json.JSONException;


@Entity(
        tableName = "user_messages",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "id",
                childColumns = "user_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {
                @Index(value = "user_id"),
                @Index(value = "timestamp"),
                @Index(value = "message_type")
        }
)
public class Usermessage {
    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "user_id")
    private Long userId;
    private String content;
    private long timestamp;

    @ColumnInfo(name = "message_is_read")
    private boolean message_isRead;

    // 新增：消息类型
    @ColumnInfo(name = "message_type")
    private int messageType; // 0=接收的消息，1=发送的消息

    // 新增：接收者ID（对于发送的消息）
    @ColumnInfo(name = "receiver_id")
    private long receiverId;

    //新增：消息携带图片
    @ColumnInfo(name = "message_image_url")
    private String messageImageUrl;


    // 新增：运营消息数据（JSON格式存储按钮信息）
    @ColumnInfo(name = "operation_data")
    private String operationData;


    // 新增：完整构造方法
    public Usermessage(long userId, String content, long timestamp, int messageType, long receiverId) {
        this.userId = userId;
        this.content = content;
        this.timestamp = timestamp;
        this.messageType = messageType;
        this.receiverId = receiverId;
        this.messageImageUrl = null;
        this.message_isRead = false;
    }

    // 为方便使用添加的构造函数，用 @Ignore 标记，Room 不会使用它
    @Ignore
    public Usermessage(long userId, String content, long timestamp) {
        this(userId, content, timestamp, 0, 0); // 调用主构造函数，设置默认值
    }


    // 新增：创建运营消息的静态方法
    @Ignore
    public static Usermessage createOperationMessage(String content, String buttonText, String actionUrl) {
        Usermessage message = new Usermessage(0, content, System.currentTimeMillis(), 2, 0); // -1表示系统用户

        try {
            JSONObject json = new JSONObject();
            json.put("type", "operation");
            json.put("buttonText", buttonText);
            json.put("actionUrl", actionUrl);
            message.setOperationData(json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return message;
    }



    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public int getMessageType() { return messageType; }
    public void setMessageType(int messageType) { this.messageType = messageType; }

    public long getReceiverId() { return receiverId; }
    public void setReceiverId(long receiverId) { this.receiverId = receiverId; }


    public String getMessageImageUrl() {
        return messageImageUrl;
    }
    public void setMessageImageUrl(String messageUrl) {
        this.messageImageUrl = messageUrl;
    }


    public boolean isMessage_isRead() {
        return message_isRead;
    }

    public void setMessage_isRead(boolean message_isRead) {
        this.message_isRead = message_isRead;
    }


    public String getOperationData() { return operationData; }
    public void setOperationData(String operationData) { this.operationData = operationData; }


    // 新增：解析操作数据为JSON
    @Ignore
    public JSONObject getOperationDataJson() {
        if (operationData == null || operationData.isEmpty()) {
            return null;
        }
        try {
            return new JSONObject(operationData);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 新增：检查是否为运营消息
    @Ignore
    public boolean isOperationMessage() {
        return messageType == 2;
    }


}