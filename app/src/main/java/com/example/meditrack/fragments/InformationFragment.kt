package com.example.meditrack.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.meditrack.R
import com.example.meditrack.activities.MainActivity
import com.example.meditrack.entities.Patient
import com.example.meditrack.entities.VitalSigns
import com.example.meditrack.viewmodels.PatientViewModel
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class InformationFragment : Fragment() {

    private lateinit var informationLayout: LinearLayout
    private lateinit var informantionScrollView: NestedScrollView
    private lateinit var linearLayout: LinearLayout
    private lateinit var rootView: View
    private var isRotated = false
    private lateinit var rotate: ObjectAnimator
    private lateinit var buttonShowSigns: ImageButton
    private lateinit var patientViewModel: PatientViewModel
    private lateinit var deletePatient: TextView

    private var isEditMode = false

    private lateinit var firstName: EditText
    private lateinit var lastName: EditText
    private lateinit var middleName: EditText
    private lateinit var birthDate: EditText
    private lateinit var phoneNumber: EditText
    private lateinit var diagnosis: EditText
    private lateinit var room: EditText
    private lateinit var medications: EditText
    private lateinit var allergies: EditText
    private lateinit var temperature: EditText
    private lateinit var heartRate: EditText
    private lateinit var respiratoryRate: EditText
    private lateinit var bloodPressure: EditText
    private lateinit var oxygenSaturation: EditText
    private lateinit var bloodGlucose: EditText
    private lateinit var isMale: CheckBox
    private lateinit var isFemale: CheckBox

    private lateinit var tfirstName: TextView
    private lateinit var tlastName: TextView
    private lateinit var tmiddleName: TextView
    private lateinit var tbirthDate: TextView
    private lateinit var tphoneNumber: TextView
    private lateinit var tdiagnosis: TextView
    private lateinit var troom: TextView
    private lateinit var tmedications: TextView
    private lateinit var tallergies: TextView
    private lateinit var ttemperature: TextView
    private lateinit var theartRate: TextView
    private lateinit var trespiratoryRate: TextView
    private lateinit var tbloodPressure: TextView
    private lateinit var toxygenSaturation: TextView
    private lateinit var tbloodGlucose: TextView

    private val client = OkHttpClient()
    private val baseUrl = "http://192.168.0.159:8080"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        isEditMode = savedInstanceState?.getBoolean("edit_mode_key", false) ?: false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.fragment_information, container, false)

        informationLayout = rootView.findViewById(R.id.linear_information)
        informantionScrollView = rootView.findViewById(R.id.information_scroll)

        informationLayout.setOnClickListener { view ->
            informationLayout.clearFocus()
            hideKeyboard(view)
        }
        informantionScrollView.setOnClickListener { view ->
            informantionScrollView.clearFocus()
            hideKeyboard(view)
        }

        deletePatient = rootView.findViewById(R.id.delete_patient)

        firstName = rootView.findViewById(R.id.edit_first_name)
        lastName = rootView.findViewById(R.id.edit_last_name)
        middleName = rootView.findViewById(R.id.edit_middle_name)
        birthDate = rootView.findViewById(R.id.edit_birthDate)
        phoneNumber = rootView.findViewById(R.id.edit_phone)
        diagnosis = rootView.findViewById(R.id.edit_diagnosis)
        room = rootView.findViewById(R.id.edit_room)
        medications = rootView.findViewById(R.id.edit_medications)
        allergies = rootView.findViewById(R.id.edit_allergies)
        temperature = rootView.findViewById(R.id.edit_vitalsigns)
        heartRate = rootView.findViewById(R.id.edit_vitalsigns1)
        respiratoryRate = rootView.findViewById(R.id.edit_vitalsigns2)
        bloodPressure = rootView.findViewById(R.id.edit_vitalsigns3)
        oxygenSaturation = rootView.findViewById(R.id.edit_vitalsigns4)
        bloodGlucose = rootView.findViewById(R.id.edit_vitalsigns5)
        isMale = rootView.findViewById(R.id.checkBoxMale)
        isFemale = rootView.findViewById(R.id.checkBoxFemale)

        tfirstName = rootView.findViewById(R.id.text_first_name)
        tlastName = rootView.findViewById(R.id.text_last_name)
        tmiddleName = rootView.findViewById(R.id.text_middle_name)
        tbirthDate = rootView.findViewById(R.id.text_birthdate)
        tphoneNumber = rootView.findViewById(R.id.text_phone_name)
        tdiagnosis = rootView.findViewById(R.id.text_diagnosis)
        troom = rootView.findViewById(R.id.text_room)
        tmedications = rootView.findViewById(R.id.text_medications)
        tallergies = rootView.findViewById(R.id.text_allergies)
        ttemperature = rootView.findViewById(R.id.text_temperature)
        theartRate = rootView.findViewById(R.id.text_chss)
        trespiratoryRate = rootView.findViewById(R.id.text_respiratory)
        tbloodPressure = rootView.findViewById(R.id.text_ad)
        toxygenSaturation = rootView.findViewById(R.id.text_saturation)
        tbloodGlucose = rootView.findViewById(R.id.text_glucose)

        buttonShowSigns = rootView.findViewById(R.id.button_plus_signs)
        buttonShowSigns.setOnClickListener {
            linearLayout = rootView.findViewById(R.id.linear_layout)
            toggleVitalSigns()
        }

        patientViewModel = ViewModelProvider(requireActivity())[PatientViewModel::class.java]
        patientViewModel.getSelectedPatient().observe(viewLifecycleOwner) { patient ->
            if (patient.sex == "Мужчина") {
                isMale.isChecked = true
                isFemale.isChecked = false
            } else {
                isFemale.isChecked = true
                isMale.isChecked = false
            }

            tfirstName.text = patient.firstName
            tlastName.text = patient.lastName
            tmiddleName.text = patient.middleName
            tbirthDate.text = patient.birthDate
            tphoneNumber.text = patient.phoneNumber
            tdiagnosis.text = patient.diagnosis
            troom.text = patient.room
            tmedications.text = patient.medications
            tallergies.text = patient.allergies

            ttemperature.text = patient.vitalSigns?.temperature
            theartRate.text = patient.vitalSigns?.heartRate
            trespiratoryRate.text = patient.vitalSigns?.respiratoryRate
            tbloodPressure.text = patient.vitalSigns?.bloodPressure
            toxygenSaturation.text = patient.vitalSigns?.oxygenSaturation
            tbloodGlucose.text = patient.vitalSigns?.bloodGlucose

            firstName.setText(patient.firstName)
            lastName.setText(patient.lastName)
            middleName.setText(patient.middleName)
            birthDate.setText(patient.birthDate)
            phoneNumber.setText(patient.phoneNumber)
            diagnosis.setText(patient.diagnosis)
            room.setText(patient.room)
            medications.setText(patient.medications)
            allergies.setText(patient.allergies)
            temperature.setText(patient.vitalSigns?.temperature)
            heartRate.setText(patient.vitalSigns?.heartRate)
            respiratoryRate.setText(patient.vitalSigns?.respiratoryRate)
            bloodPressure.setText(patient.vitalSigns?.bloodPressure)
            oxygenSaturation.setText(patient.vitalSigns?.oxygenSaturation)
            bloodGlucose.setText(patient.vitalSigns?.bloodGlucose)
        }

        deletePatient.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            val dialogView = inflater.inflate(R.layout.custom_alert_delete_patient, null)
            builder.setView(dialogView)
            val alertDialog = builder.create()

            val positiveButton = dialogView.findViewById<TextView>(R.id.positiveButtonDelete)
            val negativeButton = dialogView.findViewById<TextView>(R.id.negativeButtonDelete)
            val patient = patientViewModel.getSelectedPatient().value

            positiveButton.setOnClickListener {
                alertDialog.dismiss()
                if (patient?.id != null) {
                    deletePatientById(patient.id!!)
                }
            }
            negativeButton.setOnClickListener { alertDialog.dismiss() }

            alertDialog.show()
            alertDialog.window?.setBackgroundDrawableResource(R.drawable.alertdialog_background)
        }

        if (isEditMode) {
            switchToEditMode()
            (requireActivity() as MainActivity).closeBackButton()
            (requireActivity() as MainActivity).showCloseEditButton()
            (requireActivity() as MainActivity).showSaveButton2()
        } else {
            switchToViewMode()
        }

        return rootView
    }

    @Deprecated("Deprecated in Java")
    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val editItem = menu.findItem(R.id.action_edit)
        val closeItem1 = menu.findItem(R.id.action_close_edit1)
        if (isEditMode) {
            editItem?.isVisible = false
            closeItem1?.isVisible = true
        } else {
            editItem?.isVisible = true
            closeItem1?.isVisible = false
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_edit -> {
                isEditMode = true

                switchToEditMode()
                (requireActivity() as MainActivity).closeBackButton()

                item.isVisible = false

                (requireActivity() as MainActivity).showCloseEditButton()
                (requireActivity() as MainActivity).showSaveButton2()

                return true
            }
            R.id.action_close_edit1 -> {
                isEditMode = false

                switchToViewMode()

                (requireActivity() as MainActivity).closeBackButton()
                item.isVisible = false
                (requireActivity() as MainActivity).showCloseEditButton()

                return true
            }
            R.id.action_close_edit -> {
                isEditMode = false

                switchToViewMode()

                item.isVisible = false

                (requireActivity() as MainActivity).showBackButton()
                (requireActivity() as MainActivity).showEditButton()
                (requireActivity() as MainActivity).closeSaveButton2()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("edit_mode_key", isEditMode)
    }

    override fun onResume() {
        super.onResume()

        (requireActivity() as MainActivity).setToolbarSaveButtonListener2 {
            val originalPatient = patientViewModel.getSelectedPatient().value
                ?: return@setToolbarSaveButtonListener2

            val prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            val token = prefs.getString("jwt_token", null) ?: ""
            if (token.isEmpty()) {
                Toast.makeText(requireContext(), "Нет токена. Перезайдите в систему.", Toast.LENGTH_SHORT).show()
                return@setToolbarSaveButtonListener2
            }

            val sFirstName = firstName.text.toString().trim()
            val sLastName = lastName.text.toString().trim()
            val sMiddleName = middleName.text.toString().trim()
            val sBirthDate = birthDate.text.toString().trim()
            val sPhoneNumber = phoneNumber.text.toString().trim()
            val sDiagnosis = diagnosis.text.toString().trim()
            val sRoom = room.text.toString().trim()
            val sMedications = medications.text.toString().trim()
            val sAllergies = allergies.text.toString().trim()
            val sTemperature = temperature.text.toString().trim()
            val sHeartRate = heartRate.text.toString().trim()
            val sRespiratoryRate = respiratoryRate.text.toString().trim()
            val sBloodPressure = bloodPressure.text.toString().trim()
            val sOxygenSaturation = oxygenSaturation.text.toString().trim()
            val sBloodGlucose = bloodGlucose.text.toString().trim()

            val sexString = if (isMale.isChecked) "Мужчина" else "Женщина"

            if (sFirstName == originalPatient.firstName &&
                sLastName == originalPatient.lastName &&
                sMiddleName == originalPatient.middleName &&
                sBirthDate == originalPatient.birthDate &&
                sPhoneNumber == originalPatient.phoneNumber &&
                sDiagnosis == originalPatient.diagnosis &&
                sRoom == originalPatient.room &&
                sMedications == originalPatient.medications &&
                sAllergies == originalPatient.allergies &&
                sTemperature == originalPatient.vitalSigns?.temperature &&
                sHeartRate == originalPatient.vitalSigns?.heartRate &&
                sRespiratoryRate == originalPatient.vitalSigns?.respiratoryRate &&
                sBloodPressure == originalPatient.vitalSigns?.bloodPressure &&
                sOxygenSaturation == originalPatient.vitalSigns?.oxygenSaturation &&
                sBloodGlucose == originalPatient.vitalSigns?.bloodGlucose &&
                sexString == originalPatient.sex) {
                Toast.makeText(requireContext(), "Данные не изменились", Toast.LENGTH_SHORT).show()
                return@setToolbarSaveButtonListener2
            }

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
                put("admissionDate", originalPatient.admissionDate ?: "")
                put("mainDoctor", originalPatient.mainDoctor ?: "")
                put("vitalSigns", vitalsObject)
            }

            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val requestBody = patientObject.toString().toRequestBody(mediaType)
            val url = "$baseUrl/patients/${originalPatient.id}"

            val request = Request.Builder()
                .url(url)
                .patch(requestBody)
                .addHeader("Authorization", "Bearer $token")
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Ошибка сети: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        requireActivity().runOnUiThread {
                            if (!response.isSuccessful) {
                                Toast.makeText(requireContext(), "Ошибка: ${response.code}", Toast.LENGTH_SHORT).show()
                            } else {
                                val updatedPatient = Patient(
                                    id = originalPatient.id,
                                    firstName = sFirstName,
                                    lastName = sLastName,
                                    middleName = sMiddleName,
                                    birthDate = sBirthDate,
                                    phoneNumber = sPhoneNumber,
                                    diagnosis = sDiagnosis,
                                    room = sRoom,
                                    medications = sMedications,
                                    allergies = sAllergies,
                                    admissionDate = originalPatient.admissionDate,
                                    mainDoctor = originalPatient.mainDoctor,
                                    mainDoctorID = originalPatient.mainDoctorID,
                                    sex = sexString,
                                    vitalSigns = VitalSigns(
                                        sTemperature,
                                        sHeartRate,
                                        sRespiratoryRate,
                                        sBloodPressure,
                                        sOxygenSaturation,
                                        sBloodGlucose
                                    )
                                )
                                patientViewModel.selectPatient(updatedPatient)
                                Toast.makeText(requireContext(), "Изменения сохранены!", Toast.LENGTH_SHORT).show()
                                isEditMode = false
                                hideKeyboard(rootView)
                                switchToViewMode()
                                (requireActivity() as MainActivity).closeSaveButton2()
                                (requireActivity() as MainActivity).showEditButton()
                                (requireActivity() as MainActivity).closeCloseEditButton()
                                (requireActivity() as MainActivity).showBackButton()

                                requireActivity().invalidateOptionsMenu()
                            }
                        }
                    }
                }
            })
        }
        (requireActivity() as MainActivity).setToolbarBackButtonListener {
            (requireActivity() as MainActivity).closeBackButton()
            (requireActivity() as MainActivity).showSearchView()
            (requireActivity() as MainActivity).setTextForSearch()

            hideKeyboard(rootView)

            (requireActivity() as MainActivity).closeFragmentInformation()
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) return
        switchToViewMode()
        isEditMode = false

        if (isRotated) {
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
        hideKeyboard(rootView)
    }

    private fun deletePatientById(patientId: String) {
        val prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val token = prefs.getString("jwt_token", null) ?: ""
        if (token.isEmpty()) {
            Toast.makeText(requireContext(), "Нет токена. Перезайдите в систему.", Toast.LENGTH_SHORT).show()
            return
        }

        val url = "$baseUrl/patients/$patientId"
        val request = Request.Builder()
            .url(url)
            .delete()
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Ошибка сети: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    requireActivity().runOnUiThread {
                        if (!response.isSuccessful) {
                            Toast.makeText(requireContext(), "Ошибка: ${response.code}", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Информация удалена!", Toast.LENGTH_SHORT).show()

                            (requireActivity() as MainActivity).closeBackButton()
                            (requireActivity() as MainActivity).showSearchView()
                            (requireActivity() as MainActivity).setTextForSearch()
                            hideKeyboard(rootView)
                            (requireActivity() as MainActivity).closeFragmentInformation()
                        }
                    }
                }
            }
        })
    }

    private fun toggleVitalSigns() {
        linearLayout = rootView.findViewById(R.id.linear_layout)
        if (!isRotated) {
            rotate = ObjectAnimator.ofFloat(buttonShowSigns, "rotation", 0f, 180f)
            isRotated = true
            val fadeIn = ObjectAnimator.ofFloat(linearLayout, "alpha", 0f, 1f).setDuration(300)
            val animatorSet = AnimatorSet()
            animatorSet.playTogether(fadeIn)
            animatorSet.start()
            linearLayout.visibility = View.VISIBLE
            informantionScrollView.post { informantionScrollView.fullScroll(View.FOCUS_DOWN) }
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
        rotate.duration = 300
        rotate.start()
    }

    private fun switchToEditMode() {
        firstName.visibility = View.VISIBLE
        lastName.visibility = View.VISIBLE
        middleName.visibility = View.VISIBLE
        birthDate.visibility = View.VISIBLE
        phoneNumber.visibility = View.VISIBLE
        diagnosis.visibility = View.VISIBLE
        room.visibility = View.VISIBLE
        medications.visibility = View.VISIBLE
        allergies.visibility = View.VISIBLE
        temperature.visibility = View.VISIBLE
        heartRate.visibility = View.VISIBLE
        respiratoryRate.visibility = View.VISIBLE
        bloodPressure.visibility = View.VISIBLE
        oxygenSaturation.visibility = View.VISIBLE
        bloodGlucose.visibility = View.VISIBLE

        tfirstName.visibility = View.GONE
        tlastName.visibility = View.GONE
        tmiddleName.visibility = View.GONE
        tbirthDate.visibility = View.GONE
        tphoneNumber.visibility = View.GONE
        tdiagnosis.visibility = View.GONE
        troom.visibility = View.GONE
        tmedications.visibility = View.GONE
        tallergies.visibility = View.GONE
        ttemperature.visibility = View.GONE
        theartRate.visibility = View.GONE
        trespiratoryRate.visibility = View.GONE
        tbloodPressure.visibility = View.GONE
        toxygenSaturation.visibility = View.GONE
        tbloodGlucose.visibility = View.GONE
    }

    private fun switchToViewMode() {
        firstName.visibility = View.GONE
        lastName.visibility = View.GONE
        middleName.visibility = View.GONE
        birthDate.visibility = View.GONE
        phoneNumber.visibility = View.GONE
        diagnosis.visibility = View.GONE
        room.visibility = View.GONE
        medications.visibility = View.GONE
        allergies.visibility = View.GONE
        temperature.visibility = View.GONE
        heartRate.visibility = View.GONE
        respiratoryRate.visibility = View.GONE
        bloodPressure.visibility = View.GONE
        oxygenSaturation.visibility = View.GONE
        bloodGlucose.visibility = View.GONE

        tfirstName.visibility = View.VISIBLE
        tlastName.visibility = View.VISIBLE
        tmiddleName.visibility = View.VISIBLE
        tbirthDate.visibility = View.VISIBLE
        tphoneNumber.visibility = View.VISIBLE
        tdiagnosis.visibility = View.VISIBLE
        troom.visibility = View.VISIBLE
        tmedications.visibility = View.VISIBLE
        tallergies.visibility = View.VISIBLE
        ttemperature.visibility = View.VISIBLE
        theartRate.visibility = View.VISIBLE
        trespiratoryRate.visibility = View.VISIBLE
        tbloodPressure.visibility = View.VISIBLE
        toxygenSaturation.visibility = View.VISIBLE
        tbloodGlucose.visibility = View.VISIBLE

        hideKeyboard(rootView)
    }

    private fun hideKeyboard(view: View) {
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
