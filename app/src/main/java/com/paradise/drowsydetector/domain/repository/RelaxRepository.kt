package com.paradise.drowsydetector.domain.repository

import com.paradise.drowsydetector.utils.BoundingBox
import com.paradise.drowsydetector.utils.DAY
import com.paradise.drowsydetector.utils.DEFAULT_NUM_OF_ROWS
import com.paradise.drowsydetector.utils.ResponseState
import kotlinx.coroutines.flow.Flow
import com.paradise.drowsydetector.data.remote.parkinglot.Item as parkingLotItem
import com.paradise.drowsydetector.data.remote.rest.Item as restItem
import com.paradise.drowsydetector.data.remote.shelter.Item as shelterItem

interface RelaxRepository {
    suspend fun getAllRest(
        boundingBox: BoundingBox,
    ): Flow<ResponseState<List<restItem>>>

    suspend fun getAllShelter(
        boundingBox: BoundingBox,
    ): Flow<ResponseState<List<shelterItem>>>

    fun getParkingLots1(
        boundingBox: BoundingBox,
        parkingchargeInfo: String,
        numOfCoroutineRequired: Int,
        day: DAY,
        nowTime: String,
    ): List<Flow<ResponseState<MutableList<parkingLotItem>>>>

    fun getAllParkingLot(
        boundingBox: BoundingBox,
        parkingchargeInfo: String,
        numOfRows: Int = DEFAULT_NUM_OF_ROWS, day: DAY, nowTime: String,
    ): Flow<ResponseState<List<Flow<ResponseState<MutableList<parkingLotItem>>>>>>

    fun getParkingLot1(
        pageNo: Int, boundingBox: BoundingBox, parkingchargeInfo: String, day: DAY, nowTime: String,
    ): Flow<ResponseState<MutableList<parkingLotItem>>>
}