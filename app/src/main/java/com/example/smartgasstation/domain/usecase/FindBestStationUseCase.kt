package com.example.smartgasstation.domain.usecase

import com.example.smartgasstation.domain.entity.BestStation
import com.example.smartgasstation.domain.repository.IGasStationRepository

class FindBestStationUseCase(
    private val repository: IGasStationRepository
) {
    suspend operator fun invoke(
        fuelType: String,
        fuelAmount: Double,
        consumption: Double
    ): Result<BestStation> {
        return repository.findBestStation(fuelType, fuelAmount, consumption)
    }
}
