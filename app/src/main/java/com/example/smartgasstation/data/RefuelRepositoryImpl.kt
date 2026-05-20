package com.example.smartgasstation.data

import com.example.smartgasstation.data.filemanager.RefuelRecordsFileManager
import com.example.smartgasstation.domain.entity.RefuelRecord
import com.example.smartgasstation.domain.repository.IRefuelRepository
import javax.inject.Inject

class RefuelRepositoryImpl @Inject constructor(
    private val dao: RefuelDao,
    private val fileManager: RefuelRecordsFileManager
): IRefuelRepository {
    override suspend fun getAllList(): List<RefuelRecord> = dao.getAllList().map { record ->
        refuelRecordToDomain(record)
    }

    override suspend fun insert(record: RefuelRecord) = dao.insert(refuelRecordToData(record))

    override suspend fun update(record: RefuelRecord) = dao.update(refuelRecordToData(record))

    override suspend fun delete(record: RefuelRecord) = dao.delete(refuelRecordToData(record))

    override suspend fun clear() = dao.clear()

    override fun saveToTxt(records: List<RefuelRecord>, fileName: String) {
        val dataRecords = records.map { record ->
            refuelRecordToData(record)
        }
        fileManager.saveToTxt(dataRecords, fileName)
    }

    override fun loadFromTxt(fileName: String): List<RefuelRecord> {
        return fileManager.loadFromTxt(fileName).map { record ->
            refuelRecordToDomain(record)
        }
    }

    override fun saveToXls(records: List<RefuelRecord>, fileName: String) {
        val dataRecords = records.map { record ->
            refuelRecordToData(record)
        }
        fileManager.saveToXls(dataRecords, fileName)
    }

    override fun loadFromXls(fileName: String): List<RefuelRecord> {
        return fileManager.loadFromXls(fileName).map { record ->
            refuelRecordToDomain(record)
        }
    }

    override fun saveToPdf(records: List<RefuelRecord>, fileName: String) {
        val dataRecords = records.map { record ->
            refuelRecordToData(record)
        }
        fileManager.saveToPdf(dataRecords, fileName)
    }

    private fun refuelRecordToDomain(record: RefuelRecordEntity):RefuelRecord{
        return RefuelRecord(
            id = record.id,
            fuelAmount = record.fuelAmount,
            odometer = record.odometer,
            timestamp = record.timestamp
        )
    }

    private fun refuelRecordToData(record: RefuelRecord):RefuelRecordEntity{
        return RefuelRecordEntity(
            id = record.id,
            fuelAmount = record.fuelAmount,
            odometer = record.odometer,
            timestamp = record.timestamp
        )
    }
}
