package com.example.smartgasstation.data

// Класс для результатов поиска
data class BestStationResult(
    val station: GasStation,
    val tripCost: Double,
    val totalCost: Double
)