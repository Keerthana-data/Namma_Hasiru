package com.nammahasiru.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trees")
data class TreeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val speciesName: String,
    val latitude: Double,
    val longitude: Double,
    val plantedAtMillis: Long,
    val photoPath: String?,
    val villageTag: String,
    val addressLine: String = "",
    val status: String,
    val growthPhotoPath: String?,
    val lastUpdatedMillis: Long,
)
