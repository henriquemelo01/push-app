package com.example.pushapp.ui.workout

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.ScanResult
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.example.pushapp.ui.workout.BluetoothHandlerViewModel.Companion.BLE_ACCELERATION_CHARACTERISTIC_UUID
import com.example.pushapp.ui.workout.BluetoothHandlerViewModel.Companion.BLE_FORCE_CHARACTERISTIC_UUID
import com.example.pushapp.ui.workout.BluetoothHandlerViewModel.Companion.BLE_OFFSET_CHARACTERISTIC_UUID
import com.example.pushapp.ui.workout.BluetoothHandlerViewModel.Companion.BLE_POWER_CHARACTERISTIC_UUID
import com.example.pushapp.ui.workout.BluetoothHandlerViewModel.Companion.BLE_WEIGHT_CHARACTERISTIC_UUID
import com.example.pushapp.ui.workout.BluetoothHandlerViewModel.Companion.BLE_SERVICE_UUID
import com.example.pushapp.ui.workout.BluetoothHandlerViewModel.Companion.BLE_VELOCITY_CHARACTERISTIC_UUID
import com.example.pushapp.ui.workout.BluetoothHandlerViewModel.Companion.SECOND_WORKOUT_BLE_SERVICE
import com.example.pushapp.utils.flowObserver
import com.welie.blessed.*

abstract class BluetoothHandlerFragment : Fragment() {

    abstract val viewModel: BluetoothHandlerViewModel

