package com.paradise.network.retrofit.shelter

import com.paradise.common.network.ApiKey.Companion.API_KEY
import com.paradise.common.network.DEFAULT_NUM_OF_ROWS
import com.paradise.network.retrofit.shelter.model.ShelterModel
import retrofit2.http.GET
import retrofit2.http.Query


interface ShelterService {
    @GET("tn_pubr_public_drowsy_shelter_api?serviceKey=$API_KEY")
    suspend fun getAllShelter(
        @Query("pageNo") pageNo: Int = 1,
        @Query("numOfRows") numOfRows: Int = DEFAULT_NUM_OF_ROWS,
        @Query("type") type: String = "json",
        @Query("ctprvnNm") ctprvnNm: String? = null,
        @Query("signguNm") signguNm: String? = null,
    ): ShelterModel
}