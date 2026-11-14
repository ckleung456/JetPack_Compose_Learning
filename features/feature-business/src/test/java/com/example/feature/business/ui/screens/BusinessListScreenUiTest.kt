package com.example.feature.business.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.domain.Kind
import com.example.feature.business.business
import com.example.feature.business.model.domain.AddressDialogUi
import com.example.feature.business.model.domain.BusinessCategory
import com.example.feature.business.model.domain.BusinessFilter
import com.example.feature.business.model.domain.BusinessLikeStatus
import com.example.feature.business.model.domain.BusinessListAction
import com.example.feature.business.model.domain.BusinessListState
import com.example.feature.business.model.domain.BusinessSectionUi
import com.example.feature.business.model.domain.toBusinessUi
import com.example.feature.business.ui.components.BusinessTestTags
import com.example.presentation.model.UIState
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Robolectric tests for the stateless [BusinessListScreen]: state in, actions out. */
@RunWith(AndroidJUnit4::class)
class BusinessListScreenUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val actions = mutableListOf<BusinessListAction>()

    private val sections = persistentListOf(
        BusinessSectionUi(
            category = BusinessCategory.PIZZA,
            businesses = persistentListOf(business(id = "p1", name = "Tony's").toBusinessUi())
        ),
        BusinessSectionUi(
            category = BusinessCategory.BEER,
            businesses = persistentListOf(business(id = "b1", name = "Brewery").toBusinessUi())
        )
    )
    private val loaded = BusinessListState(content = UIState.Success(sections))

    private fun setScreen(state: BusinessListState) {
        composeRule.setContent {
            BusinessListScreen(title = "Business", state = state, onAction = { actions += it })
        }
    }

    @Test
    fun loadingState_showsProgress() {
        setScreen(BusinessListState(content = UIState.Loading))

        assertTrue(
            composeRule.onAllNodes(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
                .fetchSemanticsNodes().isNotEmpty()
        )
        composeRule.onNodeWithTag(BusinessTestTags.BUSINESS_LIST).assertDoesNotExist()
    }

    @Test
    fun errorState_showsMessage() {
        setScreen(BusinessListState(content = UIState.Error(message = "Network down", kind = Kind.NETWORK)))

        composeRule.onNodeWithText("Network down").assertIsDisplayed()
        composeRule.onNodeWithText("Try Again").assertIsDisplayed()
    }

    @Test
    fun successState_showsSectionsAndBusinesses() {
        setScreen(loaded)

        composeRule.onNodeWithText("Business").assertIsDisplayed()
        composeRule.onNodeWithTag(BusinessTestTags.sectionHeader(BusinessCategory.PIZZA)).assertIsDisplayed()
        composeRule.onNodeWithText("Pizza (1)").assertIsDisplayed()
        composeRule.onNodeWithText("Tony's").assertIsDisplayed()
        composeRule.onNodeWithTag(BusinessTestTags.BUSINESS_LIST).performScrollToNode(hasText("Brewery"))
        composeRule.onNodeWithText("Beer (1)").assertIsDisplayed()
        composeRule.onNodeWithText("Brewery").assertIsDisplayed()
    }

    @Test
    fun clickingBusiness_sendsSelection() {
        setScreen(loaded)

        composeRule.onNodeWithTag(BusinessTestTags.BUSINESS_LIST).performScrollToNode(hasText("Brewery"))
        composeRule.onNodeWithText("Brewery").performClick()

        assertEquals(listOf<BusinessListAction>(BusinessListAction.OnBusinessSelected(id = "b1")), actions)
    }

    @Test
    fun filterChips_reflectSelectionAndSendActions() {
        setScreen(loaded.copy(filter = BusinessFilter.PIZZA))

        composeRule.onNodeWithTag(BusinessTestTags.filterChip(BusinessFilter.PIZZA)).assertIsSelected()
        composeRule.onNodeWithTag(BusinessTestTags.filterChip(BusinessFilter.ALL)).assertIsNotSelected()

        composeRule.onNodeWithTag(BusinessTestTags.filterChip(BusinessFilter.BEER)).performClick()

        assertEquals(listOf<BusinessListAction>(BusinessListAction.OnFilterSelect(BusinessFilter.BEER)), actions)
    }

    @Test
    fun typingInSearch_sendsQueryUpdatesAndClearResets() {
        var state by mutableStateOf(loaded)
        composeRule.setContent {
            BusinessListScreen(
                title = "Business",
                state = state,
                onAction = { action ->
                    actions += action
                    if (action is BusinessListAction.OnSearchQueryUpdate) state = state.copy(searchQuery = action.query)
                }
            )
        }

        composeRule.onNodeWithTag(BusinessTestTags.SEARCH_FIELD).performTextInput("tony")
        assertEquals(BusinessListAction.OnSearchQueryUpdate("tony"), actions.last())

        composeRule.onNodeWithContentDescription("Clear Search").performClick()
        assertEquals(BusinessListAction.OnSearchQueryUpdate(""), actions.last())
    }

    @Test
    fun addressHeader_showsAddressAndOpensDialog() {
        setScreen(loaded.copy(address = "742 Evergreen Terrace"))

        composeRule.onNodeWithText("742 Evergreen Terrace").assertIsDisplayed()
        composeRule.onNodeWithTag(BusinessTestTags.ADDRESS_HEADER).performClick()

        assertEquals(listOf<BusinessListAction>(BusinessListAction.OnAddressClick), actions)
    }

    @Test
    fun locatingAddress_showsFindingLocation() {
        setScreen(loaded.copy(isLocatingAddress = true))

        composeRule.onNodeWithText("Finding your location…").assertIsDisplayed()
    }

    @Test
    fun sourceDialog_offersBothOptions() {
        setScreen(loaded.copy(addressDialogUi = AddressDialogUi.ChooseSource))

        composeRule.onNodeWithTag(BusinessTestTags.ADDRESS_SOURCE_DIALOG).assertIsDisplayed()
        composeRule.onNodeWithText("Use Current Location").performClick()
        composeRule.onNodeWithText("Enter Address").performClick()
        composeRule.onNodeWithText("Cancel").performClick()

        assertEquals(
            listOf(
                BusinessListAction.OnChooseUseLocation,
                BusinessListAction.OnChooseEnterAddress,
                BusinessListAction.OnDialogDismiss
            ),
            actions
        )
    }

    @Test
    fun likeButtons_reflectEachBusinessLikeStatus() {
        val liked = BusinessListState(
            content = UIState.Success(
                persistentListOf(
                    BusinessSectionUi(
                        category = BusinessCategory.PIZZA,
                        businesses = persistentListOf(
                            business(id = "p1", name = "Tony's").toBusinessUi().copy(likeStatus = BusinessLikeStatus.LIKED),
                            business(id = "p2", name = "Apizza").toBusinessUi()
                        )
                    )
                )
            )
        )
        setScreen(liked)

        likeButton("p1").assertIsOn()
        likeButton("p2").assertIsOff()
    }

    @Test
    fun likeClick_sendsLikeActionWithoutOpeningTheDetail() {
        setScreen(loaded)

        likeButton("p1").performClick()

        assertEquals(
            listOf<BusinessListAction>(BusinessListAction.OnBusinessLikeClick(businessUi = sections[0].businesses[0])),
            actions
        )
    }

    @Test
    fun likeButton_followsStateChanges() {
        var state by mutableStateOf(loaded)
        composeRule.setContent {
            BusinessListScreen(title = "Business", state = state, onAction = { actions += it })
        }
        likeButton("b1").assertIsOff()

        state = loaded.copy(
            content = UIState.Success(
                persistentListOf(
                    sections[0],
                    sections[1].copy(
                        businesses = persistentListOf(sections[1].businesses[0].copy(likeStatus = BusinessLikeStatus.LIKED))
                    )
                )
            )
        )

        likeButton("b1").assertIsOn()
    }

    /** The list is lazy, so scroll the row into view before looking up its like button. */
    private fun likeButton(id: String) = composeRule.run {
        onNodeWithTag(BusinessTestTags.BUSINESS_LIST).performScrollToNode(hasTestTag(BusinessTestTags.likeButton(id)))
        onNodeWithTag(BusinessTestTags.likeButton(id))
    }
}
