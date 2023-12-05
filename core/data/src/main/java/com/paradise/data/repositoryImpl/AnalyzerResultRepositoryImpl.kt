package com.paradise.data.repositoryImpl

import com.core.model.analyzeResultItem
import com.core.model.drowsyResultItem
import com.core.model.winkResultItem
import com.paradise.data.mapper.toDataAnalyzeResult
import com.paradise.data.mapper.toDataDrowsyCount
import com.paradise.data.mapper.toDataWinkCount
import com.paradise.data.mapper.toDomainAnalyzeResultItem
import com.paradise.data.mapper.toDomainDrowsyItem
import com.paradise.data.mapper.toDomainWinkItem
import com.paradise.database.provider.AnalyzeResultDataProvider
import com.paradise.domain.repository.AnalyzerResultRepository
import javax.inject.Inject

class AnalyzerResultRepositoryImpl @Inject constructor(private val analyzeResultDataProvider: AnalyzeResultDataProvider) :
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
        analyzeResultDataProvider.getAllRecord().map { it.toDomainAnalyzeResultItem() }

    override fun getRecord(Id: Int) =
        analyzeResultDataProvider.getRecord(Id).toDomainAnalyzeResultItem()

    override fun getRecord(time: String) =
        analyzeResultDataProvider.getRecord(time).toDomainAnalyzeResultItem()

    override fun getAllDrowsyCount() =
        analyzeResultDataProvider.getAllDrowsyCount().map { it.toDomainDrowsyItem() }

    override fun getDrowsyCount(recordId: Int) =
        analyzeResultDataProvider.getDrowsyCount(recordId).map { it.toDomainDrowsyItem() }


    override fun getAllWinkCount() =
        analyzeResultDataProvider.getAllWinkCount().map { it.toDomainWinkItem() }


    override fun getWinkCount(recordId: Int) =
        analyzeResultDataProvider.getWinkCount(recordId).map { it.toDomainWinkItem() }


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