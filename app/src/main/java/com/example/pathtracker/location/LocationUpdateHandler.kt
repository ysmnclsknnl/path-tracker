package com.example.pathtracker.location

import android.Manifest
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import com.example.pathtracker.LocationViewModel
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority

internal class LocationUpdateHandler(private val activity: Activity) {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val locationRequest = createLocationRequest()
    private var isLocationTracked = false

    internal fun setLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
    }

    internal fun setUpLocationCallBack(locationViewModel: LocationViewModel) {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.forEach { location ->
                    locationViewModel.updateLocation(location)
                }
            }
        }
    }

    internal fun createLocationRequest() = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        LOCATION_UPDATE_INTERVAL_IN_MILLISECONDS
    )
        .setMinUpdateIntervalMillis(FASTEST_LOCATION_UPDATE_INTERVAL_IN_MILLISECONDS)
        .build()

    internal fun startLocationUpdatesIfSettingsEnabled() {
        val settingsBuilder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(activity)
        client.checkLocationSettings(settingsBuilder.build())
            .addOnSuccessListener {
               startLocationUpdates()
            }
            .addOnFailureListener { exception ->
                isLocationTracked = false
                if (exception is ResolvableApiException) {
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        exception.startResolutionForResult(
                            activity,
                            REQUEST_CHECK_SETTINGS
                        )
                    } catch (sendEx: IntentSender.SendIntentException) {
                        // Ignore the error.
                    }
                }
            }
    }

    internal fun startLocationUpdates() {
        when {
            !isLocationPermissionGranted() -> return
            isLocationTracked -> return
            else -> try {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            } catch (e: Exception) {
                Log.e(
                    TAG.toString(),
                    "Error occurred while starting location updates error: ${e.message}"
                )
            }
        }
    }

    internal fun stopLocationUpdates() {
        if (::fusedLocationClient.isInitialized && ::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            isLocationTracked = false
        }
    }

    /**
     * Method to verify permissions:
     * - [Manifest.permission.ACCESS_FINE_LOCATION]
     * - [Manifest.permission.ACCESS_COARSE_LOCATION]
     */
    internal fun isLocationPermissionGranted() = ContextCompat.checkSelfPermission(
        activity,
        ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
        activity,
        ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    internal fun requestLocationPermission(launcher: ActivityResultLauncher<Array<String>>) {
        if (isLocationPermissionGranted()) return
        launcher.launch(
            arrayOf(
                ACCESS_FINE_LOCATION,
                ACCESS_COARSE_LOCATION,
            ),
        )

    }

    internal companion object {
        private const val LOCATION_UPDATE_INTERVAL_IN_MILLISECONDS = 5000L
        private const val FASTEST_LOCATION_UPDATE_INTERVAL_IN_MILLISECONDS = 1000L
        const val REQUEST_CHECK_SETTINGS = 1001
        private val TAG = LocationUpdateHandler::class
    }

}