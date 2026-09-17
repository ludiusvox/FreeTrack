package com.freeapp.freetrack.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity schema for daily weight tracking entries.
 */
@Entity(tableName = "weight_entries")
data class WeightEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val date: String,             // ISO format "YYYY-MM-DD"
    val weightKg: Float,          // Weight stored uniformly in kilograms
    val bodyFatPct: Float? = null,// Optional body fat %
    val notes: String? = null,    // Optional user log notes
    val createdAt: Long = System.currentTimeMillis()
)