package com.paradise.domain.repository

import com.paradise.common.network.DEFAULT_NUM_OF_ROWS
import com.paradise.domain.model.parkingLotItem

interface ParkingLotRepository {
    suspend fun getAllParkingLot(
        pageNo: Int,
        numOfRows: Int = DEFAULT_NUM_OF_ROWS,
        type: String = "json",
        parkingchrgeInfo: String,
    ): List<parkingLotItem>

    suspend fun getOneParkingLot(
        pageNo: Int,
        numOfRows: Int,
        type: String = "json",
        parkingchrgeInfo: String,
    ): Int
}