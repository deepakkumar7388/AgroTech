package com.agrotech.ai.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: String,
    val name: String,
    val email: String,
    val farmSize: String? = null,
    val location: String? = null
)

data class AuthResponse(
    val success: Boolean,
    val token: String? = null,
    val user: User? = null,
    val error: String? = null
)

data class WeatherData(
    val temperature: Double? = null,
    val humidity: Double? = null,
    val condition: String? = null,
    val windSpeed: Double? = null,
    val iconUrl: String? = null
)

data class SoilData(
    val moisture: Double,
    @SerializedName("n") val nitrogen: Float,
    @SerializedName("p") val phosphorus: Float,
    @SerializedName("k") val potassium: Float,
    @SerializedName("ph") val ph: Float,
    @SerializedName("temp") val temperature: Float = 25.0f,
    val humidity: Float = 70.0f,
    val rainfall: Float = 100.0f
)

data class RecommendationResponse(
    val recommendation: String,
    val confidence: Float? = null,
    val details: String? = null
)

data class StressDetectionResponse(
    val label: String,
    val confidence: Float,
    val treatment: String
)

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
