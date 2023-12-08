package com.paradise.data.repositoryImpl

import com.core.model.parkingLotItem
import com.paradise.common.network.BoundingBox
import com.paradise.common.network.DAY
import com.paradise.common.network.defaultDispatcher
import com.paradise.common.network.ioDispatcher
import com.paradise.common.network.isInTime
import com.paradise.data.repository.ParkingLotRepository
import com.paradise.network.Mapper.toParkingLotItemList
import com.paradise.network.provider.ParkingLotDataProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ParkingLotRepositoryImpl @Inject constructor(private val parkingLotDataProvider: ParkingLotDataProvider) :
    ParkingLotRepository {
    override suspend fun getOneParkingLot(
        pageNo: Int,
        numOfRows: Int,
        parkingchrgeInfo: String,
    ): Flow<Int> = parkingLotDataProvider.getAllParkingLot(
        pageNo = pageNo, numOfRows = numOfRows, parkingchrgeInfo = parkingchrgeInfo
    ).map { it.response.body.totalCount.toInt() }

    override suspend fun getAllParkingLot(
        pageNo: Int,
        numOfRows: Int,
        boundingBox: BoundingBox,
        parkingchargeInfo: String,
        day: DAY,
        nowTime: String,
        parkingchrgeInfo: String,
    ): Flow<List<parkingLotItem>> = flow<List<parkingLotItem>> {
        val freeParkingLot = mutableListOf<parkingLotItem>()
        parkingLotDataProvider.getAllParkingLot(
            pageNo = pageNo, numOfRows = numOfRows, parkingchrgeInfo = parkingchrgeInfo
        ).map { result ->
            val resultConverted = result.toParkingLotItemList()
            resultConverted.asFlow().flowOn(defaultDispatcher).filter { item ->
                val lat = item.latitude.toDoubleOrNull()
                val lon = item.longitude.toDoubleOrNull()
                val inLatRange =
                    lat != null && lat in boundingBox.minLatitude..boundingBox.maxLatitude
                val inLonRange =
                    lon != null && lon in boundingBox.minLongitude..boundingBox.maxLongitude
                val inTime = isInTime(day = day, nowTime = nowTime, item = item)
                inLatRange && inLonRange && inTime
            }.collect { item ->
                freeParkingLot.add(item)
            }
            emit(freeParkingLot)
        }
    }.flowOn(ioDispatcher).cancellable()

}