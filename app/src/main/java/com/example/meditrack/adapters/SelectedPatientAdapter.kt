package com.example.meditrack.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.meditrack.R
import com.example.meditrack.entities.Doctor
import com.example.meditrack.entities.Patient
import com.example.meditrack.holders.PatientViewHolder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Objects
import java.util.Random

class SelectedPatientAdapter(var context: Context, patients: ArrayList<Patient>) :
    RecyclerView.Adapter<PatientViewHolder>() {
    var patients: ArrayList<Patient>
    var menAvatars: IntArray =
        intArrayOf(R.drawable.img_2, R.drawable.img_3, R.drawable.img_6, R.drawable.img_8)
    var womenAvatars: IntArray =
        intArrayOf(R.drawable.img_1, R.drawable.img_4, R.drawable.img_5, R.drawable.img_7)

    init {
        this.patients = patients
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        return PatientViewHolder(
            LayoutInflater.from(context).inflate(R.layout.patient_view, parent, false)
        )
    }

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        val patient: Patient = patients[position]
        val fullName: String =
            patient.firstName + " " + patient.middleName + " " + patient.lastName

        holder.name.text = fullName
        holder.sex.text = patient.sex
        holder.doctorName.text = patient.mainDoctor
        holder.room.text = patient.room

        if (patient.sex.equals("Мужчина")) {
            val randomIndex = Random().nextInt(menAvatars.size)

            holder.avatar.setImageResource(menAvatars[randomIndex])
        } else {
            val randomIndex = Random().nextInt(womenAvatars.size)

            holder.avatar.setImageResource(womenAvatars[randomIndex])
        }
    }

    override fun getItemCount(): Int {
        return patients.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setRecyclerPatients(patients: ArrayList<Patient>) {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = Objects.requireNonNull(user)!!.uid

        val doctorRef = FirebaseDatabase.getInstance().getReference("users/doctors").child(userId)

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

                val filteredPatients: ArrayList<Patient> = ArrayList<Patient>()

                for (patient in patients) {
                    if (patient.mainDoctor != null && patient.mainDoctor
                            .equals(mainDoctor)
                    ) {
                        filteredPatients.add(patient)
                    }
                }

                this@SelectedPatientAdapter.patients = filteredPatients
                notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }
}