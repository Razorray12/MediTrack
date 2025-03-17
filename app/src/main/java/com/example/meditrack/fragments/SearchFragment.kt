package com.example.meditrack.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.meditrack.R
import com.example.meditrack.adapters.PatientAdapter
import com.example.meditrack.entities.Patient
import com.example.meditrack.viewmodels.DataLoadedViewModel
import com.example.meditrack.viewmodels.PatientViewModel
import com.facebook.shimmer.ShimmerFrameLayout
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.Locale

class SearchFragment : Fragment() {

    interface OnFragmentSwitchListener {
        fun onSwitchToInformationFragment()
    }

    private var fragmentSwitchListener: OnFragmentSwitchListener? = null
    fun setOnFragmentSwitchListener(listener: OnFragmentSwitchListener?) {
        this.fragmentSwitchListener = listener
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewModel: DataLoadedViewModel

    private var patientAdapter: PatientAdapter? = null
    private var patients = ArrayList<Patient>()

    private var shimmerView1: ShimmerFrameLayout? = null
    private var shimmerView2: ShimmerFrameLayout? = null
    private var shimmerView3: ShimmerFrameLayout? = null
    private var shimmerView4: ShimmerFrameLayout? = null
    private var shimmerView5: ShimmerFrameLayout? = null

    private val client = OkHttpClient()
    private var patientUpdatesWebSocket: WebSocket? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.fragment_search, container, false)

        viewModel = ViewModelProvider(this)[DataLoadedViewModel::class.java]

