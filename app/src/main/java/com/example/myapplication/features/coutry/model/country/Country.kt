package com.example.myapplication.features.coutry.model.country

import kotlinx.serialization.Serializable

@Serializable
data class Country(
    val capital: String?,
    val code: String?,
    val currency: CountryCurrency?,
    val flag: String?,
    val language: CountryLanguage,
    val name: String?,
    val region: String?
) : java.io.Serializable

@Serializable
data class CountryCurrency(
    val code: String?,
    val name: String?,
    val symbol: String?
)

@Serializable
data class CountryLanguage(
    val code: String?,
    val name: String?
)

sealed class CountryItem(
    val type: CountryItemViewType
) {
    data class Letter(
        val letter: String
    ) : CountryItem(type = CountryItemViewType.LETTER_HEADER)

    data class CountryInfo(
        val country: Country
    ) : CountryItem(type = CountryItemViewType.COUNTRY)
}

enum class CountryItemViewType {
    LETTER_HEADER,
    COUNTRY
}
