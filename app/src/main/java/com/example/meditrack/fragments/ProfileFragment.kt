package com.example.meditrack.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.meditrack.R
import com.example.meditrack.activities.LoginActivity
import com.example.meditrack.activities.MainActivity
import com.example.meditrack.viewmodels.ProfileLoadedViewModel
import com.facebook.shimmer.ShimmerFrameLayout
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class ProfileFragment : Fragment() {

    private var alreadyShownToast = false
    private var isDoctor = false

    private lateinit var profileLayout: LinearLayout
    private lateinit var scrollView: NestedScrollView
    private lateinit var rootView: View
    private lateinit var viewModel: ProfileLoadedViewModel

    // Вьюхи
    private lateinit var emailTextView: TextView
    private lateinit var firstNameTextView: TextView
    private lateinit var lastNameTextView: TextView
    private lateinit var middleNameTextView: TextView
    private lateinit var experienceTextView: TextView
    private lateinit var specializationTextView: TextView
    private lateinit var specializationInvisibleTextView: TextView

    private lateinit var emailEditText: EditText
    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var middleNameEditText: EditText
    private lateinit var experienceEditText: EditText
    private lateinit var specializationEditText: EditText

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        viewModel = ViewModelProvider(this)[ProfileLoadedViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.fragment_profile, container, false)

        scrollView = rootView.findViewById(R.id.scroll_profile)
        profileLayout = rootView.findViewById(R.id.linear_profile)

        emailTextView = profileLayout.findViewById(R.id.text_email)
        firstNameTextView = profileLayout.findViewById(R.id.text_first_name)
        lastNameTextView = profileLayout.findViewById(R.id.text_last_name)
        middleNameTextView = profileLayout.findViewById(R.id.text_middle_name)
        experienceTextView = profileLayout.findViewById(R.id.text_experience)
        specializationTextView = profileLayout.findViewById(R.id.text_specialization)
        specializationInvisibleTextView = profileLayout.findViewById(R.id.specialization_for_invisible)

        emailEditText = profileLayout.findViewById(R.id.edit_email)
        firstNameEditText = profileLayout.findViewById(R.id.edit_first_name)
        lastNameEditText = profileLayout.findViewById(R.id.edit_last_name)
        middleNameEditText = profileLayout.findViewById(R.id.edit_middle_name)
        experienceEditText = profileLayout.findViewById(R.id.edit_experience)
        specializationEditText = profileLayout.findViewById(R.id.edit_specialization)

        scrollView.setOnClickListener {
            scrollView.clearFocus()
            hideKeyboard()
        }
        profileLayout.setOnClickListener {
            profileLayout.clearFocus()
            hideKeyboard()
        }

        val prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val token = prefs.getString("jwt_token", null)
        val userId = prefs.getString("user_id", null)
        val userType = prefs.getString("user_type", null)

        if (token == null || userId == null || userType == null) {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
            return rootView
        }

        isDoctor = userType.equals("doctor", ignoreCase = true)

        observeViewModelData()

        if (viewModel.isDataLoaded.value != true) {
            loadUserProfile(token, userId)
        } else {

            rootView.findViewById<ShimmerFrameLayout>(R.id.goat_profile)?.visibility = View.GONE
        }

        viewModel.isEditing.observe(viewLifecycleOwner) { editing ->
            updateUIForEditing(editing)
            requireActivity().invalidateOptionsMenu()
        }

        val signOutButton = profileLayout.findViewById<Button>(R.id.sign_out)
        signOutButton.setOnClickListener {
            prefs.edit().clear().apply()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }

        val deleteAccountView = profileLayout.findViewById<TextView>(R.id.delete_account)
        deleteAccountView.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            val dialogView = LayoutInflater.from(context).inflate(R.layout.custom_alert_delete_dialog, null)
            builder.setView(dialogView)
            val alertDialog = builder.create()
            val positiveButton = dialogView.findViewById<TextView>(R.id.positiveButtonDelete)
            val negativeButton = dialogView.findViewById<TextView>(R.id.negativeButtonDelete)

            positiveButton.setOnClickListener {
                alertDialog.dismiss()
                deleteAccount(token, userType)
            }
            negativeButton.setOnClickListener { alertDialog.dismiss() }
            alertDialog.show()
            alertDialog.window?.setBackgroundDrawableResource(R.drawable.alertdialog_background)
        }

        return rootView
    }

    private fun observeViewModelData() {
        viewModel.email.observe(viewLifecycleOwner) { email ->
            if (viewModel.isEditing.value == false) {
                emailTextView.text = email
            }
            if (emailEditText.text.toString() != email) {
                emailEditText.setText(email)
            }
        }

        viewModel.firstName.observe(viewLifecycleOwner) { firstName ->
            if (viewModel.isEditing.value == false) {
                firstNameTextView.text = firstName
            }
            if (firstNameEditText.text.toString() != firstName) {
                firstNameEditText.setText(firstName)
            }
        }

        viewModel.lastName.observe(viewLifecycleOwner) { lastName ->
            if (viewModel.isEditing.value == false) {
                lastNameTextView.text = lastName
            }
            if (lastNameEditText.text.toString() != lastName) {
                lastNameEditText.setText(lastName)
            }
        }

        viewModel.middleName.observe(viewLifecycleOwner) { middleName ->
            if (viewModel.isEditing.value == false) {
                middleNameTextView.text = middleName
            }
            if (middleNameEditText.text.toString() != middleName) {
                middleNameEditText.setText(middleName)
            }
        }

        viewModel.experience.observe(viewLifecycleOwner) { exp ->
            if (viewModel.isEditing.value == false) {
                experienceTextView.text = exp
            }
            if (experienceEditText.text.toString() != exp) {
                experienceEditText.setText(exp)
            }
        }

        viewModel.specialization.observe(viewLifecycleOwner) { spec ->
            if (viewModel.isEditing.value == false) {
                specializationTextView.text = spec
            }
            if (specializationEditText.text.toString() != spec) {
                specializationEditText.setText(spec)
            }
        }
    }

    private fun loadUserProfile(token: String, userId: String) {
        val baseUrl = "http://192.168.0.159:8080"
        val endpoint = if (isDoctor) "/doctors" else "/nurses"
        val profileUrl = "$baseUrl$endpoint/$userId"

        val request = Request.Builder()
            .url(profileUrl)
            .get()
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    showToast("Ошибка загрузки профиля: ${e.message}")
                    hideShimmerAfterDelay()
                }
            }
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    requireActivity().runOnUiThread {
                        hideShimmerAfterDelay()
                    }
                    if (!response.isSuccessful) {
                        requireActivity().runOnUiThread {
                            showToast("Ошибка загрузки профиля: код ${response.code}")
                        }
                    } else {
                        val bodyStr = response.body?.string()
                        if (!bodyStr.isNullOrEmpty()) {
                            try {
                                val json = JSONObject(bodyStr)

                                viewModel.profileId = json.optString("id")

                                viewModel.email.postValue(json.optString("email", ""))
                                viewModel.firstName.postValue(json.optString("firstName", ""))
                                viewModel.lastName.postValue(json.optString("lastName", ""))
                                viewModel.middleName.postValue(json.optString("middleName", ""))
                                viewModel.experience.postValue(json.optString("experience", ""))
                                viewModel.specialization.postValue(json.optString("specialization", ""))

                                viewModel.isDataLoaded.postValue(true)
                            } catch (ex: Exception) {
                                requireActivity().runOnUiThread {
                                    showToast("Ошибка парсинга профиля: ${ex.message}")
                                }
                            }
                        }
                    }
                }
            }
        })
    }

    private fun hideShimmerAfterDelay() {
        lifecycleScope.launch {
            delay(1000)
            rootView.findViewById<ShimmerFrameLayout>(R.id.goat_profile)?.visibility = View.GONE
        }
    }

    private fun saveProfileData() {
        val email = emailEditText.text.toString().trim()
        if (!isValidEmail(email)) {
            showToast("Введите корректный email адрес!")
            return
        }
        val firstName = firstNameEditText.text.toString().trim()
        val lastName = lastNameEditText.text.toString().trim()
        val middleName = middleNameEditText.text.toString().trim()
        val experience = experienceEditText.text.toString().trim()
        val specialization = if (isDoctor) specializationEditText.text.toString().trim() else ""

        val userUpdates = JSONObject().apply {
            if (email != viewModel.email.value) put("email", email)
            if (firstName != viewModel.firstName.value) put("firstName", firstName)
            if (lastName != viewModel.lastName.value) put("lastName", lastName)
            if (middleName != viewModel.middleName.value) put("middleName", middleName)
            if (experience != viewModel.experience.value) put("experience", experience)
            if (isDoctor && specialization != viewModel.specialization.value) {
                put("specialization", specialization)
            }
        }

        if (userUpdates.length() == 0) {
            showToast("Данные не изменились")
            return
        }

        val prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val token = prefs.getString("jwt_token", null)
        val profId = viewModel.profileId
        if (profId == null || token == null) {
            showToast("Ошибка: нет профиля или токена")
            return
        }

        val baseUrl = "http://192.168.0.159:8080"
        val endpoint = if (isDoctor) "/doctors" else "/nurses"
        val updateUrl = "$baseUrl$endpoint/$profId"

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val requestBody = userUpdates.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(updateUrl)
            .patch(requestBody)
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    showToast("Ошибка обновления профиля: ${e.message}")
                    revertUIToOriginal()
                    hideKeyboard()
                }
            }
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    requireActivity().runOnUiThread {
                        hideKeyboard()
                        if (response.isSuccessful) {
                            showToast("Изменения сохранены!")
                            viewModel.email.value = email
                            viewModel.firstName.value = firstName
                            viewModel.lastName.value = lastName
                            viewModel.middleName.value = middleName
                            viewModel.experience.value = experience
                            if (isDoctor) {
                                viewModel.specialization.value = specialization
                            }
                        } else {
                            showToast("Ошибка обновления: код ${response.code}")
                            revertUIToOriginal()
                        }
                    }
                }
            }
        })
    }

    private fun revertUIToOriginal() {
        emailEditText.setText(viewModel.email.value)
        firstNameEditText.setText(viewModel.firstName.value)
        lastNameEditText.setText(viewModel.lastName.value)
        middleNameEditText.setText(viewModel.middleName.value)
        experienceEditText.setText(viewModel.experience.value)
        if (isDoctor) {
            specializationEditText.setText(viewModel.specialization.value)
        }
    }

    private fun deleteAccount(token: String, userType: String) {
        val profId = viewModel.profileId
        if (profId == null) {
            showToast("Профиль не загружен")
            return
        }
        val baseUrl = "http://192.168.0.159:8080"
        val endpoint = if (userType.equals("doctor", ignoreCase = true)) "/doctors" else "/nurses"
        val deleteUrl = "$baseUrl$endpoint/$profId"

        val request = Request.Builder()
            .url(deleteUrl)
            .delete()
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    showToast("Ошибка удаления: ${e.message}")
                }
            }
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    requireActivity().runOnUiThread {
                        if (response.isSuccessful) {
                            showToast("Аккаунт удален!")
                            val prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
                            prefs.edit().clear().apply()
                            startActivity(Intent(requireContext(), LoginActivity::class.java))
                            requireActivity().finish()
                        } else {
                            showToast("Ошибка удаления: код ${response.code}")
                        }
                    }
                }
            }
        })
    }

    private fun updateUIForEditing(editing: Boolean) {
        // Управляем видимостью TextView и EditText в зависимости от режима редактирования
        if (editing) {
            emailTextView.visibility = View.GONE
            emailEditText.visibility = View.VISIBLE
            firstNameTextView.visibility = View.GONE
            firstNameEditText.visibility = View.VISIBLE
            lastNameTextView.visibility = View.GONE
            lastNameEditText.visibility = View.VISIBLE
            middleNameTextView.visibility = View.GONE
            middleNameEditText.visibility = View.VISIBLE
            experienceTextView.visibility = View.GONE
            experienceEditText.visibility = View.VISIBLE
            if (isDoctor) {
                specializationTextView.visibility = View.GONE
                specializationEditText.visibility = View.VISIBLE
            }
        } else {
            emailTextView.visibility = View.VISIBLE
            emailEditText.visibility = View.GONE
            firstNameTextView.visibility = View.VISIBLE
            firstNameEditText.visibility = View.GONE
            lastNameTextView.visibility = View.VISIBLE
            lastNameEditText.visibility = View.GONE
            middleNameTextView.visibility = View.VISIBLE
            middleNameEditText.visibility = View.GONE
            experienceTextView.visibility = View.VISIBLE
            experienceEditText.visibility = View.GONE
            if (isDoctor) {
                specializationTextView.visibility = View.VISIBLE
                specializationEditText.visibility = View.GONE
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val editItem = menu.findItem(R.id.action_edit)
        val closeItem = menu.findItem(R.id.action_close_edit)

        if (viewModel.isEditing.value == true) {
            editItem?.isVisible = false
            closeItem?.isVisible = true
            (requireActivity() as MainActivity).showSaveButton()
        } else {
            editItem?.isVisible = true
            closeItem?.isVisible = false
            (requireActivity() as MainActivity).closeSaveButton()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_edit -> {
                viewModel.isEditing.value = true
                false
            }
            R.id.action_close_edit -> {
                revertUIToOriginal()
                viewModel.isEditing.value = false
                hideKeyboard()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).setToolbarSaveButtonListener {
            saveProfileData()
            viewModel.isEditing.value = false
        }
    }

    private fun hideKeyboard() {
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusView = requireActivity().currentFocus
        if (currentFocusView != null) {
            imm.hideSoftInputFromWindow(currentFocusView.windowToken, 0)
        } else {
            imm.hideSoftInputFromWindow(rootView.windowToken, 0)
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
        return email.matches(emailRegex.toRegex())
    }

    private fun showToast(message: String) {
        if (!alreadyShownToast) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            Handler(Looper.getMainLooper()).postDelayed({
                alreadyShownToast = false
            }, 3000)
            alreadyShownToast = true
        }
    }

    fun onHideEditText() {
        viewModel.isEditing.value = false
    }
}
