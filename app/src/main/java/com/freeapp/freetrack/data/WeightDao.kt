package com.freeapp.freetrack.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: WeightEntryEntity): Long

    @Update
    suspend fun update(entry: WeightEntryEntity)

    @Delete
    suspend fun delete(entry: WeightEntryEntity)

    @Query("SELECT * FROM weight_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<WeightEntryEntity>>

    @Query("SELECT * FROM weight_entries WHERE date = :dateString LIMIT 1")
    suspend fun getEntryByDate(dateString: String): WeightEntryEntity?

    @Query("SELECT AVG(weightKg) FROM (SELECT weightKg FROM weight_entries ORDER BY date DESC LIMIT 7)")
    fun getSevenDayAverage(): Flow<Float?>

    @Query("DELETE FROM weight_entries")
    suspend fun clearAll()
}