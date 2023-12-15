package com.paradise.data.repositoryImpl.remote

import com.core.model.BoundingBox
import com.core.model.parkingLotItem
import com.paradise.common.network.DAY
import com.paradise.common.network.compareTime
import com.paradise.common.network.defaultDispatcher
import com.paradise.common.network.ioDispatcher
import com.paradise.data.repository.ParkingLotRepository
import com.paradise.network.mapper.toParkingLotItem
import com.paradise.network.provider.ParkingLotDataProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class ParkingLotRepositoryImpl @Inject constructor(private val parkingLotDataProvider: ParkingLotDataProvider) :
    ParkingLotRepository {
    override suspend fun getOneParkingLot(): Flow<Int> = flow {
        parkingLotDataProvider.getAllParkingLot(
            pageNo = 1, numOfRows = 1
        ).collect { emit(it?.response?.body?.totalCount?.toInt() ?: 0) }
    }

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
        ).collect { result ->
            result?.response?.body?.items?.asFlow()?.flowOn(defaultDispatcher)?.filter { item ->
                val lat = item.latitude.toDoubleOrNull()
                val lon = item.longitude.toDoubleOrNull()
                val inLatRange =
                    lat != null && lat in boundingBox.minLatitude..boundingBox.maxLatitude
                val inLonRange =
                    lon != null && lon in boundingBox.minLongitude..boundingBox.maxLongitude
                val inTime = when (day) {
                    DAY.WEEKDAY -> {
                        compareTime(
                            nowTime, item.weekdayOperOpenHhmm, item.weekdayOperColseHhmm
                        )
                    }

                    DAY.SAT -> {
                        compareTime(nowTime, item.satOperOperOpenHhmm, item.satOperCloseHhmm)
                    }

                    DAY.HOLIDAY -> {
                        compareTime(
                            nowTime, item.holidayOperOpenHhmm, item.holidayCloseOpenHhmm
                        )
                    }
                }
                inLatRange && inLonRange && inTime
            }?.collect { item ->
                freeParkingLot.add(item.toParkingLotItem())
            }
            emit(freeParkingLot)
        }
    }.flowOn(ioDispatcher).cancellable()

}