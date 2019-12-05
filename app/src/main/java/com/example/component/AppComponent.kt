@file:Suppress("FunctionName")

package com.example.component

import android.util.Log
import com.example.Command
import com.example.Message
import com.example.State
import com.oliynick.max.elm.core.component.*
import com.oliynick.max.elm.core.component.androidLogger

fun Env.AppComponent(): Component<Message, State> {

    suspend fun doResolve(cmd: Command) = this.resolve(cmd)

    return component(
        initializer(),
        ::doResolve,
        ::update,
        storageInterceptor(androidLogger("Todo App"))
    )
}


private fun Env.initializer(): Initializer<State, Command> = {
    // loads application state from any storage
    (retrieve() ?: State(emptyList())) to emptySet<Nothing>()
}

fun Env.storageInterceptor(another: Interceptor<Message, State, Command>): Interceptor<Message, State, Command> =
    { message, oldState, newState, commands ->
        another(message, oldState, newState, commands)
        store(newState)
    }
