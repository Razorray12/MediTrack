package com.example.meditrack.fragments

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import com.example.meditrack.R
import com.example.meditrack.activities.LoginActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class RegisterDialogFragment : DialogFragment() {

    private var registrationDialogFocusLayout: ConstraintLayout? = null
    private var emailEditText: EditText? = null
    private var passwordEditText: EditText? = null
    private var alreadyShownToast = false

    private val client = OkHttpClient()

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
            showToastOnce("Заполните все поля!")
            return
        }

        if (password.length < 6) {
            showToastOnce("Пароль должен быть не менее 6 символов!")
            return
        }

        if (!isValidEmail(email)) {
            showToastOnce("Введите корректный email адрес!")
            return
        }

        val args = arguments
        if (args == null) {
            showToastOnce("Ошибка передачи данных!")
            return
        }

        val firstName = args.getString("firstName") ?: ""
        val lastName = args.getString("lastName") ?: ""
        val middleName = args.getString("middleName") ?: ""
        val experience = args.getString("experience") ?: ""
        val specialization = args.getString("specialization") ?: ""
        val userType = args.getString("userType") ?: ""

        val baseUrl = "https://77-221-151-8.sslip.io"
        val endpoint = if (userType == "Доктор") "/doctors" else "/nurses"
        val registrationUrl = "$baseUrl$endpoint"

        val jsonBody = JSONObject().apply {
            put("firstName", firstName)
            put("lastName", lastName)
            put("middleName", middleName)
            put("experience", experience)
            if (userType == "Доктор") {
                put("specialization", specialization)
            }
            put("email", email)
            put("password", password)
        }
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val requestBody = jsonBody.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(registrationUrl)
            .post(requestBody)
            .build()

        val progressBarReg = activity?.findViewById<ProgressBar>(R.id.progressBarRegist)
        val viewFocusReg = activity?.findViewById<View>(R.id.viewFocusRegistr)

        progressBarReg?.visibility = View.VISIBLE
        viewFocusReg?.visibility = View.VISIBLE
        registrationDialogFocusLayout?.visibility = View.INVISIBLE

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    progressBarReg?.visibility = View.GONE
                    viewFocusReg?.visibility = View.GONE
                    showToastOnce("Ошибка сети: ${e.message}")
                    registrationDialogFocusLayout?.visibility = View.VISIBLE
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    activity?.runOnUiThread {
                        progressBarReg?.visibility = View.GONE
                        viewFocusReg?.visibility = View.GONE
                        registrationDialogFocusLayout?.visibility = View.VISIBLE
                    }

                    if (!response.isSuccessful) {
                        activity?.runOnUiThread {
                            if(response.code == 500) {
                                showToastOnce("Пользователь уже зарегистрирован")
                            }
                            else showToastOnce("Ошибка сервера (${response.code})")
                        }
                    } else {

                        activity?.runOnUiThread {
                            showToastOnce("Регистрация прошла успешно!")
                            val intent = Intent(requireContext(), LoginActivity::class.java)
                            startActivity(intent)
                            activity?.finish()
                        }
                    }
                }
            }
        })
    }

    private fun showToastOnce(message: String) {
        if (!alreadyShownToast) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            alreadyShownToast = true
            Handler(Looper.getMainLooper()).postDelayed({
                alreadyShownToast = false
            }, 3000)
        }
    }
}
