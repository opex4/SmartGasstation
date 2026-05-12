package com.example.smartgasstation.data.repository

import com.example.smartgasstation.data.api.*
import com.example.smartgasstation.network.api.GasStationApi
import kotlinx.coroutines.*
import java.io.IOException

class GasStationRepository(private val api: GasStationApi) {
    suspend fun findBestStation(
        fuelType: String,
        fuelAmount: Double,
        consumption: Double
    ): Result<BestStationResponse> = withContext(Dispatchers.IO) {
        try {
            // Получение ID заправок с указанным типом топлива
            val idsResponse = api.getStationIds(fuelType)
            if (!idsResponse.isSuccessful || idsResponse.body() == null) {
                return@withContext Result.failure(IOException("Сервер недоступен или ошибка формата"))
            }

            val stationIds = idsResponse.body()!!.stationIds
            if (stationIds.isEmpty()) {
                return@withContext Result.failure(IOException("Заправки с таким топливом не найдены"))
            }

            // Отправка ID и параметров для расчета лучшей заправки
            val idsQuery = stationIds.joinToString(",")
            val bestResponse = api.getBestStation(idsQuery, fuelAmount, consumption)

            if (bestResponse.isSuccessful && bestResponse.body() != null) {
                Result.success(bestResponse.body()!!)
            } else {
                Result.failure(IOException("Ошибка при расчете лучшей заправки"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}