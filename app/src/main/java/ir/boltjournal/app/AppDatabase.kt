package ir.boltjournal.app

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TaskEntity::class, DailyNoteEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun dailyNoteDao(): DailyNoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bolt_journal_database"
                ).fallbackToDestructiveMigration() // برای جلوگیری از کرش هنگام آپدیت دیتابیس
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
