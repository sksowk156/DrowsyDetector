package com.paradise.drowsydetector.data.local.room.record

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DrowsyCountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrowsyCount(values: DrowsyCount)

    @Query("SELECT * FROM drowsyCount WHERE recordId = :recordId")
    fun getDrowsyCount(recordId: Int): Flow<List<DrowsyCount>>

    @Query("SELECT * FROM drowsyCount")
    fun getAllDrowsyCount(): Flow<List<DrowsyCount>>

    @Query("DELETE FROM drowsyCount")
    suspend fun deleteAllDrowsyCount()
}