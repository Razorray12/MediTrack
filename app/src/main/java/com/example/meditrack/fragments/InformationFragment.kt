package com.example.meditrack.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Objects

class InformationFragment : Fragment() {
    private lateinit var informationLayout: LinearLayout
    private lateinit var informantionScrollView: NestedScrollView
    private lateinit var linearLayout: LinearLayout
    private lateinit var rootView: View
    private var isDoctor = false
    private var isRotated = false
    private lateinit var rotate: ObjectAnimator
    private lateinit var buttonShowSigns: ImageButton
    private lateinit var patientViewModel: PatientViewModel
    private lateinit var deletePatient: TextView

    private var user: FirebaseUser? = null
    private var database: FirebaseDatabase? = null
    private var patientsRef: DatabaseReference? = null
    private var deletePatientsRef: DatabaseReference? = null
    private var userId: String? = null

    private lateinit var firstName: EditText
    private lateinit var lastName: EditText
    private lateinit var middleName: EditText
    private lateinit var birthDate: EditText
    private lateinit var phoneNumber: EditText
    private lateinit var diagnosis: EditText
    private lateinit var room: EditText
    private lateinit var isMale: CheckBox
    private lateinit var isFemale: CheckBox
    private lateinit var medications: EditText
    private lateinit var allergies: EditText
    private lateinit var temperature: EditText
    private lateinit var heartRate: EditText
    private lateinit var respiratoryRate: EditText
    private lateinit var bloodPressure: EditText
    private lateinit var oxygenSaturation: EditText
    private lateinit var bloodGlucose: EditText

