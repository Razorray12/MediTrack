package com.example.meditrack.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import com.example.meditrack.R
import com.example.meditrack.activities.MainActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddPatientsFragment : Fragment() {

    private lateinit var addLayout: LinearLayout
    private lateinit var addScrollView: NestedScrollView

    private var firstName: EditText? = null
    private var lastName: EditText? = null
    private var middleName: EditText? = null
    private var birthDate: EditText? = null
    private var phoneNumber: EditText? = null
    private var diagnosis: EditText? = null
    private var room: EditText? = null
    private lateinit var isMale: CheckBox
    private lateinit var isFemale: CheckBox
    private var medications: EditText? = null
    private var allergies: EditText? = null
    private var temperature: EditText? = null
    private var heartRate: EditText? = null
    private var respiratoryRate: EditText? = null
    private var bloodPressure: EditText? = null
    private var oxygenSaturation: EditText? = null
    private var bloodGlucose: EditText? = null

    private var isAlreadyShown = false

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.fragment_add_patients, container, false)

        addLayout = rootView.findViewById(R.id.linear_add)
        addScrollView = rootView.findViewById(R.id.add_scroll)

        val hideKeyboardListener = View.OnClickListener { view ->
            view.clearFocus()
            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
        addScrollView.setOnClickListener(hideKeyboardListener)
        addLayout.setOnClickListener(hideKeyboardListener)

        firstName = rootView.findViewById(R.id.edit_first_name)
        lastName = rootView.findViewById(R.id.edit_last_name)
        middleName = rootView.findViewById(R.id.edit_middle_name)
        birthDate = rootView.findViewById(R.id.edit_birthDate)
        phoneNumber = rootView.findViewById(R.id.edit_phone)
        diagnosis = rootView.findViewById(R.id.edit_diagnosis)
        room = rootView.findViewById(R.id.edit_room)
        isMale = rootView.findViewById(R.id.checkBoxMale)
        isFemale = rootView.findViewById(R.id.checkBoxFemale)
        medications = rootView.findViewById(R.id.edit_medications)
        allergies = rootView.findViewById(R.id.edit_allergies)
        temperature = rootView.findViewById(R.id.edit_vitalsigns)
        heartRate = rootView.findViewById(R.id.edit_vitalsigns1)
        respiratoryRate = rootView.findViewById(R.id.edit_vitalsigns2)
        bloodPressure = rootView.findViewById(R.id.edit_vitalsigns3)
        oxygenSaturation = rootView.findViewById(R.id.edit_vitalsigns4)
        bloodGlucose = rootView.findViewById(R.id.edit_vitalsigns5)

        val buttonShowSigns = rootView.findViewById<ImageButton>(R.id.button_plus_signs)
        buttonShowSigns.setOnClickListener(object : View.OnClickListener {
            var isRotated = false
            override fun onClick(v: View) {
                val linearLayout = rootView.findViewById<LinearLayout>(R.id.linear_layout)
                val rotate: ObjectAnimator
                if (!isRotated) {
                    rotate = ObjectAnimator.ofFloat(buttonShowSigns, "rotation", 0f, 180f)
                    isRotated = true
                    val fadeIn = ObjectAnimator.ofFloat(linearLayout, "alpha", 0f, 1f).setDuration(300)
                    val animatorSet = AnimatorSet()
                    animatorSet.playTogether(fadeIn)
                    animatorSet.start()

                    linearLayout.visibility = View.VISIBLE
                    addScrollView.post { addScrollView.fullScroll(View.FOCUS_DOWN) }
                } else {
                    rotate = ObjectAnimator.ofFloat(buttonShowSigns, "rotation", 180f, 0f)
                    isRotated = false
                    val fadeOut = ObjectAnimator.ofFloat(linearLayout, "alpha", 1f, 0f).setDuration(300)
                    val animatorSet = AnimatorSet()
                    animatorSet.playTogether(fadeOut)
                    animatorSet.start()

                    animatorSet.addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            linearLayout.visibility = View.GONE
                        }
                    })
                }
                rotate.setDuration(300)
                rotate.start()
            }
        })

        isMale.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) isFemale.isChecked = false
        }
        isFemale.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) isMale.isChecked = false
        }

        return rootView
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_close_edit1) {
            (requireActivity() as MainActivity).closeSaveButton1()
            (requireActivity() as MainActivity).setTextForSearch()
            (requireActivity() as MainActivity).showSearchView()
            (requireActivity() as MainActivity).closeFragmentAdd()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()

        (requireActivity() as MainActivity).setToolbarSaveButtonListener1 {
            savePatient()
        }
    }

    private fun savePatient() {
        val prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val sFirstName = firstName?.text.toString().trim()
        val sLastName = lastName?.text.toString().trim()
        val sMiddleName = middleName?.text.toString().trim()
        val sBirthDate = birthDate?.text.toString().trim()
        val sPhoneNumber = phoneNumber?.text.toString().trim()
        val sDiagnosis = diagnosis?.text.toString().trim()
        val sRoom = room?.text.toString().trim()
        val sMedications = medications?.text.toString().trim()
        val sAllergies = allergies?.text.toString().trim()
        val sTemperature = temperature?.text.toString().trim()
        val sHeartRate = heartRate?.text.toString().trim()
        val sRespiratoryRate = respiratoryRate?.text.toString().trim()
        val sBloodPressure = bloodPressure?.text.toString().trim()
        val sOxygenSaturation = oxygenSaturation?.text.toString().trim()
        val sBloodGlucose = bloodGlucose?.text.toString().trim()
        val mainDoctor = prefs.getString("fio",null)
        val mainDoctorID = prefs.getString("user_id",null)

        if (sFirstName.isEmpty() || sMiddleName.isEmpty() || sLastName.isEmpty() || sRoom.isEmpty()) {
            if (!isAlreadyShown) {
                Toast.makeText(requireContext(), "Заполните все необходимые поля", Toast.LENGTH_SHORT).show()
                Handler(Looper.getMainLooper()).postDelayed({ isAlreadyShown = false }, 3000)
                isAlreadyShown = true
            }
            return
        }

        val sexString = when {
            isMale.isChecked -> "Мужчина"
            isFemale.isChecked -> "Женщина"
            else -> {
                if (!isAlreadyShown) {
                    Toast.makeText(requireContext(), "Выберите пол пациента!", Toast.LENGTH_SHORT).show()
                    Handler(Looper.getMainLooper()).postDelayed({ isAlreadyShown = false }, 3000)
                    isAlreadyShown = true
                }
                return
            }
        }

        @SuppressLint("SimpleDateFormat")
        val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
        val admissionDateStr = dateFormat.format(Date())

        val vitalsObject = JSONObject().apply {
            put("temperature", sTemperature)
            put("heartRate", sHeartRate)
            put("respiratoryRate", sRespiratoryRate)
            put("bloodPressure", sBloodPressure)
            put("oxygenSaturation", sOxygenSaturation)
            put("bloodGlucose", sBloodGlucose)
        }

        val patientObject = JSONObject().apply {
            put("firstName", sFirstName)
            put("lastName", sLastName)
            put("middleName", sMiddleName)
            put("sex", sexString)
            put("birthDate", sBirthDate)
            put("phoneNumber", sPhoneNumber)
            put("diagnosis", sDiagnosis)
            put("room", sRoom)
            put("medications", sMedications)
            put("allergies", sAllergies)
            put("admissionDate", admissionDateStr)
            put("mainDoctor", mainDoctor)
            put("mainDoctorID", mainDoctorID)
            put("vitalSigns", vitalsObject)
        }

        val token = prefs.getString("jwt_token", null) ?: ""
        if (token.isEmpty()) {
            Toast.makeText(requireContext(), "Нет токена (jwt_token). Авторизуйтесь заново.", Toast.LENGTH_SHORT).show()
            return
        }

        val baseUrl = "http://192.168.0.159:8080"
        val url = "$baseUrl/patients"

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val requestBody = patientObject.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Ошибка сохранения: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        requireActivity().runOnUiThread {
                            Toast.makeText(
                                requireContext(),
                                "Ошибка сохранения: код ${response.code}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        requireActivity().runOnUiThread {
                            Toast.makeText(
                                requireContext(),
                                "Пациент добавлен!",
                                Toast.LENGTH_SHORT
                            ).show()

                            (requireActivity() as MainActivity).closeFragmentAdd()
                            (requireActivity() as MainActivity).showSearchView()
                            (requireActivity() as MainActivity).setTextForSearch()
                            (requireActivity() as MainActivity).closeSaveButton1()
                        }
                    }
                }
            }
        })
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {
            firstName?.setText("")
            lastName?.setText("")
            middleName?.setText("")
            birthDate?.setText("")
            phoneNumber?.setText("")
            diagnosis?.setText("")
            room?.setText("")
            medications?.setText("")
            allergies?.setText("")
            temperature?.setText("")
            heartRate?.setText("")
            respiratoryRate?.setText("")
            bloodPressure?.setText("")
            oxygenSaturation?.setText("")
            bloodGlucose?.setText("")
            (requireActivity() as MainActivity).closeSaveButton1()

            isMale.isChecked = false
            isFemale.isChecked = false

            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(requireView().windowToken, 0)
        }
    }
}
