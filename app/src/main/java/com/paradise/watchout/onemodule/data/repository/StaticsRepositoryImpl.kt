//package com.paradise.drowsydetector.data.repository
//
//import com.paradise.drowsydetector.data.local.room.record.AnalyzeResult
//import com.paradise.drowsydetector.data.local.room.record.AnalyzeResultDao
//import com.paradise.drowsydetector.data.local.room.record.DrowsyCount
//import com.paradise.drowsydetector.data.local.room.record.WinkCount
//import com.paradise.drowsydetector.domain.repository.StaticsRepository
//import javax.inject.Inject
//
//class StaticsRepositoryImpl @Inject constructor(
//    private val analyzeResultDao: AnalyzeResultDao,
//) : StaticsRepository {
////    companion object {
////        @Volatile
////        private var instance: StaticsRepositoryImpl? = null
////
////        fun getInstance(analyzeResultDao: AnalyzeResultDao) = instance ?: synchronized(this) {
////            instance ?: StaticsRepositoryImpl(
////                analyzeResultDao = analyzeResultDao
////            ).also { instance = it }
////        }
////    }
//
//    // AnalyzeResult
//    override suspend fun insertRecord(AnalyzeResult: AnalyzeResult) {
//        analyzeResultDao.insertRecord(AnalyzeResult)
//    }
//
//    override fun getAllRecord() = analyzeResultDao.getAllRecord()
//
//    override fun getRecord(id: Int) = analyzeResultDao.getRecord(id)
//
//    override fun getRecord(time: String) = analyzeResultDao.getRecord(time)
//
//
//    override suspend fun deleteRecord(AnalyzeResult: AnalyzeResult) {
//        analyzeResultDao.deleteRecord(AnalyzeResult)
//    }
//
//    override suspend fun deleteAllRecords() {
//        analyzeResultDao.deleteAllRecords()
//    }
//
//
//    // WinkCount
//    override suspend fun insertWinkCount(winkCount: WinkCount) {
//        analyzeResultDao.insertWinkCount(winkCount)
//    }
//
//    override fun getWinkCount(recordId: Int) = analyzeResultDao.getWinkCount(recordId)
//
//    override fun getAllWinkCount() = analyzeResultDao.getAllWinkCount()
//
//    override suspend fun deleteAllWinkCount() {
//        analyzeResultDao.deleteAllWinkCount()
//    }
//
//    // DrowsyCount
//    override suspend fun insertDrowsyCount(drowsyCount: DrowsyCount) {
//        analyzeResultDao.insertDrowsyCount(drowsyCount)
//    }
//
//    override fun getDrowsyCount(recordId: Int) = analyzeResultDao.getDrowsyCount(recordId)
//
//    override fun getAllDrowsyCount() = analyzeResultDao.getAllDrowsyCount()
//
//    override suspend fun deleteAllDrowsyCount() {
//        analyzeResultDao.deleteAllDrowsyCount()
//    }
//}