    private lateinit var TfirstName: TextView
    private lateinit var TlastName: TextView
    private lateinit var TmiddleName: TextView
    private lateinit var TbirthDate: TextView
    private lateinit var TphoneNumber: TextView
    private lateinit var Tdiagnosis: TextView
    private lateinit var Troom: TextView
    private lateinit var Tmedications: TextView
    private lateinit var Tallergies: TextView
    private lateinit var Ttemperature: TextView
    private lateinit var TheartRate: TextView
    private lateinit var TrespiratoryRate: TextView
    private lateinit var TbloodPressure: TextView
    private lateinit var ToxygenSaturation: TextView
    private lateinit var TbloodGlucose: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.fragment_information, container, false)

        informationLayout = rootView.findViewById(R.id.linear_information)
        informantionScrollView = rootView.findViewById(R.id.information_scroll)

        informationLayout.setOnClickListener { view: View ->
            informationLayout.clearFocus()
            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        informantionScrollView.setOnClickListener { view: View ->
            informantionScrollView.clearFocus()
            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
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

        TfirstName = rootView.findViewById(R.id.text_first_name)
        TlastName = rootView.findViewById(R.id.text_last_name)
        TmiddleName = rootView.findViewById(R.id.text_middle_name)
        TbirthDate = rootView.findViewById(R.id.text_birthdate)
        TphoneNumber = rootView.findViewById(R.id.text_phone_name)
        Tdiagnosis = rootView.findViewById(R.id.text_diagnosis)
        Troom = rootView.findViewById(R.id.text_room)
        Tmedications = rootView.findViewById(R.id.text_medications)
        Tallergies = rootView.findViewById(R.id.text_allergies)
        Ttemperature = rootView.findViewById(R.id.text_temperature)
        TheartRate = rootView.findViewById(R.id.text_chss)
        TrespiratoryRate = rootView.findViewById(R.id.text_respiratory)
        TbloodPressure = rootView.findViewById(R.id.text_ad)
        ToxygenSaturation = rootView.findViewById(R.id.text_saturation)
        TbloodGlucose = rootView.findViewById(R.id.text_glucose)
        isMale = rootView.findViewById(R.id.checkBoxMale)
        isFemale = rootView.findViewById(R.id.checkBoxFemale)

        buttonShowSigns = rootView.findViewById(R.id.button_plus_signs)


        buttonShowSigns.setOnClickListener {
            linearLayout = rootView.findViewById(R.id.linear_layout)
            if (!isRotated) {
                rotate = ObjectAnimator.ofFloat(buttonShowSigns, "rotation", 0f, 180f)
                isRotated = true

                val fadeIn = ObjectAnimator.ofFloat(linearLayout, "alpha", 0f, 1f)
                fadeIn.setDuration(300)

                val animatorSet = AnimatorSet()
                animatorSet.playTogether(fadeIn)
                animatorSet.start()

                linearLayout.visibility = View.VISIBLE

                informantionScrollView.post {
                    informantionScrollView.fullScroll(
                        View.FOCUS_DOWN
                    )
                }
            } else {
                rotate = ObjectAnimator.ofFloat(buttonShowSigns, "rotation", 180f, 0f)
                isRotated = false

                val fadeOut = ObjectAnimator.ofFloat(linearLayout, "alpha", 1f, 0f)
                fadeOut.setDuration(300)

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

        user = FirebaseAuth.getInstance().currentUser
        userId = Objects.requireNonNull(user)!!.uid
        val doctorsRef = FirebaseDatabase.getInstance().getReference("users/doctors")

        doctorsRef.child(userId!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    isDoctor = true
                }
                patientViewModel =
                    ViewModelProvider(requireActivity())[PatientViewModel::class.java]

                patientViewModel.getSelectedPatient().observe(viewLifecycleOwner) { patient ->
                    if (patient.sex.equals("Мужчина")) {
                        isMale.setChecked(true)
                    } else {
                        isFemale.setChecked(true)
                    }
                    TfirstName.text = patient.firstName
                    TlastName.text = patient.lastName
                    TmiddleName.text = patient.middleName
                    TbirthDate.text = patient.birthDate
                    TphoneNumber.text = patient.phoneNumber
                    Tdiagnosis.text = patient.diagnosis
                    Troom.text = patient.room
                    Tmedications.text = patient.medications
                    Tallergies.text = patient.allergies
                    Ttemperature.text = patient.vitalSigns!!.temperature
                    TheartRate.text = patient.vitalSigns!!.heartRate
                    TrespiratoryRate.text = patient.vitalSigns!!.respiratoryRate
                    TbloodPressure.text = patient.vitalSigns!!.bloodPressure
                    ToxygenSaturation.text = patient.vitalSigns!!.oxygenSaturation
                    TbloodGlucose.text = patient.vitalSigns!!.bloodGlucose

                    firstName.setText(patient.firstName)
                    lastName.setText(patient.lastName)
                    middleName.setText(patient.middleName)
                    birthDate.setText(patient.birthDate)
                    phoneNumber.setText(patient.phoneNumber)
                    diagnosis.setText(patient.diagnosis)
                    room.setText(patient.room)
                    medications.setText(patient.medications)
                    allergies.setText(patient.allergies)
                    temperature.setText(patient.vitalSigns!!.temperature)
                    heartRate.setText(patient.vitalSigns!!.heartRate)
                    respiratoryRate.setText(patient.vitalSigns!!.respiratoryRate)
                    bloodPressure.setText(patient.vitalSigns!!.bloodPressure)
                    oxygenSaturation.setText(patient.vitalSigns!!.oxygenSaturation)
                    bloodGlucose.setText(patient.vitalSigns!!.bloodGlucose)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        deletePatient.setOnClickListener {
            val builder =
                AlertDialog.Builder(context)
            val dialogView: View =
                inflater.inflate(R.layout.custom_alert_delete_patient, null)

            builder.setView(dialogView)

            val alertDialog = builder.create()

            val positiveButton =
                dialogView.findViewById<TextView>(R.id.positiveButtonDelete)
            val negativeButton =
                dialogView.findViewById<TextView>(R.id.negativeButtonDelete)

            val patientViewModel: PatientViewModel =
                ViewModelProvider(requireActivity())[PatientViewModel::class.java]
            val patient: Patient = patientViewModel.getSelectedPatient().getValue()!!
            deletePatientsRef = FirebaseDatabase.getInstance().getReference("users/patients")

            val patientId: String = patient.id!!

            positiveButton.setOnClickListener {
                alertDialog.dismiss()
                deletePatientsRef!!.child(patientId).removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(
                            requireContext(),
                            "Информация удалена!",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Ошибка!", Toast.LENGTH_SHORT).show()
                    }

                (requireActivity() as MainActivity).closeBackButton()
                (requireActivity() as MainActivity).showSearchView()
                (requireActivity() as MainActivity).setTextForSearch()
                val imm =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(rootView.windowToken, 0)
                (requireActivity() as MainActivity).closeFragmentInformation()
            }

            negativeButton.setOnClickListener { alertDialog.dismiss() }

            alertDialog.show()
            Objects.requireNonNull<Window?>(alertDialog.window)
                .setBackgroundDrawableResource(R.drawable.alertdialog_background)
        }

        return rootView
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_edit) {
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

            TfirstName.visibility = View.GONE
            TlastName.visibility = View.GONE
            TmiddleName.visibility = View.GONE
            TbirthDate.visibility = View.GONE
            TphoneNumber.visibility = View.GONE
            Tdiagnosis.visibility = View.GONE
            Troom.visibility = View.GONE
            Tmedications.visibility = View.GONE
            Tallergies.visibility = View.GONE
            Ttemperature.visibility = View.GONE
            TheartRate.visibility = View.GONE
            TrespiratoryRate.visibility = View.GONE
            TbloodPressure.visibility = View.GONE
            ToxygenSaturation.visibility = View.GONE
            TbloodGlucose.visibility = View.GONE

            (requireActivity() as MainActivity).closeBackButton()
            item.setVisible(false)
            (requireActivity() as MainActivity).showCloseEditButton()
            (requireActivity() as MainActivity).showSaveButton2()

            return true
        }

        if (id == R.id.action_close_edit1) {
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

            TfirstName.visibility = View.GONE
            TlastName.visibility = View.GONE
            TmiddleName.visibility = View.GONE
            TbirthDate.visibility = View.GONE
            TphoneNumber.visibility = View.GONE
            Tdiagnosis.visibility = View.GONE
            Troom.visibility = View.GONE
            Tmedications.visibility = View.GONE
            Tallergies.visibility = View.GONE
            Ttemperature.visibility = View.GONE
            TheartRate.visibility = View.GONE
            TrespiratoryRate.visibility = View.GONE
            TbloodPressure.visibility = View.GONE
            ToxygenSaturation.visibility = View.GONE
            TbloodGlucose.visibility = View.GONE

            (requireActivity() as MainActivity).closeBackButton()
            item.setVisible(false)
            (requireActivity() as MainActivity).showCloseEditButton()

            return true
        } else if (id == R.id.action_close_edit) {
            (requireActivity() as MainActivity).showBackButton()
            (requireActivity() as MainActivity).showEditButton()
            (requireActivity() as MainActivity).closeSaveButton2()

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

            TfirstName.visibility = View.VISIBLE
            TlastName.visibility = View.VISIBLE
            TmiddleName.visibility = View.VISIBLE
            TbirthDate.visibility = View.VISIBLE
            TphoneNumber.visibility = View.VISIBLE
            Tdiagnosis.visibility = View.VISIBLE
            Troom.visibility = View.VISIBLE
            Tmedications.visibility = View.VISIBLE
            Tallergies.visibility = View.VISIBLE
            Ttemperature.visibility = View.VISIBLE
            TheartRate.visibility = View.VISIBLE
            TrespiratoryRate.visibility = View.VISIBLE
            TbloodPressure.visibility = View.VISIBLE
            ToxygenSaturation.visibility = View.VISIBLE
            TbloodGlucose.visibility = View.VISIBLE

            item.setVisible(false)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()

        (requireActivity() as MainActivity).setToolbarSaveButtonListener2 { v ->
            val patientViewModel: PatientViewModel = ViewModelProvider(requireActivity())[PatientViewModel::class.java]
            val originalPatient: Patient = patientViewModel.getSelectedPatient().getValue() ?: Patient()
            val updatedPatient = Patient()

            updatedPatient.mainDoctor = originalPatient.mainDoctor

            updatedPatient.sex = originalPatient.sex
            updatedPatient.id = originalPatient.id
            updatedPatient.admissionDate = (originalPatient.admissionDate)
            updatedPatient.firstName = (firstName.text.toString())
            updatedPatient.lastName = (lastName.text.toString())
            updatedPatient.middleName = (middleName.text.toString())
            updatedPatient.birthDate = (birthDate.text.toString())
            updatedPatient.phoneNumber = (phoneNumber.text.toString())
            updatedPatient.diagnosis = (diagnosis.text.toString())
            updatedPatient.room = (room.text.toString())
            updatedPatient.medications = (medications.text.toString())
            updatedPatient.allergies = (allergies.text.toString())

            val vitalSigns = VitalSigns()
            vitalSigns.temperature = (temperature.text.toString())
            vitalSigns.heartRate = (heartRate.text.toString())
            vitalSigns.respiratoryRate = (respiratoryRate.text.toString())
            vitalSigns.bloodPressure = (bloodPressure.text.toString())
            vitalSigns.oxygenSaturation = (oxygenSaturation.text.toString())
            vitalSigns.bloodGlucose = (bloodGlucose.text.toString())

            updatedPatient.vitalSigns = (vitalSigns)

            patientViewModel.selectPatient(updatedPatient)

            database = FirebaseDatabase.getInstance()
            patientsRef = database!!.getReference("users/patients")
            patientsRef!!.child(updatedPatient.id!!).setValue(updatedPatient)

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

            TfirstName.visibility = View.VISIBLE
            TlastName.visibility = View.VISIBLE
            TmiddleName.visibility = View.VISIBLE
            TbirthDate.visibility = View.VISIBLE
            TphoneNumber.visibility = View.VISIBLE
            Tdiagnosis.visibility = View.VISIBLE
            Troom.visibility = View.VISIBLE
            Tmedications.visibility = View.VISIBLE
            Tallergies.visibility = View.VISIBLE
            Ttemperature.visibility = View.VISIBLE
            TheartRate.visibility = View.VISIBLE
            TrespiratoryRate.visibility = View.VISIBLE
            TbloodPressure.visibility = View.VISIBLE
            ToxygenSaturation.visibility = View.VISIBLE
            TbloodGlucose.visibility = View.VISIBLE

            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(rootView.windowToken, 0)

            (requireActivity() as MainActivity).closeSaveButton2()
            (requireActivity() as MainActivity).showEditButton()
            (requireActivity() as MainActivity).closeCloseEditButton()
            (requireActivity() as MainActivity).showBackButton()
        }

        (requireActivity() as MainActivity).setToolbarBackButtonListener { v ->
            (requireActivity() as MainActivity).closeBackButton()
            (requireActivity() as MainActivity).showSearchView()
            (requireActivity() as MainActivity).setTextForSearch()
            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(rootView.windowToken, 0)
            (requireActivity() as MainActivity).closeFragmentInformation()
        }
    }


    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
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

        TfirstName.visibility = View.VISIBLE
        TlastName.visibility = View.VISIBLE
        TmiddleName.visibility = View.VISIBLE
        TbirthDate.visibility = View.VISIBLE
        TphoneNumber.visibility = View.VISIBLE
        Tdiagnosis.visibility = View.VISIBLE
        Troom.visibility = View.VISIBLE
        Tmedications.visibility = View.VISIBLE
        Tallergies.visibility = View.VISIBLE
        Ttemperature.visibility = View.VISIBLE
        TheartRate.visibility = View.VISIBLE
        TrespiratoryRate.visibility = View.VISIBLE
        TbloodPressure.visibility = View.VISIBLE
        ToxygenSaturation.visibility = View.VISIBLE
        TbloodGlucose.visibility = View.VISIBLE

        if (isRotated) {
            rotate = ObjectAnimator.ofFloat(buttonShowSigns, "rotation", 180f, 0f)
            isRotated = false

            val fadeOut = ObjectAnimator.ofFloat(linearLayout, "alpha", 1f, 0f)
            fadeOut.setDuration(300)

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

        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(rootView.windowToken, 0)
    }
}