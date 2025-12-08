//package com.bytedance.lemon.recyclerview.database;
//
//import androidx.room.Database;
//import androidx.room.Room;
//import androidx.room.RoomDatabase;
//import android.content.Context;
//
//import com.bytedance.lemon.recyclerview.entity.User;
//
//
//// @Database 注解列出所有实体、版本号，exportSchema通常开发时设为false
//@Database(entities = {User.class}, version = 2, exportSchema = false)
//public abstract class AppDatabase extends RoomDatabase {
//    public abstract UserDao userDao(); // 提供DAO实例的抽象方法
//
//    // 单例模式，避免重复打开数据库
//    private static volatile AppDatabase INSTANCE;
//
////    public static AppDatabase getDatabase(final Context context) {
////        if (INSTANCE == null) {
////            synchronized (AppDatabase.class) {
////                if (INSTANCE == null) {
////                    // 创建数据库实例
////                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
////                                    AppDatabase.class, "user_database") // 数据库文件名称
////                            .build();
////                }
////            }
////        }
////        return INSTANCE;
////    }
//        public static synchronized AppDatabase getDatabase(Context context) {
//            if (INSTANCE == null) {
//                INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
//                                AppDatabase.class, "user_database")
//                        .addMigrations(DatabaseMigrations.MIGRATION_1_2)  // 添加迁移
//                        .build();
//            }
//            return INSTANCE;
//        }
//}
//
////your_database_name



// AppDatabase.java
package com.bytedance.lemon.recyclerview.database;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import android.content.Context;
import android.util.Log;

import com.bytedance.lemon.recyclerview.entity.User;
import com.bytedance.lemon.recyclerview.entity.Usermessage;

import java.util.concurrent.Executors;

@Database(entities = {User.class, Usermessage.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract MessageDao messageDao();

    private static volatile AppDatabase INSTANCE;

    public static synchronized AppDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "user_database")
//                    .fallbackToDestructiveMigration() // 破坏性迁移，会删除旧数据
//                    .allowMainThreadQueries() // 允许在主线程执行查询（仅用于开发调试）
//                    .addMigrations(DatabaseMigrations.MIGRATION_1_2)
                    .build();


        }
        return INSTANCE;
    }









}