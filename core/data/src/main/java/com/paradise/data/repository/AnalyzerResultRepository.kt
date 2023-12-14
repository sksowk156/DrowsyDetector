package com.paradise.data.repository

import com.core.model.analyzeResultItem
import com.core.model.drowsyResultItem
import com.core.model.winkResultItem
import kotlinx.coroutines.flow.Flow

interface AnalyzerResultRepository {
    suspend fun insertRecord(analyzeResultItem: analyzeResultItem)
    suspend fun insertDrowsyCount(drowsyResultItem: drowsyResultItem)
    suspend fun insertWinkCount(winkResultItem: winkResultItem)
    fun getAllRecord(): Flow<List<analyzeResultItem>?>
    fun getRecord(id: Int): Flow<analyzeResultItem?>
    fun getRecord(time: String): Flow<analyzeResultItem?>
    fun getAllDrowsyCount(): Flow<List<drowsyResultItem>?>
    fun getDrowsyCount(recordId: Int): Flow<List<drowsyResultItem>?>
    fun getAllWinkCount(): Flow<List<winkResultItem>?>
    fun getWinkCount(recordId: Int): Flow<List<winkResultItem>?>
    suspend fun deleteAllRecords()
    suspend fun deleteRecord(analyzeResultItem: analyzeResultItem)
    suspend fun deleteAllDrowsyCount()
    suspend fun deleteAllWinkCount()
}