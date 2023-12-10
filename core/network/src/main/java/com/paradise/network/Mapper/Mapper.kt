package com.paradise.network.Mapper

import android.util.Log
import com.core.model.parkingLotItem
import com.core.model.restItem
import com.core.model.shelterItem
import com.paradise.network.retrofit.parkinglot.model.ParkingLotModel
import com.paradise.network.retrofit.parkinglot.model.Item as parkingLot
import com.paradise.network.retrofit.rest.model.RestModel
import com.paradise.network.retrofit.rest.model.Item as rest
import com.paradise.network.retrofit.shelter.model.ShelterModel
import com.paradise.network.retrofit.shelter.model.Item as shelter

fun ParkingLotModel.toParkingLotItemCount(): Int {
    return this.response.body.totalCount.toInt()
}

fun ParkingLotModel.toParkingLotItemList(): List<parkingLotItem> {
    return this.response.body.items.map {
        Log.d("whatisthis", "parking : " + it.toString())
        parkingLotItem(
            it.holidayCloseOpenHhmm,
            it.holidayOperOpenHhmm,
            it.latitude,
            it.lnmadr,
            it.longitude,
            it.parkingchrgeInfo,
            it.prkplceNm,
            it.satOperCloseHhmm,
            it.satOperOperOpenHhmm,
            it.weekdayOperColseHhmm,
            it.weekdayOperOpenHhmm,
        )
    }
}

fun parkingLot.toParkingLotItem(): parkingLotItem {
    return parkingLotItem(
        holidayCloseOpenHhmm = this.holidayCloseOpenHhmm,
        holidayOperOpenHhmm = this.holidayOperOpenHhmm,
        latitude = this.latitude,
        lnmadr = this.lnmadr,
        longitude = this.longitude,
        parkingchrgeInfo = this.parkingchrgeInfo,
        prkplceNm = this.prkplceNm,
        satOperCloseHhmm = this.satOperCloseHhmm,
        satOperOperOpenHhmm = this.satOperOperOpenHhmm,
        weekdayOperColseHhmm = this.weekdayOperColseHhmm,
        weekdayOperOpenHhmm = this.weekdayOperOpenHhmm,
    )
}

fun ShelterModel.toShelterItemList(): List<shelterItem> {
    return this.response.body.items.map {
        Log.d("whatisthis", "shelter : " + it.toString())

        shelterItem(
            it.latitude, it.longitude, it.lnmadr, it.roadRouteDrc, it.signguNm
        )
    }
}

fun shelter.toShelterItem(): shelterItem {
    return shelterItem(
        latitude = this.latitude,
        longitude = this.longitude,
        lnmadr = this.lnmadr,
        roadRouteDrc = this.roadRouteDrc,
        signguNm = this.signguNm
    )
}

fun RestModel.toRestItemList(): List<com.core.model.restItem> {
    return this.response.body.items.map {
        Log.d("whatisthis", "rest : " + it.toString())
        com.core.model.restItem(
            it.latitude, it.longitude, it.roadRouteNm, it.roadRouteDrc
        )
    }
}

fun rest.toRestItem(): restItem {
    return restItem(
        latitude = this.latitude,
        longitude = this.longitude,
        roadRouteNm = this.roadRouteNm,
        roadRouteDrc = this.roadRouteDrc
    )
}
