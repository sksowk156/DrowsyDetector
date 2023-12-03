package com.paradise.drowsydetector.domain.repository

import com.paradise.drowsydetector.data.local.room.record.AnalyzeResult
import com.paradise.drowsydetector.data.local.room.record.DrowsyCount
import com.paradise.drowsydetector.data.local.room.record.WinkCount
import kotlinx.coroutines.flow.Flow

interface StaticsRepository {
    suspend fun insertRecord(AnalyzeResult: AnalyzeResult)
    fun getAllRecord(): Flow<List<AnalyzeResult>>
    fun getRecord(id: Int): Flow<AnalyzeResult>
    fun getRecord(time: String): Flow<AnalyzeResult>
    suspend fun deleteRecord(AnalyzeResult: AnalyzeResult)
    suspend fun deleteAllRecords()
    suspend fun insertWinkCount(winkCount: WinkCount)
    fun getWinkCount(recordId: Int): Flow<List<WinkCount>>
    fun getAllWinkCount(): Flow<List<WinkCount>>
    suspend fun deleteAllWinkCount()

    // DrowsyCount
    suspend fun insertDrowsyCount(drowsyCount: DrowsyCount)
    fun getDrowsyCount(recordId: Int): Flow<List<DrowsyCount>>
    fun getAllDrowsyCount(): Flow<List<DrowsyCount>>
    suspend fun deleteAllDrowsyCount()
}