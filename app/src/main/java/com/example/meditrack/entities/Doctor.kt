package com.example.meditrack.entities

class Doctor {
    var firstName: String? = null
    var lastName: String? = null
    var middleName: String? = null
    var id: String? = null
    var specialization: String? = null
    var experience: String? = null
    private var patients: ArrayList<Patient>? = null

    constructor(
        id: String?,
        firstName: String?,
        lastName: String?,
        middleName: String?,
        experience: String?,
        specialization: String?
    ) {
        this.firstName = firstName
        this.lastName = lastName
        this.middleName = middleName
        this.id = id
        this.specialization = specialization
        this.experience = experience
        this.patients = ArrayList()
    }

    constructor()

    fun addPatient(patient: Patient) {
        patients!!.add(patient)
    }

    fun removePatient(patient: Patient) {
        patients!!.remove(patient)
    }
}