package com.example.meditrack.ai

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface DeepSeekApiService {
    @POST("chat/completions")
    suspend fun predictDisease(@Body requestBody: RequestBody): DiseaseResponse
}
