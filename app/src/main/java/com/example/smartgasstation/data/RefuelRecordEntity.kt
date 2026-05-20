package com.example.smartgasstation.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "refuel_records")
data class RefuelRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val fuelAmount: Double,
    val odometer: Double,
    val timestamp: Long = System.currentTimeMillis()
)