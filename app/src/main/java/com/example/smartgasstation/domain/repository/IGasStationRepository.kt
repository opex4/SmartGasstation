package com.example.smartgasstation.domain.repository

import com.example.smartgasstation.domain.entity.BestStation

interface IGasStationRepository {
    suspend fun findBestStation(
        fuelType: String,
        fuelAmount: Double,
        consumption: Double
    ): Result<BestStation>
}