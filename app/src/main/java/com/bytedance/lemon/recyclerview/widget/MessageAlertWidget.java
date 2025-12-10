// MessageAlertWidget.java
package com.bytedance.lemon.recyclerview.widget;

import android.app.Application;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;

import com.bytedance.lemon.ChatActivity;
import com.bytedance.lemon.R;
import com.bytedance.lemon.recyclerview.RecyclerViewActivity;
import com.bytedance.lemon.recyclerview.entity.User;
import com.bytedance.lemon.recyclerview.repository.UserRepository;

/**
 * 消息提醒小组件 - 模拟轻量触达
 */
public class MessageAlertWidget extends AppWidgetProvider {
    private static final String TAG = "MessageAlertWidget";
    public static final String ACTION_UPDATE_WIDGET = "android.appwidget.action.APPWIDGET_UPDATE";
    public static final String ACTION_CLICK_WIDGET = "android.appwidget.action.APPWIDGET_WIDGET";
    public static final String ACTION_CLOSE_WIDGET = "android.appwidget.action.APPWIDGET.CLOSE_WIDGET";

    private static long sCurrentUserId = -1;
    private static String sCurrentUserName = "";
    private static String sCurrentAvatarUrl = "";
    private static String sCurrentMessage = "";
    private static long sCurrentTime = 0;



    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Log.d(TAG, "Widget onUpdate called");

        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId);
//            Log.d(TAG, "更新wi");
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        super.onReceive(context, intent);
        String action = intent.getAction();
        Log.d(TAG, "onReceive action: " + action);

        if (ACTION_UPDATE_WIDGET.equals(action)) {

            long userId = intent.getLongExtra("user_id", -1);
            String userName = intent.getStringExtra("user_name");
            String avatarUrl = intent.getStringExtra("avatar_url");
            String message = intent.getStringExtra("message");
            long timestamp = intent.getLongExtra("timestamp", System.currentTimeMillis());

            if (userId != -1 && userName != null) {
                sCurrentUserId = userId;
                sCurrentUserName = userName;
                sCurrentAvatarUrl = avatarUrl != null ? avatarUrl : "";
                sCurrentMessage = message != null ? message : "";
                sCurrentTime = timestamp;

                // 更新所有widget实例
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                        new ComponentName(context, MessageAlertWidget.class)
                );

                for (int appWidgetId : appWidgetIds) {
                    updateWidget(context, appWidgetManager, appWidgetId);
                }
            }
        } else if (ACTION_CLICK_WIDGET.equals(action)) {
            // 点击widget，跳转到聊天页面
            if (sCurrentUserId != -1) {
                Intent chatIntent = new Intent(context, ChatActivity.class);
                chatIntent.putExtra("USER_ID", sCurrentUserId);
                chatIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(chatIntent);

                // 点击后清除widget
                clearWidget(context);
            }
        } else if (ACTION_CLOSE_WIDGET.equals(action)) {
            // 关闭widget
            clearWidget(context);
        }
    }

    private void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_message_alert);

        if (sCurrentUserId != -1) {
            // 设置发送者信息
            views.setTextViewText(R.id.widget_sender, sCurrentUserName);
            views.setTextViewText(R.id.widget_message, sCurrentMessage);

            // 格式化时间
            String timeText = formatTime(sCurrentTime);
            views.setTextViewText(R.id.widget_time, timeText);

            // 设置点击事件 - 点击整个widget跳转到聊天页面
            Intent clickIntent = new Intent(context, MessageAlertWidget.class);
            clickIntent.setAction(ACTION_CLICK_WIDGET);
            PendingIntent clickPendingIntent = PendingIntent.getBroadcast(
                    context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            views.setOnClickPendingIntent(R.id.widget_avatar, clickPendingIntent);
            views.setOnClickPendingIntent(R.id.widget_sender, clickPendingIntent);
            views.setOnClickPendingIntent(R.id.widget_message, clickPendingIntent);

            // 设置关闭按钮点击事件
            Intent closeIntent = new Intent(context, MessageAlertWidget.class);
            closeIntent.setAction(ACTION_CLOSE_WIDGET);
            PendingIntent closePendingIntent = PendingIntent.getBroadcast(
                    context, 1, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            views.setOnClickPendingIntent(R.id.widget_close, closePendingIntent);
            views.setViewVisibility(R.id.widget_close, View.VISIBLE);

            // 加载头像
            try {
//                views.setImageViewUri(R.id.widget_avatar, Uri.parse(sCurrentAvatarUrl));
                views.setImageViewResource(R.id.widget_avatar,  R.drawable.lemon);
            } catch (Exception e) {
                views.setImageViewResource(R.id.widget_avatar, R.drawable.avator_15);
            }
        } else {
            // 没有消息时显示默认状态
            views.setTextViewText(R.id.widget_sender, "Lemon Chat");
            views.setTextViewText(R.id.widget_message, "暂时没有新消息");
            views.setTextViewText(R.id.widget_time, "");
            views.setViewVisibility(R.id.widget_close, View.GONE);

            // 点击跳转到主列表页面
            Intent mainIntent = new Intent(context, RecyclerViewActivity.class);
            PendingIntent mainPendingIntent = PendingIntent.getActivity(
                    context, 2, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            views.setOnClickPendingIntent(R.id.widget_sender, mainPendingIntent);
        }

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private void clearWidget(Context context) {
        sCurrentUserId = -1;
        sCurrentUserName = "";
        sCurrentAvatarUrl = "";
        sCurrentMessage = "";
        sCurrentTime = 0;

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                new ComponentName(context, MessageAlertWidget.class)
        );

        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId);
        }
    }

    private String formatTime(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        if (diff < 60000) { // 1分钟内
            return "刚刚";
        } else if (diff < 3600000) { // 1小时内
            return (diff / 60000) + "分钟前";
        } else if (diff < 86400000) { // 1天内
            return (diff / 3600000) + "小时前";
        } else {
            return "超过1天";
        }
    }

//    public static boolean isShowingAlert() {
//        return sCurrentAlertUser != null;
//    }
//
//    public static User getCurrentAlertUser() {
//        return sCurrentAlertUser;
//    }
}