package com.example.meditrack.holders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.meditrack.R

class PatientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var avatar: ImageView = itemView.findViewById(R.id.avatar)
    var name: TextView = itemView.findViewById(R.id.patient)
    var sex: TextView = itemView.findViewById(R.id.sex2)
    var doctorName: TextView = itemView.findViewById(R.id.doctor2)
    var room: TextView = itemView.findViewById(R.id.room2)
}