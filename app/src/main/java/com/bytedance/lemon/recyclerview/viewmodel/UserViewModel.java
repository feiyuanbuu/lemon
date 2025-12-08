// UserViewModel.java (修改导入)
package com.bytedance.lemon.recyclerview.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.annotation.NonNull;

import com.bytedance.lemon.recyclerview.entity.User;
import com.bytedance.lemon.recyclerview.entity.Usermessage; // 修改导入
import com.bytedance.lemon.recyclerview.repository.UserRepository;

import java.util.List;

public class UserViewModel extends AndroidViewModel {
    private UserRepository repository;
    private LiveData<List<User>> allUsers;

    public UserViewModel(@NonNull Application application) {
        super(application);
//        repository = new UserRepository(application);
        repository = UserRepository.getInstance(application);
        allUsers = repository.getAllUsersLive();


    }

    public void updateMessage(Usermessage message) {
        repository.updateMessage(message);
    }

    public LiveData<List<User>> getAllUsersLive() { return allUsers; }

    public LiveData<User> getUserByIdLive(long userId) {
        return repository.getUserByIdLive(userId);
    }

    // 获取用户消息历史
    public LiveData<List<Usermessage>> getMessagesByUserIdLive(long userId) {
        return repository.getMessagesByUserIdLive(userId);
    }


    // 停止自动发送消息（在Activity销毁时调用）
    public void stopAutoMessaging() {
        repository.stopAutoMessaging();
    }

    // 发送我方消息
    public void sendMessage(Usermessage message) {
        repository.sendMessage(message);
    }

    // 获取合并消息
    public LiveData<List<Usermessage>> getCombinedMessagesLive(long otherUserId, long myUserId) {
        return repository.getCombinedMessagesLive(otherUserId, myUserId);
    }

    // 检查自动消息是否运行
    public boolean isAutoMessagingRunning() {
        return repository.isAutoMessagingRunning();
    }



}