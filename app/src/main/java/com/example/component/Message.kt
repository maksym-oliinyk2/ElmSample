package com.example.component

sealed class Message

data class AddItem(
    val title: String?,
    val description: String?
) : Message()

object RemoveLastItem : Message()