    private val central by lazy {
        BluetoothCentralManager(
            requireContext(),
            object : BluetoothCentralManagerCallback() {

                override fun onScanFailed(scanFailure: ScanFailure) {
                    println("Falhou: ${scanFailure.value}")

                    viewModel.triggerBleScanFailedEvent(scanFailure)
                }

                override fun onDiscoveredPeripheral(
                    peripheral: BluetoothPeripheral,
                    scanResult: ScanResult
                ) {
                    println("onDiscoveredPeripheral - peripheral: ${peripheral.name}")

                    viewModel.onDiscoveredPeripheral(peripheral)

//                    this@BluetoothHandlerFragment.onDiscoveredPeripherals(peripheral, scanResult)
                }

                override fun onConnectedPeripheral(peripheral: BluetoothPeripheral) {

                    println("onConnectedPeripheral - peripheral: ${peripheral.name}")

                    viewModel.onConnectToESP32(peripheral)
                }

                override fun onDisconnectedPeripheral(
                    peripheral: BluetoothPeripheral,
                    status: HciStatus
                ) {
                    println("onDisconnectedPeripheral - disconnected to ${peripheral.name}")

                    viewModel.onDisconnectToESP32(peripheral)
                }

                override fun onConnectionFailed(
                    peripheral: BluetoothPeripheral,
                    status: HciStatus
                ) {
                    println("onConnectionFailed - failure to connect to ${peripheral.name}\nFailure: $status")

                    viewModel.triggerConnectionFailedESP32Event(peripheral)
                }
            },
            Handler(Looper.getMainLooper())
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupListeners()
    }

    private fun setupListeners() = with(viewModel) {

        foundedPeripherals.observe(viewLifecycleOwner) { peripheralsList ->
            println("Test foundedPeripherals: ${peripheralsList.map { it.address }}")
        }

        flowObserver(foundedEsp32Event) {
            println("Test foundedEsp32Event")

            central.stopScan()
            triggerConnectToESP32Event(it)
        }

        flowObserver(connectToESP32Event) {
            println("Connect to ${it.name}")

            central.stopScan()
            central.autoConnectPeripheral(it, blePeripheralHandler)
        }

        flowObserver(disconnectToESP32Event) { esp32 ->
            central.cancelConnection(esp32)
        }
    }

    private val blePeripheralHandler by lazy {
        object : BluetoothPeripheralCallback() {
            override fun onServicesDiscovered(peripheral: BluetoothPeripheral) {
                with(peripheral) {

                    getCharacteristic(
                        BLE_SERVICE_UUID,
                        BLE_WEIGHT_CHARACTERISTIC_UUID
                    )
                        ?.takeIf { it.descriptors.isNotEmpty() }
                        ?.let { setNotify(it, true) }

                    getCharacteristic(BLE_SERVICE_UUID, BLE_VELOCITY_CHARACTERISTIC_UUID)
                        ?.takeIf { it.descriptors.isNotEmpty() }
                        ?.let { setNotify(it, true) }

                    getCharacteristic(BLE_SERVICE_UUID, BLE_OFFSET_CHARACTERISTIC_UUID)
                        ?.takeIf { it.descriptors.isNotEmpty() }
                        ?.let { setNotify(it, true) }

                    getCharacteristic(
                        SECOND_WORKOUT_BLE_SERVICE,
                        BLE_FORCE_CHARACTERISTIC_UUID
                    )
                        ?.takeIf { it.descriptors.isNotEmpty() }
                        ?.let { setNotify(it, true) }

                    getCharacteristic(BLE_SERVICE_UUID, BLE_POWER_CHARACTERISTIC_UUID)
                        ?.takeIf { it.descriptors.isNotEmpty() }
                        ?.let { setNotify(it, true) }

                    getCharacteristic(
                        SECOND_WORKOUT_BLE_SERVICE,
                        BLE_ACCELERATION_CHARACTERISTIC_UUID
                    )
                        ?.takeIf { it.descriptors.isNotEmpty() }
                        ?.let { setNotify(it, true) }
                }
            }

            override fun onNotificationStateUpdate(
                peripheral: BluetoothPeripheral,
                characteristic: BluetoothGattCharacteristic,
                status: GattStatus
            ) {
                if (status == GattStatus.SUCCESS) {
                    if (peripheral.isNotifying(characteristic)) {
                        viewModel.onNotificationStateUpdate(
                            characteristic = characteristic,
                            isNotifying = true
                        )

                    } else {
                        viewModel.onNotificationStateUpdate(
                            characteristic = characteristic,
                            isNotifying = false
                        )
                    }
                } else {
                    Log.i(
                        "BleConnectionHandler",
                        String.format(
                            "ERROR: Changing notification state failed for %s",
                            characteristic.uuid
                        )
                    )

                    viewModel.triggerFailedToSetNotificationEvent(characteristic.uuid)
                }
            }

            override fun onCharacteristicUpdate(
                peripheral: BluetoothPeripheral,
                value: ByteArray,
                characteristic: BluetoothGattCharacteristic,
                status: GattStatus
            ) {
                when (characteristic.uuid) {
                    BLE_VELOCITY_CHARACTERISTIC_UUID -> viewModel.notifyCharacteristicDataChange(
                        characteristicId = BLE_VELOCITY_CHARACTERISTIC_UUID,
                        data = value
                    )
                    BLE_WEIGHT_CHARACTERISTIC_UUID -> viewModel.notifyCharacteristicDataChange(
                        characteristicId = BLE_WEIGHT_CHARACTERISTIC_UUID,
                        data = value
                    )
                    BLE_OFFSET_CHARACTERISTIC_UUID -> viewModel.notifyCharacteristicDataChange(
                        characteristicId = BLE_OFFSET_CHARACTERISTIC_UUID,
                        data = value
                    )
                    BLE_FORCE_CHARACTERISTIC_UUID -> viewModel.notifyCharacteristicDataChange(
                        characteristicId = BLE_FORCE_CHARACTERISTIC_UUID,
                        data = value
                    )
                    BLE_ACCELERATION_CHARACTERISTIC_UUID -> viewModel.notifyCharacteristicDataChange(
                        characteristicId = BLE_ACCELERATION_CHARACTERISTIC_UUID,
                        data = value
                    )
                    BLE_POWER_CHARACTERISTIC_UUID -> viewModel.notifyCharacteristicDataChange(
                        characteristicId = BLE_POWER_CHARACTERISTIC_UUID,
                        data = value
                    )
                }
            }
        }
    }

    fun scanESP32() {
        if (central.isScanning)
            central.stopScan()
        else
            central.scanForPeripherals()
    }

    override fun onDetach() {

        viewModel.deviceConnected?.let {
            central.cancelConnection(it)
        }

        if (central.isScanning) {
            central.stopScan()
        }

        super.onDetach()
    }
}