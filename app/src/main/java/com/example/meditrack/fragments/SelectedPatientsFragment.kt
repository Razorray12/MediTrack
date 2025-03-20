package com.example.meditrack.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.meditrack.R
import com.example.meditrack.adapters.SelectedPatientAdapter
import com.example.meditrack.entities.Patient
import com.example.meditrack.entities.VitalSigns
import com.example.meditrack.viewmodels.PatientViewModel
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class SelectedPatientsFragment : Fragment() {

    interface OnFragmentSwitchListener {
        fun onSwitchToInformationFragment()
    }
    private var fragmentSwitchListener: OnFragmentSwitchListener? = null
    fun setOnFragmentSwitchListener(listener: OnFragmentSwitchListener?) {
        fragmentSwitchListener = listener
    }

    private lateinit var recyclerView: RecyclerView
    private var patientAdapter: SelectedPatientAdapter? = null

    private val patients: ArrayList<Patient> = ArrayList()

    private var patientUpdatesWebSocket: WebSocket? = null

    private val client = OkHttpClient()

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_selected_patients, container, false)

        recyclerView = view.findViewById(R.id.recyclerview_patient2)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)

        patientAdapter = SelectedPatientAdapter(requireContext(), patients)
        recyclerView.adapter = patientAdapter

        patientAdapter?.setOnItemClickListener(object : SelectedPatientAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {

                val nextIndex = position + 1
                if (nextIndex in patients.indices) {
                    val clickedPatient = patients[nextIndex]

                    val patientViewModel = ViewModelProvider(requireActivity())[PatientViewModel::class.java]
                    patientViewModel.selectPatient(clickedPatient)

                    fragmentSwitchListener?.onSwitchToInformationFragment()
                }
            }
        })

        loadPatientsFromMongoDB()

        return view
    }

    override fun onResume() {
        super.onResume()

        if (patientUpdatesWebSocket == null) {
            connectPatientUpdates()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        patientUpdatesWebSocket?.close(1000, "Fragment paused")
        patientUpdatesWebSocket = null
    }

    private fun loadPatientsFromMongoDB() {
        val prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val currentDoctorID = prefs.getString("user_id", "") ?: ""

        val url = "http://192.168.0.159:8080/patients"
        val request = Request.Builder().url(url).get().build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) return

                    val bodyStr = response.body?.string().orEmpty()
                    if (bodyStr.isNotEmpty()) {
                        val tempList = ArrayList<Patient>()
                        try {
                            val jsonArray = JSONArray(bodyStr)
                            for (i in 0 until jsonArray.length()) {
                                val obj = jsonArray.getJSONObject(i)
                                val parsedPatient = parsePatient(obj)

                                if (parsedPatient.mainDoctorID == currentDoctorID) {
                                    tempList.add(parsedPatient)
                                }
                            }
                        } catch (_: Exception) {}

                        activity?.runOnUiThread {
                            patients.clear()
                            patients.addAll(tempList)

                            patientAdapter?.setRecyclerPatients(patients)
                        }
                    }
                }
            }
        })
    }

    private fun connectPatientUpdates() {
        val prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val currentDoctorID = prefs.getString("user_id", "") ?: ""
        val token = prefs.getString("jwt_token", null)
        if (token.isNullOrEmpty()) return

        val wsUrl = "ws://192.168.0.159:8080/patients/updates"
        val request = Request.Builder()
            .url(wsUrl)
            .addHeader("Authorization", "Bearer $token")
            .build()

        patientUpdatesWebSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {

            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onMessage(webSocket: WebSocket, text: String) {
                activity?.runOnUiThread {
                    try {
                        val obj = JSONObject(text)
                        val action = obj.optString("action", "")

                        if (action == "delete") {
                            val patientId = obj.optString("id", "")
                            if (patientId.isNotEmpty()) {

                                val index = patients.indexOfFirst { it.id == patientId }
                                if (index != -1) {
                                    patients.removeAt(index)
                                    patientAdapter?.setRecyclerPatients(ArrayList(patients))
                                    patientAdapter?.notifyItemRemoved(index)
                                }
                            }
                        } else {

                            val updatedPatient = parsePatient(obj)

                            if (updatedPatient.mainDoctorID != currentDoctorID) {
                                val idx = patients.indexOfFirst { it.id == updatedPatient.id }

                                if (idx != -1) {
                                    patients.removeAt(idx)
                                    patientAdapter?.setRecyclerPatients(ArrayList(patients))
                                    patientAdapter?.notifyItemRemoved(idx)
                                }
                                return@runOnUiThread
                            }

                            val index = patients.indexOfFirst { it.id == updatedPatient.id }
                            if (index >= 0) {

                                patients[index] = updatedPatient
                            } else {

                                patients.add(updatedPatient)
                            }

                            patientAdapter?.setRecyclerPatients(ArrayList(patients))
                            patientAdapter?.notifyDataSetChanged()
                        }

                    } catch (e: Exception) {

                        loadPatientsFromMongoDB()
                    }
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {

            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
            }
        })
    }

    private fun parsePatient(obj: JSONObject): Patient {
        val vsObj = obj.optJSONObject("vitalSigns")
        val vitalSigns = vsObj?.let { vs ->
            VitalSigns(
                temperature = vs.optString("temperature", ""),
                heartRate = vs.optString("heartRate", ""),
                respiratoryRate = vs.optString("respiratoryRate", ""),
                bloodPressure = vs.optString("bloodPressure", ""),
                oxygenSaturation = vs.optString("oxygenSaturation", ""),
                bloodGlucose = vs.optString("bloodGlucose", "")
            )
        }
        return Patient(
            id = obj.optString("id", ""),
            firstName = obj.optString("firstName", ""),
            lastName = obj.optString("lastName", ""),
            middleName = obj.optString("middleName", ""),
            sex = obj.optString("sex", ""),
            birthDate = obj.optString("birthDate", ""),
            phoneNumber = obj.optString("phoneNumber", ""),
            diagnosis = obj.optString("diagnosis", ""),
            room = obj.optString("room", ""),
            medications = obj.optString("medications", ""),
            allergies = obj.optString("allergies", ""),
            admissionDate = obj.optString("admissionDate", ""),
            mainDoctor = obj.optString("mainDoctor", ""),
            mainDoctorID = obj.optString("mainDoctorID", ""),
            vitalSigns = vitalSigns
        )
    }
}
