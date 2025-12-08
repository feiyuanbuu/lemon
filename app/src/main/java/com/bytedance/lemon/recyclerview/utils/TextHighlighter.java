package com.bytedance.lemon.recyclerview.utils;

import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.BackgroundColorSpan;
import androidx.core.content.ContextCompat;
import android.content.Context;

import com.bytedance.lemon.R;

import java.util.ArrayList;
import java.util.List;

public class TextHighlighter {

    /**
     * 高亮文本中的关键词
     * @param text 原始文本
     * @param keyword 搜索关键词
     * @param context Context用于获取颜色资源
     * @return 高亮后的SpannableString
     */
    public static SpannableString highlight(String text, String keyword, Context context) {
        if (text == null || keyword == null || keyword.isEmpty()) {
            return new SpannableString(text == null ? "" : text);
        }

        SpannableString spannableString = new SpannableString(text);

        // 将文本和关键词都转换为小写进行不区分大小写的匹配
        String lowerText = text.toLowerCase();
        String lowerKeyword = keyword.toLowerCase();

        int startIndex = 0;
        List<int[]> highlightRanges = new ArrayList<>();

        // 查找所有匹配的位置
        while (true) {
            int index = lowerText.indexOf(lowerKeyword, startIndex);
            if (index == -1) break;

            highlightRanges.add(new int[]{index, index + keyword.length()});
            startIndex = index + 1;
        }

        // 应用高亮样式
        for (int[] range : highlightRanges) {
            int start = range[0];
            int end = range[1];

            // 使用黄色背景高亮
            BackgroundColorSpan backgroundColorSpan = new BackgroundColorSpan(
                    ContextCompat.getColor(context, R.color.highlight_background)
            );

            // 使用红色文本颜色
            ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(
                    ContextCompat.getColor(context, R.color.highlight_text)
            );

            spannableString.setSpan(backgroundColorSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(foregroundColorSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return spannableString;
    }

    /**
     * 高亮多个文本字段
     */
    public static SpannableStringBuilder highlightMultipleFields(
            String[] texts, String keyword, Context context) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        for (int i = 0; i < texts.length; i++) {
            if (texts[i] != null && !texts[i].isEmpty()) {
                builder.append(highlight(texts[i], keyword, context));
                if (i < texts.length - 1) {
                    builder.append(" ");
                }
            }
        }

        return builder;
    }
}