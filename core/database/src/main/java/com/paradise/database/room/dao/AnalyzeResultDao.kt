package com.paradise.database.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.paradise.database.room.model.AnalyzeResult
import com.paradise.database.room.model.DrowsyCount
import com.paradise.database.room.model.WinkCount

@Dao
interface AnalyzeResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(AnalyzeResult: AnalyzeResult)

    @Query("SELECT * FROM drowsy_table WHERE id = :id")
    fun getRecord(id: Int): AnalyzeResult

    @Query("SELECT * FROM drowsy_table WHERE time = :time")
    fun getRecord(time: String): AnalyzeResult

    @Query("SELECT * FROM drowsy_table")
    fun getAllRecord(): List<AnalyzeResult>

    @Delete
    suspend fun deleteRecord(AnalyzeResult: AnalyzeResult)

    @Query("DELETE FROM drowsy_table")
    suspend fun deleteAllRecords()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrowsyCount(values: DrowsyCount)

    @Query("SELECT * FROM drowsyCount WHERE recordId = :recordId")
    fun getDrowsyCount(recordId: Int): List<DrowsyCount>

    @Query("SELECT * FROM drowsyCount")
    fun getAllDrowsyCount(): List<DrowsyCount>

    @Query("DELETE FROM drowsyCount")
    suspend fun deleteAllDrowsyCount()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWinkCount(values: WinkCount)

    @Query("SELECT * FROM winkCount WHERE recordId = :recordId")
    fun getWinkCount(recordId: Int): List<WinkCount>

    @Query("SELECT * FROM winkCount")
    fun getAllWinkCount(): List<WinkCount>

    @Query("DELETE FROM winkCount")
    suspend fun deleteAllWinkCount()

}