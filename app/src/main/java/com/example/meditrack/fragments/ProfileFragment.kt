package com.example.meditrack.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import com.example.meditrack.R
import com.example.meditrack.activities.LoginActivity
import com.example.meditrack.activities.MainActivity
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Objects

class ProfileFragment : Fragment() {
    private var alreadyShownToast = false
    private var isDoctor = false
    private lateinit var profileLayout: LinearLayout
    private lateinit var scrollView: NestedScrollView
    private lateinit var rootView: View

    private var user: FirebaseUser? = null
    private var userRef: DatabaseReference? = null
    private var userId: String? = null


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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
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
        specializationInvisibleTextView =
            profileLayout.findViewById(R.id.specialization_for_invisible)

        emailEditText = profileLayout.findViewById(R.id.edit_email)
        firstNameEditText = profileLayout.findViewById(R.id.edit_first_name)
        lastNameEditText = profileLayout.findViewById(R.id.edit_last_name)
        middleNameEditText = profileLayout.findViewById(R.id.edit_middle_name)
        experienceEditText = profileLayout.findViewById(R.id.edit_experience)
        specializationEditText = profileLayout.findViewById(R.id.edit_specialization)

        scrollView.setOnClickListener { view: View ->
            scrollView.clearFocus()
            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        profileLayout.setOnClickListener { view: View ->
            profileLayout.clearFocus()
            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        user = FirebaseAuth.getInstance().currentUser
        userId = Objects.requireNonNull(user)!!.uid
        val doctorsRef = FirebaseDatabase.getInstance().getReference("users/doctors")

        doctorsRef.child(userId!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    isDoctor = true
                }

                userRef = if (isDoctor) {
                    FirebaseDatabase.getInstance().getReference("users/doctors").child(
                        userId!!
                    )
                } else {
                    FirebaseDatabase.getInstance().getReference("users/nurses").child(
                        userId!!
                    )
                }

                userRef!!.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val email = user!!.email
                        val firstName = snapshot.child("firstName").getValue(
                            String::class.java
                        )
                        val lastName = snapshot.child("lastName").getValue(
                            String::class.java
                        )
                        val middleName = snapshot.child("middleName").getValue(
                            String::class.java
                        )
                        val experience = snapshot.child("experience").getValue(
                            String::class.java
                        )

                        if (isDoctor) {
                            val specialization = snapshot.child("specialization").getValue(
                                String::class.java
                            )
                            specializationEditText.setText(specialization)
                            specializationTextView.text = specialization
                        } else {
                            specializationInvisibleTextView.visibility = View.GONE
                            specializationTextView.visibility = View.GONE
                            specializationEditText.visibility = View.GONE
                        }

                        emailEditText.setText(email)
                        firstNameEditText.setText(firstName)
                        lastNameEditText.setText(lastName)
                        middleNameEditText.setText(middleName)
                        experienceEditText.setText(experience)

                        emailTextView.text = email
                        firstNameTextView.text = firstName
                        lastNameTextView.text = lastName
                        middleNameTextView.text = middleName
                        experienceTextView.text = experience
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
                Handler(Looper.getMainLooper()).postDelayed({
                    val shimmerLinearLayout =
                        rootView.findViewById<ShimmerFrameLayout>(R.id.goat_profile)
                    shimmerLinearLayout.visibility = View.GONE
                }, 1000)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        val signOutButton = profileLayout.findViewById<Button>(R.id.sign_out)
        val deleteAccountView = profileLayout.findViewById<TextView>(R.id.delete_account)

        signOutButton.setOnClickListener {
            val builder =
                AlertDialog.Builder(context)
            val dialogView: View = inflater.inflate(R.layout.custom_alert_dialog, null)

            builder.setView(dialogView)

            val alertDialog = builder.create()

            val positiveButton =
                dialogView.findViewById<TextView>(R.id.positiveButton)
            val negativeButton =
                dialogView.findViewById<TextView>(R.id.negativeButton)

            positiveButton.setOnClickListener {
                val mAuth = FirebaseAuth.getInstance()
                mAuth.signOut()

                alertDialog.dismiss()

                val intent =
                    Intent(context, LoginActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }

            negativeButton.setOnClickListener { alertDialog.dismiss() }

            alertDialog.show()
            Objects.requireNonNull<Window?>(alertDialog.window)
                .setBackgroundDrawableResource(R.drawable.alertdialog_background)
        }

        deleteAccountView.setOnClickListener {
            val builder =
                AlertDialog.Builder(context)
            val dialogView: View =
                inflater.inflate(R.layout.custom_alert_delete_dialog, null)

            builder.setView(dialogView)

            val alertDialog = builder.create()

            val positiveButton =
                dialogView.findViewById<TextView>(R.id.positiveButtonDelete)
            val negativeButton =
                dialogView.findViewById<TextView>(R.id.negativeButtonDelete)

            positiveButton.setOnClickListener {
                alertDialog.dismiss()
                userRef!!.removeValue()
                    .addOnSuccessListener { }
                    .addOnFailureListener { }

                val mAuth = FirebaseAuth.getInstance()
                mAuth.signOut()

                user!!.delete()
                Toast.makeText(requireContext(), "Аккаунт удален!", Toast.LENGTH_SHORT).show()


                val intent =
                    Intent(context, LoginActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
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
            emailTextView.visibility = View.GONE
            emailEditText.visibility = View.VISIBLE
            firstNameTextView.visibility = View.GONE
            firstNameEditText.visibility = View.VISIBLE
            lastNameTextView.visibility = View.GONE
            lastNameEditText.visibility = View.VISIBLE
            middleNameTextView.visibility = View.GONE
            middleNameEditText.visibility = View.VISIBLE
            specializationTextView.visibility = View.GONE
            if (isDoctor) {
                specializationEditText.visibility = View.VISIBLE
            }
            experienceTextView.visibility = View.GONE
            experienceEditText.visibility = View.VISIBLE

            item.setVisible(false)

            (requireActivity() as MainActivity).showSaveButton()
            (requireActivity() as MainActivity).showCloseEditButton()
            (requireActivity() as MainActivity).showCloseEditButton()

            return true
        } else if (id == R.id.action_close_edit) {
            emailTextView.visibility = View.VISIBLE
            emailEditText.visibility = View.GONE
            firstNameTextView.visibility = View.VISIBLE
            firstNameEditText.visibility = View.GONE
            lastNameTextView.visibility = View.VISIBLE
            lastNameEditText.visibility = View.GONE
            middleNameTextView.visibility = View.VISIBLE
            middleNameEditText.visibility = View.GONE
            if (isDoctor) {
                specializationTextView.visibility = View.VISIBLE
            }
            specializationEditText.visibility = View.GONE
            experienceTextView.visibility = View.VISIBLE
            experienceEditText.visibility = View.GONE

            item.setVisible(false)

            (requireActivity() as MainActivity).showEditButton()
            (requireActivity() as MainActivity).closeSaveButton()

            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()

        (requireActivity() as MainActivity).setToolbarSaveButtonListener { v ->
            val email = emailEditText.text.toString()
            if (!isValidEmail(email)) {
                if (!alreadyShownToast) {
                    Toast.makeText(
                        requireContext(),
                        "Введите корректный email адрес!",
                        Toast.LENGTH_SHORT
                    ).show()
                    Handler(Looper.getMainLooper()).postDelayed({
                        alreadyShownToast = false
                    }, 3000)
                }
                return@setToolbarSaveButtonListener
            }
            val firstName = firstNameEditText.text.toString()
            val lastName = lastNameEditText.text.toString()
            val middleName = middleNameEditText.text.toString()
            val experience = experienceEditText.text.toString()
            val specialization = specializationEditText.text.toString()

            val userUpdates: MutableMap<String, Any> =
                HashMap()
            userUpdates["email"] = email
            userUpdates["firstName"] = firstName
            userUpdates["lastName"] = lastName
            userUpdates["middleName"] = middleName
            userUpdates["experience"] = experience
            if (isDoctor) {
                userUpdates["specialization"] = specialization
            }

            userRef = if (isDoctor) {
                FirebaseDatabase.getInstance().getReference("users/doctors").child(userId!!)
            } else {
                FirebaseDatabase.getInstance().getReference("users/nurses").child(userId!!)
            }

            userRef!!.updateChildren(userUpdates)

            emailTextView.visibility = View.VISIBLE
            emailEditText.visibility = View.GONE
            firstNameTextView.visibility = View.VISIBLE
            firstNameEditText.visibility = View.GONE
            lastNameTextView.visibility = View.VISIBLE
            lastNameEditText.visibility = View.GONE
            middleNameTextView.visibility = View.VISIBLE
            middleNameEditText.visibility = View.GONE
            if (isDoctor) {
                specializationTextView.visibility = View.VISIBLE
            }
            specializationEditText.visibility = View.GONE
            experienceTextView.visibility = View.VISIBLE
            experienceEditText.visibility = View.GONE


            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(rootView.windowToken, 0)

            (requireActivity() as MainActivity).closeSaveButton()
            (requireActivity() as MainActivity).closeCloseEditButton()
            (requireActivity() as MainActivity).showEditButton()
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex =
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"

        return email.matches(emailRegex.toRegex())
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {
            emailTextView.visibility = View.VISIBLE
            emailEditText.visibility = View.GONE
            firstNameTextView.visibility = View.VISIBLE
            firstNameEditText.visibility = View.GONE
            lastNameTextView.visibility = View.VISIBLE
            lastNameEditText.visibility = View.GONE
            middleNameTextView.visibility = View.VISIBLE
            middleNameEditText.visibility = View.GONE
            if (isDoctor) {
                specializationTextView.visibility = View.VISIBLE
            }
            specializationEditText.visibility = View.GONE
            experienceTextView.visibility = View.VISIBLE
            experienceEditText.visibility = View.GONE

            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(requireView().windowToken, 0)
        }
    }

    fun onHideEditText() {
        emailTextView.visibility = View.VISIBLE
        emailEditText.visibility = View.GONE
        firstNameTextView.visibility = View.VISIBLE
        firstNameEditText.visibility = View.GONE
        lastNameTextView.visibility = View.VISIBLE
        lastNameEditText.visibility = View.GONE
        middleNameTextView.visibility = View.VISIBLE
        middleNameEditText.visibility = View.GONE
        if (isDoctor) {
            specializationTextView.visibility = View.VISIBLE
        }
        specializationEditText.visibility = View.GONE
        experienceTextView.visibility = View.VISIBLE
        experienceEditText.visibility = View.GONE
    }
}