package com.example.smartgasstation.domain.entity

data class BestStation(
    val name: String,
    val pricePerLiter: Double,
    val distance: Double,
    val tripCost: Double,
    val totalCost: Double
)
