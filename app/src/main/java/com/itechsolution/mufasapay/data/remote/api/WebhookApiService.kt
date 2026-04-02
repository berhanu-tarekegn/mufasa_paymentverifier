package com.itechsolution.mufasapay.data.remote.api

import com.itechsolution.mufasapay.data.remote.dto.SmsWebhookPayload
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.HTTP
import retrofit2.http.HeaderMap
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Url

/**
 * Retrofit API service for webhook forwarding
 * Uses @Url parameter to support dynamic webhook endpoints
 */
interface WebhookApiService {

    @POST
    suspend fun forwardSmsPost(
        @Url url: String,
        @HeaderMap headers: Map<String, String>,
        @retrofit2.http.Body payload: SmsWebhookPayload
    ): Response<ResponseBody>

    @PUT
    suspend fun forwardSmsPut(
        @Url url: String,
        @HeaderMap headers: Map<String, String>,
        @retrofit2.http.Body payload: SmsWebhookPayload
    ): Response<ResponseBody>

    @PATCH
    suspend fun forwardSmsPatch(
        @Url url: String,
        @HeaderMap headers: Map<String, String>,
        @retrofit2.http.Body payload: SmsWebhookPayload
    ): Response<ResponseBody>

    @HTTP(method = "DELETE", hasBody = false)
    suspend fun deleteSms(
        @Url url: String,
        @HeaderMap headers: Map<String, String>
    ): Response<ResponseBody>
}
