package ir.boltjournal.app

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String = "",
    val date: Long = System.currentTimeMillis(),
    val dateString: String = "", // برای آینده‌نگار (مثلاً 1405/12/25)
    val priority: String = "GRAY", // RED, GREEN, ORANGE, GRAY
    val recurrenceType: String = "NONE", // NONE, WEEKLY, MONTHLY
    val recurrenceValue: String = "", // مثلا "شنبه" یا "15"
    val reminderDays: Int = 0, // چند روز قبل یادآوری شود
    val isCompleted: Boolean = false
)
