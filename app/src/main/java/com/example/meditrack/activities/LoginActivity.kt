package com.example.meditrack.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.meditrack.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class LoginActivity : AppCompatActivity() {
    private var alreadyShownToast = false
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_activity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        editTextEmail = findViewById(R.id.editTextTextEmailAddress)
        editTextPassword = findViewById(R.id.editTextTextPassword)

        val buttonLogin = findViewById<Button>(R.id.buttonLogin)
        buttonLogin.setOnClickListener { loginUser() }

        val textViewRegisterLink = findViewById<TextView>(R.id.textViewRegisterLink)
        textViewRegisterLink.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }

        val loginFocusLayout = findViewById<ConstraintLayout>(R.id.login_activity)
        loginFocusLayout.setOnClickListener {
            loginFocusLayout.clearFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    private fun loginUser() {
        val email = editTextEmail.text.toString().trim()
        val password = editTextPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            showToastOnce("Заполните все поля!")
            return
        }

        val progressBar = findViewById<ProgressBar>(R.id.progressBarLogin)
        val viewFocus = findViewById<View>(R.id.viewFocus)
        viewFocus.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE

        val baseUrl = "https://77-221-151-8.sslip.io"
        val doctorUrl = "$baseUrl/login/doctor"
        val nurseUrl = "$baseUrl/login/nurse"

        val jsonBody = JSONObject().apply {
            put("email", email)
            put("password", password)
        }
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val requestBody = jsonBody.toString().toRequestBody(mediaType)

        fun handleSuccess(responseBody: String, userType: String) {
            try {
                val json = JSONObject(responseBody)
                val token = json.optString("token", "")
                val id = json.optString("id", "")
                val fio = json.optString("fio","")
                if (token.isNotEmpty() && id.isNotEmpty()) {
                    val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
                    prefs.edit()
                        .putString("jwt_token", token)
                        .putString("user_id", id)
                        .putString("user_type", userType)
                        .putString("fio",fio)
                        .apply()

                    runOnUiThread {
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    }
                } else {
                    runOnUiThread {
                        showToastOnce("Не найден token или id в ответе сервера")
                    }
                }
            } catch (ex: Exception) {
                runOnUiThread {
                    showToastOnce("Ошибка парсинга ответа: ${ex.message}")
                }
            }
        }

        val requestDoctor = Request.Builder()
            .url(doctorUrl)
            .post(requestBody)
            .build()

        client.newCall(requestDoctor).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    viewFocus.visibility = View.GONE
                    progressBar.visibility = View.GONE
                    showToastOnce("Ошибка сети: ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    runOnUiThread {
                        viewFocus.visibility = View.GONE
                        progressBar.visibility = View.GONE
                    }
                    if (response.isSuccessful) {
                        val bodyStr = response.body?.string()
                        if (!bodyStr.isNullOrEmpty()) {
                            handleSuccess(bodyStr, "doctor")
                        } else {
                            runOnUiThread { showToastOnce("Пустой ответ от сервера") }
                        }
                    } else {
                        val requestNurse = Request.Builder()
                            .url(nurseUrl)
                            .post(requestBody)
                            .build()

                        client.newCall(requestNurse).enqueue(object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                runOnUiThread {
                                    showToastOnce("Ошибка сети: ${e.message}")
                                }
                            }

                            override fun onResponse(call: Call, response: Response) {
                                response.use {
                                    if (response.isSuccessful) {
                                        val bodyStr = response.body?.string()
                                        if (!bodyStr.isNullOrEmpty()) {
                                            handleSuccess(bodyStr, "nurse")
                                        } else {
                                            runOnUiThread { showToastOnce("Пустой ответ от сервера") }
                                        }
                                    } else {
                                        runOnUiThread {
                                            showToastOnce("Неверный логин или пароль! (код ${response.code})")
                                        }
                                    }
                                }
                            }
                        })
                    }
                }
            }
        })
    }

    private fun showToastOnce(message: String) {
        if (!alreadyShownToast) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            Handler(Looper.getMainLooper()).postDelayed({
                alreadyShownToast = false
            }, 3000)
            alreadyShownToast = true
        }
    }
}
