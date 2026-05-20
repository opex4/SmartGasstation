package com.example.smartgasstation.data.api

data class BestStationDTO(
    val name: String,
    val pricePerLiter: Double,
    val distance: Double,
    val tripCost: Double,
    val totalCost: Double
)