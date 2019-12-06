package com.example.todo.analytics

interface Analytics {
    fun track(event: Any)
}

// add other means of analytics below
//....
object ConsoleAnalytics : Analytics {
    override fun track(event: Any) = println(event)
}
