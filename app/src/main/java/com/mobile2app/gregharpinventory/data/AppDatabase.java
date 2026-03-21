package com.mobile2app.gregharpinventory.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.mobile2app.gregharpinventory.model.InventoryItem;
import com.mobile2app.gregharpinventory.model.User;

@Database(entities = {InventoryItem.class, User.class},
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    // DAO accessors
    public abstract ItemDao itemDao();
    public abstract UserDao userDao();

    // singleton instance
    private static volatile AppDatabase INSTANCE;

    // database file name
    private static final String DB_NAME = "gh_inventory.db";

    // get or create the singleton instance
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, DB_NAME)
                            .fallbackToDestructiveMigration()
                            .fallbackToDestructiveMigrationOnDowngrade()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
