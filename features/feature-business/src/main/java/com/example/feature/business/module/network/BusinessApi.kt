package com.example.feature.business.module.network

import com.example.feature.business.model.network.APIConstants
import com.example.feature.business.model.network.APIConstants.DEFAULT_LIMIT
import com.example.feature.business.model.network.BusinessDetailResponseDto
import com.example.feature.business.model.network.BusinessSearchResponseDto
import kotlinx.coroutines.flow.Flow
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface BusinessApi {
    @GET(APIConstants.GET_BUSINESS_API_URI)
    fun searchBusinesses(
        @Query("term") term: String,
        @Query("location") location: String,
        @Query("limit") limit: Int = DEFAULT_LIMIT
    ): Flow<BusinessSearchResponseDto>

    @GET(APIConstants.GET_BUSINESS_DETAIL_API_URI)
    fun getBusinessDetail(
        @Path(APIConstants.BUSINESS_ID) id: String
    ): Flow<BusinessDetailResponseDto>
}