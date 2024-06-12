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
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import com.example.meditrack.R
import com.example.meditrack.activities.MainActivity
import com.example.meditrack.entities.Doctor
import com.example.meditrack.entities.Patient
import com.example.meditrack.entities.VitalSigns
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Objects

class AddPatientsFragment : Fragment() {
    private lateinit var addLayout: LinearLayout
    private lateinit var addscrollView: NestedScrollView

    private var database: FirebaseDatabase? = null
    var patientsRef: DatabaseReference? = null

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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView: View = inflater.inflate(R.layout.fragment_add_patients, container, false)


        addLayout = rootView.findViewById(R.id.linear_add)
        addscrollView = rootView.findViewById(R.id.add_scroll)

        addscrollView.setOnClickListener { view: View ->
            addscrollView.clearFocus()
            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        addLayout.setOnClickListener { view: View ->
            addLayout.clearFocus()
            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

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
            var isRotated: Boolean = false

            override fun onClick(v: View) {
                val rotate: ObjectAnimator
                val linearLayout = rootView.findViewById<LinearLayout>(R.id.linear_layout)
                if (!isRotated) {
                    rotate = ObjectAnimator.ofFloat(buttonShowSigns, "rotation", 0f, 180f)
                    isRotated = true

                    val fadeIn = ObjectAnimator.ofFloat(linearLayout, "alpha", 0f, 1f)
                    fadeIn.setDuration(300)

                    val animatorSet = AnimatorSet()
                    animatorSet.playTogether(fadeIn)
                    animatorSet.start()

                    linearLayout.visibility = View.VISIBLE

                    addscrollView.post { addscrollView.fullScroll(View.FOCUS_DOWN) }
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
        })

        isMale.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            if (isChecked) {
                isFemale.setChecked(false)
            }
        }

        isFemale.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            if (isChecked) {
                isMale.setChecked(false)
            }
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
        (requireActivity() as MainActivity).setToolbarSaveButtonListener1 { v ->
            database = FirebaseDatabase.getInstance()
            patientsRef = database!!.getReference("users/patients")
            val patient = Patient()
            val vitalSigns = VitalSigns()


            val SfirstName = firstName!!.text.toString()
            val SlastName = lastName!!.text.toString()
            val SmiddleName = middleName!!.text.toString()
            val SbirthDate = birthDate!!.text.toString()
            val SphoneNumber = phoneNumber!!.text.toString()
            val Sdiagnosis = diagnosis!!.text.toString()
            val Sroom = room!!.text.toString()
            val Smedications = medications!!.text.toString()
            val Sallergies = allergies!!.text.toString()
            val Stemperature = temperature!!.text.toString()
            val SheartRate = heartRate!!.text.toString()
            val SrespiratoryRate = respiratoryRate!!.text.toString()
            val SbloodPressure = bloodPressure!!.text.toString()
            val SoxygenSaturation = oxygenSaturation!!.text.toString()
            val SbloodGlucose = bloodGlucose!!.text.toString()

            if (SfirstName.isEmpty() || SmiddleName.isEmpty() || SlastName.isEmpty() || Sroom.isEmpty()) {
                if (!isAlreadyShown) {
                    Toast.makeText(
                        activity,
                        "Заполните все необходимые поля",
                        Toast.LENGTH_SHORT
                    ).show()
                    Handler(Looper.getMainLooper()).postDelayed({
                        isAlreadyShown = false
                    }, 3000)
                    isAlreadyShown = true
                }
                return@setToolbarSaveButtonListener1
            }
            if (isMale.isChecked) {
                patient.sex ="Мужчина"
            } else if (isFemale.isChecked) {
                patient.sex = "Женщина"
            } else {
                if (!isAlreadyShown) {
                    Toast.makeText(activity, "Выберите пол пациента!", Toast.LENGTH_SHORT)
                        .show()
                    Handler(Looper.getMainLooper()).postDelayed({
                        isAlreadyShown = false
                    }, 3000)
                    isAlreadyShown = true
                }
                return@setToolbarSaveButtonListener1
            }

            val user = FirebaseAuth.getInstance().currentUser
            val userId =
                Objects.requireNonNull(user)!!.uid

            val doctorRef =
                FirebaseDatabase.getInstance().getReference("users/doctors").child(userId)

            doctorRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val doctor: Doctor? = dataSnapshot.getValue(Doctor::class.java)
                    val mainDoctor: String = if (doctor != null) {
                        val lastName = doctor.lastName ?: ""
                        val firstName = doctor.firstName?.firstOrNull()?.toString() ?: ""
                        val middleName = doctor.middleName?.firstOrNull()?.toString() ?: ""

                        "$lastName ${firstName.uppercase()}.${middleName.uppercase()}."
                    } else {
                        ""
                    }

                    patient.firstName = SfirstName
                    patient.lastName = SlastName
                    patient.middleName = SmiddleName
                    patient.birthDate = SbirthDate
                    patient.phoneNumber = SphoneNumber
                    patient.diagnosis = Sdiagnosis
                    patient.room = Sroom
                    patient.medications = Smedications
                    patient.allergies = Sallergies

                    val calendar = Calendar.getInstance()
                    val today = calendar.time

                    @SuppressLint("SimpleDateFormat") val format =
                        SimpleDateFormat("dd.MM.yy")
                    val dateToStr = format.format(today)

                    patient.admissionDate = dateToStr



                    patient.mainDoctor = mainDoctor
                    vitalSigns.temperature = Stemperature
                    vitalSigns.heartRate = SheartRate
                    vitalSigns.respiratoryRate = SrespiratoryRate
                    vitalSigns.bloodPressure = SbloodPressure
                    vitalSigns.oxygenSaturation = SoxygenSaturation
                    vitalSigns.bloodGlucose = SbloodGlucose

                    patient.vitalSigns = vitalSigns

                    val id = patientsRef!!.push().key ?: ""

                    patient.id = id

                    patientsRef!!.child(id)
                        .setValue(patient)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            })
            (requireActivity() as MainActivity).closeFragmentAdd()
            (requireActivity() as MainActivity).showSearchView()
            (requireActivity() as MainActivity).setTextForSearch()
            (requireActivity() as MainActivity).closeSaveButton1()
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)

        if (hidden) {
            firstName!!.setText("")
            lastName!!.setText("")
            middleName!!.setText("")
            birthDate!!.setText("")
            phoneNumber!!.setText("")
            diagnosis!!.setText("")
            room!!.setText("")
            medications!!.setText("")
            allergies!!.setText("")
            temperature!!.setText("")
            heartRate!!.setText("")
            respiratoryRate!!.setText("")
            bloodPressure!!.setText("")
            oxygenSaturation!!.setText("")
            bloodGlucose!!.setText("")
            (requireActivity() as MainActivity).closeSaveButton1()

            isMale.isChecked = false
            isFemale.isChecked = false

            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(requireView().windowToken, 0)
        }
    }
}