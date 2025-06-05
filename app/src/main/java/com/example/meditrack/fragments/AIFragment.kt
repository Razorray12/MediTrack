package com.example.meditrack.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListPopupWindow
import android.widget.RadioButton
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.meditrack.R
import com.example.meditrack.adapters.HistoryAdapter
import com.example.meditrack.ai.AiSearchHistoryManager
import com.example.meditrack.ai.DiseaseRequest
import com.example.meditrack.ai.DiseaseResponse
import com.example.meditrack.ai.Message
import com.example.meditrack.ai.MistralApiService
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
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

    private lateinit var refreshButton: Button
    private lateinit var refreshLayout: LinearLayout
    private var lastQuery: String = ""

    private lateinit var historyManager: AiSearchHistoryManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val apiKey = ""

        historyManager = AiSearchHistoryManager(requireContext())

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

        refreshLayout = view.findViewById(R.id.layoutRefresh)
        refreshButton = view.findViewById(R.id.buttonRefresh)
        refreshButton.setOnClickListener {
            if (lastQuery.isNotEmpty()) {
                onQueryTextSubmit(lastQuery)
            }
        }

        return view
    }

    @SuppressLint("SetTextI18n")
    private fun fetchData(request: String, symptoms: Boolean) {
        lastQuery = request
        constraintProgressBar.isVisible = true
        scrollView.isVisible = false
        scrollView2.isVisible = false
        refreshLayout.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val chatResponse = if (symptoms) {
                    fetchSymptomsDiseasesInfo(request)
                } else {
                    fetchDiseaseInfo(request)
                }

                if (chatResponse == null ||
                    chatResponse.choices.isEmpty() ||
                    chatResponse.choices[0].message.content.isNullOrEmpty()
                ) {
                    diseaseNameView.text = "Информация не найдена."
                    diseasesSymptomsView.text = "Информация не найдена."
                    refreshLayout.visibility = View.VISIBLE
                } else {
                    if (symptoms) {
                        val diseaseInfoList = parseDiseaseSymptomsInfo(chatResponse.choices[0].message.content.toString())
                        if (diseaseInfoList.isEmpty()) {
                            diseasesSymptomsView.text = "Информация не найдена."
                            refreshLayout.visibility = View.VISIBLE
                        } else {
                            refreshLayout.visibility = View.GONE
                            displayDiseaseSymptomsInfo(chatResponse)
                        }
                    } else {
                        val diseaseInfo = parseDiseaseInfo(chatResponse.choices[0].message.content.toString())
                        if (diseaseInfo["diseaseName"]?.toString()?.isEmpty() != false) {
                            diseaseNameView.text = "Информация не найдена."
                            refreshLayout.visibility = View.VISIBLE
                        } else {
                            refreshLayout.visibility = View.GONE
                            displayDiseaseInfo(chatResponse)
                        }
                    }
                }
                constraintProgressBar.isVisible = false
                if (isSymptom) {
                    scrollView2.isVisible = true
                } else {
                    scrollView.isVisible = true
                }
            } catch (e: Exception) {
                Log.e("AIFragment", "Ошибка извлечения данных", e)
                diseaseNameView.text = "Информация не найдена."
                constraintProgressBar.isVisible = false
                refreshLayout.visibility = View.VISIBLE
            }
        }
    }

    private suspend fun fetchDiseaseInfo(diseaseName: String): DiseaseResponse? {
        gson = Gson()
        val messages = listOf(
            Message("user", "Не добавляй никаких дополнительных слов или объяснений, только JSON. Дай ответ на русском языке в формате:" +
                    "diseaseName:$diseaseName" +
                    "drugsName:" +
                    "symptoms:" +
                    "description")
        )

        Log.d("AIFragment", "Message content: ${messages[0].content}")
        val request = DiseaseRequest("mistral-large-latest", messages, 1.0)
        val json = gson.toJson(request)
        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())

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
        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())

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
            val drugName = (diseaseInfo["drugsName"] as? List<*>)?.takeIf { it.isNotEmpty() }?.joinToString(", ") ?: "Информация о лекарстве не найдена."
            val symptoms = (diseaseInfo["symptoms"] as? List<*>)?.takeIf { it.isNotEmpty() }?.joinToString(", ") ?: "Симптомы не найдены."
            val description = diseaseInfo["description"] as? String ?: "Описание заболевания не найдено."
            val diseaseName = diseaseInfo["diseaseName"] as? String ?: "Название заболевания не найдено."

            diseaseNameView.text = diseaseName
            symptopmsNameView.text = symptoms
            medicationsNameView.text = drugName
            descriptionNameView.text = description
        } else {
            diseaseNameView.text = "Дополнительная информация не найдена."
        }
    }

    private fun parseDiseaseInfo(content: String): Map<String, Any> {
        val jsonContent = content.substringAfter("```json").substringBeforeLast("```")
        val type = object : com.google.gson.reflect.TypeToken<Map<String, Any>>() {}.type
        return gson.fromJson(jsonContent, type)
    }

    private fun parseDiseaseSymptomsInfo(content: String): List<Map<String, String>> {
        val jsonContent = content.substringAfter("```json").substringBeforeLast("```").trim()
        return try {
            val jsonElement = JsonParser.parseString(jsonContent)
            if (jsonElement.isJsonObject) {
                val diseaseMap: Map<String, List<Map<String, String>>> =
                    Gson().fromJson(jsonElement, object : com.google.gson.reflect.TypeToken<Map<String, List<Map<String, String>>>>() {}.type)
                diseaseMap["diseases"] ?: diseaseMap["disease"] ?: emptyList()
            } else if (jsonElement.isJsonArray) {
                Gson().fromJson(jsonElement, object : com.google.gson.reflect.TypeToken<List<Map<String, String>>>() {}.type)
            } else {
                emptyList()
            }
        } catch (e: JsonSyntaxException) {
            emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun onQueryTextSubmit(query: String) {
        if (!diseaseButton.isChecked && !symptomsButton.isChecked) {
            Toast.makeText(context, "Выберите фильтр поиска по заболеваниям!", Toast.LENGTH_SHORT).show()
        } else {
            historyManager.addQuery(query)
            lifecycleScope.launch {
                fetchData(query, isSymptom)
            }
        }
    }

    fun showHistoryDropdown(anchor: View) {
        val historyList = historyManager.getHistory()
        if (historyList.isEmpty()) return

        val adapter = HistoryAdapter(requireContext(), historyList)
        val listPopupWindow = ListPopupWindow(requireContext()).apply {
            anchorView = anchor
            setAdapter(adapter)
            isModal = false
        }

        anchor.post {
            val screenWidth = resources.displayMetrics.widthPixels
            val fixedWidth = (screenWidth * 0.9).toInt()

            listPopupWindow.width = fixedWidth

            listPopupWindow.setOnItemClickListener { _, _, position, _ ->
                if (position < historyList.size) {

                    val selectedQuery = historyList[position]
                    onQueryTextSubmit(selectedQuery)
                } else {

                    historyManager.clearHistory()
                    Toast.makeText(requireContext(), "История очищена", Toast.LENGTH_SHORT).show()
                }
                listPopupWindow.dismiss()
            }

            listPopupWindow.show()
        }
    }
}
