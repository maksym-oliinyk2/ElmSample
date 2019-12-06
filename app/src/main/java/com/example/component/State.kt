package com.example.component

import com.example.domain.*

data class State(
    // fixme: leaky abstraction, replace annotation with json adapter inside storage file
    @Transient val validatedTitle: Validated<Title> = Empty,
    @Transient val validatedDescription: Validated<Description> = Empty,
    val todoList: List<TodoItem> = emptyList()
)

fun State.addItem(
    item: TodoItem
) = copy(
    todoList = todoList + item,
    validatedTitle = Valid(item.title),
    validatedDescription = Valid(item.description)
)

fun State.addItem(
    title: Title,
    description: Description
) = addItem(TodoItem(todoList.lastOrNull()?.id?.inc() ?: 0L, title, description))

fun State.removeItem(
    id: Long
) = copy(todoList = todoList.filter { it.id != id })

fun State.removeLastItem() =
    todoList.lastOrNull()?.id?.let { id -> removeItem(id) } ?: this

fun State.validated(
    title: Validated<Title>,
    description: Validated<Description>
) = copy(validatedTitle = title, validatedDescription = description)
