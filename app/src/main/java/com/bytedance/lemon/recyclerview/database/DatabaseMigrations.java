package com.bytedance.lemon.recyclerview.database;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;


public class DatabaseMigrations {

    // 从版本 1 迁移到版本 2
    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // 为 users 表添加 newest_info 列
            database.execSQL("ALTER TABLE users ADD COLUMN newest_info TEXT DEFAULT ''");
        }
    };
}