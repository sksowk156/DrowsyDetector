package com.paradise.drowsydetector.data.remote.shelter

import com.paradise.drowsydetector.utils.ApiKey.Companion.API_KEY
import com.paradise.drowsydetector.utils.DEFAULTNUMOFROWS
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface DrowyShelterInterface {
    @GET("tn_pubr_public_drowsy_shelter_api?serviceKey=$API_KEY")
    suspend fun getAllShelter(
        @Query("pageNo") pageNo: Int = 1,
        @Query("numOfRows") numOfRows: Int = DEFAULTNUMOFROWS,
        @Query("type") type: String = "json",
        @Query("ctprvnNm") ctprvnNm: String?=null,
        @Query("signguNm") signguNm: String?=null,
    ): Response<DrowsyShelter>
}