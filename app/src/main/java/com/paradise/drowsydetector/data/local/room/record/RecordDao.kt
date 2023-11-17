package com.paradise.drowsydetector.data.local.room.record

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.paradise.drowsydetector.data.local.room.music.Music
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMusic(music: Music)

    @Query("SELECT * FROM music_table")
    fun getAllMusic(): Flow<List<Music>>

    @Update
    suspend fun updateMusic(music: Music)

    @Query("""DELETE FROM music_table WHERE id=:id""")
    suspend fun deleteMusic(id: Int)
}
