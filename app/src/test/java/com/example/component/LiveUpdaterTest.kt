package com.example.component

import com.example.domain.*
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.properties.forAll
import io.kotlintest.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

// no mocks needed
// no external SDKs like Android specific ones needed
// no Android emulator needed

@RunWith(JUnit4::class)
class LiveUpdaterTest {

    @Test
    fun `test Updater doesn't remove last item from empty state`() {

        val initial = State()
        val (nextState, commands) = LiveUpdater.update(RemoveLastItem, initial)

        nextState shouldBe initial
        commands.shouldBeEmpty()
    }

    @Test
    fun `test Updater removes only the last item from non empty state`() {

        val initialItems = List(10) { i -> TodoItem(i.toLong(), Title.new("title-$i"), Description.new("description-$i")) }
        var state = State(todoList = initialItems)

        for (i in initialItems.indices) {

            val (nextState, _) = LiveUpdater.update(RemoveLastItem, state)

            nextState.todoList shouldBe initialItems.subList(0, initialItems.size - i - 1)

            state = nextState
        }
    }

    @Test
    fun `test Updater inserts a new TodoItem properly and creates analytics event`() {

        val (nextState, commands) = LiveUpdater.update(AddItem("title", "description"), State())

        val expectedTitle = Title.new("title")
        val expectedDescription = Description.new("description")

        nextState shouldBe State(Valid(expectedTitle), Valid(expectedDescription), listOf(TodoItem(0L, expectedTitle, expectedDescription)))
        commands shouldContainExactlyInAnyOrder setOf(TrackItemAdded(0L))
    }

    @Test
    fun `test Updater inserts and validates a new TodoItem properly and creates analytics event`() {

        fun newState(
            title: Title,
            description: Description
        ) = State(Valid(title), Valid(description), listOf(TodoItem(0L, title, description)))

        fun checkForValidInputCorrespondingStateGetsCreated(
            title: Title,
            description: Description,
            nextState: State,
            commands: Set<Command>
        ) = // For valid input a valid state should be created together with analytics event
            nextState == newState(title, description) && commands.size == 1 && commands.first() == TrackItemAdded(0L)

        fun checkForInvalidInputCorrespondingStateGetsCreated(
            nextState: State,
            commands: Set<Command>
        ) = // Validated fields should be invalid for invalid input + there shouldn't be commands to execute
            nextState.todoList.isEmpty() && (nextState.validatedTitle !is Valid || nextState.validatedDescription !is Valid) && commands.isEmpty()

        forAll { a: String, b: String ->

            val (nextState, commands) = LiveUpdater.update(AddItem(a, b), State())

            (Title.isValid(a) && Description.isValid(b) && checkForValidInputCorrespondingStateGetsCreated(Title.new(a), Description.new(b), nextState, commands))
                    || checkForInvalidInputCorrespondingStateGetsCreated(nextState, commands)
        }
    }

}