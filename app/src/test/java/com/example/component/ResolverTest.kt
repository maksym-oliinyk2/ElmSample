@file:Suppress("TestFunctionName")

package com.example.component

import com.example.domain.Description
import com.example.domain.Title
import com.example.domain.TodoItem
import com.example.env.Resolver
import com.example.todo.analytics.Analytics
import com.oliynick.max.elm.core.component.invoke
import io.kotlintest.matchers.collections.shouldContain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.concurrent.Executors

// Integration test sample running with JUnit

@RunWith(JUnit4::class)
class ResolverTest {

    private val mainThreadSurrogate = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        mainThreadSurrogate.close()
    }

    @Test
    fun `test resolver calls analytics with appropriate item id when new item is added`() = runBlockingTest {

        val trackedIds = mutableListOf<Long>()
        val trackingAnalytics = object : Analytics by NoOpAnalytics() {

            override fun trackItemAdded(itemId: Long) {
                trackedIds += itemId
            }
        }

        val component = TestComponent(TestEnv(trackingAnalytics))

        component(AddItem("title", "description")).take(1).collect()

        trackedIds shouldContain 0L
    }

    @Test
    fun `test resolver calls analytics with appropriate item id when item is removed`() = runBlockingTest {

        val trackedIds = mutableListOf<Long>()
        val trackingAnalytics = object : Analytics by NoOpAnalytics() {

            override fun trackItemRemoved(itemId: Long) {
                trackedIds += itemId
            }
        }

        val component = TestComponent(
            TestEnv(trackingAnalytics),
            State(todoList = listOf(TodoItem(10L, Title.new("title"), Description.new("description"))))
        )

        component(RemoveLastItem).take(1).collect()

        trackedIds shouldContain 10L
    }

    private fun TestEnv(
        analytics: Analytics
    ) = TestEnv(
        dispatcher = mainThreadSurrogate,
        updater = LiveUpdater,
        resolver = Resolver(),
        analytics = analytics
    )

}