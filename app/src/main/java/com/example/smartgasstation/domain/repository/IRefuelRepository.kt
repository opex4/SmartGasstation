package com.example.smartgasstation.domain.repository

import com.example.smartgasstation.data.RefuelRecordEntity
import com.example.smartgasstation.domain.entity.RefuelRecord

interface IRefuelRepository {
    suspend fun getAllList(): List<RefuelRecord>

    suspend fun insert(record: RefuelRecord)

    suspend fun update(record: RefuelRecord)

    suspend fun delete(record: RefuelRecord)

    suspend fun clear()

    fun saveToTxt(records: List<RefuelRecord>, fileName: String)

    fun loadFromTxt(fileName: String): List<RefuelRecord>

    fun saveToXls(records: List<RefuelRecord>, fileName: String)

    fun loadFromXls(fileName: String): List<RefuelRecord>

    fun saveToPdf(records: List<RefuelRecord>, fileName: String)
}