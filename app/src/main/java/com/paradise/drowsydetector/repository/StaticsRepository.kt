package com.paradise.drowsydetector.repository

import com.paradise.drowsydetector.data.local.room.record.DrowsyCount
import com.paradise.drowsydetector.data.local.room.record.DrowsyCountDao
import com.paradise.drowsydetector.data.local.room.record.DrowsyRecord
import com.paradise.drowsydetector.data.local.room.record.RecordDao
import com.paradise.drowsydetector.data.local.room.record.WinkCount
import com.paradise.drowsydetector.data.local.room.record.WinkCountDao

class StaticsRepository(
    private val recordDao: RecordDao,
    private val winkCountDao: WinkCountDao,
    private val drowsyCountDao: DrowsyCountDao,
) {
    companion object {
        @Volatile
        private var instance: StaticsRepository? = null

        fun getInstance(
            recordDao: RecordDao, winkCountDao: WinkCountDao, drowsyCountDao: DrowsyCountDao,
        ) =
            instance ?: synchronized(this) {
                instance ?: StaticsRepository(
                    recordDao = recordDao,
                    winkCountDao = winkCountDao,
                    drowsyCountDao = drowsyCountDao
                ).also { instance = it }
            }
    }

    // DrowsyRecord
    suspend fun insertRecord(drowsyRecord: DrowsyRecord) {
        recordDao.insertRecord(drowsyRecord)
    }

    fun getAllRecord() = recordDao.getAllRecord()

    fun getRecord(id: Int) = recordDao.getRecord(id)

    fun getRecord(time: String) = recordDao.getRecord(time)


    suspend fun deleteRecord(drowsyRecord: DrowsyRecord) {
        recordDao.deleteRecord(drowsyRecord)
    }

    suspend fun deleteAllRecords() {
        recordDao.deleteAllRecords()
    }


    // WinkCount
    suspend fun insertWinkCount(winkCount: WinkCount) {
        winkCountDao.insertWinkCount(winkCount)
    }

    fun getWinkCount(recordId: Int) = winkCountDao.getWinkCount(recordId)

    fun getAllWinkCount() = winkCountDao.getAllWinkCount()

    suspend fun deleteAllWinkCount() = winkCountDao.deleteAllWinkCount()

    // DrowsyCount
    suspend fun insertDrowsyCount(drowsyCount: DrowsyCount) {
        drowsyCountDao.insertDrowsyCount(drowsyCount)
    }

    fun getDrowsyCount(recordId: Int) =
        drowsyCountDao.getDrowsyCount(recordId)

    fun getAllDrowsyCount() =
        drowsyCountDao.getAllDrowsyCount()

    suspend fun deleteAllDrowsyCount(){
        drowsyCountDao.deleteAllDrowsyCount()
    }
}