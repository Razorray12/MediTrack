package com.example.meditrack.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfileLoadedViewModel : ViewModel() {
    val isDataLoaded = MutableLiveData(false)
    val isEditing = MutableLiveData(false)
}