package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_cities")
data class SavedCityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cityName: String,
    val country: String,
    val timeZoneId: String,
    val isHomeCity: Boolean = false,
    val displayOrder: Int = 0
)
