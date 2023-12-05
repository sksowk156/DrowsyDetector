package com.paradise.domain.repository

import com.core.model.analyzeResultItem
import com.core.model.drowsyResultItem
import com.core.model.winkResultItem

interface AnalyzerResultRepository {
    suspend fun insertRecord(analyzeResultItem: analyzeResultItem)
    suspend fun insertDrowsyCount(drowsyResultItem: drowsyResultItem)
    suspend fun insertWinkCount(winkResultItem: winkResultItem)
    fun getAllRecord(): List<analyzeResultItem>
    fun getRecord(id: Int): analyzeResultItem
    fun getRecord(time: String): analyzeResultItem
    fun getAllDrowsyCount(): List<drowsyResultItem>
    fun getDrowsyCount(recordId: Int): List<drowsyResultItem>
    fun getAllWinkCount(): List<winkResultItem>
    fun getWinkCount(recordId: Int): List<winkResultItem>
    suspend fun deleteAllRecords()
    suspend fun deleteRecord(analyzeResultItem: analyzeResultItem)
    suspend fun deleteAllDrowsyCount()
    suspend fun deleteAllWinkCount()
}