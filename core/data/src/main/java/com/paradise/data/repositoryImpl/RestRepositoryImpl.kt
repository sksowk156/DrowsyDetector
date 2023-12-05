package com.paradise.data.repositoryImpl

import com.core.model.restItem
import com.paradise.data.mapper.toDomainRestItemList
import com.paradise.domain.repository.RestRepository
import com.paradise.network.provider.RestDataProvider
import javax.inject.Inject

class RestRepositoryImpl @Inject constructor(private val restDataProvider: RestDataProvider) :
    RestRepository {
    override suspend fun getAllRest(
        pageNo: Int,
        numOfRows: Int,
        type: String,
    ): List<restItem> {
        return restDataProvider.getAllRest(pageNo, numOfRows, type).toDomainRestItemList()
    }

}