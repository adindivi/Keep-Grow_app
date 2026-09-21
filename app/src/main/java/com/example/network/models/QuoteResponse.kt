package com.example.network.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class QuoteResponse(
    @field:Json(name = "c") val currentPrice: Double? = null,
    @field:Json(name = "d") val change: Double? = null,
    @field:Json(name = "dp") val percentChange: Double? = null,
    @field:Json(name = "h") val highPrice: Double? = null,
    @field:Json(name = "l") val lowPrice: Double? = null,
    @field:Json(name = "o") val openPrice: Double? = null,
    @field:Json(name = "pc") val previousClosePrice: Double? = null,
    @field:Json(name = "t") val timestamp: Long? = null
)
