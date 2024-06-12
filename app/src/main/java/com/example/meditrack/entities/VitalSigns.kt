package com.example.meditrack.entities

class VitalSigns {
    var temperature: String? = null
    var heartRate: String? = null
    var respiratoryRate: String? = null
    var bloodPressure: String? = null
    var oxygenSaturation: String? = null
    var bloodGlucose: String? = null

    constructor(
        temperature: String?, heartRate: String?, respiratoryRate: String?,
        bloodPressure: String?, oxygenSaturation: String?, bloodGlucose: String?
    ) {
        this.temperature = temperature
        this.heartRate = heartRate
        this.respiratoryRate = respiratoryRate
        this.bloodPressure = bloodPressure
        this.oxygenSaturation = oxygenSaturation
        this.bloodGlucose = bloodGlucose
    }

    constructor()
}