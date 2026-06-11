package com.autopulse.dev.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {

    @POST("api/v1/business/create")
    suspend fun createMerchant(
        @Body request: CreateMerchantRequest
    ): Response<CreateMerchantResponse>

    @POST("api/v1/customer/create")
    suspend fun createCustomer(
        @Header("Authorization") token: String,
        @Body request: CreateCustomerRequest
    ): Response<CreateCustomerResponse>

    @POST("api/v1/visit/create")
    suspend fun createVisit(
        @Header("Authorization") token: String,
        @Body request: CreateVisitRequest
    ): Response<CreateVisitResponse>
}
