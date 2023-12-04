package com.paradise.network.retrofit.rest

import com.paradise.common.network.ApiKey
import com.paradise.common.network.DEFAULT_NUM_OF_ROWS
import com.paradise.network.retrofit.rest.model.RestModel
import retrofit2.http.GET
import retrofit2.http.Query

interface RestService {
    @GET("tn_pubr_public_rest_area_api?serviceKey=${ApiKey.API_KEY}")
    suspend fun getAllRest(
        @Query("pageNo") pageNo: Int = 1,
        @Query("numOfRows") numOfRows: Int = DEFAULT_NUM_OF_ROWS,
        @Query("type") type: String = "json",
    ): RestModel
}