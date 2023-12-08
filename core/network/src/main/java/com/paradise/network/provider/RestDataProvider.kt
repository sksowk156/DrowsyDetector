package com.paradise.network.provider

import com.paradise.common.network.DEFAULT_NUM_OF_ROWS
import com.paradise.network.retrofit.rest.RestService
import com.paradise.network.retrofit.rest.model.RestModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RestDataProvider @Inject constructor(private val restService: RestService) {
    suspend fun getAllRest(
        pageNo: Int = 1,
        numOfRows: Int = DEFAULT_NUM_OF_ROWS,
        type: String = "json",
    ): Flow<RestModel> = flow { restService.getAllRest(pageNo, numOfRows, type) }
}