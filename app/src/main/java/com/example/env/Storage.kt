@file:Suppress("FunctionName")

package com.example.env

import android.content.Context
import com.example.component.State
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private val DEFAULT_GSON: Gson by lazy {
    GsonBuilder().serializeNulls().setPrettyPrinting().create()
}

fun <Env> Storage(): Storage<Env> where Env : HasJsonSerializers,
                                        Env : HasCachingDir = object : SimpleStorage<Env> {}

fun HasCache(context: Context): HasCachingDir =
    HasCache(
        File(
            context.cacheDir,
            "db.json"
        )
    )

fun HasCache(file: File) = object : HasCachingDir {

    init {
        file.createNewFile()
        require(file.isFile) { "Specified file $file is not actually a valid file" }
    }

    override val cache: File = file

}

fun GsonSerializers(gson: Gson = DEFAULT_GSON) = object :
    HasJsonSerializers {
    override fun toJson(any: Any): String = gson.toJson(any)
    override fun <T> fromJson(json: String, clazz: Class<T>): T = gson.fromJson(json, clazz)
}

fun GsonSerializers(
    config: GsonBuilder.() -> Unit
): HasJsonSerializers =
    GsonSerializers(
        GsonBuilder().apply(
            config
        ).create()
    )

interface Storage<Env> {
    suspend fun Env.store(state: State)
    suspend fun Env.retrieve(): State?
}

interface HasJsonSerializers {
    fun toJson(any: Any): String
    fun <T> fromJson(json: String, clazz: Class<T>): T
}

interface HasCachingDir {
    val cache: File
}

interface SimpleStorage<Env> : Storage<Env>
        where Env : HasJsonSerializers,
              Env : HasCachingDir {

    override suspend fun Env.store(state: State) {
        coroutineScope {
            launch(Dispatchers.IO) {
                cache.writeText(toJson(state))
            }
        }
    }

    override suspend fun Env.retrieve(): State? =
        withContext(Dispatchers.IO) {
            cache.readText()
                .takeIf { content -> content.isNotEmpty() }
                ?.let { content -> fromJson(content, State::class.java) }
        }

}
