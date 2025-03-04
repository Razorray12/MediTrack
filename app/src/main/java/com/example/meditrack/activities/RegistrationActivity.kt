package com.example.meditrack.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.meditrack.R
import com.example.meditrack.fragments.RegisterDialogFragment

class RegistrationActivity : AppCompatActivity() {
    private var alreadyShownToast = false

    private lateinit var editTextFirstName: EditText
    private lateinit var editTextLastName: EditText
    private lateinit var editTextMiddleName: EditText
    private lateinit var editTextExperience: EditText
    private lateinit var editTextSpecialization: EditText
    private lateinit var checkBoxNurse: CheckBox
    private lateinit var checkBoxDoctor: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registration_activity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val registrationFocusLayout: ConstraintLayout = findViewById(R.id.registration_activity)
        registrationFocusLayout.setOnClickListener { view: View ->
            registrationFocusLayout.clearFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        editTextFirstName = findViewById(R.id.editTextTextFirstName)
        editTextLastName = findViewById(R.id.editTextTextLastName)
        editTextMiddleName = findViewById(R.id.editTextTextMiddleName)
        editTextExperience = findViewById(R.id.editTextTextExperience)
        editTextSpecialization = findViewById(R.id.editTextTextSpecialization)
        checkBoxNurse = findViewById(R.id.checkBoxNurse)
        checkBoxDoctor = findViewById(R.id.checkBoxDoctor)

        checkBoxNurse.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkBoxDoctor.isChecked = false
                editTextSpecialization.isEnabled = false
                editTextSpecialization.setText("")
            }
        }
        checkBoxDoctor.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkBoxNurse.isChecked = false
                editTextSpecialization.isEnabled = true
            } else {
                editTextSpecialization.isEnabled = false
                editTextSpecialization.setText("")
            }
        }

        val buttonOnRegisterDialogFragment: Button = findViewById(R.id.buttonOnRegisterDialogFragment)
        buttonOnRegisterDialogFragment.setOnClickListener {
            val firstName = editTextFirstName.text.toString().trim()
            val lastName = editTextLastName.text.toString().trim()
            val middleName = editTextMiddleName.text.toString().trim()
            val experience = editTextExperience.text.toString().trim()
            val specialization = editTextSpecialization.text.toString().trim()

            if (firstName.isEmpty() || lastName.isEmpty() || middleName.isEmpty() || experience.isEmpty()) {
                showToastOnce("Заполните все поля!")
                return@setOnClickListener
            }

            val userType = when {
                checkBoxNurse.isChecked -> {
                    "Медсестра"
                }
                checkBoxDoctor.isChecked -> {
                    if (specialization.isEmpty()) {
                        showToastOnce("Укажите специализацию!")
                        return@setOnClickListener
                    }
                    "Доктор"
                }
                else -> {
                    showToastOnce("Укажите должность!")
                    return@setOnClickListener
                }
            }

            val registerDialog = RegisterDialogFragment().apply {
                arguments = Bundle().apply {
                    putString("firstName", firstName)
                    putString("lastName", lastName)
                    putString("middleName", middleName)
                    putString("experience", experience)
                    putString("specialization", specialization)
                    putString("userType", userType)
                }
            }
            registerDialog.show(supportFragmentManager, "registerDialog")
        }
    }

    private fun showToastOnce(message: String) {
        if (!alreadyShownToast) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            alreadyShownToast = true
            Handler(Looper.getMainLooper()).postDelayed({
                alreadyShownToast = false
            }, 3000)
        }
    }
}
