package com.example.meditrack.ai

import com.google.gson.annotations.SerializedName

data class DiseaseRequest(
    val model: String,
    val messages: List<Message>,
    val temperature: Double
)
