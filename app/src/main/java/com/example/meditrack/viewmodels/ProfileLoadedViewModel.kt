package com.example.meditrack.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfileLoadedViewModel : ViewModel() {
    val isDataLoaded = MutableLiveData<Boolean>().apply { value = false }
    val isEditing = MutableLiveData<Boolean>().apply { value = false }

    val email = MutableLiveData<String>()
    val firstName = MutableLiveData<String>()
    val lastName = MutableLiveData<String>()
    val middleName = MutableLiveData<String>()
    val experience = MutableLiveData<String>()
    val specialization = MutableLiveData<String>()

    var profileId: String? = null
}
