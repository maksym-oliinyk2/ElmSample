package com.example

import android.app.Activity
import android.app.Application
import androidx.fragment.app.Fragment
import com.example.component.AppComponent
import com.example.component.Message
import com.example.component.State
import com.example.env.LiveEnv
import com.oliynick.max.elm.core.component.Component
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class AndroidApp : Application(), CoroutineScope by ComponentScope {

    private val env by unsafeLazy {
        LiveEnv(this@AndroidApp, this@AndroidApp)
    }

    val component by unsafeLazy { AppComponent(env) }

}

inline val Activity.appComponent: Component<Message, State>
    get() = (application as AndroidApp).component

inline val Fragment.appComponent: Component<Message, State>
    get() = requireActivity().appComponent

private fun <T> unsafeLazy(block: () -> T) = lazy(LazyThreadSafetyMode.NONE, block)

private object ComponentScope : CoroutineScope {
    override val coroutineContext: CoroutineContext =
        Executors.newSingleThreadExecutor { r -> Thread(r, "App Scheduler") }
            .asCoroutineDispatcher()
}