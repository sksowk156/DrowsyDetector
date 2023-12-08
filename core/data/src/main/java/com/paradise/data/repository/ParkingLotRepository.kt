package com.paradise.data.repository

import com.core.model.parkingLotItem
import com.paradise.common.network.BoundingBox
import com.paradise.common.network.DAY
import com.paradise.common.network.DEFAULT_NUM_OF_ROWS
import com.paradise.network.Mapper.toParkingLotItemList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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