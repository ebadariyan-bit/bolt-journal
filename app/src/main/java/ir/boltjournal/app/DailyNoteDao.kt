package ir.boltjournal.app

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyNoteDao {
    @Query("SELECT * FROM daily_notes WHERE date = :date")
    fun getNoteForDate(date: String): Flow<DailyNoteEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateNote(note: DailyNoteEntity)
}
