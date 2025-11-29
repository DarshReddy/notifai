package com.notifa.ai.ai

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface HuggingFaceApi {

    @POST("models/facebook/bart-large-cnn")
    suspend fun getSummary(
        @Header("Authorization") apiKey: String,
        @Body request: SummaryRequest
    ): List<SummaryResponse>

    data class SummaryRequest(
        val inputs: String
    )

    data class SummaryResponse(
        val summary_text: String
    )
}
