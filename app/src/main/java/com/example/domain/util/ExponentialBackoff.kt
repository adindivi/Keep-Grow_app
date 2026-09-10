package com.example.domain.util

import android.util.Log
import kotlinx.coroutines.delay
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.ThreadLocalRandom
import retrofit2.HttpException

/**
 * Exponential Backoff Retry Engine for resilient networking.
 *
 * Automatically retries recoverable failures (timeouts, network drops, 429 rate limits, 5xx server errors)
 * using an exponentially increasing delay with random jitter to prevent thundering herd problems.
 */
object ExponentialBackoff {
    private const val TAG = "ExponentialBackoff"

    /**
     * Executes [block] with exponential backoff retries.
     *
     * @param maxAttempts Maximum total attempts (including the initial try). Minimum 1.
     * @param initialDelayMs Initial delay before the first retry in milliseconds.
     * @param maxDelayMs Cap for the maximum delay between retries.
     * @param factor Multiplier applied to the delay on each subsequent retry.
     * @param useJitter Whether to apply random +/- 15% jitter to the delay.
     * @param retryIf Predicate defining which exceptions qualify for a retry.
     * @param onRetry Callback invoked before each retry with the attempt count, delay, and causing exception.
     * @param block The suspendable action to execute.
     */
    suspend fun <T> retry(
        maxAttempts: Int = 3,
        initialDelayMs: Long = 1000L,
        maxDelayMs: Long = 10000L,
        factor: Double = 2.0,
        useJitter: Boolean = true,
        tag: String = TAG,
        retryIf: (Throwable) -> Boolean = { isRecoverableException(it) },
        onRetry: ((attempt: Int, delayMs: Long, error: Throwable) -> Unit)? = null,
        block: suspend (attempt: Int) -> T
    ): T {
        require(maxAttempts >= 1) { "maxAttempts must be at least 1" }

        var currentDelay = initialDelayMs
        for (attempt in 1..maxAttempts) {
            try {
                return block(attempt)
            } catch (t: Throwable) {
                if (attempt >= maxAttempts || !retryIf(t)) {
                    // Exhausted retries or unrecoverable error -> rethrow
                    throw t
                }

                // Calculate jittered delay
                val jitteredDelay = if (useJitter) {
                    val jitterFactor = 1.0 + (ThreadLocalRandom.current().nextDouble(-0.15, 0.15))
                    (currentDelay * jitterFactor).toLong().coerceIn(100L, maxDelayMs)
                } else {
                    currentDelay.coerceAtMost(maxDelayMs)
                }

                try {
                    Log.w(
                        tag,
                        "Attempt $attempt/$maxAttempts failed: [${t.javaClass.simpleName}: ${t.message}]. Retrying in ${jitteredDelay}ms..."
                    )
                } catch (_: Throwable) {
                    println("[$tag] Attempt $attempt/$maxAttempts failed: [${t.javaClass.simpleName}: ${t.message}]. Retrying in ${jitteredDelay}ms...")
                }
                onRetry?.invoke(attempt, jitteredDelay, t)

                delay(jitteredDelay)

                // Exponentially scale delay for next round
                currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelayMs)
            }
        }
        error("Unreachable statement in ExponentialBackoff.retry")
    }

    /**
     * Determines whether an exception represents a temporary, recoverable failure.
     */
    fun isRecoverableException(throwable: Throwable): Boolean {
        return when (throwable) {
            is SocketTimeoutException -> true
            is UnknownHostException -> true
            is IOException -> true
            is HttpException -> {
                val code = throwable.code()
                // Retry on rate limit (429) and temporary server errors (500, 502, 503, 504)
                code == 429 || code in 500..599
            }
            else -> {
                val msg = throwable.message.orEmpty()
                msg.contains("429") || msg.contains("timeout", ignoreCase = true) || msg.contains("reset", ignoreCase = true)
            }
        }
    }
}
