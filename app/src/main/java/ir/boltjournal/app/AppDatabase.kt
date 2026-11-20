package ir.boltjournal.app

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// نسخه 6 قفل شد. از این به بعد ساختار دیتابیس تغییر نمی‌کند.
@Database(entities = [TaskEntity::class, DailyNoteEntity::class, CollectionItemEntity::class], version = 6, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun dailyNoteDao(): DailyNoteDao
    abstract fun collectionDao(): CollectionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bolt_journal_database"
                ).build() // fallbackToDestructiveMigration حذف شد تا داده‌ها حفظ شوند
                INSTANCE = instance
                instance
            }
        }
    }
}
