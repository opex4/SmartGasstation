package com.example.smartgasstation.data

import androidx.lifecycle.LiveData
import javax.inject.Inject

class RefuelRepository @Inject constructor(private val dao: RefuelDao) {

    val allRecords: LiveData<List<RefuelRecordEntity>> = dao.getAll()

    suspend fun getAllList(): List<RefuelRecordEntity> = dao.getAllList()

    suspend fun insert(record: RefuelRecordEntity) = dao.insert(record)

    suspend fun update(record: RefuelRecordEntity) = dao.update(record)

    suspend fun delete(record: RefuelRecordEntity) = dao.delete(record)

    suspend fun clear() = dao.clear()
}
