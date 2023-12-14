package com.paradise.data.mapper

import com.core.model.analyzeResultItem
import com.core.model.drowsyResultItem
import com.core.model.musicItem
import com.core.model.winkResultItem
import com.paradise.database.room.model.AnalyzeResult
import com.paradise.database.room.model.DrowsyCount
import com.paradise.database.room.model.Music
import com.paradise.database.room.model.WinkCount

fun analyzeResultItem.toDataAnalyzeResult(): AnalyzeResult {
    return AnalyzeResult(time = this.time, id = this.id)
}


fun drowsyResultItem.toDataDrowsyCount(): DrowsyCount {
    return DrowsyCount(
        recordId = this.recordId,
        value = this.value,
        id = this.id,
    )
}

fun winkResultItem.toDataWinkCount(): WinkCount {
    return WinkCount(
        recordId = this.recordId,
        value = this.value,
        id = this.id,
    )
}

fun musicItem.toDataMusic(): Music {
    return Music(
        title = this.title!!, newPath = this.newPath, startTime = this.startTime, // 초기엔 0초부터 시작
        durationTime = this.durationTime, // 기본 3초 -> 후에 변경 가능하게 변경할 예정
        originalPath = this.originalPath!!, id = this.id
    )
}

//fun ParkingLotModel.toDomainParkingLotItemCount(): Int {
//    return this.response.body.totalCount.toInt()
//}
//
//fun ParkingLotModel.toDomainParkingLotItemList(): List<parkingLotItem> {
//    return this.response.body.items.map {
//        parkingLotItem(
//            it.holidayCloseOpenHhmm,
//            it.holidayOperOpenHhmm,
//            it.latitude,
//            it.lnmadr,
//            it.longitude,
//            it.parkingchrgeInfo,
//            it.prkplceNm,
//            it.satOperCloseHhmm,
//            it.satOperOperOpenHhmm,
//            it.weekdayOperColseHhmm,
//            it.weekdayOperOpenHhmm,
//        )
//    }
//}
//
//fun ShelterModel.toDomainShelterItemList(): List<shelterItem> {
//    return this.response.body.items.map {
//        shelterItem(
//            it.latitude, it.longitude, it.lnmadr, it.roadRouteDrc, it.signguNm
//        )
//    }
//}
//
//fun RestModel.toDomainRestItemList(): List<restItem> {
//    return this.response.body.items.map {
//        restItem(
//            it.latitude, it.longitude, it.roadRouteNm, it.roadRouteDrc
//        )
//    }
//}