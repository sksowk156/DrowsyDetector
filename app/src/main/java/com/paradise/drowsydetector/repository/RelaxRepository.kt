package com.paradise.drowsydetector.repository

import com.paradise.drowsydetector.data.remote.parkinglot.ParkingLotInterface
import com.paradise.drowsydetector.data.remote.rest.RestInterface
import com.paradise.drowsydetector.data.remote.shelter.DrowyShelterInterface
import com.paradise.drowsydetector.utils.BoundingBox
import com.paradise.drowsydetector.utils.DAY
import com.paradise.drowsydetector.utils.DEFAULT_NUM_OF_ROWS
import com.paradise.drowsydetector.utils.ResponseState
import com.paradise.drowsydetector.utils.defaultDispatcher
import com.paradise.drowsydetector.utils.ioDispatcher
import com.paradise.drowsydetector.utils.isInTime
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
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
        ) = instance ?: synchronized(this) {
            instance ?: RelaxRepository(
                drowyShelterInterface, parkingLotInterface, restInterface
            ).also { instance = it }
        }
    }

    suspend fun getAllRest(
        boundingBox: BoundingBox,
    ): Flow<ResponseState<List<restItem>>> = flow {
        try {
            val response = restInterface.getAllRest()
            if (response.isSuccessful) {
                response.body()?.let {
                    val nearRest = mutableListOf<restItem>()
                    it.response.body.items.asFlow().flowOn(defaultDispatcher)
                        .filter { item ->
                            val lat = item.latitude.toDoubleOrNull()
                            val lon = item.longitude.toDoubleOrNull()
                            val inLatRange =
                                lat != null && lat in boundingBox.minLatitude..boundingBox.maxLatitude
                            val inLonRange =
                                lon != null && lon in boundingBox.minLongitude..boundingBox.maxLongitude
                            inLatRange && inLonRange
                        }.collect { item ->
                            nearRest.add(item)
                        }
                    emit(ResponseState.Success(nearRest))
                }
            } else {
                emit(ResponseState.Fail(response.code(), response.message()))
            }
        } catch (e: Exception) {
            emit(ResponseState.Error(e))
        } finally {
            currentCoroutineContext().cancel()
        } as Unit
    }.flowOn(ioDispatcher).cancellable()

    suspend fun getAllShelter(
        boundingBox: BoundingBox,
    ): Flow<ResponseState<List<shelterItem>>> = flow {
        try {
            val response = drowyShelterInterface.getAllShelter()
            if (response.isSuccessful) {
                response.body()?.let {
                    val nearShelter = mutableListOf<shelterItem>()
                    it.response.body.items.asFlow().flowOn(defaultDispatcher)
                        .filter { item ->
                            val lat = item.latitude.toDoubleOrNull()
                            val lon = item.longitude.toDoubleOrNull()
                            val inLatRange =
                                lat != null && lat in boundingBox.minLatitude..boundingBox.maxLatitude
                            val inLonRange =
                                lon != null && lon in boundingBox.minLongitude..boundingBox.maxLongitude
                            inLatRange && inLonRange
                        }.collect { item ->
                            nearShelter.add(item)
                        }
                    emit(ResponseState.Success(nearShelter))
                }
            } else {
                emit(ResponseState.Fail(response.code(), response.message()))
            }
        } catch (e: Exception) {
            emit(ResponseState.Error(e))
        } as Unit
    }.flowOn(ioDispatcher).cancellable()

    suspend fun getParkingLots1(
        boundingBox: BoundingBox,
        parkingchargeInfo: String,
        numOfCoroutineRequired: Int,
        day: DAY,
        nowTime: String,
    ) = (1..numOfCoroutineRequired).map {
        getParkingLot1(it, boundingBox, parkingchargeInfo, day, nowTime)
    }

    suspend fun getAllParkingLot(
        boundingBox: BoundingBox,
        parkingchargeInfo: String,
        numOfRows: Int = DEFAULT_NUM_OF_ROWS, day: DAY, nowTime: String,
    ) = flow {
        try {
            val response = parkingLotInterface.getAllParkingLot(
                pageNo = 1, numOfRows = 1, parkingchrgeInfo = "무료"
            )

            if (response.isSuccessful) {
                response.body()?.let {
                    val totalCount = it.response.body.totalCount.toInt()
                    var numOfCoroutineRequired = totalCount / numOfRows
                    if (totalCount % numOfRows != 0) numOfCoroutineRequired++
                    emit(
                        ResponseState.Success(
                            getParkingLots1(
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
        } as Unit
    }.flowOn(defaultDispatcher).cancellable()

    /**
     * Get parking lot1
     *
     * ioDispatcher 내부에서 withContext로 defaultDisptcher를 잠시 생성해 연산한다.
     * @param pageNo
     * @param boundingBox
     * @param parkingchargeInfo
     * @param day
     * @param nowTime
     */
    suspend fun getParkingLot1(
        pageNo: Int, boundingBox: BoundingBox, parkingchargeInfo: String, day: DAY, nowTime: String,
    ) = flow {
        try {
            val response = parkingLotInterface.getAllParkingLot(
                pageNo = pageNo, parkingchrgeInfo = parkingchargeInfo
            )
            if (response.isSuccessful) {
                response.body()?.let { parkingLot ->
                    val freeParkingLot = mutableListOf<parkingLotItem>()

                    parkingLot.response.body.items.asFlow().flowOn(defaultDispatcher)
                        .filter { item ->
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

                    emit(ResponseState.Success(freeParkingLot))
                }
            } else {
                emit(ResponseState.Fail(response.code(), response.message()))
            }
        } catch (e: Exception) {
            emit(ResponseState.Error(e))
        } as Unit
    }.flowOn(ioDispatcher).cancellable()

    /**
     * Get one parking lot
     *
     * 데이터를 하나를 먼저 요청하는 부분을 위 코드와 다르게 따로 빼서 실행함
     * @return
     */
//    suspend fun getOneParkingLot(): Flow<ResponseState<ParkingLot>> = flow {
//        try {
//            val response = parkingLotInterface.getAllParkingLot(
//                pageNo = 1,
//                numOfRows = 1,
//                parkingchrgeInfo = "무료"
//            )
//            if (response.isSuccessful) {
//                response.body()?.let {
//                    emit(ResponseState.Success(it))
//                }
//            } else {
//                emit(ResponseState.Fail(response.code(), response.message()))
//            }
//        } catch (e: Exception) {
//            emit(ResponseState.Error(e))
//        } as Unit
//    }.flowOn(defaultDispatcher).cancellable()
//
//    suspend fun getAllParkingLot(
//        boundingBox: BoundingBox,
//        parkingchargeInfo: String,
//        numOfCoroutineRequired: Int,
//        day: DAY,
//        nowTime: String,
//    ) = combine((1..numOfCoroutineRequired).map {
//        getParkingLot1(it, boundingBox, parkingchargeInfo, day, nowTime)
//    }) { responses ->
//        try {
//            val combinedList =
//                responses.filterIsInstance<ResponseState.Success<List<parkingLotItem>>>()
//                    .flatMap { it.data }
//            ResponseState.Success(combinedList)
//        } catch (error: Throwable) {
//            ResponseState.Error(error)
//        }
//    }.cancellable()

    /**
     * Get parking lot2
     *
     * flowOn을 사용해 데이터를 filter할 때 context를 스위칭한다.(context 스위칭에 오버헤드가 더 크므로 getParkingLot1를 사용)
     * @param pageNo
     * @param boundingBox
     * @param parkingchargeInfo
     * @param day
     * @param nowTime
     */
//    suspend fun getParkingLot2(
//        pageNo: Int, boundingBox: BoundingBox, parkingchargeInfo: String, day: DAY, nowTime: String,
//    ) = flow {
//        emit(
//            parkingLotInterface.getAllParkingLot(
//                pageNo = pageNo, parkingchrgeInfo = parkingchargeInfo
//            )
//        )
//    }.flowOn(ioDispatcher).cancellable()
//        .flowOn(defaultDispatcher)
//        .catch { ResponseState.Error<List<parkingLotItem>>(it) }
//        .mapNotNull { response ->
//            if (response.isSuccessful) {
//                response.body()?.let { parkingLot ->
//                    parkingLot.response.body.items.asFlow().filter { item1 ->
//                        val lat = item1.latitude.toDoubleOrNull()
//                        lat != null && lat in boundingBox.minLatitude..boundingBox.maxLatitude
//                    }.filter { item2 ->
//                        val lon = item2.longitude.toDoubleOrNull()
//                        lon != null && lon in boundingBox.minLongitude..boundingBox.maxLongitude
//                    }.filter { item3 ->
//                        isInTime(day = day, nowTime = nowTime, item = item3)
//                    }.toList().let { freeParkingLot ->
//                            ResponseState.Success<List<parkingLotItem>>(freeParkingLot)
//                        }
////                    val freeParkingLot = parkingLot.response.body.items.filter { item1 ->
////                        val lat = item1.latitude.toDoubleOrNull()
////                        if (lat != null) lat in boundingBox.minLatitude..boundingBox.maxLatitude
////                        else false
////                    }.filter { item2 ->
////                        val lon = item2.longitude.toDoubleOrNull()
////                        if (lon != null) lon in boundingBox.minLongitude..boundingBox.maxLongitude
////                        else false
////                    }.filter { item3 ->
////                        isInTime(
////                            day = day, nowTime = nowTime, item = item3
////                        ) // 현재 시간에 운영하고 있는지 확인
////                    }
////                    (ResponseState.Success<List<parkingLotItem>>(freeParkingLot.toList()))
//                }
//            } else {
//                (ResponseState.Fail<List<parkingLotItem>>(response.code(), response.message()))
//            }
//        }.cancellable()

    /**
     * Get parking lot3
     *
     * 데이터 filter 또한 ioDispatcher에서 수행한다.
     * @param pageNo
     * @param boundingBox
     * @param parkingchargeInfo
     * @param day
     * @param nowTime
     * @return
     */
//    suspend fun getParkingLot3(
//        pageNo: Int, boundingBox: BoundingBox, parkingchargeInfo: String, day: DAY, nowTime: String,
//    ): Flow<ResponseState<List<parkingLotItem>>> = flow {
//        try {
//            val response = parkingLotInterface.getAllParkingLot(
//                pageNo = pageNo, parkingchrgeInfo = parkingchargeInfo
//            )
//            if (response.isSuccessful) {
//                response.body()?.let { parkingLot ->
//                    val freeParkingLot = parkingLot.response.body.items.filter { item1 ->
//                        val lat = item1.latitude.toDoubleOrNull()
//                        if (lat != null) lat in boundingBox.minLatitude..boundingBox.maxLatitude
//                        else false
//                    }.filter { item2 ->
//                        val lon = item2.longitude.toDoubleOrNull()
//                        if (lon != null) lon in boundingBox.minLongitude..boundingBox.maxLongitude
//                        else false
//                    }.filter { item3 ->
//                        isInTime(
//                            day = day, nowTime = nowTime, item = item3
//                        ) // 현재 시간에 운영하고 있는지 확인
//                    }
//                    emit(ResponseState.Success(freeParkingLot.toList()))
//                }
//            } else {
//                emit(ResponseState.Fail(response.code(), response.message()))
//            }
//        } catch (e: Exception) {
//            emit(ResponseState.Error(e))
//        } as Unit
//    }.flowOn(ioDispatcher).cancellable()


}