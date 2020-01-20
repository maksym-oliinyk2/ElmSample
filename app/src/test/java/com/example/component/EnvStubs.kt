@file:Suppress("TestFunctionName")

package com.example.component

import com.example.env.Resolver
import com.example.env.Storage
import com.example.todo.analytics.Analytics
import com.oliynick.max.elm.core.component.Component
import com.oliynick.max.elm.core.component.UpdateWith
import com.oliynick.max.elm.core.component.component
import com.oliynick.max.elm.core.component.noCommand
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope

interface TestEnv :
        Resolver<TestEnv>,
        Updater,
        Analytics,
        Storage<TestEnv>,
        CoroutineScope

// Test dependencies

fun NoOpUpdater(): Updater = object : Updater {
    override fun update(
        message: Message,
        state: State
    ): UpdateWith<State, Command> = state.noCommand()
}

fun <Env> NoOpStorage(): Storage<Env> = object : Storage<Env> {
    override suspend fun Env.store(state: State) = Unit
    override suspend fun Env.retrieve(): State? = null
}

fun <Env> NoOpResolver(): Resolver<Env> = object : Resolver<Env> {
    override suspend fun Env.resolve(command: Command): Set<Message> = emptySet()
}

fun NoOpAnalytics() = object : Analytics {
    override fun trackItemAdded(itemId: Long) = Unit
    override fun trackItemRemoved(itemId: Long) = Unit
}

fun TestComponent(
    testEnv: TestEnv,
    state: State = State()
): Component<Message, State> {

    suspend fun doResolve(
        cmd: Command
    ) = testEnv.run { resolve(cmd) }

    fun doUpdate(
        msg: Message,
        s: State
    ) = testEnv.run { update(msg, s) }

    return testEnv.component(
        state,
        ::doResolve,
        ::doUpdate
    )
}

// assembles test environment. Any component can be replaced if needed
fun TestEnv(
    dispatcher: CoroutineDispatcher,
    resolver: Resolver<TestEnv> = NoOpResolver(),
    updater: Updater = NoOpUpdater(),
    analytics: Analytics = NoOpAnalytics(),
    storage: Storage<TestEnv> = NoOpStorage()
): TestEnv = object : TestEnv,
                      Resolver<TestEnv> by resolver,
                      Updater by updater,
                      Analytics by analytics,
                      Storage<TestEnv> by storage,
                      CoroutineScope by CoroutineScope(dispatcher) {}

