package com.example.feature.business.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.domain.Kind
import com.example.feature.business.FakeBusinessRepository
import com.example.feature.business.MainDispatcherRule
import com.example.feature.business.businessDetail
import com.example.feature.business.model.domain.BusinessDetailAction
import com.example.feature.business.model.domain.toBusinessDetailUi
import com.example.feature.business.usecase.GetBusinessDetailUseCase
import com.example.network.RetrofitException
import com.example.presentation.model.UIState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class BusinessDetailViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val repository = FakeBusinessRepository(detail = businessDetail(id = "abc"))
    private val savedStateHandle = SavedStateHandle()

    private fun viewModel() = BusinessDetailViewModel(
        savedStateHandle = savedStateHandle,
        detailId = "abc",
        getBusinessDetailUseCase = GetBusinessDetailUseCase(repository)
    )

    private fun TestScope.subscribe(viewModel: BusinessDetailViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
    }

    @Test
    fun `loads the detail on subscription`() = runTest {
        val viewModel = viewModel()
        subscribe(viewModel)

        assertEquals(UIState.Success(businessDetail(id = "abc").toBusinessDetailUi()), viewModel.uiState.value)
        assertEquals(listOf("abc"), repository.requestedDetailIds)
        assertEquals("abc", savedStateHandle["BusinessDetailViewModel.ARGUMENT_BUSINESS_DETAIL_ID"])
    }

    @Test
    fun `shows an error when loading fails`() = runTest {
        repository.detailError = RetrofitException.NetworkError(IOException("offline"))
        val viewModel = viewModel()
        subscribe(viewModel)

        assertEquals(UIState.Error(message = "offline", kind = Kind.NETWORK), viewModel.uiState.value)
    }

    @Test
    fun `retry reloads the detail`() = runTest {
        repository.detailError = RetrofitException.NetworkError(IOException("offline"))
        val viewModel = viewModel()
        subscribe(viewModel)

        repository.detailError = null
        viewModel.onAction(BusinessDetailAction.OnRetry)

        assertEquals(UIState.Success(businessDetail(id = "abc").toBusinessDetailUi()), viewModel.uiState.value)
        assertEquals(listOf("abc", "abc"), repository.requestedDetailIds)
    }
}
