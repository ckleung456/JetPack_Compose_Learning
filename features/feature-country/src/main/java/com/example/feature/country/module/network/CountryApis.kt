package com.example.feature.country.module.network

import com.example.feature.country.model.domain.Country
import com.example.feature.country.model.network.APIConstants
import kotlinx.coroutines.flow.Flow
import retrofit2.http.GET

interface CountryApis {
    @GET(APIConstants.GET_COUNTRY_API_URI)
    fun fetchCountriesAPI(): Flow<List<Country>>
}