package com.example.meditrack.ai

import com.google.gson.annotations.SerializedName

data class DiseaseResponse(
    val id: String,
    val choices: List<Choice>,
    val created: Long,
    val model: String,
    val system_fingerprint: String,
    val Object: String,
    val usage: Usage
)