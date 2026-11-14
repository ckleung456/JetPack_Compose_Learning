package com.example.feature.business.usecase

import com.example.network.RetrofitException
import com.example.shared.lib.FlowUseCase
import com.example.domain.usecase.UseCaseOutputWithStatus
import com.example.feature.business.model.domain.Business
import com.example.feature.business.model.domain.BusinessCategory
import com.example.feature.business.model.domain.ErrorData
import com.example.feature.business.model.domain.NearbyBusinesses
import com.example.feature.business.model.domain.NearByBusinessesUi
import com.example.feature.business.model.domain.toBusinessUi
import com.example.feature.business.module.network.BusinessRepository
import com.example.feature.business.utils.Utils.asResult
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@ViewModelScoped
class SearchPizzaAndBeerUseCase @Inject constructor(
    private val repository: BusinessRepository
): FlowUseCase<String, NearbyBusinesses, NearByBusinessesUi>() {
    companion object {
        private const val TAG = "SearchPizzaAndBeerUseCase"
    }

    override fun isValidInput(input: String): Boolean  = input.isNotBlank()

    override suspend fun flowWork(input: String): Flow<NearbyBusinesses> = combine(
        repository.getPizza(location = input).asResult(),
        repository.getBeer(location = input).asResult()
    ) { pizzaResult, beerResult ->
        if (pizzaResult.isFailure && beerResult.isFailure) {
            throw RetrofitException.UnexpectedError(
                exception = pizzaResult.exceptionOrNull()
                    ?: beerResult.exceptionOrNull()
                    ?: Exception("Both pizza and beer searches failed")
            )
        }

        val failed = buildMap {
            if (pizzaResult.isFailure) put(BusinessCategory.PIZZA, object : ErrorData{})
            if (beerResult.isFailure) put(BusinessCategory.BEER, object : ErrorData{})
        }

        NearbyBusinesses(
            pizza = pizzaResult.getOrNull().orEmpty(),
            beer = beerResult.getOrNull().orEmpty(),
            failed = failed
        )
    }

    override suspend fun onSucceedDataHandling(intermediate: NearbyBusinesses): UseCaseOutputWithStatus.Success<NearByBusinessesUi> =
        UseCaseOutputWithStatus.Success(
            result = NearByBusinessesUi(
                pizza = intermediate.pizza.toUi(),
                beer = intermediate.beer.toUi(),
                failedCategories = intermediate.failed.keys.toImmutableSet()
            )
        )

    private fun List<Business>.toUi() = map { it.toBusinessUi() }.toImmutableList()
}
