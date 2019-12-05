@file:Suppress("FunctionName")

package com.example.component

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.example.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.oliynick.max.elm.core.component.UpdateWith
import com.oliynick.max.elm.core.component.command
import com.oliynick.max.elm.core.component.effect
import com.oliynick.max.elm.core.component.noCommand
import kotlinx.coroutines.*

// Acts as Dagger's Component declaration
// but it doesn't require kapt, code generation and other fancy stuff;
// you don't waste your time on kapt which means no more endless builds.
// This approach guaranties compile time safety;
// Any component can be replaced with another one (e.g. for tests).
// Also we can create activity, screen or whatever scoped environments
interface Env :
    SharedPreferences,
    Resolver<Env>,
    Updater,
    Analytics,
    Db<Env>,
    CoroutineScope

// assembles production environment
fun LiveEnv(
    componentScope: CoroutineScope,
    application: Application
): Env = object : Env,
    Resolver<Env> by object : LiveResolver<Env> {},
    Updater by LiveUpdater,
    Analytics by TestAnalytics,
    Db<Env> by object : SimpleDb<Env> {},
    SharedPreferences by application.getSharedPreferences(
        BuildConfig.APPLICATION_ID,
        Context.MODE_PRIVATE
    ),
    CoroutineScope by componentScope {}

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
                is DoAddItem -> command.effect { ItemsUpdated(to + item) }
                is DoRemoveItem -> command.effect { ItemsUpdated(to.filter { it.id != id }) }
            }

        return runCatching { resolve() }
            // some analytics
            .onSuccess { track("Command: $command triggered messages: $it") }
            // error handling in one place
            .getOrThrow()// actually we should return an error message here
    }
}

interface Analytics {
    fun track(event: Any)
}

interface Db<Env> {
    suspend fun Env.store(state: State)
    suspend fun Env.retrieve(): State?
}

private val GSON: Gson by lazy {
    GsonBuilder().serializeNulls().setPrettyPrinting().create()
}

interface SimpleDb<Env> : Db<Env> where Env : SharedPreferences {

    @SuppressLint("ApplySharedPref")
    override suspend fun Env.store(state: State) {
        coroutineScope {
            launch(Dispatchers.IO) {
                edit().putString("state", GSON.toJson(state)).commit()
            }
        }
    }

    override suspend fun Env.retrieve(): State? =
        coroutineScope {
            withContext(Dispatchers.IO) {
                getString("state", null)
                    ?.let { json -> GSON.fromJson(json, State::class.java) }
            }
        }

}

object TestAnalytics : Analytics {
    override fun track(event: Any) = println(event)
}
// add other means of analytics below
//....

// Component that is responsible for the app state computations
// should be implemented as pure function, this makes business logic testable
// by default, allows us to apply such technique as memoization
interface Updater {
    // computes state transition
    fun update(
        message: Message,
        state: State
    ): UpdateWith<State, Command>
}

object LiveUpdater : Updater {

    override fun update(
        message: Message,
        state: State
    ): UpdateWith<State, Command> =
        // calculations of what should be executed by Resolver
        when (message) {
            is AddItem -> state command DoAddItem(message.item, state.todoList)
            is RemoveLastItem -> tryRemoveLast(state)
            is ItemsUpdated -> state.copy(todoList = message.todoList).noCommand()// we've updated state, finish
        }

    fun tryRemoveLast(state: State): UpdateWith<State, Command> {
        val id = state.todoList.lastOrNull()?.id ?: return state.noCommand()

        return state command DoRemoveItem(id, state.todoList)
    }

}
