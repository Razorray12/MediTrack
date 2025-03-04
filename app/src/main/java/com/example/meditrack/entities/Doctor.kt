package com.example.meditrack.entities

class Doctor(
    var id: String?,
    var email: String?,
    var password: String?,
    var firstName: String?,
    var lastName: String?,
    var middleName: String?,
    var experience: String?,
    var specialization: String?
) {
    private var patients: ArrayList<Patient>? = null

    init {
        this.patients = ArrayList()
    }

}