        recyclerView = rootView.findViewById(R.id.recyclerview_patient)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)

        patientAdapter = PatientAdapter(requireContext(), patients).apply {
            setOnItemClickListener(object : PatientAdapter.OnItemClickListener {
                override fun onItemClick(position: Int) {
                    fragmentSwitchListener?.let {
                        val clickedPatient: Patient = patients[position]
                        val patientViewModel: PatientViewModel =
                            ViewModelProvider(requireActivity())[PatientViewModel::class.java]
                        patientViewModel.selectPatient(clickedPatient)
                        it.onSwitchToInformationFragment()
                    }
                }
            })
        }
        recyclerView.adapter = patientAdapter

        loadPatientsFromServer(rootView)

        return rootView
    }

    override fun onResume() {
        super.onResume()
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        loadPatientsFromServer(requireView())
        connectPatientUpdates()
    }

    override fun onPause() {
        super.onPause()
        patientUpdatesWebSocket?.close(1000, "Fragment paused")
        patientUpdatesWebSocket = null
    }

    private fun loadPatientsFromServer(rootView: View) {
        val url = "http://192.168.0.159:8080/patients"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        return
                    }
                    val bodyStr = response.body?.string()
                    if (bodyStr.isNullOrEmpty()) {
                        return
                    }

                    val newPatients = ArrayList<Patient>()
                    val jsonArray = JSONArray(bodyStr)
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val patient = parsePatient(obj)
                        newPatients.add(patient)
                    }

                    requireActivity().runOnUiThread {
                        patients = newPatients
                        patientAdapter?.setRecyclerPatients(newPatients)
                        patientAdapter?.notifyDataSetChanged()

                        if (viewModel.isDataLoaded.value == true) {
                            stopShimmer(rootView)
                        } else {
                            Handler(Looper.getMainLooper()).postDelayed({
                                stopShimmer(rootView)
                                viewModel.isDataLoaded.value = true
                            }, 2000)
                        }
                    }
                }
            }
        })
    }

    private fun connectPatientUpdates() {
        val prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val token = prefs.getString("jwt_token", null)
        if (token.isNullOrEmpty()) {
            return
        }

        val wsUrl = "ws://192.168.0.159:8080/patients/updates"
        val request = Request.Builder()
            .url(wsUrl)
            .addHeader("Authorization", "Bearer $token")
            .build()

        patientUpdatesWebSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                requireActivity().runOnUiThread {
                    try {
                        val obj = JSONObject(text)
                        val newPatient = parsePatient(obj)
                        val index = patients.indexOfFirst { it.id == newPatient.id }
                        if (index >= 0) {
                            patients[index] = newPatient
                            patientAdapter?.notifyItemChanged(index)
                        } else {
                            patients.add(0, newPatient)
                            patientAdapter?.notifyItemInserted(0)
                            recyclerView.scrollToPosition(0)
                        }
                    } catch (e: Exception) {
                        loadPatientsFromServer(requireView())
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
        val id = obj.optString("id", null.toString())
        val firstName = obj.optString("firstName", "")
        val lastName = obj.optString("lastName", "")
        val middleName = obj.optString("middleName", null.toString())
        val sex = obj.optString("sex", null.toString())
        val birthDate = obj.optString("birthDate", null.toString())
        val phoneNumber = obj.optString("phoneNumber", null.toString())
        val diagnosis = obj.optString("diagnosis", null.toString())
        val room = obj.optString("room", null.toString())
        val medications = obj.optString("medications", null.toString())
        val allergies = obj.optString("allergies", null.toString())
        val admissionDate = obj.optString("admissionDate", null.toString())
        val mainDoctor = obj.optString("mainDoctor", null.toString())

        val vitalSignsObj = obj.optJSONObject("vitalSigns")
        val vitalSigns = vitalSignsObj?.let {
            val temperature = it.optString("temperature", null.toString())
            val heartRate = it.optString("heartRate", null.toString())
            val respiratoryRate = it.optString("respiratoryRate", null.toString())
            val bloodPressure = it.optString("bloodPressure", null.toString())
            val oxygenSaturation = it.optString("oxygenSaturation", null.toString())
            val bloodGlucose = it.optString("bloodGlucose", null.toString())

            com.example.meditrack.entities.VitalSigns(
                temperature,
                heartRate,
                respiratoryRate,
                bloodPressure,
                oxygenSaturation,
                bloodGlucose
            )
        }

        return Patient(
            id = id,
            firstName = firstName,
            lastName = lastName,
            middleName = middleName,
            sex = sex,
            birthDate = birthDate,
            phoneNumber = phoneNumber,
            diagnosis = diagnosis,
            room = room,
            medications = medications,
            allergies = allergies,
            admissionDate = admissionDate,
            mainDoctor = mainDoctor,
            vitalSigns = vitalSigns
        )
    }

    private fun stopShimmer(view: View) {
        val shimmerLinearLayout = view.findViewById<LinearLayout>(R.id.shimmer_view_container)
        shimmerView1 = view.findViewById(R.id.shimmer_view_1)
        shimmerView2 = view.findViewById(R.id.shimmer_view_2)
        shimmerView3 = view.findViewById(R.id.shimmer_view_3)
        shimmerView4 = view.findViewById(R.id.shimmer_view_4)
        shimmerView5 = view.findViewById(R.id.shimmer_view_5)

        shimmerView1?.stopShimmer()
        shimmerView2?.stopShimmer()
        shimmerView3?.stopShimmer()
        shimmerView4?.stopShimmer()
        shimmerView5?.stopShimmer()

        shimmerLinearLayout.visibility = View.GONE
    }

    @SuppressLint("NotifyDataSetChanged")
    fun onQueryTextSubmit(query: String) {
        val filteredPatients = filterPatients(patients, query)
        patientAdapter?.setRecyclerPatients(filteredPatients)
        patientAdapter?.notifyDataSetChanged()
    }

    private fun filterPatients(patients: ArrayList<Patient>, query: String): ArrayList<Patient> {
        val filteredPatients = ArrayList<Patient>()
        val lowerQuery = query.lowercase(Locale.getDefault())

        for (patient in patients) {
            val first = patient.firstName!!.lowercase(Locale.getDefault())
            val last = patient.lastName!!.lowercase(Locale.getDefault())
            val mid = patient.middleName?.lowercase(Locale.getDefault()) ?: ""

            if (first.contains(lowerQuery) || last.contains(lowerQuery) || mid.contains(lowerQuery)) {
                filteredPatients.add(patient)
            }
        }
        return filteredPatients
    }

    @SuppressLint("NotifyDataSetChanged")
    fun showAllPatients() {
        patientAdapter?.setRecyclerPatients(patients)
        patientAdapter?.notifyDataSetChanged()
    }
}
