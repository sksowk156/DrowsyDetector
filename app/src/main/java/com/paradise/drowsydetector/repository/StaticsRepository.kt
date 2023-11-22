package com.paradise.drowsydetector.repository

import com.paradise.drowsydetector.data.local.room.record.DrowsyCount
import com.paradise.drowsydetector.data.local.room.record.AnalyzeResult
import com.paradise.drowsydetector.data.local.room.record.AnalyzeResultDao
import com.paradise.drowsydetector.data.local.room.record.WinkCount

class StaticsRepository(
    private val analyzeResultDao: AnalyzeResultDao,
) {
    companion object {
        @Volatile
        private var instance: StaticsRepository? = null

        fun getInstance(analyzeResultDao: AnalyzeResultDao) =
            instance ?: synchronized(this) {
                instance ?: StaticsRepository(
                    analyzeResultDao = analyzeResultDao
                ).also { instance = it }
            }
    }

    // AnalyzeResult
    suspend fun insertRecord(AnalyzeResult: AnalyzeResult) {
        analyzeResultDao.insertRecord(AnalyzeResult)
    }

    fun getAllRecord() = analyzeResultDao.getAllRecord()

    fun getRecord(id: Int) = analyzeResultDao.getRecord(id)

    fun getRecord(time: String) = analyzeResultDao.getRecord(time)


    suspend fun deleteRecord(AnalyzeResult: AnalyzeResult) {
        analyzeResultDao.deleteRecord(AnalyzeResult)
    }

    suspend fun deleteAllRecords() {
        analyzeResultDao.deleteAllRecords()
    }


    // WinkCount
    suspend fun insertWinkCount(winkCount: WinkCount) {
        analyzeResultDao.insertWinkCount(winkCount)
    }

    fun getWinkCount(recordId: Int) = analyzeResultDao.getWinkCount(recordId)

    fun getAllWinkCount() = analyzeResultDao.getAllWinkCount()

    suspend fun deleteAllWinkCount() = analyzeResultDao.deleteAllWinkCount()

    // DrowsyCount
    suspend fun insertDrowsyCount(drowsyCount: DrowsyCount) {
        analyzeResultDao.insertDrowsyCount(drowsyCount)
    }

    fun getDrowsyCount(recordId: Int) =
        analyzeResultDao.getDrowsyCount(recordId)

    fun getAllDrowsyCount() =
        analyzeResultDao.getAllDrowsyCount()

    suspend fun deleteAllDrowsyCount() {
        analyzeResultDao.deleteAllDrowsyCount()
    }
}