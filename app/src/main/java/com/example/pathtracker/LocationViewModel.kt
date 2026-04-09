package com.example.pathtracker

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocationViewModel : ViewModel() {
    private val _locationState  = MutableStateFlow<List<Location>>(emptyList())
    val locationState = _locationState.asStateFlow()

    fun updateLocation(location: Location) {
        _locationState.value = _locationState.value.plus(location)
    }

}