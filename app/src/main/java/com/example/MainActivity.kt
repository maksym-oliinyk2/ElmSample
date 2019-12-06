package com.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.component.AddItem
import com.example.component.Message
import com.example.component.RemoveLastItem
import com.example.domain.error
import com.example.domain.value
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow

class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val simpleAdapter = SimpleAdapter()

        rv.adapter = simpleAdapter

        launch {

            val messages = Channel<Message>()

            btn_add.setOnClickListener {
                messages.offer(
                    AddItem(
                        et_title.text.toString(),
                        et_description.text.toString()
                    )
                )
            }

            btn_remove.setOnClickListener {
                messages.offer(RemoveLastItem)
            }

            appComponent(messages.consumeAsFlow()).collect(Dispatchers.Main) { state ->
                with(state) {
                    simpleAdapter.swap(todoList)
                    et_title.setText(validatedTitle.value?.value)
                    et_description.setText(validatedDescription.value?.value)
                    et_title.error = validatedTitle.error
                    et_description.error = validatedDescription.error
                }
            }
        }
    }

    override fun onDestroy() {
        cancel()
        super.onDestroy()
    }
}

// util stuff
suspend inline fun <T> Flow<T>.collect(
    on: CoroutineDispatcher,
    crossinline action: suspend (value: T) -> Unit
) {
    withContext(on) {
        collect { action(it) }
    }
}

