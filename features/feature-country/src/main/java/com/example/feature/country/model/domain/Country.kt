package com.example.feature.country.model.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Country(
    val capital: String?,
    val code: String?,
    val currency: CountryCurrency?,
    val flag: String?,
    val language: CountryLanguage,
    val name: String?,
    val region: String?
) : Parcelable

@Parcelize
data class CountryCurrency(
    val code: String?,
    val name: String?,
    val symbol: String?
) : Parcelable

@Parcelize
data class CountryLanguage(
    val code: String?,
    val name: String?
) : Parcelable

sealed class CountryItem(
    val type: CountryItemViewType
) : Parcelable {
    @Parcelize
    data class Letter(
        val letter: String
    ) : CountryItem(type = CountryItemViewType.LETTER_HEADER)

    @Parcelize
    data class CountryInfo(
        val country: Country
    ) : CountryItem(type = CountryItemViewType.COUNTRY)
}

@Parcelize
enum class CountryItemViewType : Parcelable {
    LETTER_HEADER,
    COUNTRY
}