package com.example.feature.business

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.feature.business.model.domain.BusinessCategory
import com.example.feature.business.model.domain.BusinessLikeStatus
import com.example.feature.business.model.domain.BusinessSectionUi
import com.example.feature.business.model.domain.BusinessUi
import com.example.feature.business.ui.components.BusinessTestTags
import com.example.feature.business.ui.components.LikeToggleButton
import com.example.feature.business.ui.screens.BusinessList
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * On-device Compose tests for the like button, on its own and inside the business list.
 * No network or Hilt: the list gets its sections directly and like clicks flip a local set of liked ids,
 * the same way the ViewModel rebuilds sections from the liked-ids stream.
 */
@RunWith(AndroidJUnit4::class)
class BusinessLikeInstrumentedTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun businessUi(id: String, name: String) = BusinessUi(
        id = id,
        name = name,
        imageUrl = null,
        ratingText = "4.5",
        reviewCount = 10,
        priceText = "$$",
        distanceText = "0.5 mi",
        address = "1 Main St"
    )

    private val pizza = (1..6).map { businessUi(id = "p$it", name = "Pizza $it") }
    private val beer = (1..6).map { businessUi(id = "b$it", name = "Beer $it") }

    private fun sections(likedIds: Set<String>): ImmutableList<BusinessSectionUi> {
        fun List<BusinessUi>.withLikes() = map {
            it.copy(likeStatus = if (it.id in likedIds) BusinessLikeStatus.LIKED else BusinessLikeStatus.UNLIKED)
        }.toImmutableList()
        return persistentListOf(
            BusinessSectionUi(BusinessCategory.PIZZA, pizza.withLikes()),
            BusinessSectionUi(BusinessCategory.BEER, beer.withLikes())
        )
    }

    private var likedIds by mutableStateOf(emptySet<String>())
    private val clicked = mutableListOf<String>()

    private fun setList() {
        composeRule.setContent {
            BusinessList(
                sections = sections(likedIds),
                onBusinessClick = { clicked += it.id },
                onLikeClick = { business ->
                    likedIds = if (business.id in likedIds) likedIds - business.id else likedIds + business.id
                }
            )
        }
    }

    private fun likeButton(id: String) = composeRule.run {
        onNodeWithTag(BusinessTestTags.BUSINESS_LIST).performScrollToNode(hasTestTag(BusinessTestTags.likeButton(id)))
        onNodeWithTag(BusinessTestTags.likeButton(id))
    }

    @Test
    fun likeToggleButton_isAccessibleAndTogglesState() {
        var liked by mutableStateOf(false)
        composeRule.setContent {
            LikeToggleButton(
                modifier = Modifier.testTag(TAG),
                isLiked = liked,
                onToggle = { liked = !liked }
            )
        }
        val button = composeRule.onNodeWithTag(TAG)

        button.assertIsDisplayed().assertHasClickAction().assertContentDescriptionEquals("Like").assertIsOff()
        button.performClick().assertIsOn()
        button.performClick().assertIsOff()
    }

    @Test
    fun listLikeButton_togglesOnlyThatBusiness() {
        setList()

        likeButton("p1").performClick()

        likeButton("p1").assertIsOn()
        likeButton("p2").assertIsOff()
        assertEquals(setOf("p1"), likedIds)
    }

    @Test
    fun listLikeButton_doesNotOpenTheDetail() {
        setList()

        likeButton("p1").performClick()

        assertEquals(emptyList<String>(), clicked)
    }

    @Test
    fun listLikeButton_worksForRowsFurtherDown() {
        setList()

        likeButton("b6").performClick()
        likeButton("b6").assertIsOn()

        likeButton("b6").performClick()
        likeButton("b6").assertIsOff()
    }

    @Test
    fun listShowsLikesChangedElsewhere() {
        setList()
        likeButton("b3").assertIsOff()

        // e.g. liked on the detail screen, then the list rebuilt its sections from the liked ids
        likedIds = setOf("b3")

        likeButton("b3").assertIsOn()
    }

    @Test
    fun tappingTheRow_opensTheDetail() {
        setList()

        composeRule.onNodeWithTag(BusinessTestTags.businessItem("p2")).performClick()

        assertEquals(listOf("p2"), clicked)
    }

    private companion object {
        const val TAG = "like_toggle"
    }
}
