package com.example.pathtracker

import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
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
import androidx.core.content.ContextCompat
import com.example.pathtracker.ui.theme.PathtrackerTheme

class MainActivity : ComponentActivity() {
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
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
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        locationListener = LocationListener {}
        if (!isLocationPermissionGranted()) requestLocationPermission()
    }

    override fun onResume() {
        super.onResume()
        if (isLocationPermissionGranted()) startLocationUpdates()
    }

    /**
     * Method to verify permissions:
     * - [Manifest.permission.ACCESS_FINE_LOCATION]
     * - [Manifest.permission.ACCESS_COARSE_LOCATION]
     */
    private fun isLocationPermissionGranted() = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    private fun requestLocationPermission() {
        val locationPermissionRequest =
            registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions(),
            ) { permissions ->
                if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                    permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)
                ) {
                    // Show user location
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.location_permission_denied),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }

        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ),
        )
    }

    private fun startLocationUpdates() {
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            1000,
            10f,
            locationListener
        )
    }
}

@Composable
fun MainScreen(message: String, modifier: Modifier = Modifier) {
    Text(
        text = "$message!",
        modifier = modifier
    )
}
