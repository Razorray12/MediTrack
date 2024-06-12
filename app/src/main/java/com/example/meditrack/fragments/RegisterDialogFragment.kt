package com.example.meditrack.fragments

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import com.example.meditrack.R
import com.example.meditrack.activities.LoginActivity
import com.example.meditrack.entities.Doctor
import com.example.meditrack.entities.Nurse
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterDialogFragment : DialogFragment() {

    private var registrationDialogFocusLayout: ConstraintLayout? = null
    private var emailEditText: EditText? = null
    private var passwordEditText: EditText? = null
    private var alreadyShownToast = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity(), R.style.RoundedCornersDialog)
        val inflater = requireActivity().layoutInflater
        val view: View = inflater.inflate(R.layout.fragment_dialog_registration, null)

        emailEditText = view.findViewById(R.id.editTextTextEmailAddress)
        passwordEditText = view.findViewById(R.id.editTextTextPassword)
        registrationDialogFocusLayout = view.findViewById(R.id.fragment_dialog_registration)

        registrationDialogFocusLayout?.setOnClickListener {
            registrationDialogFocusLayout?.clearFocus()
            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(registrationDialogFocusLayout?.windowToken, 0)
        }

        view.findViewById<Button>(R.id.onLoginActivity)?.setOnClickListener {
            registerUser()
        }

        view.findViewById<ImageButton>(R.id.CloseRegister)?.setOnClickListener {
            dialog?.dismiss()
        }

        builder.setView(view)
        return builder.create()
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(1000, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog?.window?.setGravity(Gravity.CENTER)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setWindowAnimations(R.style.RoundedCornersDialog)
            val params = attributes
            params.dimAmount = 0.7f
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            attributes = params
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
        return email.matches(emailRegex.toRegex())
    }

    private fun registerUser() {
        val email = emailEditText?.text.toString().trim()
        val password = passwordEditText?.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            if (!alreadyShownToast) {
                Toast.makeText(requireContext(), "Заполните все поля!", Toast.LENGTH_SHORT).show()
                alreadyShownToast = true
                Handler(Looper.getMainLooper()).postDelayed({
                    alreadyShownToast = false
                }, 3000)
            }
            return
        }

        if (password.length < 6) {
            if (!alreadyShownToast) {
                Toast.makeText(requireContext(), "Пароль должен быть не менее 6 символов!", Toast.LENGTH_SHORT).show()
                alreadyShownToast = true
                Handler(Looper.getMainLooper()).postDelayed({
                    alreadyShownToast = false
                }, 3000)
            }
            return
        }

        if (!isValidEmail(email)) {
            if (!alreadyShownToast) {
                Toast.makeText(requireContext(), "Введите корректный email адрес!", Toast.LENGTH_SHORT).show()
                alreadyShownToast = true
                Handler(Looper.getMainLooper()).postDelayed({
                    alreadyShownToast = false
                }, 3000)
            }
            return
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task: Task<AuthResult?> ->
                if (task.isSuccessful) {
                    val firebaseUser = FirebaseAuth.getInstance().currentUser
                    if (firebaseUser != null) {
                        val progressBarReg = requireActivity().findViewById<ProgressBar>(R.id.progressBarRegist)
                        val viewFocusReg = requireActivity().findViewById<View>(R.id.viewFocusRegistr)

                        progressBarReg.visibility = View.VISIBLE
                        viewFocusReg.visibility = View.VISIBLE
                        registrationDialogFocusLayout?.visibility = View.INVISIBLE

                        val userId = firebaseUser.uid
                        val args = arguments

                        saveUserToDataBase(
                            userId,
                            args?.getString("firstName"),
                            args?.getString("lastName"),
                            args?.getString("middleName"),
                            args?.getString("experience"),
                            args?.getString("specialization")
                        )

                        val intent = Intent(requireContext(), LoginActivity::class.java)
                        FirebaseAuth.getInstance().signOut()
                        startActivity(intent)
                        requireActivity().finish()
                    }
                } else {
                    if (!alreadyShownToast) {
                        Toast.makeText(requireContext(), "Пользователь уже зарегистрирован!", Toast.LENGTH_SHORT).show()
                        alreadyShownToast = true
                        Handler(Looper.getMainLooper()).postDelayed({
                            alreadyShownToast = false
                        }, 3000)
                    }
                }
            }
    }

    private fun saveUserToDataBase(
        userId: String,
        firstName: String?,
        lastName: String?,
        middleName: String?,
        experience: String?,
        specialization: String?
    ) {
        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("users")
        val args = arguments

        if (args != null) {
            val userType = args.getString("userType")
            when (userType) {
                "Доктор" -> {
                    val doctor = Doctor(userId, firstName, lastName, middleName, experience, specialization)
                    usersRef.child("doctors").child(userId).setValue(doctor)
                }
                "Медсестра" -> {
                    val nurse = Nurse(userId, firstName.toString(),
                        lastName.toString(), middleName.toString(), experience.toString()
                    )
                    usersRef.child("nurses").child(userId).setValue(nurse)
                }
            }
        }
    }
}
