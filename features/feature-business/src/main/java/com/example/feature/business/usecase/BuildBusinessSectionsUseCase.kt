package com.example.feature.business.usecase

import com.example.shared.lib.FlowUseCase
import com.example.domain.usecase.UseCaseOutputWithStatus
import com.example.feature.business.model.domain.BuildBusinessSectionsInput
import com.example.feature.business.model.domain.BusinessCategory
import com.example.feature.business.model.domain.BusinessFilter
import com.example.feature.business.model.domain.BusinessSectionUi
import com.example.feature.business.model.domain.BusinessUi
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@ViewModelScoped
class BuildBusinessSectionsUseCase @Inject constructor() :
    FlowUseCase<BuildBusinessSectionsInput, List<BusinessSectionUi>, ImmutableList<BusinessSectionUi>>() {

    private val byNameComparator: Comparator<BusinessUi> = compareBy<BusinessUi, String>(String.CASE_INSENSITIVE_ORDER) { businessUi -> businessUi.name }
        .thenBy { it.id }

    override suspend fun flowWork(input: BuildBusinessSectionsInput): Flow<List<BusinessSectionUi>> =
        flow {
            val query = input.searchQuery
            val sections = buildList {
                if (input.filter.includes(BusinessCategory.PIZZA)) {
                    add(
                        BusinessSectionUi(
                            category = BusinessCategory.PIZZA,
                            businesses = input.businesses.pizza.matching(query = query)
                        )
                    )
                }
                if (input.filter.includes(BusinessCategory.BEER)) {
                    add(
                        BusinessSectionUi(
                            category = BusinessCategory.BEER,
                            businesses = input.businesses.beer.matching(query = query)
                        )
                    )
                }
            }
            emit(sections.filter { it.businesses.isNotEmpty() })
        }

    override suspend fun onSucceedDataHandling(intermediate: List<BusinessSectionUi>): UseCaseOutputWithStatus.Success<ImmutableList<BusinessSectionUi>> =
        UseCaseOutputWithStatus.Success(result = intermediate.toImmutableList())

    private fun BusinessFilter.includes(category: BusinessCategory): Boolean = when (this) {
        BusinessFilter.ALL -> true
        BusinessFilter.PIZZA -> category == BusinessCategory.PIZZA
        BusinessFilter.BEER -> category == BusinessCategory.BEER
    }

    private fun List<BusinessUi>.matching(query: String): ImmutableList<BusinessUi> =
        asSequence()
            .filter { query.isEmpty() || it.name.contains(query, ignoreCase = true) }
            .distinctBy { it.id }
            .sortedWith(byNameComparator)
            .toList()
            .toImmutableList()
}