package com.example.smartgasstation.data.api

import com.example.smartgasstation.domain.entity.BestStation
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GasStationApi {
    @GET("stations/filter")
    suspend fun getStationIds(@Query("fuel_type") fuelType: String): Response<StationIdsDTO>

    @GET("stations/best")
    suspend fun getBestStation(
        @Query("ids") ids: String,
        @Query("fuel_amount") fuelAmount: Double,
        @Query("consumption") consumption: Double
    ): Response<BestStation>
}
