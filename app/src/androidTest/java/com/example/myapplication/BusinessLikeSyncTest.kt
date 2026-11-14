package com.example.myapplication

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.feature.business.ui.components.BusinessTestTags
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end check that a business's like status stays in sync between the list and the detail screen
 * in the real app (real Hilt graph, Yelp backend and SharedPreferences).
 *
 * The data comes from the live API, so the test uses whichever business is listed first, works with
 * its current like status (likes persist across runs), and restores that status at the end. It is skipped
 * if the list can't load (no network or no API key).
 */
@RunWith(AndroidJUnit4::class)
class BusinessLikeSyncTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private val anyListLikeButton = SemanticsMatcher("is a business list like button") {
        it.config.getOrNull(SemanticsProperties.TestTag)?.startsWith(LIKE_TAG_PREFIX) == true
    }

    private fun exists(matcher: SemanticsMatcher) =
        composeRule.onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty()

    private fun waitUntil(timeoutMillis: Long = 10_000, condition: () -> Boolean) =
        composeRule.waitUntil(timeoutMillis) { condition() }

    private fun SemanticsNodeInteraction.isOn() =
        fetchSemanticsNode().config.getOrNull(SemanticsProperties.ToggleableState) == ToggleableState.On

    /** The same business can be listed under both Pizza and Beer, so use the first matching row. */
    private fun listLikeButton(id: String) = composeRule.onAllNodesWithTag(BusinessTestTags.likeButton(id))[0]
    private fun detailLikeButton() = composeRule.onNodeWithTag(BusinessTestTags.DETAIL_LIKE_BUTTON)

    private fun openBusinessList() {
        composeRule.onNode(hasText("Business") and isSelectable()).performClick()
        val loaded = runCatching { waitUntil(timeoutMillis = 30_000) { exists(anyListLikeButton) } }.isSuccess
        assumeTrue("Business list didn't load (network or Yelp API key missing)", loaded)
    }

    private fun openDetail(id: String) {
        composeRule.onAllNodesWithTag(BusinessTestTags.businessItem(id))[0].performClick()
        waitUntil { exists(hasTestTag(BusinessTestTags.DETAIL_LIKE_BUTTON)) }
        waitUntil(timeoutMillis = 20_000) { !exists(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)) }
    }

    @Test
    fun likeStatusStaysInSyncBetweenListAndDetail() {
        openBusinessList()
        val id = composeRule.onAllNodes(anyListLikeButton)[0]
            .fetchSemanticsNode().config[SemanticsProperties.TestTag].removePrefix(LIKE_TAG_PREFIX)
        val initiallyLiked = listLikeButton(id).isOn()

        // The detail screen shows the same status as the list.
        openDetail(id)
        waitUntil { detailLikeButton().isOn() == initiallyLiked }

        // Toggle on the detail screen...
        detailLikeButton().performClick()
        waitUntil { detailLikeButton().isOn() == !initiallyLiked }

        // ...and the list shows it after going back (this used to show the old status).
        Espresso.pressBack()
        waitUntil { exists(hasTestTag(BusinessTestTags.likeButton(id))) }
        waitUntil { listLikeButton(id).isOn() == !initiallyLiked }

        // Toggle back on the list, and the detail screen follows. This also restores the original status.
        listLikeButton(id).performClick()
        waitUntil { listLikeButton(id).isOn() == initiallyLiked }
        openDetail(id)
        waitUntil { detailLikeButton().isOn() == initiallyLiked }
        Espresso.pressBack()
    }

    private companion object {
        val LIKE_TAG_PREFIX = BusinessTestTags.likeButton(businessId = "")
    }
}
