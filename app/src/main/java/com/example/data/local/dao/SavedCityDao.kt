package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.SavedCityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedCityDao {
    @Query("SELECT * FROM saved_cities ORDER BY displayOrder ASC, cityName ASC")
    fun getAllSavedCities(): Flow<List<SavedCityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCity(city: SavedCityEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCities(cities: List<SavedCityEntity>)

    @Delete
    suspend fun deleteCity(city: SavedCityEntity)

    @Query("DELETE FROM saved_cities WHERE id = :id")
    suspend fun deleteCityById(id: Long)
}
