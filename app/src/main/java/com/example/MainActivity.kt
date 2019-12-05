package com.example

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.domain.Description
import com.example.domain.Title
import com.example.domain.TodoItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow

class ViewHolder(v: View) : RecyclerView.ViewHolder(v)

class SimpleAdapter : RecyclerView.Adapter<ViewHolder>() {

    private val data = mutableListOf<TodoItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
            .let(::ViewHolder)

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.itemView.text.text =
            "Title ${item.title.value}, description: ${item.description.value}"
    }

    fun swap(new: List<TodoItem>) {
        val copy = data.toList()
        data.clear()
        data.addAll(new)
        DiffUtil.calculateDiff(Differ(copy, new)).dispatchUpdatesTo(this)
    }

}

data class Differ(
    private val old: List<TodoItem>,
    private val new: List<TodoItem>
) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        old[oldItemPosition] == new[newItemPosition]

    override fun getOldListSize(): Int = old.size

    override fun getNewListSize(): Int = new.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        old[oldItemPosition] == new[newItemPosition]

}

class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val simpleAdapter = SimpleAdapter()

        rv.adapter = simpleAdapter

        launch {

            val messages = Channel<Message>()

            btn_add.setOnClickListener {
                messages.offer(AddItem(randomItem()))
            }

            btn_remove.setOnClickListener {
                messages.offer(RemoveLastItem)
            }

            appComponent(messages.consumeAsFlow()).collect(Dispatchers.Main) { state ->
                simpleAdapter.swap(state.todoList)
            }
        }
    }

    override fun onDestroy() {
        cancel()
        super.onDestroy()
    }
}

// util stuff
private var counter: Long = 0

private fun randomItem() = TodoItem(
    counter++,
    Title.new("times $counter"),
    Description.new("description $counter")
)

suspend inline fun <T> Flow<T>.collect(
    on: CoroutineDispatcher,
    crossinline action: suspend (value: T) -> Unit
) {
    withContext(on) {
        collect { action(it) }
    }
}

