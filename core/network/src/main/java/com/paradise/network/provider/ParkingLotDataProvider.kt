package com.paradise.network.provider

import com.paradise.common.network.DEFAULT_NUM_OF_ROWS
import com.paradise.network.retrofit.parkinglot.ParkingLotService
import com.paradise.network.retrofit.parkinglot.model.ParkingLotModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ParkingLotDataProvider @Inject constructor(
    private val parkingLotService: ParkingLotService,
) {
    suspend fun getAllParkingLot(
        pageNo: Int,
        numOfRows: Int = DEFAULT_NUM_OF_ROWS,
        type: String = "json",
        parkingchrgeInfo: String,
    ): Flow<ParkingLotModel> =
        flow { parkingLotService.getAllParkingLot(pageNo, numOfRows, type, parkingchrgeInfo) }
}
