package com.example.meditrack.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.meditrack.R
import com.example.meditrack.adapters.SelectedPatientAdapter
import com.example.meditrack.entities.Patient
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SelectedPatientsFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private var database: DatabaseReference? = null
    var patientAdapter: SelectedPatientAdapter? = null
    var patients: ArrayList<Patient> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_selected_patients, container, false)
        database = FirebaseDatabase.getInstance().getReference("users/patients")
        recyclerView = view.findViewById(R.id.recyclerview_patient2)

        val layoutManager = LinearLayoutManager(context)
        recyclerView.setLayoutManager(layoutManager)
        recyclerView.setHasFixedSize(true)

        patientAdapter = SelectedPatientAdapter(requireContext(), patients)
        recyclerView.adapter = patientAdapter

        database!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                patients.clear()
                for (patientSnapshot in snapshot.children) {
                    val patient: Patient? = patientSnapshot.getValue(Patient::class.java)
                    patient?.let { patients.add(it) }
                }
                patientAdapter?.setRecyclerPatients(patients)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })


        return view
    }
}