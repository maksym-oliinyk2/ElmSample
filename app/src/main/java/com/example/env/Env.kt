@file:Suppress("FunctionName")

package com.example.env

import android.content.Context
import com.example.component.LiveUpdater
import com.example.component.Updater
import com.example.todo.analytics.Analytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

// Acts as Dagger's Component declaration (scoping + dependencies provisioning)
// Such approach doesn't require code generation, reflection and other fancy stuff -> build time gets reduced.
// This approach guaranties compile time safety;
// Any component can be replaced with another one (e.g. for tests, look at tests source set).
// Environment can be tied to Activity or Fragment lifecycle, no magic here
interface Env :
    Resolver<Env>,
    Updater,
    Analytics,
    Storage<Env>,
    HasCachingDir,
    HasJsonSerializers,
    CoroutineScope

// assembles production environment
// test environment can be found in the tests
fun LiveEnv(
    context: Context
): Env = object : Env,
    Resolver<Env> by Resolver(),
    Updater by LiveUpdater,
    Analytics by Analytics(),
    Storage<Env> by Storage(),
    HasCachingDir by HasCache(context),
    HasJsonSerializers by GsonSerializers(),
    CoroutineScope by ComponentScope {}

private object ComponentScope : CoroutineScope {
    override val coroutineContext: CoroutineContext =
        Executors.newSingleThreadExecutor { r -> Thread(r, "App Scheduler") }
            .asCoroutineDispatcher()
}