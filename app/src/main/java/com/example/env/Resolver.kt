@file:Suppress("FunctionName")

package com.example.env

import com.example.component.Command
import com.example.component.Message
import com.example.component.TrackItemAdded
import com.example.component.TrackItemRemoved
import com.example.todo.analytics.Analytics
import com.oliynick.max.elm.core.component.sideEffect

fun <Env> Resolver(): Resolver<Env> where Env : Analytics = object : LiveResolver<Env> {}

// Component that is capable of executing commands such as
// navigation, requests, db access, etc.
interface Resolver<Env> {

    suspend fun Env.resolve(command: Command): Set<Message>

}

interface LiveResolver<Env> : Resolver<Env>
        where Env : Analytics // Dependencies needed for this component to work, add more `where`
// clauses if needed
{

    override suspend fun Env.resolve(command: Command): Set<Message> {

        suspend fun resolve(): Set<Message> =
            // navigation, http requests, logging, IO operations should be done here
            when (command) {
                is TrackItemAdded -> command.sideEffect { trackItemAdded(id) }
                is TrackItemRemoved -> command.sideEffect { trackItemRemoved(id) }
            }

        return runCatching { resolve() }
            // error handling in one place
            .getOrThrow()// actually we should return an error message here
    }
}