package com.example.smartgasstation.domain.usecase

import com.example.smartgasstation.data.RefuelRecordEntity
import com.example.smartgasstation.data.RefuelRepository
import javax.inject.Inject

class AddRefuelUseCase @Inject constructor(
    private val repository: RefuelRepository
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
        repository.insert(RefuelRecordEntity(fuelAmount = fuelAmount, odometer = odometer))
    }
}
