package com.example.meditrack.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.meditrack.R
import com.example.meditrack.ai.DiseaseRequest
import com.example.meditrack.ai.DiseaseResponse
import com.example.meditrack.ai.Message
import com.example.meditrack.ai.MistralApiService
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AIFragment : Fragment() {

    private lateinit var mistralApiService: MistralApiService
    private lateinit var scrollView: ScrollView
    private lateinit var scrollView2: ScrollView
    private lateinit var linerAI: LinearLayout
    private lateinit var diseaseNameView: TextView
    private lateinit var diseasesSymptomsView: TextView
    private lateinit var medicationsNameView: TextView
    private lateinit var symptopmsNameView: TextView
    private lateinit var descriptionNameView: TextView
    private lateinit var constraintProgressBar: ConstraintLayout
    private lateinit var symptomsButton: RadioButton
    private var isSymptom = false
    private lateinit var diseaseButton: RadioButton
    private lateinit var gson: Gson

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val apiKey = "yJ2EuRaojKn7nhRrSPmWPWR6YNQAINVY"

        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()

                val requestBuilder = original.newBuilder()
                    .header("Authorization", "Bearer $apiKey")

                val request = requestBuilder.build()
                Log.d("AIFragment", "Request: $request")
                chain.proceed(request)
            }
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.mistral.ai/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        mistralApiService = retrofit.create(MistralApiService::class.java)
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_a_i, container, false)

        scrollView = view.findViewById(R.id.scroll_ai)
        scrollView2 = view.findViewById(R.id.scroll_ai_symptoms)
        linerAI = view.findViewById(R.id.linear_ai)

        diseaseNameView = scrollView.findViewById(R.id.diagnos_name_text)
        symptopmsNameView = scrollView.findViewById(R.id.symptopms_name_text)
        medicationsNameView = scrollView.findViewById(R.id.medications_name_text)
        descriptionNameView = scrollView.findViewById(R.id.description_name_text)
        diseasesSymptomsView = scrollView2.findViewById(R.id.diagnosis_text)
        diseaseButton = linerAI.findViewById(R.id.button_diagnosis)
        symptomsButton = linerAI.findViewById(R.id.button_symptoms)
        constraintProgressBar = linerAI.findViewById(R.id.constraint_progress)
        diseaseButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                symptomsButton.isChecked = false
                isSymptom = false
            }
        }

        symptomsButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                diseaseButton.isChecked = false
                isSymptom = true
            }
        }

        return view
    }

    private suspend fun fetchDiseaseInfo(diseaseName: String): DiseaseResponse? {
        gson = Gson()
        val messages = listOf(
            Message("user", "Дай ответ на русском языке в формате:" +
                    "diseaseName:$diseaseName" +
                    "drugsName:" +
                    "symptoms:" +
                    "description")
        )

        Log.d("AIFragment", "Message content: ${messages[0].content}")

        val request = DiseaseRequest("mistral-large-latest", messages, 1.0)
        val json = gson.toJson(request)
        val requestBody = RequestBody.create(MediaType.parse("application/json"), json)

        Log.d("AIFragment", "Request body: $json")

        return try {
            val response = mistralApiService.completeChat(requestBody)
            Log.d("AIFragment", "Response: ${gson.toJson(response)}")
            response
        } catch (e: HttpException) {
            Log.e("AIFragment", "HTTP error: ${e.code()} ${e.message()}")
            null
        } catch (e: Exception) {
            Log.e("AIFragment", "Ошибка извлечения данных.", e)
            null
        }
    }

    private suspend fun fetchSymptomsDiseasesInfo(symptoms: String): DiseaseResponse? {
        gson = Gson()
        val messages = listOf(
            Message("user", "Дай ответ на русском языке и напиши 5 возможных болезней с их описанием по указанным симптомам $symptoms.Не добавляй никаких дополнительных слов или объяснений, только JSON." +
                    "diseaseName: " +
                    "description: ")
        )

        Log.d("AIFragment", "Message content: ${messages[0].content}")

        val request = DiseaseRequest("mistral-large-latest", messages, 1.0)
        val json = gson.toJson(request)
        val requestBody = RequestBody.create(MediaType.parse("application/json"), json)

        Log.d("AIFragment", "Request body: $json")

        return try {
            val response = mistralApiService.completeChat(requestBody)
            Log.d("AIFragment", "Response: ${gson.toJson(response)}")
            response
        } catch (e: HttpException) {
            Log.e("AIFragment", "HTTP error: ${e.code()} ${e.message()}")
            null
        } catch (e: Exception) {
            Log.e("AIFragment", "Ошибка извлечения данных.", e)
            null
        }
    }

    private fun displayDiseaseSymptomsInfo(chatResponse: DiseaseResponse?) {
        val firstChoice = chatResponse?.choices?.get(0)
        val message = firstChoice?.message
        if (message?.content != null) {
            val diseaseInfoList = parseDiseaseSymptomsInfo(message.content)
            val formattedInfo = StringBuilder()
            for ((index, diseaseInfo) in diseaseInfoList.withIndex()) {
                val diseaseName = diseaseInfo["diseaseName"] ?: "Название не найдено"
                val description = diseaseInfo["description"] ?: "Описание не найдено"
                formattedInfo.append("${index + 1}. Название: $diseaseName\nОписание: $description\n\n")
            }
            diseasesSymptomsView.text = formattedInfo.toString()
        } else {
            diseasesSymptomsView.text = "Информация не найдена."
        }
    }

    private fun displayDiseaseInfo(chatResponse: DiseaseResponse?) {
        val firstChoice = chatResponse?.choices?.get(0)
        val message = firstChoice?.message
        if (message?.content != null) {
            val diseaseInfo = parseDiseaseInfo(message.content)
            val drugName = diseaseInfo["drugsName"] ?: "Информация о лекарстве не найдена."
            val symptoms = diseaseInfo["symptoms"] ?: "Симптомы не найдены."
            val description = diseaseInfo["description"] ?: "Описание заболевания не найдено."

            diseaseNameView.text = diseaseInfo["diseaseName"]
            symptopmsNameView.text = symptoms
            medicationsNameView.text = drugName
            descriptionNameView.text = description
        } else {
            diseaseNameView.text = "Дополнительная информация не найдена."
        }
    }

    private fun parseDiseaseInfo(content: String): Map<String, String> {
        val diseaseInfo = mutableMapOf<String, String>()
        val lines = content.split("\n")
        for (line in lines) {
            val parts = line.split(":", limit = 2)
            if (parts.size == 2) {
                diseaseInfo[parts[0].trim()] = parts[1].trim()
            }
        }
        return diseaseInfo
    }

    private fun parseDiseaseSymptomsInfo(content: String): List<Map<String, String>> {
        val jsonContent = content.substringAfter("```json").substringBeforeLast("\n```")

        val type = object : TypeToken<List<Map<String, String>>>() {}.type
        val diseaseInfoList: List<Map<String, String>> = gson.fromJson(jsonContent, type)

        return diseaseInfoList
    }

    @SuppressLint("SetTextI18n")
    private suspend fun fetchData(request: String, symptoms: Boolean) {
        constraintProgressBar.isVisible = true
        scrollView.isVisible = false
        scrollView2.isVisible = false
        lifecycleScope.launch {
            try {
                val chatResponse = if (symptoms) {
                    fetchSymptomsDiseasesInfo(request)
                } else {
                    fetchDiseaseInfo(request)
                }

                if (symptoms) {
                    displayDiseaseSymptomsInfo(chatResponse)
                } else {
                    displayDiseaseInfo(chatResponse)
                }

                constraintProgressBar.isVisible = false
                if (isSymptom) {
                    scrollView2.isVisible = true
                } else {
                    scrollView.isVisible = true
                }
            } catch (e: Exception) {
                diseaseNameView.text = "Ошибка: ${e.message}"
                constraintProgressBar.isVisible = false
            }
        }
    }

    fun onQueryTextSubmit(query: String) {
        if (!diseaseButton.isChecked && !symptomsButton.isChecked) {
            Toast.makeText(context, "Выберите фильтр поиска по заболеваниям!", Toast.LENGTH_SHORT).show()
        } else {
            lifecycleScope.launch {
                fetchData(query, isSymptom)
            }
        }
    }
}
