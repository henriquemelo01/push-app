package com.example.pushapp.ui.workout

import android.bluetooth.BluetoothGattCharacteristic
import androidx.lifecycle.*
import com.example.pushapp.models.BluetoothConnectionStatus
import com.example.pushapp.utils.processFloatData
import com.example.pushapp.utils.processIntegerData
import com.github.mikephil.charting.data.Entry
import com.welie.blessed.BluetoothPeripheral
import com.welie.blessed.ScanFailure
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

abstract class BluetoothHandlerViewModel : ViewModel() {

    private val foundedBleDevices = mutableListOf<BluetoothPeripheral>()

    private val _foundedPeripherals = MutableLiveData(mutableListOf<BluetoothPeripheral>())
    val foundedPeripherals: LiveData<List<BluetoothPeripheral>>
        get() = _foundedPeripherals.map { it as List<BluetoothPeripheral> }

    private val _foundedEsp32Event = MutableSharedFlow<BluetoothPeripheral>()
    val foundedEsp32Event = _foundedEsp32Event.asSharedFlow()

    private val _bleScanFailedEvent = MutableSharedFlow<ScanFailure>()
    val bleScanFailedEvent get() = _bleScanFailedEvent.asSharedFlow()

    var deviceConnected: BluetoothPeripheral? = null

    var previousDeviceConnected: BluetoothPeripheral? = null

    private var _statusDevice =
        MutableStateFlow(BluetoothConnectionStatus.DISCONNECTED)
    val statusDevice get() = _statusDevice.asStateFlow()

    private val _connectToESP32Event = MutableSharedFlow<BluetoothPeripheral>()
    val connectToESP32Event get() = _connectToESP32Event.asSharedFlow()

    private val _disconnectToESP32Event = MutableSharedFlow<BluetoothPeripheral>()
    val disconnectToESP32Event get() = _disconnectToESP32Event.asSharedFlow()

    private val _connectionFailedESP32Event = MutableSharedFlow<BluetoothPeripheral>()
    val connectionFailedESP32Event get() = _connectionFailedESP32Event.asSharedFlow()

    private val _velocityNotificationCharUpdateEvent = MutableSharedFlow<Boolean>()
    val velocityNotificationCharUpdateEvent get() = _velocityNotificationCharUpdateEvent.asSharedFlow()

    private val _weightNotificationCharUpdateEvent = MutableSharedFlow<Boolean>()
    val weightNotificationCharUpdateEvent get() = _weightNotificationCharUpdateEvent.asSharedFlow()

    private val _offsetNotificationCharUpdateEvent = MutableSharedFlow<Boolean>()
    val offsetNotificationCharUpdateEvent get() = _offsetNotificationCharUpdateEvent.asSharedFlow()

    private val _forceNotificationCharUpdateEvent = MutableSharedFlow<Boolean>()
    val forceNotificationCharUpdateEvent get() = _forceNotificationCharUpdateEvent.asSharedFlow()

    private val _accelerationNotificationCharUpdateEvent = MutableSharedFlow<Boolean>()
    val accelerationNotificationCharUpdateEvent get() = _accelerationNotificationCharUpdateEvent.asSharedFlow()

    private val _powerNotificationCharUpdateEvent = MutableSharedFlow<Boolean>()
    val powerNotificationCharUpdateEvent get() = _powerNotificationCharUpdateEvent.asSharedFlow()

    private val _failedToSetNotificationEvent = MutableSharedFlow<UUID>()
    val failedToSetNotificationEvent get() = _failedToSetNotificationEvent.asSharedFlow()

    protected val connectedDeviceCharacteristics = mutableMapOf<UUID, MutableList<Entry>>()

    protected val _velocityData = MutableLiveData<Float>()
    val velocityData: LiveData<Float> get() = _velocityData

    private val _weightData = MutableLiveData<Float>()
    val weightData: LiveData<Float> = _weightData

    private val _offsetData = MutableLiveData<Int>()
    val offsetData: LiveData<Int> = _offsetData

    private val _forceData = MutableLiveData<Int>()
    val forceData: LiveData<Int> = _forceData

    private val _accelerationData = MutableLiveData<Float>()
    val accelerationData: LiveData<Float> = _accelerationData

    private val _powerData = MutableLiveData<Int>()
    val powerData: LiveData<Int> = _powerData

    fun onDiscoveredPeripheral(peripheral: BluetoothPeripheral) {

        foundedBleDevices
            .takeIf { !it.contains(peripheral) && peripheral.address.isNotEmpty() }
            ?.apply { add(peripheral) }
            ?.also { _foundedPeripherals.value = it }

        if (peripheral.address == ESP_32_MAC_ADDRESS)
            viewModelScope.launch {
                _foundedEsp32Event.emit(peripheral)
            }
    }

    fun triggerBleScanFailedEvent(scanFailure: ScanFailure) = viewModelScope.launch {
        _bleScanFailedEvent.emit(scanFailure)
    }

    fun triggerConnectToESP32Event(peripheral: BluetoothPeripheral) = viewModelScope.launch {
        _connectToESP32Event.emit(peripheral)
    }

    fun triggerDisconnectToESP32Event() = viewModelScope.launch {
        deviceConnected?.let {
            _disconnectToESP32Event.emit(it)
        }
    }

