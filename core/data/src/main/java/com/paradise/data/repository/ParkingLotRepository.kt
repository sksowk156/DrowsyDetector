package com.paradise.data.repository

import com.core.model.BoundingBox
import com.core.model.parkingLotItem
import com.paradise.common.network.DAY
import kotlinx.coroutines.flow.Flow

interface ParkingLotRepository {
    suspend fun getOneParkingLot(
        pageNo: Int,
        numOfRows: Int,
        parkingchrgeInfo: String,
    ): Flow<Int>

    suspend fun getAllParkingLot(
        pageNo: Int,
        numOfRows: Int,
        boundingBox: BoundingBox,
        parkingchargeInfo: String,
        day: DAY,
        nowTime: String,
        parkingchrgeInfo: String,
    ): Flow<List<parkingLotItem>>
}