package com.example.smartgasstation.viewModels

import androidx.lifecycle.ViewModel
import com.example.smartgasstation.data.BestStationResult
import com.example.smartgasstation.data.GasStation

class AddRefuelVM :  ViewModel(){
    // Список заправок
    private val gasStations = listOf(
        GasStation(
            id = 1,
            name = "Лукойл №1",
            fuelPrice = 50.5,
            distance = 2.5,
            availableFuels = listOf("АИ-95", "АИ-92", "ДТ")
        ),
        GasStation(
            id = 2,
            name = "Роснефть №2",
            fuelPrice = 49.8,
            distance = 5.0,
            availableFuels = listOf("АИ-95", "АИ-92", "ДТ", "Газ")
        ),
        GasStation(
            id = 3,
            name = "Газпромнефть №3",
            fuelPrice = 51.2,
            distance = 1.0,
            availableFuels = listOf("АИ-95", "АИ-98", "ДТ")
        )
    )

    // Поиск лучшей заправки
    fun findBestGasStation(fuelType: String, fuelAmount: Double, fuelConsumption: Double): BestStationResult? {
        // Фильтруем заправки по типу топлива
        val suitableStations = gasStations.filter { station ->
            station.availableFuels.any { it.contains(fuelType, ignoreCase = true) }
        }

        if (suitableStations.isEmpty()) return null

        // Рассчитываем стоимость для каждой подходящей заправки
        val stationsWithCost = suitableStations.map { station ->
            val fuelCost = station.fuelPrice * fuelAmount
            val tripFuel = (station.distance * fuelConsumption) / 100
            val tripCost = tripFuel * station.fuelPrice
            val totalCost = fuelCost + tripCost

            BestStationResult(station, tripCost, totalCost)
        }

        // Возвращаем заправку с минимальной общей стоимостью
        return stationsWithCost.minByOrNull { it.totalCost }
    }

    fun calculateBestGasStation(fuelType: String, fuelAmount: Double, consumption: Double): String? {
        // Поиск лучшей заправки
        val bestStation = findBestGasStation(fuelType, fuelAmount, consumption)

        if (bestStation != null) {
            val result = """
                    Лучшая заправка: ${bestStation.station.name}
                    Цена за литр: ${bestStation.station.fuelPrice} руб.
                    Расстояние: ${bestStation.station.distance} км
                    Стоимость поездки: ${String.format("%.2f", bestStation.tripCost)} руб.
                    Общая стоимость: ${String.format("%.2f", bestStation.totalCost)} руб.
                """.trimIndent()
            return result
        } else {
            return null
        }
    }
}