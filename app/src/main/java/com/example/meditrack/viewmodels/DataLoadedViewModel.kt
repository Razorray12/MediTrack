package com.example.meditrack.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DataLoadedViewModel : ViewModel() {
    val isDataLoaded = MutableLiveData(false)
}