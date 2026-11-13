package com.example.feature.business.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.feature.business.model.domain.AddressDialogUi
import com.example.feature.business.model.domain.BusinessListAction
import com.example.feature.business.ui.components.AddressDialog
import com.example.feature.business.ui.components.BusinessTestTags
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddressDialogUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val actions = mutableListOf<BusinessListAction>()

    /** Mirrors the ViewModel: text changes update the dialog state so the field re-renders. */
    private fun setDialog(initial: AddressDialogUi) {
        var dialogUi by mutableStateOf(initial)
        composeRule.setContent {
            AddressDialog(dialogUi = dialogUi) { action ->
                actions += action
                if (action is BusinessListAction.OnAddressTextChange) dialogUi = AddressDialogUi.EnterAddress(action.address)
            }
        }
    }

    @Test
    fun hiddenDialog_rendersNothing() {
        setDialog(AddressDialogUi.Hidden)

        composeRule.onNodeWithTag(BusinessTestTags.ADDRESS_SOURCE_DIALOG).assertDoesNotExist()
        composeRule.onNodeWithTag(BusinessTestTags.ADDRESS_EDIT_DIALOG).assertDoesNotExist()
    }

    @Test
    fun editDialog_prefillsAddressAndConfirms() {
        setDialog(AddressDialogUi.EnterAddress("1 Main St"))

        composeRule.onNodeWithTag(BusinessTestTags.ADDRESS_EDIT_FIELD).assertTextContains("1 Main St")
        composeRule.onNodeWithTag(BusinessTestTags.ADDRESS_EDIT_FIELD).performTextReplacement("Berkeley, CA")
        composeRule.onNodeWithText("Search").assertIsEnabled().performClick()

        assertEquals(
            listOf(BusinessListAction.OnAddressTextChange("Berkeley, CA"), BusinessListAction.OnAddressConfirm),
            actions
        )
    }

    @Test
    fun editDialog_disablesSearchForBlankAddress() {
        setDialog(AddressDialogUi.EnterAddress("   "))

        composeRule.onNodeWithText("Search").assertIsNotEnabled()
    }

    @Test
    fun editDialog_cancelDismisses() {
        setDialog(AddressDialogUi.EnterAddress("1 Main St"))

        composeRule.onNodeWithText("Cancel").performClick()

        assertEquals(listOf<BusinessListAction>(BusinessListAction.OnDialogDismiss), actions)
    }
}
