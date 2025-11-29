package com.notifa.ai.ai

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HuggingFaceSummaryProvider(private val apiKey: String) : SummaryProvider {

    private val api: HuggingFaceApi

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api-inference.huggingface.co/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        api = retrofit.create(HuggingFaceApi::class.java)
    }

    override suspend fun getSummary(notifications: List<String>): String {
        return try {
            val inputText = notifications.joinToString(separator = "\n")
            val request = HuggingFaceApi.SummaryRequest(inputs = inputText)
            val response = api.getSummary("Bearer $apiKey", request)
            response.firstOrNull()?.summary_text ?: "No summary available."
        } catch (e: Exception) {
            "Error generating summary: ${e.message}"
        }
    }
}
