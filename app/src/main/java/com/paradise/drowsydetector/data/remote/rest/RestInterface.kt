package com.paradise.drowsydetector.data.remote.rest

import com.paradise.drowsydetector.utils.ApiKey
import com.paradise.drowsydetector.utils.DEFAULTNUMOFROWS
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface RestInterface {
    @GET("tn_pubr_public_rest_area_api?serviceKey=${ApiKey.API_KEY}")
    suspend fun getAllRest(
        @Query("pageNo") pageNo: Int = 1,
        @Query("numOfRows") numOfRows: Int = DEFAULTNUMOFROWS,
        @Query("type") type: String = "json",
    ): Response<Rest>
}