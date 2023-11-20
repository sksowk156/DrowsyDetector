package com.paradise.drowsydetector.data.local.room.record

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [ForeignKey(
        entity = DrowsyRecord::class,
        parentColumns = ["id"],
        childColumns = ["recordId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class DrowsyCount(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val recordId: Int,
    val value: Int,
)