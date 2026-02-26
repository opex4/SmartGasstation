package com.example.smartgasstation.data

object RefuelHistory {

    private val refuelRecords = mutableListOf<RefuelRecord>()

    // Удаление записи
    fun deleteRefuelRecords(position: Int){
        // Проверка корректности индекса
        if (position < 0 || position >= refuelRecords.size) {
            throw Exception("Некорректный индекс записи")
        }

        refuelRecords.removeAt(position)
    }

    // Обновление записи
    fun updateRefuelRecord(position: Int, fuelAmount: Double, odometer: Double) {
        // Проверка валидности входных данных
        if (fuelAmount <= 0) {
            throw Exception("Количество топлива должно быть больше 0")
        }

        if (odometer < 0) {
            throw Exception("Пробег не может быть отрицательным")
        }

        // Проверка, что список не пуст
        if (refuelRecords.isEmpty()) {
            throw Exception("Список заправок пуст")
        }

        // Проверка корректности индекса
        if (position < 0 || position >= refuelRecords.size) {
            throw Exception("Некорректный индекс записи")
        }

        // Проверка соблюдения последовательности пробегов
        when (position) {
            0 -> { // Первая запись
                if (refuelRecords.size > 1) {
                    val nextRecord = refuelRecords[1]
                    if (odometer >= nextRecord.odometer) {
                        throw Exception("Пробег первой записи должен быть меньше пробега следующей записи")
                    }
                }
            }
            refuelRecords.lastIndex -> { // Последняя запись
                if (refuelRecords.size > 1) {
                    val previousRecord = refuelRecords[refuelRecords.lastIndex - 1]
                    if (odometer <= previousRecord.odometer) {
                        throw Exception("Пробег последней записи должен быть больше пробега предыдущей записи")
                    }
                }
            }
            else -> { // Запись в середине списка
                val previousRecord = refuelRecords[position - 1]
                val nextRecord = refuelRecords[position + 1]

                if (odometer <= previousRecord.odometer) {
                    throw Exception("Пробег текущей записи должен быть больше пробега предыдущей записи")
                }

                if (odometer >= nextRecord.odometer) {
                    throw Exception("Пробег текущей записи должен быть меньше пробега следующей записи")
                }
            }
        }

        // Обновляем запись
        val updatedRecord = RefuelRecord(
            fuelAmount = fuelAmount,
            odometer = odometer,
            timestamp = refuelRecords[position].timestamp
        )
        refuelRecords[position] = updatedRecord
    }

    fun addRefuelRecord(fuelAmount: Double, odometer: Double) {
        // Проверка валидности входных данных
        if (fuelAmount <= 0) {
            throw Exception("Количество топлива должно быть больше 0")
        }

        if (odometer < 0) {
            throw Exception("Пробег не может быть отрицательным")
        }

        if (refuelRecords.isEmpty()){
            val record = RefuelRecord(fuelAmount = fuelAmount, odometer = odometer)
            refuelRecords.add(record)
        } else {
            if(refuelRecords.last().odometer < odometer){
                val record = RefuelRecord(fuelAmount = fuelAmount, odometer = odometer)
                refuelRecords.add(record)
            } else {
                throw Exception("Пробег текущей записи должен быть больше пробега предыдущей записи)")
            }
        }
    }

    fun getHistory(): List<RefuelRecord> {
        return refuelRecords.toList()
    }

    fun getLastOdometer(): Double {
        if (refuelRecords.isNotEmpty()) {
            return refuelRecords.last().odometer
        } else {
            throw Exception("Список заправок пуст")
        }
    }

    fun calculateAverageConsumption(): Double {
        // Для расчета расхода нужно минимум 2 записи о заправках
        if (refuelRecords.size < 2){
            throw Exception("Для расчета расхода нужно минимум 2 записи о заправках")
        }

        // Суммируем все заправки топлива, кроме последней
        val totalFuel = refuelRecords.dropLast(1).sumOf { it.fuelAmount }

        // Общий пробег = последний пробег - первый пробег
        val totalDistance = refuelRecords.last().odometer - refuelRecords.first().odometer

        // Расход в л/100км
        return (totalFuel / totalDistance) * 100
    }

    fun clearHistory() {
        refuelRecords.clear()
    }
}