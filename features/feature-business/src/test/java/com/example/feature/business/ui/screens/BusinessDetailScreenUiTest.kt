package com.example.feature.business.ui.screens

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.feature.business.FakeBusinessRepository
import com.example.feature.business.businessDetail
import com.example.feature.business.model.domain.Hours
import com.example.feature.business.model.domain.Open
import com.example.feature.business.model.domain.SpecialHour
import com.example.feature.business.ui.viewmodel.BusinessDetailViewModel
import com.example.feature.business.usecase.GetBusinessDetailUseCase
import com.example.network.RetrofitException
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/** Drives the real [BusinessDetailViewModel] against a fake repository, rendered with Robolectric. */
@RunWith(AndroidJUnit4::class)
class BusinessDetailScreenUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val repository = FakeBusinessRepository(detail = businessDetail(id = "abc"))
    private var backClicks = 0

    private fun setScreen() {
        val viewModel = BusinessDetailViewModel(SavedStateHandle(), "abc", GetBusinessDetailUseCase(repository))
        composeRule.setContent {
            BusinessDetailScreen(viewModel = viewModel, onBackClick = { backClicks++ })
        }
    }

    private fun awaitDetail() = composeRule.waitUntil {
        composeRule.onAllNodes(hasText("Detail abc")).fetchSemanticsNodes().isNotEmpty()
    }

    @Test
    fun showsLoadedDetail() {
        setScreen()

        composeRule.onNodeWithText("Business Detail").assertIsDisplayed()
        composeRule.waitUntil { composeRule.onAllNodes(hasText("Detail abc")).fetchSemanticsNodes().isNotEmpty() }
        composeRule.onNodeWithText("Detail abc").assertIsDisplayed()
        composeRule.onNodeWithText("Phone: (555) 555-5555").assertExists()
    }

    // Regression tests: BusinessDetail used to pass R.string.yes/no and the day-of-week resource ids
    // straight into %s format args, rendering numbers like "Business claimed: 2131755xxx".

    @Test
    fun yesNoFlagsAreRenderedAsWords() {
        repository.detail = businessDetail(id = "abc").copy(isClaimed = true, isClosed = false)
        setScreen()
        awaitDetail()

        composeRule.onNodeWithText("Business claimed: Yes").assertIsDisplayed()
        composeRule.onNodeWithText("Business closed: No").assertIsDisplayed()
    }

    // Note: aapt collapses the runs of spaces in business_day_hours, so rows render single-spaced.

    // Yelp numbers days 0 = Monday … 6 = Sunday.
    private val weeklyHours = Hours(
        open = listOf(
            Open(isOvernight = false, start = "1100", end = "2200", day = 0),
            Open(isOvernight = true, start = "1700", end = "0200", day = 4),
            Open(isOvernight = false, start = "1200", end = "2000", day = 6)
        ),
        hoursType = "REGULAR",
        isOpenNow = true
    )

    @Test
    fun hoursRowsShowDayNamesAndYesNo() {
        repository.detail = businessDetail(id = "abc").copy(hours = weeklyHours)
        setScreen()
        awaitDetail()

        listOf(
            "Monday 1100 - 2200 overnight: No",
            "Friday 1700 - 0200 overnight: Yes",
            "Sunday 1200 - 2000 overnight: No"
        ).forEach { row -> composeRule.onNodeWithText(row).performScrollTo().assertIsDisplayed() }
    }

    @Test
    fun hoursShowOpenNowAndHoursType() {
        repository.detail = businessDetail(id = "abc").copy(hours = weeklyHours)
        setScreen()
        awaitDetail()

        composeRule.onNodeWithText("Is open now: Yes").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Hours type: REGULAR").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun specialHoursShowYesNo() {
        repository.detail = businessDetail(id = "abc").copy(
            specialHours = listOf(
                SpecialHour(date = "2026-12-25", end = "1400", isClosed = true, isOvernight = false, start = "1000")
            )
        )
        setScreen()
        awaitDetail()

        composeRule.onNodeWithText("Special Hours").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("2026-12-25 1000 - 1400 overnight: No, closed: Yes")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun noRowShowsARawResourceId() {
        repository.detail = businessDetail(id = "abc").copy(
            hours = Hours(open = listOf(Open(false, "0900", "1700", 0)), hoursType = "REGULAR", isOpenNow = false),
            specialHours = listOf(SpecialHour("2026-01-01", "1200", false, true, "0800"))
        )
        setScreen()
        awaitDetail()

        // Resource ids are large integers (0x7f......); none should leak into visible text.
        val rawId = Regex("""\b\d{7,}\b""")
        val leaked = composeRule.onAllNodes(hasText("", substring = true)).fetchSemanticsNodes()
            .flatMap { node -> node.config.getOrElse(SemanticsProperties.Text) { emptyList() } }
            .map { it.text }
            .filter { rawId.containsMatchIn(it) }
        assertEquals(emptyList<String>(), leaked)
    }

    @Test
    fun backButtonInvokesCallback() {
        setScreen()

        composeRule.onNodeWithContentDescription("Back").performClick()

        assertEquals(1, backClicks)
    }

    @Test
    fun errorShowsRetryWhichReloads() {
        repository.detailError = RetrofitException.NetworkError(IOException("offline"))
        setScreen()

        composeRule.onNodeWithText("offline").assertIsDisplayed()

        repository.detailError = null
        composeRule.onNodeWithText("Try Again").performClick()

        composeRule.waitUntil { composeRule.onAllNodes(hasText("Detail abc")).fetchSemanticsNodes().isNotEmpty() }
        assertEquals(listOf("abc", "abc"), repository.requestedDetailIds)
    }
}
