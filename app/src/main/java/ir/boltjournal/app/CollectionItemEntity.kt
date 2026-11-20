package ir.boltjournal.app

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "collection_items")
data class CollectionItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String, // مثلا "کتاب‌ها"، "فهرست خرید"
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false
)
