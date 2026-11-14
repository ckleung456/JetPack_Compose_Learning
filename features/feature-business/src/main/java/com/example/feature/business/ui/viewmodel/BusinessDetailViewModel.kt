package com.example.feature.business.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.usecase.UseCaseOutputWithStatus
import com.example.feature.business.model.domain.BusinessDetailAction
import com.example.feature.business.model.domain.BusinessDetailUi
import com.example.feature.business.usecase.GetBusinessDetailUseCase
import com.example.feature.business.usecase.ToggleBusinessLikeUseCase
import com.example.presentation.model.UIState
import com.example.presentation.model.toUiError
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = BusinessDetailViewModel.Factory::class)
class BusinessDetailViewModel @AssistedInject constructor(
    private val savedStateHandle: SavedStateHandle,
    @Assisted val detailId: String,
    private val getBusinessDetailUseCase: GetBusinessDetailUseCase,
    private val toggleBusinessLikeUseCase: ToggleBusinessLikeUseCase
) : ViewModel() {
    companion object {
        private const val TAG = "BusinessDetailViewModel"

        private const val ARGUMENT_BUSINESS_DETAIL_ID = "$TAG.ARGUMENT_BUSINESS_DETAIL_ID"
    }

    @AssistedFactory
    interface Factory {
        fun create(businessId: String): BusinessDetailViewModel
    }

    private val _detailId: String
        get() = savedStateHandle[ARGUMENT_BUSINESS_DETAIL_ID] ?: detailId

    private var hasLoaded = false
    private var loadJob: Job? = null

    private val _uiState = MutableStateFlow<UIState<BusinessDetailUi>>(UIState.Loading)
    val uiState = _uiState
        .onStart {
            if (!hasLoaded) {
                hasLoaded = true
                loadBusinessDetail(_detailId)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UIState.Loading
        )

    init {
        savedStateHandle[ARGUMENT_BUSINESS_DETAIL_ID] = detailId
    }

    fun onAction(action: BusinessDetailAction) {
        when(action) {
            BusinessDetailAction.OnRetry -> loadBusinessDetail(_detailId)
            BusinessDetailAction.OnLikeClick -> onLikeClick()
        }
    }

    private fun onLikeClick() {
        viewModelScope.launch {
            toggleBusinessLikeUseCase(input = _detailId).collect {}
        }
    }

    private fun loadBusinessDetail(id: String) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            getBusinessDetailUseCase(id).collect { output ->
                when(output) {
                    is UseCaseOutputWithStatus.Progress -> _uiState.update { UIState.Loading }
                    is UseCaseOutputWithStatus.Success -> _uiState.update { UIState.Success(output.result) }
                    is UseCaseOutputWithStatus.Failed -> _uiState.update {
                        val err = output.error.toUiError()
                        UIState.Error(message = err.message, kind = err.kind)
                    }
                }
            }
        }
    }
}