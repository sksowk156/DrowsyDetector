package com.paradise.drowsydetector.data.remote.parkinglot

import com.paradise.drowsydetector.utils.ApiKey
import com.paradise.drowsydetector.utils.DEFAULT_NUM_OF_ROWS
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ParkingLotInterface {
    @GET("tn_pubr_prkplce_info_api?serviceKey=${ApiKey.API_KEY}")
    suspend fun getAllParkingLot(
        @Query("pageNo") pageNo: Int = 1,
        @Query("numOfRows") numOfRows: Int = DEFAULT_NUM_OF_ROWS,
        @Query("type") type: String = "json",
        @Query("parkingchrgeInfo") parkingchrgeInfo : String
    ): Response<ParkingLot>
}