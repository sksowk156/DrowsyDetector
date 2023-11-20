package com.paradise.drowsydetector.data.local.room.record

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [ForeignKey(
        entity = DrowsyRecord::class,
        parentColumns = ["id"], // Record의 id
        childColumns = ["recordId"], // 그것을 참조하는 recordId
        onDelete = ForeignKey.CASCADE // 부모 테이블의 행이 삭제되거나 해당 행의 기본 키가 변경되면, 이와 연관된 자식 테이블의 행도 자동으로 삭제되거나 변경
    )]
)
data class WinkCount(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val recordId: Int,
    val value: Int,
)