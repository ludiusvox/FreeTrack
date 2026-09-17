package com.freeapp.freetrack.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GpsPointDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(point: GpsPointEntity): Long

    @Query("SELECT * FROM gps_points ORDER BY timestamp ASC")
    fun getAllTrackPoints(): Flow<List<GpsPointEntity>>

    @Query("SELECT * FROM gps_points ORDER BY timestamp DESC LIMIT 1")
    fun getLatestPoint(): Flow<GpsPointEntity?>

    @Query("SELECT COUNT(*) FROM gps_points")
    suspend fun getPointCount(): Int

    @Query("SELECT MAX(altitudeBaro) FROM gps_points")
    suspend fun getMaxAltitude(): Double?

    @Query("SELECT MIN(altitudeBaro) FROM gps_points")
    suspend fun getMinAltitude(): Double?

    @Query("DELETE FROM gps_points")
    suspend fun clearAll()
}