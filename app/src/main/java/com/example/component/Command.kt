package com.example.component

sealed class Command

data class TrackItemAdded(
    val id: Long
) : Command()

data class TrackItemRemoved(
    val id: Long
) : Command()

