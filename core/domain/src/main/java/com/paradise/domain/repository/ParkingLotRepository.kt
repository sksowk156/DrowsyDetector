package com.paradise.domain.repository

import com.core.model.parkingLotItem
import com.paradise.common.network.DEFAULT_NUM_OF_ROWS

interface ParkingLotRepository {
    suspend fun getAllParkingLot(
        pageNo: Int,
        numOfRows: Int = DEFAULT_NUM_OF_ROWS,
        type: String = "json",
        parkingchrgeInfo: String,
    ): List<parkingLotItem>

    suspend fun getOneParkingLot(
        pageNo: Int = 1,
        numOfRows: Int = 1,
        type: String = "json",
        parkingchrgeInfo: String,
    ): Int
}