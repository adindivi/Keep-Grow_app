package com.example.domain.util

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Unit tests for ExponentialBackoff retry logic and OfflineSyncManager.
 */
class ExponentialBackoffTest {

    @Test
    fun retry_successOnFirstAttempt_executesOnceAndReturnsResult() = runBlocking {
        var callCount = 0
        val result = ExponentialBackoff.retry(
            maxAttempts = 3,
            initialDelayMs = 10L,
            useJitter = false
        ) { attempt ->
            callCount++
            "SUCCESS-$attempt"
        }

        assertEquals("SUCCESS-1", result)
        assertEquals(1, callCount)
    }

    @Test
    fun retry_recoverableFailureThenSuccess_retriesAndSucceeds() = runBlocking {
        var callCount = 0
        val retryCallbacks = mutableListOf<Pair<Int, Long>>()

        val result = ExponentialBackoff.retry(
            maxAttempts = 4,
            initialDelayMs = 20L,
            factor = 2.0,
            useJitter = false,
            onRetry = { attempt, delayMs, _ ->
                retryCallbacks.add(Pair(attempt, delayMs))
            }
        ) { attempt ->
            callCount++
            if (attempt < 3) {
                throw IOException("Temporary connection drop on attempt $attempt")
            }
            "RECOVERED_ON_3"
        }

        assertEquals("RECOVERED_ON_3", result)
        assertEquals(3, callCount)
        assertEquals(2, retryCallbacks.size)
        assertEquals(1, retryCallbacks[0].first)
        assertEquals(20L, retryCallbacks[0].second)
        assertEquals(2, retryCallbacks[1].first)
        assertEquals(40L, retryCallbacks[1].second)
    }

    @Test
    fun retry_allAttemptsFail_throwsLastException() = runBlocking {
        var callCount = 0
        try {
            ExponentialBackoff.retry(
                maxAttempts = 3,
                initialDelayMs = 10L,
                useJitter = false
            ) { attempt ->
                callCount++
                throw SocketTimeoutException("Timeout attempt $attempt")
            }
            fail("Expected SocketTimeoutException was not thrown")
        } catch (e: SocketTimeoutException) {
            assertEquals("Timeout attempt 3", e.message)
            assertEquals(3, callCount)
        }
    }

    @Test
    fun retry_unrecoverableFailure_throwsImmediatelyWithoutRetry() = runBlocking {
        var callCount = 0
        try {
            ExponentialBackoff.retry(
                maxAttempts = 3,
                initialDelayMs = 10L,
                useJitter = false
            ) { attempt ->
                callCount++
                throw IllegalArgumentException("Permanent client error (e.g. invalid query)")
            }
            fail("Expected IllegalArgumentException was not thrown")
        } catch (e: IllegalArgumentException) {
            assertEquals("Permanent client error (e.g. invalid query)", e.message)
            assertEquals(1, callCount) // Stopped immediately without retry
        }
    }

    @Test
    fun isRecoverableException_verifiesCorrectness() {
        assertTrue(ExponentialBackoff.isRecoverableException(SocketTimeoutException("timeout")))
        assertTrue(ExponentialBackoff.isRecoverableException(UnknownHostException("no host")))
        assertTrue(ExponentialBackoff.isRecoverableException(IOException("network reset")))
        assertTrue(ExponentialBackoff.isRecoverableException(RuntimeException("HTTP 429 Too Many Requests")))
        assertTrue(ExponentialBackoff.isRecoverableException(RuntimeException("Connection timeout error")))

        assertFalse(ExponentialBackoff.isRecoverableException(IllegalArgumentException("bad argument")))
        assertFalse(ExponentialBackoff.isRecoverableException(NullPointerException("null value")))
        assertFalse(ExponentialBackoff.isRecoverableException(IllegalStateException("invalid state")))
    }

    @Test
    fun offlineSyncManager_queueOperations_workCorrectly() = runBlocking {
        val manager = OfflineSyncManager()

        assertTrue(manager.pendingTasks.value.isEmpty())
        assertFalse(manager.isSyncing.value)

        // Enqueue tasks
        manager.enqueue(SyncTask.EXCHANGE_RATE)
        manager.enqueue(SyncTask.PORTFOLIO_PRICES)

        assertEquals(2, manager.pendingTasks.value.size)
        assertTrue(manager.pendingTasks.value.contains(SyncTask.EXCHANGE_RATE))
        assertTrue(manager.pendingTasks.value.contains(SyncTask.PORTFOLIO_PRICES))

        // Set syncing state
        manager.setSyncing(true, retryCount = 1)
        assertTrue(manager.isSyncing.value)
        assertEquals(1, manager.activeRetryCount.value)

        // Dequeue one
        manager.dequeue(SyncTask.EXCHANGE_RATE)
        assertEquals(1, manager.pendingTasks.value.size)
        assertFalse(manager.pendingTasks.value.contains(SyncTask.EXCHANGE_RATE))
        assertTrue(manager.pendingTasks.value.contains(SyncTask.PORTFOLIO_PRICES))

        // Clear all
        manager.clearAll()
        assertTrue(manager.pendingTasks.value.isEmpty())
        assertFalse(manager.isSyncing.value)
        assertEquals(0, manager.activeRetryCount.value)
    }
}
