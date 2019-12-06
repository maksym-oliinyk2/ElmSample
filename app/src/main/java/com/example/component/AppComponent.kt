@file:Suppress("FunctionName")

package com.example.component

import com.example.env.Env
import com.oliynick.max.elm.core.component.*

fun Env.AppComponent(): Component<Message, State> {

    suspend fun doResolve(cmd: Command) = this.resolve(cmd)

    fun doUpdate(msg: Message, s: State) = this.update(msg, s)

    return component(
        initializer(),
        ::doResolve,
        ::doUpdate,
        storageInterceptor(androidLogger("Todo App"))
    )
}

private fun Env.initializer(): Initializer<State, Command> = {
    // loads application state from any storage
    (retrieve() ?: State()) to emptySet<Nothing>()
}

fun Env.storageInterceptor(another: Interceptor<Message, State, Command>): Interceptor<Message, State, Command> =
    { message, oldState, newState, commands ->
        another(message, oldState, newState, commands)
        store(newState)
    }
