package com.example.smartgasstation.viewModels

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import android.app.Application
import androidx.lifecycle.viewModelScope
import com.example.smartgasstation.data.AppDatabase
import com.example.smartgasstation.data.RefuelRecordEntity
import com.example.smartgasstation.data.RefuelRepository
import com.example.smartgasstation.filemanager.RefuelHistoryFileManager
import kotlinx.coroutines.launch

class MainVM(application: Application) : AndroidViewModel(application){
    private val dao = AppDatabase.getDatabase(application).refuelDao()
    private val repository = RefuelRepository(dao)
    val refuelRecords = repository.allRecords
    private val fileManager = RefuelHistoryFileManager(application)

    val lastOdometer: LiveData<Double?> = refuelRecords.map { list ->
        if (list.isEmpty()) null else list.last().odometer
    }

    val avgConsumption: LiveData<Double?> = refuelRecords.map { list ->
        if (list.size < 2) {
            null
        } else {

            val totalFuel = list.dropLast(1).sumOf { it.fuelAmount }

            val totalDistance =
                list.last().odometer - list.first().odometer

            (totalFuel / totalDistance) * 100
        }
    }

    fun addRefuelRecord(fuelAmount: Double, odometer: Double) {
        viewModelScope.launch {
            repository.addRefuelRecord(fuelAmount, odometer)
        }
    }

    fun deleteRefuelRecord(record: RefuelRecordEntity) {
        viewModelScope.launch {
            repository.deleteRefuelRecord(record)
        }
    }

    fun updateRefuelRecord(
        record: RefuelRecordEntity,
        fuelAmount: Double,
        odometer: Double
    ) {
        viewModelScope.launch {
            repository.updateRefuelRecord(record, fuelAmount, odometer)
        }
    }

    fun clearRefuelHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun saveToTxt() {
        viewModelScope.launch {
            repository.exportToTxt(fileManager)
        }
    }

    fun saveToXls() {
        viewModelScope.launch {
            repository.exportToXls(fileManager)
        }
    }

    fun saveToPdf() {
        viewModelScope.launch {
            repository.exportToPdf(fileManager)
        }
    }

    fun loadFromTxt() {
        viewModelScope.launch {
            repository.importFromTxt(fileManager)
        }
    }

    fun loadFromXls() {
        viewModelScope.launch {
            repository.importFromXls(fileManager)
        }
    }
}