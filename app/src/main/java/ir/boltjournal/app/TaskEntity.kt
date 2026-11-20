package ir.boltjournal.app

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String = "",
    val date: Long = System.currentTimeMillis(),
    val dateString: String = "", // تاریخ دقیق به شمسی برای گروه‌بندی
    val time: String = "",
    val isCompleted: Boolean = false
)
