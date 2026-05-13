package com.example.smartgasstation.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.smartgasstation.data.RefuelRecordEntity
import com.example.smartgasstation.domain.usecase.ClearHistoryUseCase
import com.example.smartgasstation.domain.usecase.DeleteRefuelUseCase
import com.example.smartgasstation.domain.usecase.ExportToPdfUseCase
import com.example.smartgasstation.domain.usecase.ExportToTxtUseCase
import com.example.smartgasstation.domain.usecase.ExportToXlsUseCase
import com.example.smartgasstation.domain.usecase.GetRefuelRecordsUseCase
import com.example.smartgasstation.domain.usecase.ImportFromTxtUseCase
import com.example.smartgasstation.domain.usecase.ImportFromXlsUseCase
import com.example.smartgasstation.domain.usecase.UpdateRefuelUseCase
import com.example.smartgasstation.multithreading.CoroutineManager
import com.example.smartgasstation.multithreading.ThreadManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainVM @Inject constructor(
    private val getRefuelRecordsUseCase: GetRefuelRecordsUseCase,
    private val deleteRefuelUseCase: DeleteRefuelUseCase,
    private val updateRefuelUseCase: UpdateRefuelUseCase,
    private val clearHistoryUseCase: ClearHistoryUseCase,
    private val exportToTxtUseCase: ExportToTxtUseCase,
    private val exportToXlsUseCase: ExportToXlsUseCase,
    private val exportToPdfUseCase: ExportToPdfUseCase,
    private val importFromTxtUseCase: ImportFromTxtUseCase,
    private val importFromXlsUseCase: ImportFromXlsUseCase,
    private val threadManager: ThreadManager,
    private val coroutineManager: CoroutineManager
) : ViewModel() {

    val refuelRecords: LiveData<List<RefuelRecordEntity>> = getRefuelRecordsUseCase()

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

    fun deleteRefuelRecord(record: RefuelRecordEntity) {
        viewModelScope.launch {
            deleteRefuelUseCase(record)
        }
    }

    fun updateRefuelRecord(record: RefuelRecordEntity, fuelAmount: Double, odometer: Double) {
        viewModelScope.launch {
            updateRefuelUseCase(record, fuelAmount, odometer)
        }
    }

    fun clearRefuelHistory() {
        viewModelScope.launch {
            clearHistoryUseCase()
        }
    }

    fun saveToTxt() {
        viewModelScope.launch(Dispatchers.IO) {
            exportToTxtUseCase()
        }
    }

    fun saveToXls() {
        viewModelScope.launch(Dispatchers.IO) {
            exportToXlsUseCase()
        }
    }

    fun saveToPdf() {
        viewModelScope.launch(Dispatchers.IO) {
            exportToPdfUseCase()
        }
    }

    fun loadFromTxt() {
        viewModelScope.launch(Dispatchers.IO) {
            importFromTxtUseCase()
        }
    }

    fun loadFromXls() {
        viewModelScope.launch(Dispatchers.IO) {
            importFromXlsUseCase()
        }
    }

    fun startThreadExport() {
        _progress.postValue(0)
        viewModelScope.launch(Dispatchers.IO) {
            val records = refuelRecords.value ?: return@launch
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
        viewModelScope.launch {
            val records = refuelRecords.value ?: return@launch
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
