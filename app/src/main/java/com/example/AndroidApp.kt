package com.example

import android.app.Activity
import android.app.Application
import androidx.fragment.app.Fragment
import com.example.component.AppComponent
import com.example.component.Message
import com.example.component.State
import com.example.env.LiveEnv
import com.oliynick.max.elm.core.component.Component

class AndroidApp : Application() {

    val component by lazy(LazyThreadSafetyMode.NONE) { AppComponent(LiveEnv(this@AndroidApp)) }

}

inline val Activity.appComponent: Component<Message, State>
    get() = (application as AndroidApp).component

inline val Fragment.appComponent: Component<Message, State>
    get() = requireActivity().appComponent
