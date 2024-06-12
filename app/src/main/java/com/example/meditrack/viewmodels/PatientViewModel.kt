package com.example.meditrack.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.meditrack.entities.Patient

class PatientViewModel : ViewModel() {
    private val selectedPatient: MutableLiveData<Patient> = MutableLiveData<Patient>()

    fun selectPatient(patient: Patient) {
        selectedPatient.value = patient
    }

    fun getSelectedPatient(): LiveData<Patient> {
        return selectedPatient
    }
}
