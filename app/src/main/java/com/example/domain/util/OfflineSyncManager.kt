package com.example.domain.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Tasks that can be queued during offline periods and synchronized when online.
 */
enum class SyncTask(val displayName: String) {
    EXCHANGE_RATE("환율 정보"),
    SCREENER_STOCKS("주가 100종목 데이터"),
    PORTFOLIO_PRICES("포트폴리오 및 관심종목 시세")
}

/**
 * Thread-safe queue manager for offline-first background synchronization.
 */
class OfflineSyncManager {

    private val mutex = Mutex()

    private val _pendingTasks = MutableStateFlow<Set<SyncTask>>(emptySet())
    val pendingTasks: StateFlow<Set<SyncTask>> = _pendingTasks.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _activeRetryCount = MutableStateFlow(0)
    val activeRetryCount: StateFlow<Int> = _activeRetryCount.asStateFlow()

    suspend fun enqueue(task: SyncTask) {
        mutex.withLock {
            _pendingTasks.value = _pendingTasks.value + task
        }
    }

    suspend fun dequeue(task: SyncTask) {
        mutex.withLock {
            _pendingTasks.value = _pendingTasks.value - task
        }
    }

    fun setSyncing(syncing: Boolean, retryCount: Int = 0) {
        _isSyncing.value = syncing
        _activeRetryCount.value = retryCount
    }

    suspend fun clearAll() {
        mutex.withLock {
            _pendingTasks.value = emptySet()
            _isSyncing.value = false
            _activeRetryCount.value = 0
        }
    }
}
