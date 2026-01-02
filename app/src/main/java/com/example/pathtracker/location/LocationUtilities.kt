package com.example.pathtracker.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority

/**
 * Method to verify permissions:
 * - [Manifest.permission.ACCESS_FINE_LOCATION]
 * - [Manifest.permission.ACCESS_COARSE_LOCATION]
 */
internal fun Context.isLocationPermissionGranted() = ContextCompat.checkSelfPermission(
    this,
    Manifest.permission.ACCESS_FINE_LOCATION
) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
    this,
    Manifest.permission.ACCESS_COARSE_LOCATION
) == PackageManager.PERMISSION_GRANTED

internal fun ActivityResultLauncher<Array<String>>.requestLocationPermission() {
    launch(
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ),
    )
}
internal fun createLocationRequest() = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_UPDATE_INTERVAL_IN_MILLISECONDS)
    .setMinUpdateIntervalMillis(FASTEST_LOCATION_UPDATE_INTERVAL_IN_MILLISECONDS)
    .build()

private const val LOCATION_UPDATE_INTERVAL_IN_MILLISECONDS = 5000L
private const val FASTEST_LOCATION_UPDATE_INTERVAL_IN_MILLISECONDS = 1000L
