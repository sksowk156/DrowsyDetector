package com.paradise.network.retrofit.parkinglot

import com.paradise.common.network.ApiKey
import com.paradise.common.network.DEFAULT_NUM_OF_ROWS
import com.paradise.network.retrofit.parkinglot.model.ParkingLotModel
import retrofit2.http.GET
import retrofit2.http.Query

interface ParkingLotService {
    @GET("tn_pubr_prkplce_info_api?serviceKey=${ApiKey.API_KEY}")
    suspend fun getAllParkingLot(
        @Query("pageNo") pageNo: Int = 1,
        @Query("numOfRows") numOfRows: Int = DEFAULT_NUM_OF_ROWS,
        @Query("type") type: String = "json",
        @Query("parkingchrgeInfo") parkingchrgeInfo : String
    ): ParkingLotModel
}