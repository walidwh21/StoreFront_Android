package com.storefront.app.core.util

import kotlin.coroutines.cancellation.CancellationException

/**
 * Like `runCatching`, but never swallows coroutine cancellation — a cancelled
 * view-model scope must keep unwinding instead of being reported as an error.
 */
inline fun <T> attempt(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

fun Throwable.userMessage(fallback: String = "Something went wrong."): String =
    message?.takeIf { it.isNotBlank() } ?: fallback
