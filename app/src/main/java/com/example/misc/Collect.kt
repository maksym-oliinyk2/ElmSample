package com.example.misc

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext

// util stuff
suspend inline fun <T> Flow<T>.collect(
    on: CoroutineDispatcher,
    crossinline action: suspend (value: T) -> Unit
) {
    withContext(on) {
        collect { action(it) }
    }
}