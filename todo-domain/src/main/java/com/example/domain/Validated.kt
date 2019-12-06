package com.example.domain

sealed class Validated<out T>

data class Valid<out T>(val t: T) : Validated<T>()

data class Invalid(
    val description: String
) : Validated<Nothing>()

object Empty : Validated<Nothing>()

inline val <T> Validated<T>.value: T?
    get() = (this as? Valid)?.t

inline val Validated<*>.error: String?
    get() = (this as? Invalid)?.description

fun <T> validate(
    s: String?,
    optionalCreator: (String?) -> T?,
    ifInvalid: String
): Validated<T> = optionalCreator(s)?.let(::Valid) ?: Invalid(ifInvalid)

inline fun <T1, T2, R> unwrap(
    t1: Validated<T1>,
    t2: Validated<T2>,
    ifValid: (T1, T2) -> R,
    ifInvalid: (Validated<T1>, Validated<T2>) -> R
): R = if (t1 is Valid && t2 is Valid) ifValid(t1.t, t2.t) else ifInvalid(t1, t2)
