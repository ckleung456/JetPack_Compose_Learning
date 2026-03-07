package com.example.myapplication.features.coutry.module.network

import com.example.myapplication.features.coutry.model.country.Country
import com.example.myapplication.features.coutry.model.network.APIConstants
import kotlinx.coroutines.flow.Flow
import retrofit2.http.GET

interface CountryAPIs {
    @GET(APIConstants.GET_COUNTRY_API_URI)
    fun fetchCountriesAPI(): Flow<List<Country>>
}