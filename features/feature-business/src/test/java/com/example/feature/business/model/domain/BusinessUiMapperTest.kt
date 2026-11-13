package com.example.feature.business.model.domain

import com.example.feature.business.MainDispatcherRule
import com.example.feature.business.R
import com.example.feature.business.business
import com.example.feature.business.businessDetail
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class BusinessUiMapperTest {

    @get:Rule
    val mainRule = MainDispatcherRule() // pins Locale.US for number formatting

    @Test
    fun `formats rating with one decimal and distance in miles`() {
        val ui = business(id = "1", rating = 4.25, distanceMeters = 2 * METERS_PER_MILE).toBusinessUi()

        assertEquals("4.3", ui.ratingText)
        assertEquals("2.0 mi", ui.distanceText)
        assertEquals("$$", ui.priceText)
        assertEquals("1 Main St", ui.address)
    }

    @Test
    fun `missing distance has no distance text`() {
        assertNull(business(id = "1", distanceMeters = null).toBusinessUi().distanceText)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invalid day of week is rejected`() {
        Open(isOvernight = false, start = "1100", end = "2200", day = 7).toOpenUi()
    }

    @Test
    fun `detail mapping copies hours and special hours`() {
        val detail = businessDetail().copy(
            hours = Hours(open = listOf(Open(false, "1100", "2200", 0)), hoursType = "REGULAR", isOpenNow = true),
            specialHours = listOf(SpecialHour("2026-12-25", "1800", true, false, "1000"))
        )

        val ui = detail.toBusinessDetailUi()

        assertEquals("4.3", ui.ratingText)
        // day is mapped to a day-of-week string resource id; Yelp's day 0 is Monday
        assertEquals(HoursUi(listOf(OpenUi(false, "1100", "2200", R.string.Monday)), "REGULAR", true), ui.hours)
        assertEquals(listOf(SpecialHourUi("2026-12-25", "1800", true, false, "1000")), ui.specialHours)
    }

    // Regression: day 0 used to map to Sunday, shifting every Yelp weekday by one.
    @Test
    fun `yelp days 0 to 6 map to Monday through Sunday`() {
        val expected = listOf(
            R.string.Monday, R.string.Tuesday, R.string.Wednesday, R.string.Thursday,
            R.string.Friday, R.string.Saturday, R.string.Sunday
        )

        assertEquals(expected, (0..6).map { day -> Open(false, "0900", "1700", day).toOpenUi().day })
    }
}
