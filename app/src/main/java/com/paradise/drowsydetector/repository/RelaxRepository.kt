package com.paradise.drowsydetector.repository

import com.paradise.drowsydetector.data.remote.parkinglot.ParkingLotInterface
import com.paradise.drowsydetector.data.remote.rest.RestInterface
import com.paradise.drowsydetector.data.remote.shelter.DrowyShelterInterface
import com.paradise.drowsydetector.utils.BoundingBox
import com.paradise.drowsydetector.utils.DAY
import com.paradise.drowsydetector.utils.DEFAULT_NUM_OF_ROWS
import com.paradise.drowsydetector.utils.ResponseState
import com.paradise.drowsydetector.utils.isInTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancel
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import com.paradise.drowsydetector.data.remote.parkinglot.Item as parkingLotItem
import com.paradise.drowsydetector.data.remote.rest.Item as restItem
import com.paradise.drowsydetector.data.remote.shelter.Item as shelterItem

class RelaxRepository(
    private val drowyShelterInterface: DrowyShelterInterface,
    private val parkingLotInterface: ParkingLotInterface,
    private val restInterface: RestInterface,
) {
    //Singleton으로 객체 생성
    companion object {
        @Volatile
        private var instance: RelaxRepository? = null
        fun getInstance(
            drowyShelterInterface: DrowyShelterInterface,
            parkingLotInterface: ParkingLotInterface,
            restInterface: RestInterface,
        ) =
            instance ?: synchronized(this) {
                instance ?: RelaxRepository(
                    drowyShelterInterface,
                    parkingLotInterface,
                    restInterface
                ).also { instance = it }
            }
    }

    suspend fun getAllRest(
        boundingBox: BoundingBox,
    ): Flow<ResponseState<List<restItem>>> = flow {
        try {
            val response =
                restInterface.getAllRest()
            if (response.isSuccessful) {
                response.body()?.let {
                    val nearShelter = it.response.body.items.filter {
                        val lat = it.latitude.toDouble()
                        (lat < boundingBox.maxLatitude && lat > boundingBox.minLatitude)
                    }.filter {
                        val lon = it.longitude.toDouble()
                        (lon < boundingBox.maxLongitude && lon > boundingBox.minLongitude)
                    }
                    emit(ResponseState.Success(nearShelter))
                }
            } else {
                emit(ResponseState.Fail(response.code(), response.message()))
            }
        } catch (e: Exception) {
            emit(ResponseState.Error(e))
        } finally {
            currentCoroutineContext().cancel()
        } as Unit
    }.flowOn(Dispatchers.IO).cancellable()

    suspend fun getAllShelter(
        boundingBox: BoundingBox,
    ): Flow<ResponseState<List<shelterItem>>> = flow {
        try {
            val response =
                drowyShelterInterface.getAllShelter()
            if (response.isSuccessful) {
                response.body()?.let {
                    val nearShelter = it.response.body.items.filter {
                        val lat = it.latitude.toDouble()
                        (lat < boundingBox.maxLatitude && lat > boundingBox.minLatitude)
                    }.filter {
                        val lon = it.longitude.toDouble()
                        (lon < boundingBox.maxLongitude && lon > boundingBox.minLongitude)
                    }
                    emit(ResponseState.Success(nearShelter))
                }
            } else {
                emit(ResponseState.Fail(response.code(), response.message()))
            }
        } catch (e: Exception) {
            emit(ResponseState.Error(e))
        }  finally {
            currentCoroutineContext().cancel()
        }as Unit
    }.flowOn(Dispatchers.IO).cancellable()

    private val parkingLotJobsList = mutableListOf<Flow<ResponseState<List<parkingLotItem>>>>()

    fun cancleParkingLotFlow() {
        parkingLotJobsList.map {  }
    }

    suspend fun getParkingLots(
        boundingBox: BoundingBox,
        parkingchargeInfo: String,
        numOfCoroutineRequired: Int,
        day: DAY,
        nowTime: String,
    ) = (1..numOfCoroutineRequired).map {
        getPartParkingLot(
            it,
            boundingBox,
            parkingchargeInfo,
            day,
            nowTime
        )
    }

    suspend fun getAllParkingLot(
        boundingBox: BoundingBox,
        parkingchargeInfo: String,
        numOfRows: Int = DEFAULT_NUM_OF_ROWS, day: DAY, nowTime: String,
    ): Flow<ResponseState<List<Flow<ResponseState<List<parkingLotItem>>>>>> =
        flow {
            try {
                val response =
                    parkingLotInterface.getAllParkingLot(
                        pageNo = 1,
                        numOfRows = 1,
                        parkingchrgeInfo = "무료"
                    )

                if (response.isSuccessful) {
                    response.body()?.let {
                        val totalCount = it.response.body.totalCount.toInt()
                        var numOfCoroutineRequired = totalCount / numOfRows
                        if (totalCount % numOfRows != 0) numOfCoroutineRequired++
                        emit(
                            ResponseState.Success(
                                getParkingLots(
                                    boundingBox = boundingBox,
                                    parkingchargeInfo = parkingchargeInfo,
                                    numOfCoroutineRequired = numOfCoroutineRequired,
                                    day = day,
                                    nowTime = nowTime
                                )
                            )
                        )
                    }
                } else {
                    emit(ResponseState.Fail(response.code(), response.message()))
                }
            } catch (e: Exception) {
                emit(ResponseState.Error(e))
            }  finally {
                currentCoroutineContext().cancel()
            }as Unit
        }.flowOn(Dispatchers.IO).cancellable()

    suspend fun getPartParkingLot(
        pageNo: Int, boundingBox: BoundingBox, parkingchargeInfo: String, day: DAY, nowTime: String,
    ): Flow<ResponseState<List<parkingLotItem>>> =
        flow {
            try {
                val response =
                    parkingLotInterface.getAllParkingLot(
                        pageNo = pageNo,
                        parkingchrgeInfo = parkingchargeInfo
                    )
                if (response.isSuccessful) {
                    response.body()?.let { parkingLot ->
                        val freeParkingLot = parkingLot.response.body.items.filter { item1 ->
                            val lat = item1.latitude.toDoubleOrNull()
                            if (lat != null) lat in boundingBox.minLatitude..boundingBox.maxLatitude
                            else false
                        }.filter { item2 ->
                            val lon = item2.longitude.toDoubleOrNull()
                            if (lon != null) lon in boundingBox.minLongitude..boundingBox.maxLongitude
                            else false
                        }.filter { item3 ->
                            isInTime(
                                day = day,
                                nowTime = nowTime,
                                item = item3
                            ) // 현재 시간에 운영하고 있는지 확인
                        }
                        emit(ResponseState.Success(freeParkingLot.toList()))
                    }
                } else {
                    emit(ResponseState.Fail(response.code(), response.message()))
                }
            } catch (e: Exception) {
                emit(ResponseState.Error(e))
            }  finally {
                currentCoroutineContext().cancel()
            }as Unit
        }.flowOn(Dispatchers.Default).cancellable()

}