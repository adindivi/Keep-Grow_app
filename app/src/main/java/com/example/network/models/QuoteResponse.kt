package com.example.network.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class QuoteResponse(
    @Json(name = "c") val currentPrice: Double? = null,
    @Json(name = "d") val change: Double? = null,
    @Json(name = "dp") val percentChange: Double? = null,
    @Json(name = "h") val highPrice: Double? = null,
    @Json(name = "l") val lowPrice: Double? = null,
    @Json(name = "o") val openPrice: Double? = null,
    @Json(name = "pc") val previousClosePrice: Double? = null,
    @Json(name = "t") val timestamp: Long? = null
)
