package com.example.meditrack.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.meditrack.entities.Patient

class PatientViewModel : ViewModel() {
    private val selectedPatient: MutableLiveData<Patient> = MutableLiveData<Patient>()
    private val _allPatients = MutableLiveData<List<Patient>>(emptyList())
    val allPatients: LiveData<List<Patient>> get() = _allPatients

    fun selectPatient(patient: Patient) {
        selectedPatient.value = patient
    }

    fun getSelectedPatient(): LiveData<Patient> {
        return selectedPatient
    }

    fun setAllPatients(newList: List<Patient>) {
        _allPatients.value = newList
    }
}
