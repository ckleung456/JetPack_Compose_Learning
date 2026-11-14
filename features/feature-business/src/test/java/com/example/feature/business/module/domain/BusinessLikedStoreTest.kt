package com.example.feature.business.module.domain

import app.cash.turbine.test
import com.example.feature.business.FakeSharedPreferences
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BusinessLikedStoreTest {

    private val prefs = FakeSharedPreferences()
    private val store = BusinessLikedStore(prefs)

    @Test
    fun `starts empty when nothing is saved`() {
        assertEquals(emptySet<String>(), store.businessLikedIds.value)
    }

    @Test
    fun `toggle returns the new liked state`() {
        assertTrue(store.toggleBusinessLiked("a"))
        assertFalse(store.toggleBusinessLiked("a"))
    }

    @Test
    fun `toggle only affects the given id`() {
        store.toggleBusinessLiked("a")
        store.toggleBusinessLiked("b")
        store.toggleBusinessLiked("a")

        assertEquals(setOf("b"), store.businessLikedIds.value)
    }

    @Test
    fun `likes are saved and restored by a new store`() {
        store.toggleBusinessLiked("a")
        store.toggleBusinessLiked("b")

        assertEquals(setOf("a", "b"), BusinessLikedStore(prefs).businessLikedIds.value)
    }

    @Test
    fun `clear removes all likes from memory and preferences`() {
        store.toggleBusinessLiked("a")

        store.clear()

        assertEquals(emptySet<String>(), store.businessLikedIds.value)
        assertEquals(emptySet<String>(), BusinessLikedStore(prefs).businessLikedIds.value)
    }

    @Test
    fun `observers receive every change`() = runTest {
        store.businessLikedIds.test {
            assertEquals(emptySet<String>(), awaitItem())
            store.toggleBusinessLiked("a")
            assertEquals(setOf("a"), awaitItem())
            store.toggleBusinessLiked("a")
            assertEquals(emptySet<String>(), awaitItem())
        }
    }
}
