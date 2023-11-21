package com.paradise.drowsydetector.data.local.room.record

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [ForeignKey(
        entity = DrowsyRecord::class,
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