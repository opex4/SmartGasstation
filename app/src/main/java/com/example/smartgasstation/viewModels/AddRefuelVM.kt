package com.example.smartgasstation.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartgasstation.data.BestStationResult
import com.example.smartgasstation.data.GasStation
import com.example.smartgasstation.data.api.BestStationResponse
import com.example.smartgasstation.data.repository.GasStationRepository
import com.example.smartgasstation.network.NetworkModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddRefuelVM(application: Application) : AndroidViewModel(application) {

    private val repository = GasStationRepository(
        api = NetworkModule.provideGasStationApi(application)
    )

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val localStations = listOf(
        GasStation(1, "Лукойл №1", 50.5, 2.5, listOf("АИ-95", "АИ-92", "ДТ")),
        GasStation(2, "Роснефть №2", 49.8, 5.0, listOf("АИ-95", "АИ-92", "ДТ", "Газ")),
        GasStation(3, "Газпромнефть №3", 51.2, 1.0, listOf("АИ-95", "АИ-98", "ДТ"))
    )

    fun searchBestStation(fuelType: String, fuelAmount: Double, consumption: Double) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val result = repository.findBestStation(fuelType, fuelAmount, consumption)
                if (result.isSuccess) {
                    _uiState.value = UiState.ServerResult(result.getOrNull()!!)
                } else {
                    handleFallback(fuelType, fuelAmount, consumption, result.exceptionOrNull())
                }
            } catch (e: Exception) {
                handleFallback(fuelType, fuelAmount, consumption, e)
            }
        }
    }

    private fun handleFallback(fuelType: String, fuelAmount: Double, consumption: Double, error: Throwable?) {
        val local = findLocalBest(fuelType, fuelAmount, consumption)
        _uiState.value = if (local != null) {
            UiState.LocalResult(local)
        } else {
            UiState.Error(error?.message ?: "Ошибка обработки")
        }
    }

    private fun findLocalBest(fuelType: String, fuelAmount: Double, consumption: Double): BestStationResult? {
        val suitable = localStations.filter { station ->
            station.availableFuels.any { it.contains(fuelType, ignoreCase = true) }
        }
        if (suitable.isEmpty()) return null
        return suitable.map { station ->
            val fuelCost = station.fuelPrice * fuelAmount
            val tripFuel = (station.distance * consumption) / 100
            val tripCost = tripFuel * station.fuelPrice
            BestStationResult(station, tripCost, fuelCost + tripCost)
        }.minByOrNull { it.totalCost }
    }

    fun cancelSearch() {
        _uiState.value = UiState.Idle
    }

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class ServerResult(val response: BestStationResponse) : UiState()
        data class LocalResult(val result: BestStationResult) : UiState()
        data class Error(val message: String) : UiState()
    }
}