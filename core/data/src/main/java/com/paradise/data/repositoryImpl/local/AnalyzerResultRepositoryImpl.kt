package com.paradise.data.repositoryImpl.local

import com.core.model.analyzeResultItem
import com.core.model.drowsyResultItem
import com.core.model.winkResultItem
import com.paradise.data.mapper.toDataAnalyzeResult
import com.paradise.data.mapper.toDataDrowsyCount
import com.paradise.data.mapper.toDataWinkCount
import com.paradise.data.repository.AnalyzerResultRepository
import com.paradise.database.mapper.toAnalyzeResultItem
import com.paradise.database.mapper.toDrowsyItem
import com.paradise.database.mapper.toWinkItem
import com.paradise.database.room.dao.AnalyzeResultDao
import com.paradise.database.room.model.AnalyzeResult
import com.paradise.database.room.model.DrowsyCount
import com.paradise.database.room.model.WinkCount
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject

class AnalyzerResultRepositoryImpl @Inject constructor(private val analyzeResultDataProvider: AnalyzeResultDao) :
    AnalyzerResultRepository {
    override suspend fun insertRecord(analyzeResultItem: analyzeResultItem) {
        analyzeResultDataProvider.insertRecord(analyzeResultItem.toDataAnalyzeResult())
    }

    override suspend fun insertDrowsyCount(drowsyResultItem: drowsyResultItem) {
        analyzeResultDataProvider.insertDrowsyCount(drowsyResultItem.toDataDrowsyCount())
    }

    override suspend fun insertWinkCount(winkResultItem: winkResultItem) {
        analyzeResultDataProvider.insertWinkCount(winkResultItem.toDataWinkCount())
    }

    override fun getAllRecord() =
        analyzeResultDataProvider.getAllRecord().map { it?.map(AnalyzeResult::toAnalyzeResultItem) }

    override fun getRecord(Id: Int) =
        analyzeResultDataProvider.getRecord(Id).map { it?.toAnalyzeResultItem() }

    override fun getRecord(time: String) =
        analyzeResultDataProvider.getRecord(time).map { it?.toAnalyzeResultItem() }

    override fun getAllDrowsyCount() =
        analyzeResultDataProvider.getAllDrowsyCount().map{ it?.map(DrowsyCount::toDrowsyItem) }

    override fun getDrowsyCount(recordId: Int) =
        analyzeResultDataProvider.getDrowsyCount(recordId).map { it?.map(DrowsyCount::toDrowsyItem) }

    override fun getAllWinkCount() =
        analyzeResultDataProvider.getAllWinkCount().map { it?.map(WinkCount::toWinkItem) }


    override fun getWinkCount(recordId: Int) =
        analyzeResultDataProvider.getWinkCount(recordId).map { it?.map(WinkCount::toWinkItem) }


    override suspend fun deleteAllRecords() {
        analyzeResultDataProvider.deleteAllRecords()
    }

    override suspend fun deleteRecord(analyzeResultItem: analyzeResultItem) {
        analyzeResultDataProvider.deleteRecord(analyzeResultItem.toDataAnalyzeResult())
    }

    override suspend fun deleteAllDrowsyCount() {
        analyzeResultDataProvider.deleteAllDrowsyCount()
    }

    override suspend fun deleteAllWinkCount() {
        analyzeResultDataProvider.deleteAllWinkCount()
    }

}