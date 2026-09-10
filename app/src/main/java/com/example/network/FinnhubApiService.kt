package com.example.network

import com.example.network.models.QuoteResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface FinnhubApiService {
    @GET("quote")
    suspend fun getQuote(
        @Query("symbol") symbol: String
    ): QuoteResponse
}
