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
    private val velocityNotificationCharUpdateEvent get() = _velocityNotificationCharUpdateEvent.asSharedFlow()

    private val _dummyCharNotificationCharUpdateEvent = MutableSharedFlow<Boolean>()
    private val dummyCharNotificationCharUpdateEvent get() = _dummyCharNotificationCharUpdateEvent.asSharedFlow()

    private val _failedToSetNotificationEvent = MutableSharedFlow<UUID>()
    val failedToSetNotificationEvent get() = _failedToSetNotificationEvent.asSharedFlow()

    protected val connectedDeviceCharacteristics = mutableMapOf<UUID, MutableList<Entry>>()

    protected val _velocityData = MutableLiveData<Float>()
    val velocityData: LiveData<Float> get() = _velocityData

    private val _dummyIntegerVariableData = MutableLiveData<Int>()
    val dummyIntegerVariableData: LiveData<Int> = _dummyIntegerVariableData

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

        if (characteristic.uuid == BLE_VELOCITY_CHARACTERISTIC_UUID) {
            _velocityNotificationCharUpdateEvent.emit(isNotifying)

            if (isNotifying)
                connectedDeviceCharacteristics[BLE_VELOCITY_CHARACTERISTIC_UUID] = mutableListOf()

        } else if (characteristic.uuid == BLE_CHARACTERISTIC_TX_UUID) {
            _dummyCharNotificationCharUpdateEvent.emit(isNotifying)

            if (isNotifying)
                connectedDeviceCharacteristics[BLE_CHARACTERISTIC_TX_UUID] = mutableListOf()
        }
    }

    fun triggerFailedToSetNotificationEvent(uuid: UUID) = viewModelScope.launch {
        _failedToSetNotificationEvent.emit(uuid)
    }

    fun notifyCharacteristicDataChange(characteristicId: UUID, data: ByteArray) {
        if (characteristicId == BLE_VELOCITY_CHARACTERISTIC_UUID)
            _velocityData.value = data.processFloatData()
        else if (characteristicId == BLE_CHARACTERISTIC_TX_UUID)
            _dummyIntegerVariableData.value = data.processIntegerData()
    }

    companion object {
        const val ESP_32_MAC_ADDRESS = "CC:50:E3:95:BA:A2"

        val BLE_SERVICE_UUID: UUID = UUID.fromString("f8ab3678-b2b6-11ec-b909-0242ac120002")

        val BLE_CHARACTERISTIC_TX_UUID: UUID =
            UUID.fromString("f8ab3a6a-b2b6-11ec-b909-0242ac120002")

        val BLE_VELOCITY_CHARACTERISTIC_UUID: UUID =
            UUID.fromString("9a3843fe-ed19-11ec-8ea0-0242ac120002")
    }
}

