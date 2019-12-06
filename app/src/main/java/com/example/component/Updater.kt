package com.example.component

import com.example.domain.*
import com.oliynick.max.elm.core.component.UpdateWith
import com.oliynick.max.elm.core.component.command
import com.oliynick.max.elm.core.component.noCommand

// Component that is responsible for the app state computations
// should be implemented as pure function, this makes business logic testable
// by default, allows us to apply such technique as memoization
interface Updater {
    // computes state transition
    fun update(
        message: Message,
        state: State
    ): UpdateWith<State, Command>
}

@Suppress("MemberVisibilityCanBePrivate")
object LiveUpdater : Updater {

    override fun update(
        message: Message,
        state: State
    ): UpdateWith<State, Command> =
        // calculations of what should be executed by Resolver
        when (message) {
            is AddItem -> tryToCreateNew(
                message.title,
                message.description,
                state
            )
            is RemoveLastItem -> tryToRemoveLast(
                state
            )
        }

    fun tryToRemoveLast(
        state: State
    ): UpdateWith<State, Command> {
        val lastId = state.todoList.lastOrNull()?.id ?: return state.noCommand()

        return state.removeItem(lastId) command TrackItemRemoved(lastId)
    }

    fun tryToCreateNew(
        title: String?,
        description: String?,
        state: State
    ): UpdateWith<State, Command> =
        unwrap(
            validate(
                title,
                Title::tryNew,
                "Invalid title"
            ),
            validate(
                description,
                Description::tryNew,
                "Invalid description"
            ),
            { t, d -> addItem(state, t, d) },
            { t, d -> updateValidation(state, t, d) }
        )

    fun addItem(
        state: State,
        title: Title,
        description: Description
    ): UpdateWith<State, TrackItemAdded> =
        state.addItem(title, description).let { updated -> updated command TrackItemAdded(updated.todoList.last().id) }

    fun updateValidation(
        state: State,
        title: Validated<Title>,
        description: Validated<Description>
    ): UpdateWith<State, Command> = state.validated(title, description).noCommand()

}
