package com.paradise.drowsydetector.data.local.room.record

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "drowsy_table", indices = [Index(value = ["id"], unique = true)])
data class AnalyzeResult(
    @ColumnInfo(name = "time") // 총 감지 시간
    val time: String? = "",
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,    // 음원 자체의 ID
)

@Entity(
    foreignKeys = [ForeignKey(
        entity = AnalyzeResult::class,
        parentColumns = ["id"],
        childColumns = ["recordId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("recordId")]
)
data class DrowsyCount(
    @ColumnInfo(name = "recordId")
    val recordId: Int,
    val value: Int,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
)

@Entity(
    foreignKeys = [ForeignKey(
        entity = AnalyzeResult::class,
        parentColumns = ["id"], // Record의 id
        childColumns = ["recordId"], // 그것을 참조하는 recordId
        onDelete = ForeignKey.CASCADE // 부모 테이블의 행이 삭제되거나 해당 행의 기본 키가 변경되면, 이와 연관된 자식 테이블의 행도 자동으로 삭제되거나 변경
    )],
    indices = [Index("recordId")]
)
data class WinkCount(
    @ColumnInfo(name = "recordId")
    val recordId: Int,
    val value: Int,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
)