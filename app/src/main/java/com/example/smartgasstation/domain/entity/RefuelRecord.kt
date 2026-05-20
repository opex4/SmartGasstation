package com.example.smartgasstation.domain.entity

data class RefuelRecord(
    val id: Int = 0,
    val fuelAmount: Double,
    val odometer: Double,
    val timestamp: Long = System.currentTimeMillis()
)
