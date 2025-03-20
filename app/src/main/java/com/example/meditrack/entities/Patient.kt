package com.example.meditrack.entities

class Patient {
    var id: String? = null
    var firstName: String? = null
    var lastName: String? = null
    var middleName: String? = null
    var birthDate: String? = null
    var phoneNumber: String? = null
    var diagnosis: String? = null
    var room: String? = null
    var sex: String? = null
    var admissionDate: String? = null
    var medications: String? = null
    var allergies: String? = null
    var vitalSigns: VitalSigns? = null
    var mainDoctor: String? = null
    var mainDoctorID: String? = null

    constructor(
        id: String?, firstName: String?, lastName: String?, middleName: String?,
        birthDate: String?, phoneNumber: String?, diagnosis: String?,
        room: String?, admissionDate: String?, allergies: String?,
        medications: String?, vitalSigns: VitalSigns?, sex: String?, mainDoctor: String?, mainDoctorID: String?
    ) {
        this.id = id
        this.firstName = firstName
        this.lastName = lastName
        this.middleName = middleName
        this.birthDate = birthDate
        this.phoneNumber = phoneNumber
        this.diagnosis = diagnosis
        this.room = room
        this.admissionDate = admissionDate
        this.medications = medications
        this.allergies = allergies
        this.vitalSigns = vitalSigns
        this.sex = sex
        this.mainDoctor = mainDoctor
        this.mainDoctorID = mainDoctorID
    }

    constructor()
}