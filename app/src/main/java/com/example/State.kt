package com.example

import com.example.domain.TodoItem

data class State(
    val todoList: List<TodoItem>
)

sealed class Message

data class AddItem(
    val item: TodoItem
) : Message()

object RemoveLastItem : Message()

data class ItemsUpdated(
    val todoList: List<TodoItem>
) : Message()

sealed class Command

data class DoAddItem(
    val item: TodoItem,
    val to: List<TodoItem>
) : Command()

data class DoRemoveItem(
    val id: Long,
    val to: List<TodoItem>
) : Command()
