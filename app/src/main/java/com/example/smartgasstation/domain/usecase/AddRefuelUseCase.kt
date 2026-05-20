package com.example.smartgasstation.domain.usecase

import com.example.smartgasstation.domain.entity.RefuelRecord
import com.example.smartgasstation.domain.repository.IRefuelRepository

class AddRefuelUseCase(
    private val repository: IRefuelRepository
) {
    suspend operator fun invoke(fuelAmount: Double, odometer: Double) {
        if (fuelAmount <= 0) throw Exception("Количество топлива должно быть больше 0")
        if (odometer < 0) throw Exception("Пробег не может быть отрицательным")

        val list = repository.getAllList()
        if (list.isNotEmpty()) {
            val last = list.last()
            if (odometer <= last.odometer) {
                throw Exception("Пробег текущей записи должен быть больше предыдущей записи")
            }
        }
        repository.insert(RefuelRecord(fuelAmount = fuelAmount, odometer = odometer))
    }
}
