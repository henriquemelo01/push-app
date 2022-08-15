package com.example.pushapp.utils

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

open class BlePermissionsHandler(
    private val activityResultRegistry: ActivityResultRegistry,
    private val context: Context
) : DefaultLifecycleObserver {

    private var requestPermissions : ActivityResultLauncher<Array<String>>? = null

    private var turnOnBluetoothRequest: ActivityResultLauncher<Intent>? = null

    private var enableGps: ActivityResultLauncher<Intent>? = null

    private var onBluetoothPermissionsGranted: (() -> Unit)? = null

    private var onBluetoothPermissionDenied: (() -> Unit)? = null

    private var onEnableBleAccepted: (() -> Unit)? = null

    private var onEnableBleDenied: (() -> Unit)? = null

    private var onEnableGpsAccepted: (() -> Unit)? = null

    private var onEnableGpsDenied: (() -> Unit)? = null

    private var onPermissionsAccepted: (() -> Unit)? = null

    private var onPermissionsDenied: (() -> Unit)? = null

    private val blePermissions = arrayOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    override fun onCreate(owner: LifecycleOwner) {

        requestPermissions = activityResultRegistry.register(
            BLE_PERMISSION_REQUEST,
            owner,
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissionsMap ->
            permissionsMap?.let {
                // Não tenho certeza se validei de forma correta se as permissões foram aceitas
                if(allPermissionsGranted())
                    gpsNeeded()
//                    onBluetoothPermissionsGranted?.invoke()
                else
                    onBluetoothPermissionDenied?.invoke()
//                    onPermissionsDenied?.invoke()
            }
        }

        turnOnBluetoothRequest = activityResultRegistry.register(
            TURN_ON_BLE_REQUEST,
            owner,
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                onEnableBleAccepted?.invoke()
            } else {
                onEnableBleDenied?.invoke()
            }

        }

        enableGps = activityResultRegistry.register(
            TURN_ON_GPS_REQUEST,
            owner,
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                onPermissionsAccepted?.invoke()
//                onEnableGpsAccepted?.invoke()
            } else {
                onPermissionsDenied?.invoke()
//                onEnableGpsDenied?.invoke()
            }
        }
    }

    fun checkPermissions() {
        if (allPermissionsGranted()) {
//            onBluetoothPermissionsGranted?.invoke()
            gpsNeeded()
        } else {
            requestPermissions?.launch(blePermissions)
        }
    }

    fun turnOnBluetooth() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        turnOnBluetoothRequest?.launch(enableBtIntent)
    }

    private fun activateGps() {
        val enableGpsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        enableGps?.launch(enableGpsIntent)
    }

    private fun allPermissionsGranted() = blePermissions.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkGPSIsOpen(): Boolean {
        val locationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
                ?: return false
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun gpsNeeded(){
        if(checkGPSIsOpen())
            onPermissionsAccepted?.invoke()
        else{
            activateGps()
        }
    }

    fun setOnBluetoothPermissionsGranted(callback: () -> Unit) {
        onBluetoothPermissionsGranted = callback
    }

    fun setOnBluetoothPermissionsDenied(callback: () -> Unit) {
        onBluetoothPermissionDenied = callback
    }

    fun setOnEnableBleAccepted(callback: () -> Unit) {
        onEnableBleAccepted = callback
    }

    fun setOnEnableBleDenied(callback: () -> Unit) {
        onEnableBleDenied = callback
    }

    fun setOnEnableGpsAccepted(callback: () -> Unit) {
        onEnableGpsAccepted = callback
    }

    fun setOnEnableGpsDenied(callback: () -> Unit) {
        onEnableGpsDenied = callback
    }

    fun setOnPermissionsAccepted(callback: () -> Unit) {
        onPermissionsAccepted = callback
    }

    fun setOnPermissionsDenied(callback: () -> Unit) {
        onPermissionsDenied = callback
    }

    private companion object {
        const val BLE_PERMISSION_REQUEST = "blePermissionRequest"
        const val TURN_ON_BLE_REQUEST = "turnOnBleRequest"
        const val TURN_ON_GPS_REQUEST = "turnOnGpsRequest"
    }
}

