package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.network.RetrofitClient
import com.example.network.models.QuoteResponse
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import com.example.ui.util.UserFriendlyError

data class HighGrowthStock(
    val symbol: String,
    val name: String,
    val quote: QuoteResponse? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class HighGrowthViewModel : ViewModel() {
    private val tag = "HighGrowthViewModel"

    private val _stocks = MutableStateFlow<List<HighGrowthStock>>(emptyList())
    val stocks: StateFlow<List<HighGrowthStock>> = _stocks.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _errorMessage = MutableSharedFlow<String>(extraBufferCapacity = 16)
    val errorMessage: SharedFlow<String> = _errorMessage.asSharedFlow()

    private val highGrowthList = listOf(
        "AAPL" to "Apple Inc.",
        "MSFT" to "Microsoft Corp.",
        "GOOGL" to "Alphabet Inc.",
        "AMZN" to "Amazon.com Inc.",
        "NVDA" to "NVIDIA Corp.",
        "META" to "Meta Platforms",
        "TSLA" to "Tesla, Inc.",
        "AMD" to "Advanced Micro Devices",
        "CRM" to "Salesforce",
        "ADBE" to "Adobe Inc.",
        "NFLX" to "Netflix Inc.",
        "PLTR" to "Palantir Tech",
        "SNOW" to "Snowflake Inc.",
        "CRWD" to "CrowdStrike",
        "PANW" to "Palo Alto Networks",
        "MDB" to "MongoDB",
        "DDOG" to "Datadog",
        "NET" to "Cloudflare",
        "ZS" to "Zscaler",
        "SHOP" to "Shopify",
        "SQ" to "Block Inc.",
        "ROKU" to "Roku Inc.",
        "TWLO" to "Twilio Inc.",
        "U" to "Unity Software"
    )

    init {
        fetchQuotes()
    }

    fun fetchQuotes() {
        // Prevent concurrent or duplicate refresh calls
        if (_isRefreshing.value) return

        _isRefreshing.value = true
        val currentStocks = _stocks.value.ifEmpty { 
            highGrowthList.map { HighGrowthStock(it.first, it.second, isLoading = true) }
        }
        
        _stocks.value = currentStocks.map { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val api = RetrofitClient.finnhubApi
            // Rate limit: max 3 concurrent requests to prevent Finnhub 429 Too Many Requests
            val semaphore = Semaphore(3)
            var hasAnyError = false
            var firstErrorMessage = ""

            val updatedStocks = coroutineScope {
                currentStocks.mapIndexed { index, stock ->
                    async {
                        semaphore.withPermit {
                            delay(index * 40L)
                            try {
                                val quote = api.getQuote(stock.symbol)
                                stock.copy(quote = quote, isLoading = false, error = null)
                            } catch (e: Exception) {
                                val userMsg = UserFriendlyError.logAndGetMessage(tag, "Quote for ${stock.symbol}", e)
                                hasAnyError = true
                                if (firstErrorMessage.isEmpty()) firstErrorMessage = userMsg
                                stock.copy(isLoading = false, error = userMsg)
                            }
                        }
                    }
                }.awaitAll()
            }
            
            _stocks.value = updatedStocks
            _isRefreshing.value = false

            if (hasAnyError && firstErrorMessage.isNotEmpty()) {
                _errorMessage.emit(firstErrorMessage)
            }
        }
    }
}
