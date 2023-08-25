package com.synervoz.voicecommunicationapp.ui.common

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

// Convenience function for Flow debouncing
fun <T> Flow<T>.throttleFirst(periodMillis: Long): Flow<T> {
    require(periodMillis > 0) { "throttleFirst period should be positive" }
    return flow {
        var lastTime = 0L
        collect { value ->
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastTime >= periodMillis) {
                lastTime = currentTime
                emit(value)
            }
        }
    }
}

// Convenience suspending function that allows collection of a Flow bound to lifecycle (button clicks, UI states, etc.)
suspend inline fun <reified T> Flow<T>.observe(
    lifecycleOwner: LifecycleOwner,
    noinline collector: suspend (T) -> Unit
): Unit = flowWithLifecycle(lifecycleOwner.lifecycle).collect {
    collector(it)
}
