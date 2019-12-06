package com.example

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.domain.TodoItem
import kotlinx.android.synthetic.main.item.view.*

class ViewHolder(v: View) : RecyclerView.ViewHolder(v)

class SimpleAdapter : RecyclerView.Adapter<ViewHolder>() {

    private var data = listOf<TodoItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        LayoutInflater.from(parent.context).inflate(
            R.layout.item, parent, false)
            .let(::ViewHolder)

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.itemView.text.text =
            "#${item.id}, ${item.title.value}, ${item.description.value}"
    }

    fun swap(new: List<TodoItem>) {
        val old = data
        data = new
        DiffUtil.calculateDiff(
            Differ(
                old,
                new
            )
        ).dispatchUpdatesTo(this)
    }

}

private data class Differ(
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