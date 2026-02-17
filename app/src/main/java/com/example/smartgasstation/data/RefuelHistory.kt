package com.example.smartgasstation.data

object RefuelHistory {

    private val refuelRecords = mutableListOf<RefuelRecord>()
    private var nextId = 1

    fun addRefuelRecord(fuelAmount: Double, odometer: Double) {
        val record = RefuelRecord(id = nextId++, fuelAmount = fuelAmount, odometer = odometer)
        refuelRecords.add(record)
    }

    fun getHistory(): List<RefuelRecord> {
        return refuelRecords.toList()
    }

    fun getLastOdometer(): Double? {
        return if (refuelRecords.isNotEmpty()) {
            refuelRecords.maxByOrNull { it.odometer }?.odometer
        } else {
            null
        }
    }

    fun calculateAverageConsumption(): Double {
        // Для расчета расхода нужно минимум 2 записи о заправках
        if (refuelRecords.size < 2) return 0.0

        // Сортируем записи по пробегу (по возрастанию)
        val sortedRecords = refuelRecords.sortedBy { it.odometer }

        // Проверяем, что все пробеги идут по возрастанию
        for (i in 1 until sortedRecords.size) {
            if (sortedRecords[i].odometer <= sortedRecords[i-1].odometer) {
                return 0.0 // Нарушена последовательность пробегов
            }
        }

        // Суммируем все заправки топлива, кроме последней
        val totalFuel = sortedRecords.dropLast(1).sumOf { it.fuelAmount }

        // Общий пробег = последний пробег - первый пробег
        val totalDistance = sortedRecords.last().odometer - sortedRecords.first().odometer

        if (totalDistance <= 0) return 0.0

        // Расход в л/100км
        return (totalFuel / totalDistance) * 100
    }

    fun clearHistory() {
        refuelRecords.clear()
        nextId = 1
    }
}