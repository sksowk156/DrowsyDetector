package com.paradise.drowsydetector.data.local.room.record

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

interface DrowsyCountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrowsyCount(values: DrowsyCount)

    @Query("SELECT * FROM drowsyCount WHERE recordId = :recordId")
    fun getAllDrowsyCount(recordId: Int): Flow<List<DrowsyCount>>
}