package com.example.smartgasstation.viewModels

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.smartgasstation.data.AppDatabase
import com.example.smartgasstation.data.RefuelRecordEntity
import com.example.smartgasstation.data.RefuelRepository
import com.example.smartgasstation.filemanager.RefuelRecordsFileManager
import com.example.smartgasstation.multithreading.CoroutineManager
import com.example.smartgasstation.multithreading.ThreadManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainVM(application: Application) : AndroidViewModel(application){
    private val dao = AppDatabase.getDatabase(application).refuelDao()
    private val repository = RefuelRepository(dao)
    val refuelRecords = repository.allRecords
    private val fileManager = RefuelRecordsFileManager(application)

    private val threadManager = ThreadManager(fileManager)
    private val coroutineManager = CoroutineManager(fileManager)
    private var coroutineJob: Job? = null
    private val _progress = MutableLiveData<Int>()
    val progress: LiveData<Int> = _progress

    val lastOdometer: LiveData<Double?> = refuelRecords.map { list ->
        if (list.isEmpty()) null else list.last().odometer
    }

    val avgConsumption: LiveData<Double?> = refuelRecords.map { list ->
        if (list.size < 2) {
            null
        } else {
            val totalFuel = list.dropLast(1).sumOf { it.fuelAmount }
            val totalDistance = list.last().odometer - list.first().odometer
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
        viewModelScope.launch(Dispatchers.IO) {
            repository.exportToTxt(fileManager)
        }
    }

    fun saveToXls() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.exportToXls(fileManager)
        }
    }

    fun saveToPdf() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.exportToPdf(fileManager)
        }
    }

    fun loadFromTxt() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.importFromTxt(fileManager)
        }
    }

    fun loadFromXls() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.importFromXls(fileManager)
        }
    }

    fun startThreadExport() {
        _progress.postValue(0)
        viewModelScope.launch(Dispatchers.IO) {
            val records = repository.allRecords.value ?: return@launch
            threadManager.startSequentialExport(
                records = records,
                onProgress = { progress -> _progress.postValue(progress) },
                onError = { error ->
                    _progress.postValue(-1)
                    Log.e("MainVM", "Ошибка экспорта (threads): ${error.message}", error)
                }
            )
        }
    }

    fun startCoroutineExport() {
        _progress.postValue(0)
        coroutineJob = viewModelScope.launch {
            val records = repository.allRecords.value ?: return@launch
            coroutineManager.startSequentialExport(
                records = records,
                onProgress = { progress -> _progress.postValue(progress) },
                onError = { error ->
                    _progress.postValue(-1)
                    Log.e("MainVM", "Ошибка экспорта (coroutines): ${error.message}", error)
                }
            )
        }
    }

    fun cancelThreadExport() {
        threadManager.cancelTasks()
    }

    fun cancelCoroutineExport() {
        coroutineManager.cancelTasks()
    }
}