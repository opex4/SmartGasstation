package com.example.smartgasstation.data

data class GasStation(
    val id: Int,
    val name: String,
    val fuelPrice: Double, // цена за литр
    val distance: Double, // расстояние в км
    val availableFuels: List<String> // типы доступного топлива
)