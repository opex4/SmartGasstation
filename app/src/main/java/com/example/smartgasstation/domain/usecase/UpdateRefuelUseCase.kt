package com.example.smartgasstation.domain.usecase

import com.example.smartgasstation.data.RefuelRecordEntity
import com.example.smartgasstation.data.RefuelRepository
import javax.inject.Inject

class UpdateRefuelUseCase @Inject constructor(
    private val repository: RefuelRepository
) {
    suspend operator fun invoke(record: RefuelRecordEntity, fuelAmount: Double, odometer: Double) {
        if (fuelAmount <= 0) throw Exception("Количество топлива должно быть больше 0")
        if (odometer < 0) throw Exception("Пробег не может быть отрицательным")

        val list = repository.getAllList()
        val position = list.indexOfFirst { it.id == record.id }
        if (position == -1) throw Exception("Запись не найдена")

        when (position) {
            0 -> {
                if (list.size > 1 && odometer >= list[1].odometer) {
                    throw Exception("Пробег первой записи должен быть меньше пробега следующей записи")
                }
            }
            list.lastIndex -> {
                if (list.size > 1 && odometer <= list[list.lastIndex - 1].odometer) {
                    throw Exception("Пробег последней записи должен быть больше предыдущей записи")
                }
            }
            else -> {
                if (odometer <= list[position - 1].odometer) {
                    throw Exception("Пробег должен быть больше предыдущей записи")
                }
                if (odometer >= list[position + 1].odometer) {
                    throw Exception("Пробег должен быть меньше следующей записи")
                }
            }
        }
        repository.update(record.copy(fuelAmount = fuelAmount, odometer = odometer))
    }
}
