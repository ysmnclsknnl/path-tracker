package com.example.pathtracker

import android.Manifest
import android.app.ComponentCaller
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.pathtracker.location.createLocationRequest
import com.example.pathtracker.location.isLocationPermissionGranted
import com.example.pathtracker.location.requestLocationPermission
import com.example.pathtracker.ui.theme.PathtrackerTheme
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val locationRequest = createLocationRequest()
    private val locationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { permissions ->
            if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)
            ) {
                checkLocationSettings()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.location_permission_denied),
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    private var isLocationTracked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PathtrackerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        message = "Welcome to Path tracker",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (!isLocationPermissionGranted()) {
            locationPermissionLauncher.requestLocationPermission()
        }
        else {
            checkLocationSettings()
        }
        setUpLocationCallBack()
    }

    private fun setUpLocationCallBack() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.forEach { location ->
                    // updateUI with location data for now just print it
                    Log.d("Main Activity", "latitude ${location.latitude} longitude ${location.longitude}")
                }
            }
        }
    }

    private fun checkLocationSettings() {
        val settingsBuilder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(this)
       client.checkLocationSettings(settingsBuilder.build())
           .addOnSuccessListener {
               startLocationUpdates(locationRequest)
               isLocationTracked = true
           }
           .addOnFailureListener { exception ->
            if (exception is ResolvableApiException){
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(this,
                        REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }
    override fun onResume() {
        super.onResume()
        if(!isLocationTracked && isLocationPermissionGranted()) startLocationUpdates(locationRequest)
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        caller: ComponentCaller
    ) {
        super.onActivityResult(requestCode, resultCode, data, caller)
        if(requestCode == REQUEST_CHECK_SETTINGS)
        {
            if(resultCode == RESULT_OK) startLocationUpdates(locationRequest)
            else Toast.makeText(this, "Location settings are not satisfied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startLocationUpdates(locationRequest: LocationRequest) {
        if (!isLocationPermissionGranted()) {
            Log.w("MainActivity", "Location permission is not granted. Cannot request location updates.")
            return
        }
        if(isLocationTracked) return

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper())
        }
        catch (e: SecurityException) {
            Log.e("MainActivity", "Security Exception ${e.message}")
        }
    }

    private fun stopLocationUpdates() {
        if(::fusedLocationClient.isInitialized && ::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }

    }
    private companion object {
        const val REQUEST_CHECK_SETTINGS = 1001
    }
}

@Composable
fun MainScreen(message: String, modifier: Modifier = Modifier) {
    Text(
        text = "$message!",
        modifier = modifier
    )
}
