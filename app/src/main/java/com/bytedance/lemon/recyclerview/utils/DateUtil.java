package com.bytedance.lemon.recyclerview.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtil {
    /**
     * 将时间戳（毫秒）转换为易读的日期时间字符串
     * @param timestamp 毫秒时间戳
     * @return 格式化的字符串，根据规则：
     *         - 1 分钟内：刚刚
     *         - 1 小时内：xx 分钟前
     *         - 今天：xx:xx
     *         - 昨天：昨天 xx:xx
     *         - 7 天内：x 天前
     *         - 其他：MM-dd
     */
    public static String getTimeString(long timestamp) {
        long currentTime = System.currentTimeMillis();
        long diff = currentTime - timestamp;

        // 1 分钟内：刚刚
        if (diff < 60 * 1000) {
            return "刚刚";
        }
        // 1 小时内：xx 分钟前
        else if (diff < 60 * 60 * 1000) {
            long minutes = diff / (60 * 1000);
            return minutes + "分钟前";
        }

        // 获取当前日期和传入日期的Calendar实例
        Calendar currentCal = Calendar.getInstance();
        Calendar targetCal = Calendar.getInstance();
        targetCal.setTimeInMillis(timestamp);

        // 今天：xx:xx
        if (isSameDay(currentCal, targetCal)) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }

        // 昨天：昨天 xx:xx
        Calendar yesterdayCal = Calendar.getInstance();
        yesterdayCal.add(Calendar.DATE, -1);
        if (isSameDay(yesterdayCal, targetCal)) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return "昨天 " + sdf.format(new Date(timestamp));
        }

        // 计算天数差
        int dayDiff = getDaysBetween(targetCal, currentCal);

        // 7 天内：x 天前（排除今天和昨天）
        if (dayDiff > 1 && dayDiff <= 7) {
            return (dayDiff - 1) + "天前";
        }

        // 其他：MM-dd
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    /**
     * 判断两个Calendar是否表示同一天
     */
    private static boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * 计算两个Calendar之间的天数差
     */
    private static int getDaysBetween(Calendar startCal, Calendar endCal) {
        // 将时间都设置为午夜，只比较日期部分
        Calendar start = (Calendar) startCal.clone();
        Calendar end = (Calendar) endCal.clone();

        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        end.set(Calendar.HOUR_OF_DAY, 0);
        end.set(Calendar.MINUTE, 0);
        end.set(Calendar.SECOND, 0);
        end.set(Calendar.MILLISECOND, 0);

        long diffMillis = end.getTimeInMillis() - start.getTimeInMillis();
        return (int) (diffMillis / (24 * 60 * 60 * 1000));
    }
}