    fun onConnectToESP32(peripheral: BluetoothPeripheral) {
        deviceConnected = peripheral
        _statusDevice.value = BluetoothConnectionStatus.CONNECTED
    }

    fun onDisconnectToESP32(peripheral: BluetoothPeripheral) {
        deviceConnected = null
        _statusDevice.value = BluetoothConnectionStatus.DISCONNECTED
        previousDeviceConnected = deviceConnected
    }

    fun triggerConnectionFailedESP32Event(peripheral: BluetoothPeripheral) = viewModelScope.launch {
        _connectionFailedESP32Event.emit(peripheral)
    }

    fun onNotificationStateUpdate(
        characteristic: BluetoothGattCharacteristic,
        isNotifying: Boolean
    ) = viewModelScope.launch {
        when (characteristic.uuid) {
            BLE_VELOCITY_CHARACTERISTIC_UUID -> {
                _velocityNotificationCharUpdateEvent.emit(isNotifying)

                if (isNotifying)
                    connectedDeviceCharacteristics[BLE_VELOCITY_CHARACTERISTIC_UUID] =
                        mutableListOf()
            }

            BLE_WEIGHT_CHARACTERISTIC_UUID -> {
                _weightNotificationCharUpdateEvent.emit(isNotifying)

                if (isNotifying)
                    connectedDeviceCharacteristics[BLE_WEIGHT_CHARACTERISTIC_UUID] = mutableListOf()
            }

            BLE_OFFSET_CHARACTERISTIC_UUID -> {
                _offsetNotificationCharUpdateEvent.emit(isNotifying)

                if (isNotifying)
                    connectedDeviceCharacteristics[BLE_OFFSET_CHARACTERISTIC_UUID] = mutableListOf()
            }

            BLE_FORCE_CHARACTERISTIC_UUID -> {
                _forceNotificationCharUpdateEvent.emit(isNotifying)

                if (isNotifying)
                    connectedDeviceCharacteristics[BLE_FORCE_CHARACTERISTIC_UUID] = mutableListOf()
            }

            BLE_ACCELERATION_CHARACTERISTIC_UUID -> {
                _accelerationNotificationCharUpdateEvent.emit(isNotifying)

                if (isNotifying)
                    connectedDeviceCharacteristics[BLE_ACCELERATION_CHARACTERISTIC_UUID] =
                        mutableListOf()
            }

            BLE_POWER_CHARACTERISTIC_UUID -> {
                _powerNotificationCharUpdateEvent.emit(isNotifying)

                if (isNotifying)
                    connectedDeviceCharacteristics[BLE_POWER_CHARACTERISTIC_UUID] = mutableListOf()
            }
        }
    }

    fun triggerFailedToSetNotificationEvent(uuid: UUID) = viewModelScope.launch {
        _failedToSetNotificationEvent.emit(uuid)
    }

    fun notifyCharacteristicDataChange(characteristicId: UUID, data: ByteArray) {
        when (characteristicId) {
            BLE_VELOCITY_CHARACTERISTIC_UUID -> _velocityData.value = data.processFloatData()
            BLE_WEIGHT_CHARACTERISTIC_UUID -> _weightData.value = data.processFloatData()
            BLE_OFFSET_CHARACTERISTIC_UUID -> _offsetData.value = data.processIntegerData()
            BLE_ACCELERATION_CHARACTERISTIC_UUID -> _accelerationData.value =
                data.processFloatData()
            BLE_FORCE_CHARACTERISTIC_UUID -> _forceData.value = data.processIntegerData()
            BLE_POWER_CHARACTERISTIC_UUID -> _powerData.value = data.processIntegerData()
        }
    }

    companion object {
//        const val ESP_32_MAC_ADDRESS = "CC:50:E3:95:BA:A2" // Prototipo
        const val ESP_32_MAC_ADDRESS = "EC:62:60:93:6B:D6"

        val BLE_SERVICE_UUID: UUID = UUID.fromString("f8ab3678-b2b6-11ec-b909-0242ac120002")
        val SECOND_WORKOUT_BLE_SERVICE: UUID =
            UUID.fromString("86f12c7f-1d2c-44a1-b1a6-30a264c15dc4")

        val BLE_WEIGHT_CHARACTERISTIC_UUID: UUID =
            UUID.fromString("f8ab3a6a-b2b6-11ec-b909-0242ac120002")

        val BLE_VELOCITY_CHARACTERISTIC_UUID: UUID =
            UUID.fromString("9a3843fe-ed19-11ec-8ea0-0242ac120002")

        val BLE_OFFSET_CHARACTERISTIC_UUID: UUID =
            UUID.fromString("cf4e2566-14a1-4d71-84ad-96eebd5b9bc3")

        val BLE_FORCE_CHARACTERISTIC_UUID: UUID = UUID.fromString(
            "20abb7fa-52a0-486b-a5f1-91fe12236c3a"
        )

        val BLE_POWER_CHARACTERISTIC_UUID: UUID =
            UUID.fromString("69454f3f-f575-4f59-87dd-42ce3207ddbf")

        val BLE_ACCELERATION_CHARACTERISTIC_UUID: UUID =
            UUID.fromString("d89f2437-86c0-4d8c-9c21-8a39e600827d")
    }
}

