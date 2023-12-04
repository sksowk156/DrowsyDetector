package com.paradise.network.provider

import com.paradise.network.retrofit.parkinglot.ParkingLotService
import javax.inject.Inject

class ParkingLotDataProvider @Inject constructor(
    private val parkingLotService: ParkingLotService,
) {
    suspend fun getAllParkingLot(
        pageNo: Int,
        numOfRows: Int,
        type: String,
        parkingchrgeInfo: String,
    ) = parkingLotService.getAllParkingLot(pageNo, numOfRows, type, parkingchrgeInfo)
}