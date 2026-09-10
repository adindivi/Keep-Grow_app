package com.example.ui.util

import android.os.SystemClock
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ripple
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.semantics.Role

/**
 * Modifier extension to prevent rapid multiple clicks (debouncing).
 * Prevents multiple simultaneous triggers from frantic user taps.
 */
fun Modifier.debouncedClickable(
    debounceTimeMs: Long = 600L,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: () -> Unit
): Modifier = composed {
    var lastClickTime by remember { mutableLongStateOf(0L) }

    this.clickable(
        enabled = enabled,
        onClickLabel = onClickLabel,
        role = role,
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple()
    ) {
        val currentTime = SystemClock.uptimeMillis()
        if (currentTime - lastClickTime >= debounceTimeMs) {
            lastClickTime = currentTime
            onClick()
        }
    }
}

/**
 * Helper class to throttle standalone function calls (e.g. for non-Modifier callbacks).
 */
class ClickThrottler(private val minIntervalMs: Long = 600L) {
    private var lastExecutionTime = 0L

    fun runIfAllowed(action: () -> Unit): Boolean {
        val now = SystemClock.uptimeMillis()
        return if (now - lastExecutionTime >= minIntervalMs) {
            lastExecutionTime = now
            action()
            true
        } else {
            false
        }
    }
}
