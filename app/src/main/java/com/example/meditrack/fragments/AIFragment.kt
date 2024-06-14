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
import com.example.meditrack.ai.DeepSeekApiService
import com.example.meditrack.ai.DiseaseRequest
import com.example.meditrack.ai.DiseaseResponse
import com.example.meditrack.ai.Message
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class AIFragment : Fragment() {

    private lateinit var deepSeekApiService: DeepSeekApiService
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

        val apiKey = "sk-2d9c861e51ab41fba84f87ede0f157ff"

         val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()

                val requestBuilder = original.newBuilder()
                    .header("Authorization", "Bearer $apiKey")

                val request = requestBuilder.build()
                chain.proceed(request)
            }
             .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()


        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.deepseek.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        deepSeekApiService = retrofit.create(DeepSeekApiService::class.java)
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
        val request = DiseaseRequest("deepseek-chat", messages, 1.0)
        val json = gson.toJson(request)
        val requestBody = RequestBody.create(MediaType.parse("application/json"), json)

        return try {
            deepSeekApiService.predictDisease(requestBody)
        } catch (e: Exception) {
            Log.e("AIFragment", "Ошибка извлечения данных.", e)
            null
        }
    }

    private suspend fun fetchSymptomsDiseasesInfo(symptoms: String): DiseaseResponse? {
        gson = Gson()
        val messages = listOf(
            Message("user", "Дай ответ на русском языке и напиши 5 возможных болезней с их описанием по указанным симптомам $symptoms в формате json" +
                    "diseaseName: " +
                    "description: ")
        )
        val request = DiseaseRequest("deepseek-chat", messages, 1.0)
        val json = gson.toJson(request)
        val requestBody = RequestBody.create(MediaType.parse("application/json"), json)

        return try {
            deepSeekApiService.predictDisease(requestBody)
        } catch (e: Exception) {
            Log.e("AIFragment", "Ошибка извлечения данных.", e)
            null
        }
    }

    private fun displayDiseaseSymptomsInfo(diseaseResponse: DiseaseResponse?) {
        val firstChoice = diseaseResponse?.choices?.get(0)
        val message = firstChoice?.message
        //Log.d("DiseaseInfo", message?.content.toString())
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

    private fun displayDiseaseInfo(diseaseResponse: DiseaseResponse?) {
        val firstChoice = diseaseResponse?.choices?.get(0)
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
        val jsonContent = content.trim().removePrefix("```json\n").removeSuffix("\n```")

        val type = object : TypeToken<List<Map<String, String>>>() {}.type
        val diseaseInfoList: List<Map<String, String>> = gson.fromJson(jsonContent, type)

        //Log.d("Parsed JSON", diseaseInfoList.toString())

        return diseaseInfoList
    }




    @SuppressLint("SetTextI18n")
    private suspend fun fetchData(request: String, symptoms: Boolean) {
        constraintProgressBar.isVisible = true
        scrollView.isVisible = false
        scrollView2.isVisible = false
        lifecycleScope.launch {
            try {
                val diseaseResponse = if (symptoms) {
                    fetchSymptomsDiseasesInfo(request)
                } else {
                    fetchDiseaseInfo(request)
                }
                //val jsonResponse = gson.toJson(diseaseResponse)

                //Log.d("JSON Response", jsonResponse)
                if (symptoms) {
                    displayDiseaseSymptomsInfo(diseaseResponse)
                } else {
                    displayDiseaseInfo(diseaseResponse)
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