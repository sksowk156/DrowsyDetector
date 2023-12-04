package com.paradise.network.provider

import com.paradise.network.retrofit.rest.RestService
import javax.inject.Inject

class RestDataProvider @Inject constructor(private val restService: RestService) {
    suspend fun getAllRest(
        pageNo: Int,
        numOfRows: Int,
        type: String,
    ) = restService.getAllRest(pageNo, numOfRows, type)
}