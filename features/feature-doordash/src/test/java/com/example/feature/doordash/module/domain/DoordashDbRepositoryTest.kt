package com.example.feature.doordash.module.domain

import com.example.feature.doordash.FakeLikedDao
import com.example.feature.doordash.model.domain.LikedStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DoordashDbRepositoryTest {

    private val dao = FakeLikedDao()
    private val repository = DoordashDbRepository(dao)

    @Test
    fun `saves and reads liked status by id`() = runTest {
        repository.saveLikedStatus(restaurantId = 7, likedStatus = LikedStatus.LIKED)

        assertEquals(LikedStatus.LIKED, repository.getLikedStatusById(7))
        assertNull(repository.getLikedStatusById(8))
        assertNull(repository.getLikedStatusById(null))
    }

    @Test
    fun `saving again replaces the previous status`() = runTest {
        repository.saveLikedStatus(restaurantId = 7, likedStatus = LikedStatus.LIKED)
        repository.saveLikedStatus(restaurantId = 7, likedStatus = LikedStatus.UN_LIKED)

        assertEquals(listOf(RestaurantLikedDb(7, LikedStatus.UN_LIKED)), repository.getAllLikedStatus().first())
    }

    @Test
    fun `clearDb removes all rows`() = runTest {
        repository.saveLikedStatus(restaurantId = 1, likedStatus = LikedStatus.LIKED)
        repository.saveLikedStatus(restaurantId = 2, likedStatus = LikedStatus.UN_LIKED)

        repository.clearDb()

        assertTrue(dao.all.isEmpty())
    }
}
