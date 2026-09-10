package com.example.ui.util

import android.util.Log
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import retrofit2.HttpException

/**
 * Maps technical exceptions and HTTP errors to user-friendly Korean messages.
 * Logs full diagnostic details to Logcat while presenting empathetic messages to users.
 */
object UserFriendlyError {

    fun toUserMessage(throwable: Throwable?): String {
        if (throwable == null) return "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."

        return when (throwable) {
            is UnknownHostException -> "인터넷 연결을 확인해 주세요. (오프라인 상태)"
            is SocketTimeoutException -> "서버 응답 시간이 초과되었습니다. 네트워크 상태를 확인해 주세요."
            is HttpException -> {
                when (throwable.code()) {
                    429 -> "조회 요청이 일시적으로 많습니다. 잠시 후(약 30초 뒤) 다시 시도해 주세요."
                    401, 403 -> "데이터 서버 인증에 실패했습니다. 관리자에게 문의해 주세요."
                    404 -> "요청하신 주식 정보를 찾을 수 없습니다."
                    in 500..599 -> "주식 데이터 서버가 점검 중이거나 일시적 장애 상태입니다."
                    else -> "데이터 서버와 통신 중 문제가 발생했습니다. (${throwable.code()})"
                }
            }
            is IOException -> "네트워크 통신 중 오류가 발생했습니다. 연결을 확인해 주세요."
            is org.json.JSONException -> "주식 데이터를 해석하는 중에 문제가 발생했습니다."
            else -> {
                val msg = throwable.message ?: ""
                when {
                    msg.contains("429", ignoreCase = true) || msg.contains("rate limit", ignoreCase = true) ->
                        "조회 요청이 일시적으로 많습니다. 잠시 후 다시 시도해 주세요."
                    msg.contains("timeout", ignoreCase = true) ->
                        "연결 시간이 초과되었습니다. 다시 시도해 주세요."
                    msg.contains("unable to resolve host", ignoreCase = true) ->
                        "인터넷 연결이 원활하지 않습니다."
                    else -> "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."
                }
            }
        }
    }

    fun logAndGetMessage(tag: String, contextDescription: String, throwable: Throwable?): String {
        val userMsg = toUserMessage(throwable)
        Log.e(tag, "[$contextDescription] Technical error: ${throwable?.message}", throwable)
        return userMsg
    }
}
