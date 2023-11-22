package com.paradise.drowsydetector.data.local.room.record

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalyzeResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(AnalyzeResult: AnalyzeResult)

    @Query("SELECT * FROM drowsy_table WHERE id = :id")
    fun getRecord(id: Int): Flow<AnalyzeResult>

    @Query("SELECT * FROM drowsy_table WHERE time = :time")
    fun getRecord(time: String): Flow<AnalyzeResult>

    @Query("SELECT * FROM drowsy_table")
    fun getAllRecord(): Flow<List<AnalyzeResult>>

    @Delete
    suspend fun deleteRecord(AnalyzeResult: AnalyzeResult)

    @Query("DELETE FROM drowsy_table")
    suspend fun deleteAllRecords()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrowsyCount(values: DrowsyCount)

    @Query("SELECT * FROM drowsyCount WHERE recordId = :recordId")
    fun getDrowsyCount(recordId: Int): Flow<List<DrowsyCount>>

    @Query("SELECT * FROM drowsyCount")
    fun getAllDrowsyCount(): Flow<List<DrowsyCount>>

    @Query("DELETE FROM drowsyCount")
    suspend fun deleteAllDrowsyCount()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWinkCount(values: WinkCount)

    @Query("SELECT * FROM winkCount WHERE recordId = :recordId")
    fun getWinkCount(recordId: Int): Flow<List<WinkCount>>

    @Query("SELECT * FROM winkCount")
    fun getAllWinkCount(): Flow<List<WinkCount>>

    @Query("DELETE FROM winkCount")
    suspend fun deleteAllWinkCount()

}