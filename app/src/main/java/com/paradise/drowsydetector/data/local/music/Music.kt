package com.paradise.drowsydetector.data.local.music

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "music_table")
data class Music(
    @ColumnInfo(name = "title") // 음원 제목
    val title: String? = "",
    @ColumnInfo(name = "newPath") // 음원 복사본 위치
    val newPath: String?= "",
    @ColumnInfo(name = "endTime") // 음원 시작 시간
    var startTime: Long = 0L, // 초기엔 0초부터 시작
    @ColumnInfo(name = "durationTime") // 음원 총 시간
    var durationTime: Long = 5000L, // 기본 3초 -> 후에 변경 가능하게 변경할 예정
    @ColumnInfo(name = "originalPath") // 음원 원본 위치
    val originalPath: String? = "",
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,    // 음원 자체의 ID
)