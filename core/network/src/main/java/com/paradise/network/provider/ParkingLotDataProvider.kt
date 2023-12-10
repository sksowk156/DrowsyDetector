package com.paradise.network.provider

import com.paradise.common.network.DEFAULT_NUM_OF_ROWS
import com.paradise.network.retrofit.parkinglot.ParkingLotService
import com.paradise.network.retrofit.parkinglot.model.ParkingLotModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response

class ParkingLotDataProvider(
    private val parkingLotService: ParkingLotService,
) {
    suspend fun getAllParkingLot(
        pageNo: Int,
        numOfRows: Int = DEFAULT_NUM_OF_ROWS,
        type: String = "json",
        parkingchrgeInfo: String? = "무료",
    ): Flow<Response<ParkingLotModel>> = flow {
        emit(parkingLotService.getAllParkingLot(pageNo, numOfRows, type, parkingchrgeInfo))
    }
}
