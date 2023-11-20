package com.paradise.drowsydetector.data.local.room.record

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

interface WinkCountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWinkCount(values: WinkCount)

    @Query("SELECT * FROM winkCount WHERE recordId = :recordId")
    fun getAllWinkCount(recordId: Int): Flow<List<WinkCount>>
}