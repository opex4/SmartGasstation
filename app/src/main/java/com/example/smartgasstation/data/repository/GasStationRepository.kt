package com.example.smartgasstation.data.repository

import com.example.smartgasstation.data.api.GasStationApi
import com.example.smartgasstation.domain.entity.BestStation
import com.example.smartgasstation.domain.repository.IGasStationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class GasStationRepository(private val api: GasStationApi) :
    IGasStationRepository {

    override suspend fun findBestStation(
        fuelType: String,
        fuelAmount: Double,
        consumption: Double
    ): Result<BestStation> = withContext(Dispatchers.IO) {
        try {
            val idsResponse = api.getStationIds(fuelType)
            if (!idsResponse.isSuccessful || idsResponse.body() == null) {
                return@withContext Result.failure(IOException("Сервер недоступен или ошибка формата"))
            }

            val stationIds = idsResponse.body()!!.stationIds
            if (stationIds.isEmpty()) {
                return@withContext Result.failure(IOException("Заправки с таким топливом не найдены"))
            }

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
