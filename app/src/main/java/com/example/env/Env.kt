@file:Suppress("FunctionName")

package com.example.env

import android.content.Context
import com.example.component.LiveUpdater
import com.example.component.Updater
import com.example.todo.analytics.Analytics
import com.example.todo.analytics.ConsoleAnalytics
import kotlinx.coroutines.CoroutineScope

// Acts as Dagger's Component declaration
// but it doesn't require kapt, code generation and other fancy stuff;
// you don't waste your time on kapt which means no more endless builds.
// This approach guaranties compile time safety;
// Any component can be replaced with another one (e.g. for tests).
// Also we can create activity, screen or whatever scoped environments
interface Env :
    Resolver<Env>,
    Updater,
    Analytics,
    Storage<Env>,
    HasCachingDir,
    HasJsonSerializers,
    CoroutineScope

// assembles production environment
fun LiveEnv(
    componentScope: CoroutineScope,
    context: Context
): Env = object : Env,
    Resolver<Env> by Resolver(),
    Updater by LiveUpdater,
    Analytics by ConsoleAnalytics,
    Storage<Env> by Storage(),
    HasCachingDir by HasCache(context),
    HasJsonSerializers by GsonSerializers(),
    CoroutineScope by componentScope {}

