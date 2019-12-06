@file:Suppress("MemberVisibilityCanBePrivate", "unused")

// our domain models and definitions
// ideally we want it to be readable by
// non programmers such as managers, BO, etc.

package com.example.domain

import kotlin.contracts.contract

// all domain related stuff
// for sake of simplicity all entities reside in one file

data class TodoItem(
    val id: Long,
    val title: Title,
    val description: Description
)

/**
 * Immutable domain title representation, should be used
 * instead of raw [String] because:
 * * raw [String] can be passed between methods, app layers
 * without guaranties of being valid (it can be empty or it mightn't meet business
 * requirements)
 * * Consider the following function:
 * ```kotlin
 * fun foo(name: String, email: String) {
 * // some code
 * }
 * ```
 * and her call site
 * ```kotlin
 * fun bar() {
 *  foo("some@email.com", "Alex")
 * }
 * ```
 * Here we can see problem, we passed incorrect arguments and our program compiled just
 * fine. In the following case
 *
 * ```kotlin
 * fun foo(name: Name, email: Email) {}
 * ```
 *
 * it won't compile, we can be sure passed arguments meet business requirements,
 * we can safely work with them.
 */
data class Title internal constructor(// prohibits creation outside our 'domain' module
    inline val value: String
) {
    // our factory
    companion object {
        // we can trim or format our string in one place,
        // no need to look through the whole codebase
        fun new(value: String) = Title(value)

        fun tryNew(value: String?) =
            if (isValid(value)) new(value) else null

    }

    init {
        // constructed instance will never be in invalid state
        require(isValid(value)) { "Invalid title, was $value" }
    }

}

data class Description internal constructor(// prohibits creation outside our 'domain' module
    inline val value: String
) {
    // our factory
    companion object {
        // we can trim or format our string in one place,
        // no need to look through the whole codebase
        fun new(value: String) = Description(value)

        fun tryNew(value: String?) =
            if (isValid(value)) new(value) else null

    }

    init {
        // constructed instance will never be in invalid state
        require(isValid(value)) { "Invalid title, was $value" }
    }

}

/**
 * business validation goes here
 */
fun Title.Companion.isValid(value: String?): Boolean {
    // compiler hint
    contract {
        returns(true) implies (value != null)
    }

    return value.isNotNullOrEmpty()
}

fun Description.Companion.isValid(value: String?): Boolean {
    // compiler hint
    contract {
        returns(true) implies (value != null)
    }

    return value.isNotNullOrEmpty()
}

private fun String?.isNotNullOrEmpty(): Boolean = this != null && isNotEmpty()

