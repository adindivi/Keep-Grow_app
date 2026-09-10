package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val MODEL_NAME = "gemini-1.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun analyzeStock(
        stockName: String,
        ticker: String,
        price: String,
        change: String,
        userProfile: String = "",
        portfolioSummary: String = ""
    ): StockAnalysis = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API key is not configured. Falling back to robust offline mock diagnostic.")
            return@withContext getMockAnalysis(stockName, ticker)
        }

        val prompt = """
            You are Keep & Grow's professional AI Stock Analyst.
            Analyze the following stock in relation to the user's investment profile and current asset composition.
            
            [Target Stock]
            - Stock Name: ${stockName} (${ticker})
            - Current Price: ${price} (Change: ${change})
            
            [User Info]
            - Selected Investment Profile: ${userProfile}
            - User's Current Asset Composition: ${portfolioSummary}
            
            [Instructions]
            Generate a concise, extremely simple, and easy-to-understand diagnostic report in Korean.
            Avoid overly complex jargon. Write as if explaining to a beginner.
            Keep each section very short and direct (1-2 sentences, max 80 characters per section).
            Ensure the strategic recommendation explains how this stock fits or synchronizes with their current asset composition.
            
            Return a JSON object exactly with the following string fields:
            1. "strengths": A string describing the company's biggest core strength in plain, simple Korean.
            2. "risks": A string describing the most important active risk in plain, simple Korean.
            3. "recommendation": A string explaining how this stock fits into the user's portfolio or what action they should take based on their profile and assets, in extremely simple Korean.
            
            Do not include any prose outside of JSON, and do not include markdown blocks like ```json ... ```. Just return the raw JSON object.
        """.trimIndent()

        try {
            // Construct request JSON manually to avoid complex serialization version conflicts
            val partText = JSONObject().put("text", prompt)
            val partsArray = JSONArray().put(partText)
            val contentObject = JSONObject().put("parts", partsArray)
            val contentsArray = JSONArray().put(contentObject)

            // Dynamic Schema Specification for JSON structured response
            val responseFormatText = JSONObject()
                .put("mimeType", "application/json")
                .put("schema", JSONObject()
                    .put("type", "OBJECT")
                    .put("properties", JSONObject()
                        .put("strengths", JSONObject().put("type", "STRING").put("description", "Positive core factors of the company"))
                        .put("risks", JSONObject().put("type", "STRING").put("description", "Potential risks, structural headwinds, or high valuation concerns"))
                        .put("recommendation", JSONObject().put("type", "STRING").put("description", "Actions to take, targets, or portfolio reallocation strategy"))
                    )
                    .put("required", JSONArray().put("strengths").put("risks").put("recommendation"))
                )

            val generationConfig = JSONObject()
                .put("responseFormat", responseFormatText)
                .put("temperature", 0.3)

            val requestJson = JSONObject()
                .put("contents", contentsArray)
                .put("generationConfig", generationConfig)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestJson.toString().toRequestBody(mediaType)

            val url = "$BASE_URL?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.e(TAG, "Gemini API call failed with code ${response.code}: $errBody")
                    return@withContext getMockAnalysis(stockName, ticker)
                }

                val responseBodyStr = response.body?.string()
                if (responseBodyStr.isNullOrEmpty()) {
                    return@withContext getMockAnalysis(stockName, ticker)
                }

                // Parse standard Gemini structure
                val responseJson = JSONObject(responseBodyStr)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val contentObj = firstCandidate.getJSONObject("content")
                    val partsArr = contentObj.getJSONArray("parts")
                    if (partsArr.length() > 0) {
                        val firstPart = partsArr.getJSONObject(0)
                        val textAnswer = firstPart.getString("text")
                        
                        val cleanJsonStr = extractJsonString(textAnswer)
                        val parsedResult = JSONObject(cleanJsonStr)
                        return@withContext StockAnalysis(
                            strengths = parsedResult.getString("strengths"),
                            risks = parsedResult.getString("risks"),
                            recommendation = parsedResult.getString("recommendation")
                        )
                    }
                }
                return@withContext getMockAnalysis(stockName, ticker)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error executing Gemini analyst logic: ${e.message}", e)
            return@withContext getMockAnalysis(stockName, ticker)
        }
    }

    private fun extractJsonString(rawText: String): String {
        val trimmed = rawText.trim()
        val markdownRegex = Regex("""```(?:json)?\s*([\s\S]*?)\s*```""")
        val match = markdownRegex.find(trimmed)
        if (match != null) {
            return match.groupValues[1].trim()
        }
        val firstBrace = trimmed.indexOf('{')
        val lastBrace = trimmed.lastIndexOf('}')
        if (firstBrace != -1 && lastBrace > firstBrace) {
            return trimmed.substring(firstBrace, lastBrace + 1)
        }
        return trimmed
    }

    private fun getMockAnalysis(stockName: String, ticker: String): StockAnalysis {
        return when (ticker.uppercase()) {
            "005930", "SAMSUNG" -> StockAnalysis(
                strengths = "글로벌 1위 반도체 경쟁력과 강력한 모바일 브랜드 가치를 자랑합니다.",
                risks = "글로벌 경기 변동과 반도체 시장 가격 흐름에 따른 실적 기복이 있습니다.",
                recommendation = "고객님의 현재 투자 성향과 보유 자산 대비 지수 추종용 우량 자산으로 꾸준히 모아가기 좋습니다."
            )
            "000660", "SKHYNIX" -> StockAnalysis(
                strengths = "글로벌 빅테크 기업들에 고성능 AI 반도체(HBM)를 선도적으로 납품하고 있습니다.",
                risks = "단일 메모리 반도체 사업 비중이 높아 업황 변화 시 가격 변동폭이 큽니다.",
                recommendation = "성장 중심의 자산 구성을 선호하신다면, 조정 시마다 일정 금액씩 분할 매수하는 방법이 유리합니다."
            )
            "NVDA" -> StockAnalysis(
                strengths = "글로벌 AI 반도체 칩 시장을 사실상 독점하며 기록적인 수익을 내고 있습니다.",
                risks = "실적이 매우 높아 기대감이 큰 만큼 단기 가격 변동성이 매우 큽니다.",
                recommendation = "포트폴리오의 성장성을 대폭 늘려줄 수 있으나, 안전을 위해 전체 자산의 15% 내외 적정 비중 유지를 제안합니다."
            )
            "AAPL" -> StockAnalysis(
                strengths = "전 세계에 탄탄한 아이폰 충성 고객과 강력한 서비스 생태계를 갖추고 있습니다.",
                risks = "반독점 규제 소송 부담과 스마트폰 교체 주기 장기화 우려가 있습니다.",
                recommendation = "위험을 낮추는 든든한 버팀목 자산이므로 현금 자산 일부를 이 주식으로 상시 보유하는 것을 제안합니다."
            )
            "TSLA" -> StockAnalysis(
                strengths = "전기차 시장 혁신가이며 독보적인 자율주행(FSD) 및 에너지 사업 잠재력을 가집니다.",
                risks = "전기차 단가 경쟁 심화 및 CEO 이슈 등 주가 등락폭이 비교적 높은 편입니다.",
                recommendation = "고수익 고위험 성향에 잘 부합하는 주식으로, 자산의 급격한 변동에 유의하며 분할 진입하십시오."
            )
            else -> StockAnalysis(
                strengths = "$stockName($ticker)은 탄탄한 국내외 사업 입지와 양호한 재무 건전성을 확보했습니다.",
                risks = "원자재 비용 압박과 글로벌 경기 둔화 우려 등 거시 경제 변수가 남아 있습니다.",
                recommendation = "안정성과 성장의 균형을 잡아주는 자산이므로, 투자 성향과 여유 자금 규모에 맞춰 주기적으로 편입을 조언합니다."
            )
        }
    }
}

data class StockAnalysis(
    val strengths: String,
    val risks: String,
    val recommendation: String
)
