package com.paradise.database.mapper

import com.core.model.analyzeResultItem
import com.core.model.drowsyResultItem
import com.core.model.musicItem
import com.core.model.winkResultItem
import com.paradise.database.room.model.AnalyzeResult
import com.paradise.database.room.model.DrowsyCount
import com.paradise.database.room.model.Music
import com.paradise.database.room.model.WinkCount

fun AnalyzeResult.toAnalyzeResultItem(): analyzeResultItem {
    return analyzeResultItem(time = this.time, id = this.id)
}
fun DrowsyCount.toDrowsyItem(): drowsyResultItem {
    return drowsyResultItem(
        recordId = this.recordId,
        value = this.value,
        id = this.id,
    )
}

fun WinkCount.toWinkItem(): winkResultItem {
    return winkResultItem(
        recordId = this.recordId,
        value = this.value,
        id = this.id,
    )
}

fun Music.toMusicItem(): musicItem {
    return musicItem(
        title = this.title!!, newPath = this.newPath, startTime = this.startTime, // 초기엔 0초부터 시작
        durationTime = this.durationTime, // 기본 3초 -> 후에 변경 가능하게 변경할 예정
        originalPath = this.originalPath!!, id = this.id
    )
}