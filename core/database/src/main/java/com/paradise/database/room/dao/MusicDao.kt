package com.paradise.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.paradise.database.room.model.Music
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMusic(music: Music)

    @Query("SELECT * FROM music_table")
    fun getAllMusic(): List<Music>

    @Update
    suspend fun updateMusic(music: Music)

    @Query("""DELETE FROM music_table WHERE id=:id""")
    suspend fun deleteMusic(id: Int)
}