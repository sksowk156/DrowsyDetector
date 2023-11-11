package com.paradise.drowsydetector.repository

import com.paradise.drowsydetector.data.remote.parkinglot.ParkingLot
import com.paradise.drowsydetector.data.remote.parkinglot.ParkingLotInterface
import com.paradise.drowsydetector.data.remote.shelter.DrowsyShelter
import com.paradise.drowsydetector.data.remote.shelter.DrowyShelterInterface
import com.paradise.drowsydetector.utils.ResponseState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.IOException

class RelaxRepository(
    private val drowyShelterInterface: DrowyShelterInterface,
    private val parkingLotInterface: ParkingLotInterface,
) {

    //Singleton으로 객체 생성
    companion object {
        @Volatile
        private var instance: RelaxRepository? = null
        fun getInstance(
            drowyShelterInterface: DrowyShelterInterface,
            parkingLotInterface: ParkingLotInterface,
        ) =
            instance ?: synchronized(this) {
                instance ?: RelaxRepository(
                    drowyShelterInterface,
                    parkingLotInterface
                ).also { instance = it }
            }
    }



    suspend fun getAllShelter(
        ctprvnNm: String,
        signguNm: String,
    ): Flow<ResponseState<DrowsyShelter>> = flow {
        try {
            val response =
                drowyShelterInterface.getAllShelter(ctprvnNm = ctprvnNm, signguNm = signguNm)
            if (response.isSuccessful) {
                response.body()?.let {
                    emit(ResponseState.Success(it))
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

    suspend fun getAllParkingLot(): Flow<ResponseState<ParkingLot>> =
        flow {
            try {
                val response =
                    parkingLotInterface.getAllParkingLot()
                if (response.isSuccessful) {
                    response.body()?.let {
                        emit(ResponseState.Success(it))
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

}