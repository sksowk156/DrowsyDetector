package com.paradise.database.provider

import com.paradise.database.room.dao.AnalyzeResultDao
import com.paradise.database.room.model.AnalyzeResult
import com.paradise.database.room.model.DrowsyCount
import com.paradise.database.room.model.WinkCount
import javax.inject.Inject

class AnalyzeResultDataProvider(private val analyzeResultDao: AnalyzeResultDao) {
    suspend fun insertRecord(AnalyzeResult: AnalyzeResult) {
        analyzeResultDao.insertRecord(AnalyzeResult)
    }

    suspend fun insertDrowsyCount(values: DrowsyCount) {
        analyzeResultDao.insertDrowsyCount(values)
    }

    suspend fun insertWinkCount(values: WinkCount) {
        analyzeResultDao.insertWinkCount(values)
    }

    fun getAllRecord() = analyzeResultDao.getAllRecord()
    fun getRecord(id: Int) = analyzeResultDao.getRecord(id)
    fun getRecord(time: String) = analyzeResultDao.getRecord(time)

    fun getAllDrowsyCount() = analyzeResultDao.getAllDrowsyCount()

    fun getDrowsyCount(recordId: Int) = analyzeResultDao.getDrowsyCount(recordId)

    fun getAllWinkCount() = analyzeResultDao.getAllWinkCount()

    fun getWinkCount(recordId: Int) = analyzeResultDao.getWinkCount(recordId)

    suspend fun deleteAllRecords() {
        analyzeResultDao.deleteAllRecords()
    }

    suspend fun deleteRecord(AnalyzeResult: AnalyzeResult) {
        analyzeResultDao.deleteRecord(AnalyzeResult)
    }

    suspend fun deleteAllDrowsyCount() {
        analyzeResultDao.deleteAllDrowsyCount()
    }

    suspend fun deleteAllWinkCount() {
        analyzeResultDao.deleteAllWinkCount()
    }

}