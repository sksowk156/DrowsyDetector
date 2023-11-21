package com.paradise.drowsydetector.data.local.room.record

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(drowsyRecord: DrowsyRecord)

    @Query("SELECT * FROM drowsy_table WHERE id = :id")
    fun getRecord(id: Int): Flow<DrowsyRecord>

    @Query("SELECT * FROM drowsy_table WHERE time = :time")
    fun getRecord(time: String): Flow<DrowsyRecord>

    @Query("SELECT * FROM drowsy_table")
    fun getAllRecord(): Flow<List<DrowsyRecord>>

    @Delete
    suspend fun deleteRecord(drowsyRecord: DrowsyRecord)

    @Query("DELETE FROM drowsy_table")
    suspend fun deleteAllRecords()

}