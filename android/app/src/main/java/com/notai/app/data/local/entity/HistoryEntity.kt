package com.notai.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val originalUri: String,
    val resultUri: String,
    val platform: String,
    val watermarkRegionX: Float,
    val watermarkRegionY: Float,
    val watermarkRegionW: Float,
    val watermarkRegionH: Float,
    val status: String,
    val processedAt: Long,
    val processingTimeMs: Long,
    val isFavorited: Boolean = false
)
