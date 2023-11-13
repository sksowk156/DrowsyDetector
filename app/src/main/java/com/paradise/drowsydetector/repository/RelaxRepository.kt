package com.paradise.drowsydetector.repository

import com.paradise.drowsydetector.data.remote.parkinglot.ParkingLotInterface
import com.paradise.drowsydetector.data.remote.rest.RestInterface
import com.paradise.drowsydetector.data.remote.shelter.DrowyShelterInterface
import com.paradise.drowsydetector.utils.BoundingBox
import com.paradise.drowsydetector.utils.DEFAULTNUMOFROWS
import com.paradise.drowsydetector.utils.ResponseState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.IOException
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
                try {
                    emit(ResponseState.Fail(response.code(), response.message()))
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            emit(ResponseState.Error(e))
        } as Unit
    }.flowOn(Dispatchers.IO)

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
                try {
                    emit(ResponseState.Fail(response.code(), response.message()))
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            emit(ResponseState.Error(e))
        } as Unit
    }.flowOn(Dispatchers.IO)

    suspend fun getAllParkingLot(
        boundingBox: BoundingBox, parkingchargeInfo: String, numOfCoroutineRequired: Int,
    ) = (1..numOfCoroutineRequired).map { getPartParkingLot(it, boundingBox, parkingchargeInfo) }

    suspend fun getAllParkingLot2(
        boundingBox: BoundingBox,
        parkingchargeInfo: String,
        numOfRows: Int = DEFAULTNUMOFROWS,
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
                                getAllParkingLot(
                                    boundingBox = boundingBox,
                                    parkingchargeInfo = parkingchargeInfo,
                                    numOfCoroutineRequired = numOfCoroutineRequired
                                )
                            )
                        )
                    }
                } else {
                    try {
                        emit(ResponseState.Fail(response.code(), response.message()))
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                emit(ResponseState.Error(e))
            } as Unit
        }.flowOn(Dispatchers.IO)

    suspend fun getPartParkingLot(
        pageNo: Int, boundingBox: BoundingBox, parkingchargeInfo: String,
    ): Flow<ResponseState<List<parkingLotItem>>> =
        flow {
            try {
                val response =
                    parkingLotInterface.getAllParkingLot(
                        pageNo = pageNo,
                        parkingchrgeInfo = parkingchargeInfo
                    )
                if (response.isSuccessful) {
                    response.body()?.let {
                        val freeParkingLot = it.response.body.items.filter {
                            val lat = it.latitude.toDouble()
                            (lat < boundingBox.maxLatitude && lat > boundingBox.minLatitude)
                        }.filter {
                            val lon = it.longitude.toDouble()
                            (lon < boundingBox.maxLongitude && lon > boundingBox.minLongitude)
                        }
                        emit(ResponseState.Success(freeParkingLot))
                    }
                } else {
                    try {
                        emit(ResponseState.Fail(response.code(), response.message()))
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                emit(ResponseState.Error(e))
            } as Unit
        }.flowOn(Dispatchers.IO)

//    suspend fun getAllParkingLot(): Flow<ResponseState<ParkingLot>> =
//        flow {
//            try {
//                val response =
//                    parkingLotInterface.getAllParkingLot()
//                if (response.isSuccessful) {
//                    response.body()?.let {
//                        emit(ResponseState.Success(it))
//                    }
//                } else {
//                    try {
//                        emit(ResponseState.Fail(response.code(), response.message()))
//                    } catch (e: IOException) {
//                        e.printStackTrace()
//                    }
//                }
//            } catch (e: Exception) {
//                emit(ResponseState.Error(e))
//            } as Unit
//        }.flowOn(Dispatchers.IO)

}