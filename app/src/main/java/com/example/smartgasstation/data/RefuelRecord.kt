package com.example.smartgasstation.data

data class RefuelRecord(
    val fuelAmount: Double, // количество заправленного топлива в литрах
    val odometer: Double,   // пробег автомобиля в км на момент заправки
    val timestamp: Long = System.currentTimeMillis()
)