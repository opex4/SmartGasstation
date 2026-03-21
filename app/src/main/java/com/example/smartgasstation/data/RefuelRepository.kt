package com.example.smartgasstation.data

import androidx.lifecycle.LiveData
import com.example.smartgasstation.filemanager.RefuelRecordsFileManager

class RefuelRepository(private val dao: RefuelDao) {
    val allRecords: LiveData<List<RefuelRecordEntity>> = dao.getAll()

    suspend fun addRefuelRecord(fuelAmount: Double, odometer: Double) {
        if (fuelAmount <= 0) {
            throw Exception("Количество топлива должно быть больше 0")
        }

        if (odometer < 0) {
            throw Exception("Пробег не может быть отрицательным")
        }

        val list = dao.getAllList()
        if (list.isEmpty()) {
            dao.insert(
                RefuelRecordEntity(
                    fuelAmount = fuelAmount,
                    odometer = odometer
                )
            )
        } else {
            val last = list.last()
            if (odometer <= last.odometer) {
                throw Exception("Пробег текущей записи должен быть больше предыдущей записи")
            }

            dao.insert(
                RefuelRecordEntity(
                    fuelAmount = fuelAmount,
                    odometer = odometer
                )
            )
        }
    }

    suspend fun deleteRefuelRecord(record: RefuelRecordEntity) {
        dao.delete(record)
    }

    suspend fun updateRefuelRecord(
        record: RefuelRecordEntity,
        fuelAmount: Double,
        odometer: Double
    ) {
        if (fuelAmount <= 0) {
            throw Exception("Количество топлива должно быть больше 0")
        }

        if (odometer < 0) {
            throw Exception("Пробег не может быть отрицательным")
        }

        val list = dao.getAllList()
        val position = list.indexOfFirst { it.id == record.id }

        if (position == -1) {
            throw Exception("Запись не найдена")
        }

        when (position) {
            0 -> {
                if (list.size > 1) {
                    val nextRecord = list[1]

                    if (odometer >= nextRecord.odometer) {
                        throw Exception("Пробег первой записи должен быть меньше пробега следующей записи")
                    }
                }
            }

            list.lastIndex -> {
                if (list.size > 1) {
                    val prevRecord = list[list.lastIndex - 1]

                    if (odometer <= prevRecord.odometer) {
                        throw Exception("Пробег последней записи должен быть больше предыдущей записи")
                    }
                }
            }
            else -> {
                val prevRecord = list[position - 1]
                val nextRecord = list[position + 1]

                if (odometer <= prevRecord.odometer) {
                    throw Exception("Пробег должен быть больше предыдущей записи")
                }

                if (odometer >= nextRecord.odometer) {
                    throw Exception("Пробег должен быть меньше следующей записи")
                }
            }
        }

        dao.update(
            record.copy(
                fuelAmount = fuelAmount,
                odometer = odometer
            )
        )
    }

    suspend fun clearHistory() {
        dao.clear()
    }

    suspend fun exportToTxt(fileManager: RefuelRecordsFileManager) {
        val records = dao.getAllList()
        fileManager.saveToTxt(records, "RefuelHistoryTxt")
    }

    suspend fun exportToXls(fileManager: RefuelRecordsFileManager) {
        val records = dao.getAllList()
        fileManager.saveToXls(records, "RefuelHistoryXls")
    }

    suspend fun exportToPdf(fileManager: RefuelRecordsFileManager) {
        val records = dao.getAllList()
        fileManager.saveToPdf(records, "RefuelHistoryPdf")
    }

    suspend fun importFromTxt(fileManager: RefuelRecordsFileManager) {
        val records = fileManager.loadFromTxt("RefuelHistoryTxt")
        dao.clear()
        records.forEach {
            dao.insert(it)
        }
    }

    suspend fun importFromXls(fileManager: RefuelRecordsFileManager) {
        val records = fileManager.loadFromXls("RefuelHistoryXls")
        dao.clear()
        records.forEach {
            dao.insert(it)
        }
    }
}