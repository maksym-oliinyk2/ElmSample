@file:Suppress("FunctionName")

package com.example.todo.analytics

/**
 * Some analytics API
 */
interface Analytics {
    fun trackItemAdded(itemId: Long)
    fun trackItemRemoved(itemId: Long)
}

fun Analytics() = object : Analytics by ConsoleAnalytics {}

// Analytics implementation details encapsulated in
// its own module and invisible to other modules
internal object ConsoleAnalytics : Analytics {
    override fun trackItemAdded(itemId: Long) = println("Item with id $itemId was added")
    override fun trackItemRemoved(itemId: Long) = println("Item with id $itemId was removed")
}
