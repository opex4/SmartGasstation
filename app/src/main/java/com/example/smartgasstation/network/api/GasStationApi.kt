package com.example.smartgasstation.network.api

import com.example.smartgasstation.data.api.*
import retrofit2.Response
import retrofit2.http.*

interface GasStationApi {
    @GET("stations/filter")
    suspend fun getStationIds(@Query("fuel_type") fuelType: String): Response<StationIdsResponse>

    @GET("stations/best")
    suspend fun getBestStation(
        @Query("ids") ids: String,
        @Query("fuel_amount") fuelAmount: Double,
        @Query("consumption") consumption: Double
    ): Response<BestStationResponse>
}