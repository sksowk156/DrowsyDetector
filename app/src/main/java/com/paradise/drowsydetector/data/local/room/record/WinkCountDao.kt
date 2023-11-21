package com.paradise.drowsydetector.data.local.room.record

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WinkCountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWinkCount(values: WinkCount)

    @Query("SELECT * FROM winkCount WHERE recordId = :recordId")
    fun getWinkCount(recordId: Int): Flow<List<WinkCount>>

    @Query("SELECT * FROM winkCount")
    fun getAllWinkCount(): Flow<List<WinkCount>>

    @Query("DELETE FROM winkCount")
    suspend fun deleteAllWinkCount()
}