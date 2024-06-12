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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {
    private var alreadyShownToast = false
    private lateinit var mAuth: FirebaseAuth
    private var currentUser: FirebaseUser? = null
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_activity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth.currentUser
        if (currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
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
            if (!alreadyShownToast) {
                Toast.makeText(this, "Заполните все поля!", Toast.LENGTH_SHORT).show()
                Handler(Looper.getMainLooper()).postDelayed({
                    alreadyShownToast = false
                }, 3000)
                alreadyShownToast = true
            }
            return
        }

        val progressBar = findViewById<ProgressBar>(R.id.progressBarLogin)
        val viewFocus = findViewById<View>(R.id.viewFocus)

        viewFocus.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                viewFocus.visibility = View.GONE
                progressBar.visibility = View.GONE

                if (task.isSuccessful) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    val errorMessage = task.exception?.message ?: "Неизвестная ошибка"
                    if (errorMessage.contains("incorrect") || errorMessage.contains("6") || errorMessage.contains("format")) {
                        if (!alreadyShownToast) {
                            Toast.makeText(this, "Неверный логин или пароль!", Toast.LENGTH_SHORT).show()
                            Handler(Looper.getMainLooper()).postDelayed({
                                alreadyShownToast = false
                            }, 3000)
                            alreadyShownToast = true
                        }
                    } else {
                        if (!alreadyShownToast) {
                            Toast.makeText(this, "Ошибка входа: $errorMessage", Toast.LENGTH_SHORT).show()
                            Handler(Looper.getMainLooper()).postDelayed({
                                alreadyShownToast = false
                            }, 3000)
                            alreadyShownToast = true
                        }
                    }
                }
            }
    }

}