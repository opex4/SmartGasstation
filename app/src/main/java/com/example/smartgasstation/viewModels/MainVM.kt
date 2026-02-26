package com.example.smartgasstation.viewModels

import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.smartgasstation.data.RefuelHistory
import com.example.smartgasstation.data.RefuelRecord
import com.example.smartgasstation.storages.RefuelHistoryFileManager
import android.app.Application

class MainVM(application: Application) : AndroidViewModel(application){
    private var refuelHistory = RefuelHistory()
    private val fileManager = RefuelHistoryFileManager(application)

    // LiveData для записей заправок
    private val _refuelRecords = MutableLiveData<List<RefuelRecord>>(refuelHistory.getHistory())
    val refuelRecords: LiveData<List<RefuelRecord>> = _refuelRecords

    // Обновление UI
    private fun refreshData() {
        _refuelRecords.value = refuelHistory.getHistory()
    }

    fun calculateAvgConsumption():Double{
        return refuelHistory.calculateAverageConsumption()
    }

    fun calculateAverageConsumption(): String {
        // Средний расход
        try {
            if (getHistory().isEmpty()) {
                return "Нет данных о заправках"
            } else {
                val averageConsumption = refuelHistory.calculateAverageConsumption()
                return "Средний расход: ${String.format("%.2f", averageConsumption)} л/100км"
            }
        } catch (e: Exception){
            return e.message?: "Ошибка расчёта расхода"
        }
    }

    fun addRefuelRecord(fuelAmount: Double, odometer: Double){
        refuelHistory.addRefuelRecord(fuelAmount, odometer)
        refreshData()
    }

    fun getLastOdometer(): Double {
        return refuelHistory.getLastOdometer()
    }

    private fun getHistory(): List<RefuelRecord> {
        return refuelHistory.getHistory()
    }

    fun clearRefuelHistory(){
        refuelHistory.clearHistory()
        refreshData()
    }

    fun deleteRefuelRecord(position: Int) {
        refuelHistory.deleteRefuelRecords(position)
        refreshData()
    }

    fun updateRefuelRecord(position: Int, fuelAmount: Double, odometer: Double) {
        refuelHistory.updateRefuelRecord(position, fuelAmount, odometer)
        refreshData()
    }

    fun saveToTxt(){
        fileManager.saveToTxt(refuelHistory, "RefuelHistoryTxt")
    }

    fun saveToXls(){
        fileManager.saveToXls(refuelHistory, "RefuelHistoryXls")
    }

    fun saveToPdf(){
        fileManager.saveToPdf(refuelHistory, "RefuelHistoryPdf")
    }

    fun loadFromTxt(){
        refuelHistory = fileManager.loadFromTxt("RefuelHistoryTxt")
        refreshData()
    }

    fun loadFromXls(){
        refuelHistory = fileManager.loadFromXls("RefuelHistoryXls")
        refreshData()
    }

    fun loadFromPdf(){
        refuelHistory = fileManager.loadFromPdf("RefuelHistoryPdf")
        refreshData()
    }
}