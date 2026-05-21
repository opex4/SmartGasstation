package com.example.smartgasstation.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.smartgasstation.domain.entity.BestStation
import com.example.smartgasstation.domain.entity.RefuelRecord
import com.example.smartgasstation.domain.usecase.AddRefuelUseCase
import com.example.smartgasstation.domain.usecase.FindBestStationUseCase
import com.example.smartgasstation.domain.usecase.GetRefuelRecordsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AddRefuelVM @Inject constructor(
    private val addRefuelUseCase: AddRefuelUseCase,
    private val findBestStationUseCase: FindBestStationUseCase,
    private val getRefuelRecordsUseCase: GetRefuelRecordsUseCase
) : ViewModel() {

    private var _allRecords = MutableLiveData<List<RefuelRecord>>(emptyList())
    val allRecords: LiveData<List<RefuelRecord>> = _allRecords

    init {
        viewModelScope.launch {
            _allRecords.value = getRefuelRecordsUseCase()
        }
    }

    val lastOdometer: LiveData<Double?> = _allRecords.map { list ->
        if (list.isEmpty()) null else list.last().odometer
    }

    val avgConsumption: LiveData<Double?> = _allRecords.map { list ->
        if (list.size < 2) {
            null
        } else {
            val totalFuel = list.dropLast(1).sumOf { it.fuelAmount }
            val totalDistance = list.last().odometer - list.first().odometer
            (totalFuel / totalDistance) * 100
        }
    }

    val avgConsumptionText: LiveData<String> = avgConsumption.map { avg ->
        if (avg != null && avg > 0) String.format(Locale.US, "%.2f", avg)
        else "10.0"
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    fun searchBestStation(fuelType: String, fuelAmount: Double, consumption: Double) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = findBestStationUseCase(fuelType, fuelAmount, consumption)
            _uiState.value = if (result.isSuccess) {
                UiState.Success(result.getOrNull()!!)
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "Ошибка запроса к серверу")
            }
        }
    }

    fun saveRefuel(fuelAmount: Double, odometer: Double) {
        viewModelScope.launch {
            _saveState.value = SaveState.Loading
            try {
                addRefuelUseCase(fuelAmount, odometer)
                _saveState.value = SaveState.Success
            } catch (e: Exception) {
                _saveState.value = SaveState.Error(e.message ?: "Ошибка сохранения")
            }
        }
    }

    fun cancelSearch() {
        _uiState.value = UiState.Idle
    }

    fun resetSaveState() {
        _saveState.value = SaveState.Idle
    }

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Success(val response: BestStation) : UiState()
        data class Error(val message: String) : UiState()
    }

    sealed class SaveState {
        object Idle : SaveState()
        object Loading : SaveState()
        object Success : SaveState()
        data class Error(val message: String) : SaveState()
    }
}
