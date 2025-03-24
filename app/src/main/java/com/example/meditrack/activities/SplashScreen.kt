package com.example.meditrack.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.auth0.android.jwt.JWT
import com.example.meditrack.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity() {
    private var alreadyShownToast = false
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_splash_screen)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        Handler(Looper.getMainLooper()).postDelayed({
            val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
            val existingToken = prefs.getString("jwt_token", null)

            when {
                existingToken.isNullOrEmpty() -> {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                isTokenExpired(existingToken) -> {
                    showToastOnce("Ваша сессия завершена! Войдите снова.")
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                else -> {
                    verifyTokenOnServer(existingToken)
                }
            }
        }, 1500)

    }

    private fun isTokenExpired(token: String): Boolean {
        return try {
            val jwt = JWT(token)
            jwt.isExpired(10)
        } catch (e: Exception) {
            true
        }
    }

    private fun verifyTokenOnServer(token: String) {
        val baseUrl = "https://77-221-151-8.sslip.io"
        val verifyUrl = "$baseUrl/auth/verifyToken"

        val request = Request.Builder()
            .url(verifyUrl)
            .header("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    showToastOnce("Ошибка сети при проверке токена: ${e.message}")
                    startActivity(Intent(this@SplashScreen, LoginActivity::class.java))
                    finish()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        runOnUiThread {
                            showToastOnce("Срок действия вашей сессии истёк!")
                            startActivity(Intent(this@SplashScreen, LoginActivity::class.java))
                            finish()
                        }
                    } else {
                        runOnUiThread {
                            startActivity(Intent(this@SplashScreen, MainActivity::class.java))
                            finish()
                        }
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