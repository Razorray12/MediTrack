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

class SelectedPatientAdapter(
    private val context: Context,
    private var patients: List<Patient>
) : RecyclerView.Adapter<PatientViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    private var listener: OnItemClickListener? = null

    private val menAvatars = intArrayOf(R.drawable.img_2, R.drawable.img_3, R.drawable.img_6, R.drawable.img_8)
    private val womenAvatars = intArrayOf(R.drawable.img_1, R.drawable.img_4, R.drawable.img_5, R.drawable.img_7)

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.patient_view, parent, false)
        return PatientViewHolder(view)
    }

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        val patient = patients[position]

        val fullName = "${patient.firstName} ${patient.middleName} ${patient.lastName}"
        holder.name.text = fullName
        holder.sex.text = patient.sex
        holder.doctorName.text = patient.mainDoctor
        holder.room.text = patient.room

        if (patient.sex.equals("Мужчина", ignoreCase = true)) {
            val randomIndex = Random().nextInt(menAvatars.size)
            holder.avatar.setImageResource(menAvatars[randomIndex])
        } else {
            val randomIndex = Random().nextInt(womenAvatars.size)
            holder.avatar.setImageResource(womenAvatars[randomIndex])
        }

        holder.itemView.setOnClickListener {
            listener?.onItemClick(holder.adapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return patients.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setRecyclerPatients(newList: List<Patient>?) {
        this.patients = newList ?: emptyList()
        notifyDataSetChanged()
    }
}
