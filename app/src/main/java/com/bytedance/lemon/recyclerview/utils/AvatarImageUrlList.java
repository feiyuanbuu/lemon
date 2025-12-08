// AvatarImageUrlList.java
package com.bytedance.lemon.recyclerview.utils;

import java.util.Arrays;
import java.util.List;

public class AvatarImageUrlList {
    // 头像图片 URL 列表
    private static final List<String> AVATAR_URLS = Arrays.asList(
            "https://img95.699pic.com/photo/50130/5562.jpg_wh300.jpg",
            "https://randomuser.me/api/portraits/women/2.jpg",
            "https://randomuser.me/api/portraits/women/3.jpg",
            "https://randomuser.me/api/portraits/women/5.jpg",
            "https://randomuser.me/api/portraits/women/6.jpg",
            "https://randomuser.me/api/portraits/women/8.jpg",
            "https://randomuser.me/api/portraits/women/11.jpg",
            "https://randomuser.me/api/portraits/women/25.jpg",
            "https://randomuser.me/api/portraits/women/26.jpg",
            "https://randomuser.me/api/portraits/men/1.jpg",
            "https://randomuser.me/api/portraits/men/9.jpg",
            "https://randomuser.me/api/portraits/men/15.jpg",
            "https://randomuser.me/api/portraits/men/60.jpg",
            "https://randomuser.me/api/portraits/men/62.jpg",
            "https://randomuser.me/api/portraits/men/86.jpg",
            "android.resource://com.bytedance.lemon/drawable/avator_1",
            "android.resource://com.bytedance.lemon/drawable/avator_2",
            "android.resource://com.bytedance.lemon/drawable/avator_4",
            "android.resource://com.bytedance.lemon/drawable/avator_5",
            "android.resource://com.bytedance.lemon/drawable/avator_8",
            "android.resource://com.bytedance.lemon/drawable/avator_10",
            "android.resource://com.bytedance.lemon/drawable/avator_12",
            "android.resource://com.bytedance.lemon/drawable/avator_13",
            "android.resource://com.bytedance.lemon/drawable/avator_14",
            "android.resource://com.bytedance.lemon/drawable/avator_15",
            "android.resource://com.bytedance.lemon/drawable/avator_16",
            "android.resource://com.bytedance.lemon/drawable/avator_17",
            "android.resource://com.bytedance.lemon/drawable/avator_18",
            "android.resource://com.bytedance.lemon/drawable/avator_19",
            "android.resource://com.bytedance.lemon/drawable/avator_20",
            "android.resource://com.bytedance.lemon/drawable/avator_21"
    );

    private AvatarImageUrlList() {
        // 工具类
    }

    /**
     * 根据索引获取头像 URL（支持循环）
     * @param index 索引
     * @return 头像 URL
     */
    public static String get(int index) {
        int size = AVATAR_URLS.size();
        if (size == 0) {
            return "https://randomuser.me/api/portraits/lego/1.jpg"; // 默认头像
        }

        int normalizedIndex = index % size;
        if (normalizedIndex < 0) {
            normalizedIndex += size;
        }
        return AVATAR_URLS.get(normalizedIndex);
    }

    /**
     * 根据用户ID获取头像URL（确定性，相同ID总是返回相同头像）
     * @param userId 用户ID
     * @return 头像URL
     */
    public static String getByUserId(long userId) {
        int size = AVATAR_URLS.size();
        if (size == 0) {
            return "https://randomuser.me/api/portraits/lego/1.jpg";
        }

        // 使用用户ID作为种子选择头像（取模）
        int index = (int) (userId % size);
        if (index < 0) {
            index = -index; // 处理负的ID
        }
        return AVATAR_URLS.get(index % size);
    }

    /**
     * 获取随机头像
     * @return 随机头像URL
     */
    public static String getRandom() {
        int size = AVATAR_URLS.size();
        if (size == 0) {
            return "https://randomuser.me/api/portraits/lego/1.jpg";
        }
        int randomIndex = (int) (Math.random() * size);
        return AVATAR_URLS.get(randomIndex);
    }

    /**
     * 获取所有头像URL
     * @return 头像URL列表
     */
    public static List<String> getAll() {
        return AVATAR_URLS;
    }

    /**
     * 获取头像数量
     * @return 头像数量
     */
    public static int getCount() {
        return AVATAR_URLS.size();
    }
}