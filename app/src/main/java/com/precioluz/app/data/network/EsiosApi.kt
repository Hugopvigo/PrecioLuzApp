package com.precioluz.app.data.network

import com.precioluz.app.BuildConfig
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface EsiosApi {

    @GET("/indicators/1001")
    suspend fun getPVPCPrices(
        @Header("x-api-key") apiKey: String = BuildConfig.ESIOS_API_TOKEN,
        @Header("Accept") accept: String = "application/json",
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("time_trunc") timeTrunc: String = "hour"
    ): EsiosResponse
}
