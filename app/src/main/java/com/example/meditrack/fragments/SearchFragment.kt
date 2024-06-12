package com.example.meditrack.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.meditrack.R
import com.example.meditrack.adapters.PatientAdapter
import com.example.meditrack.entities.Patient
import com.example.meditrack.viewmodels.PatientViewModel
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale

class SearchFragment : Fragment() {
    private var fragmentSwitchListener: OnFragmentSwitchListener? = null
    private lateinit var recyclerView: RecyclerView
    private var database: DatabaseReference? = null
    var patientAdapter: PatientAdapter? = null
    var patients: ArrayList<Patient> = ArrayList()
    private var shimmerView1: ShimmerFrameLayout? = null
    private var shimmerView2: ShimmerFrameLayout? = null
    private var shimmerView3: ShimmerFrameLayout? = null
    private var shimmerView4: ShimmerFrameLayout? = null
    private var shimmerView5: ShimmerFrameLayout? = null


    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        database = FirebaseDatabase.getInstance().getReference("users/patients")
        recyclerView = view.findViewById(R.id.recyclerview_patient)

        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)

        patientAdapter = PatientAdapter(requireContext(), patients)

        patientAdapter?.setOnItemClickListener(object : PatientAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                fragmentSwitchListener?.let {
                    val clickedPatient: Patient = patients[position]
                    val patientViewModel: PatientViewModel = ViewModelProvider(requireActivity())[PatientViewModel::class.java]
                    patientViewModel.selectPatient(clickedPatient)
                    it.onSwitchToInformationFragment()
                }
            }
        })


        recyclerView.adapter = patientAdapter

        database?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newPatients = ArrayList<Patient>()
                for (patientSnapshot in snapshot.children) {
                    val patient: Patient? = patientSnapshot.getValue(Patient::class.java)
                    patient?.let { newPatients.add(it) }
                }
                patients = newPatients
                patientAdapter?.setRecyclerPatients(newPatients)
                patientAdapter?.notifyDataSetChanged()

                Handler(Looper.getMainLooper()).postDelayed({
                    val shimmerLinearLayout =
                        view.findViewById<LinearLayout>(R.id.shimmer_view_container)
                    shimmerView1 = view.findViewById(R.id.shimmer_view_1)
                    shimmerView2 = view.findViewById(R.id.shimmer_view_2)
                    shimmerView3 = view.findViewById(R.id.shimmer_view_3)
                    shimmerView4 = view.findViewById(R.id.shimmer_view_4)
                    shimmerView5 = view.findViewById(R.id.shimmer_view_5)


                    shimmerView1?.stopShimmer()
                    shimmerView2?.stopShimmer()
                    shimmerView3?.stopShimmer()
                    shimmerView4?.stopShimmer()
                    shimmerView5?.stopShimmer()
                    shimmerLinearLayout.visibility = View.GONE
                }, 2000)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        return view
    }

    @SuppressLint("NotifyDataSetChanged")
    fun onQueryTextSubmit(query: String) {
        val filteredPatients: ArrayList<Patient> = filterPatients(patients, query)
        patientAdapter?.setRecyclerPatients(filteredPatients)
        patientAdapter?.notifyDataSetChanged()
    }

    private fun filterPatients(patients: ArrayList<Patient>, query: String): ArrayList<Patient> {
        val filteredPatients: ArrayList<Patient> = ArrayList()
        for (patient in patients) {
            patient.let {
                if (it.firstName!!.lowercase(Locale.getDefault())
                        .contains(query.lowercase(Locale.getDefault())) ||
                    it.lastName!!.lowercase(Locale.getDefault())
                        .contains(query.lowercase(Locale.getDefault())) ||
                    it.middleName!!.lowercase(Locale.getDefault())
                        .contains(query.lowercase(Locale.getDefault()))
                ) {
                    filteredPatients.add(it)
                }
            }
        }
        return filteredPatients
    }

    @SuppressLint("NotifyDataSetChanged")
    fun showAllPatients() {
        patientAdapter?.setRecyclerPatients(patients)
        patientAdapter?.notifyDataSetChanged()
    }

    interface OnFragmentSwitchListener {
        fun onSwitchToInformationFragment()
    }

    fun setOnFragmentSwitchListener(listener: OnFragmentSwitchListener?) {
        this.fragmentSwitchListener = listener
    }
}
