package com.example.feature.country.model.domain

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.example.feature.country.utils.countryCodeToFlagEmoji
import kotlinx.parcelize.Parcelize

@Immutable
@Parcelize
data class CountryUi(
    val capital: String?,
    val code: String?,
    val currency: CountryCurrency?,
    val flag: String?,
    val language: CountryLanguage,
    val name: String?,
    val region: String?
) : Parcelable

@Parcelize
data class Country(
    val capital: String?,
    val code: String?,
    val currency: CountryCurrency?,
    val flag: String?,
    val language: CountryLanguage,
    val name: String?,
    val region: String?
) : Parcelable{
    fun toUi() = CountryUi(
        capital = capital,
        code = code,
        currency = currency,
        flag = countryCodeToFlagEmoji(code),
        language = language,
        name = name,
        region = region
    )


}

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
        val country: CountryUi
    ) : CountryItem(type = CountryItemViewType.COUNTRY)
}

@Parcelize
enum class CountryItemViewType : Parcelable {
    LETTER_HEADER,
    COUNTRY
}