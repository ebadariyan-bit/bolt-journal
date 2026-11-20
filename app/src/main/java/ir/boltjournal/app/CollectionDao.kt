package ir.boltjournal.app

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {
    @Query("SELECT * FROM collection_items ORDER BY id DESC")
    fun getAllItems(): Flow<List<CollectionItemEntity>>

    @Insert
    suspend fun insertItem(item: CollectionItemEntity)

    @Update
    suspend fun updateItem(item: CollectionItemEntity)

    @Delete
    suspend fun deleteItem(item: CollectionItemEntity)
}
