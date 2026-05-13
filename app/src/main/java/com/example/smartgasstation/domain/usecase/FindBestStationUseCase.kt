package com.example.smartgasstation.domain.usecase

import com.example.smartgasstation.data.api.BestStationResponse
import com.example.smartgasstation.data.repository.GasStationRepository
import javax.inject.Inject

class FindBestStationUseCase @Inject constructor(
    private val repository: GasStationRepository
) {
    suspend operator fun invoke(
        fuelType: String,
        fuelAmount: Double,
        consumption: Double
    ): Result<BestStationResponse> {
        return repository.findBestStation(fuelType, fuelAmount, consumption)
    }
}
