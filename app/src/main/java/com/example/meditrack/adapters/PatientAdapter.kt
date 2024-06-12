package com.example.meditrack.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.meditrack.R
import com.example.meditrack.entities.Patient
import com.example.meditrack.holders.PatientViewHolder
import java.util.Random

class PatientAdapter(var context: Context, patients: ArrayList<Patient>) :
    RecyclerView.Adapter<PatientViewHolder>() {
    private var listener: OnItemClickListener? = null
    var patients: ArrayList<Patient>
    private var menAvatars: IntArray =
        intArrayOf(R.drawable.img_2, R.drawable.img_3, R.drawable.img_6, R.drawable.img_8)
    private var womenAvatars: IntArray =
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

        if (patient.sex != null && patient.sex.equals("Мужчина")) {
            val randomIndex = Random().nextInt(menAvatars.size)
            holder.avatar.setImageResource(menAvatars[randomIndex])
        } else {
            val randomIndex = Random().nextInt(womenAvatars.size)
            holder.avatar.setImageResource(womenAvatars[randomIndex])
        }

        holder.itemView.setOnClickListener {
            if (listener != null) {
                listener!!.onItemClick(holder.getAdapterPosition())
            }
        }
    }

    override fun getItemCount(): Int {
        return patients.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setRecyclerPatients(patients: ArrayList<Patient>) {
        this.patients = patients
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        this.listener = listener
    }
}