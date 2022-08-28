package com.example.pushapp.ui.workout

import android.app.Application
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.example.pushapp.R
import com.example.pushapp.models.Offset
import com.example.pushapp.models.ReportModel
import com.example.pushapp.models.WorkoutConfigurationModel
import com.example.pushapp.models.training_configuration.Exercise
import com.example.pushapp.models.training_configuration.TrainingMethodology
import com.example.pushapp.services.PushAppAuthService
import com.github.mikephil.charting.data.Entry
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

class WorkoutViewModel(
    private val workoutConfigModel: WorkoutConfigurationModel,
    private val application: Application,
    private val authService: PushAppAuthService
) : BluetoothHandlerViewModel() {

    val title = liveData {
        emit(workoutConfigModel.trainingMethodology.value)
    }

    // Será removido -> Input de Massa
    val weight = liveData {
        emit(workoutConfigModel.weight)
    }

    val showBarPositionContainer = liveData {
        emit(workoutConfigModel.exercise == Exercise.SMITH_MACHINE)
    }

    val showVelocityWheel = liveData {
        emit(workoutConfigModel.trainingMethodology == TrainingMethodology.VELOCITY_BASED_TRAINING)
    }

    val velocityWheelColor = _velocityData.map { getVelocityColorWheelColor(it) }

    private var velocityNotificationStartTime = 0L

    private var offsetNotificationStartTime = 0L

    private var forceNotificationStartTime = 0L

    private var powerNotificationStartTime = 0L

    private var accelerationNotificationStartTime = 0L

    private val _velocityEntries = MutableLiveData<MutableList<Entry>>()
    val velocityEntries: LiveData<MutableList<Entry>> get() = _velocityEntries

    private val _offsetEntries = MutableLiveData<MutableList<Entry>>()
    val offsetEntries get(): LiveData<MutableList<Entry>> = _offsetEntries

    private val _accelerationEntries = MutableLiveData<MutableList<Entry>>()
    val accelerationEntries: LiveData<MutableList<Entry>> get() = _accelerationEntries

    private val _powerEntries = MutableLiveData<MutableList<Entry>>()
    val powerEntries: LiveData<MutableList<Entry>> get() = _powerEntries

    private val _forceEntries = MutableLiveData<MutableList<Entry>>()
    val forceEntries: LiveData<MutableList<Entry>> get() = _forceEntries

    private val _navigateToDetailedReportEvent = MutableSharedFlow<ReportModel>()
    val navigateToDetailedReportEvent get() = _navigateToDetailedReportEvent.asSharedFlow()

    fun <T : Number> saveData(characteristicId: UUID, data: T) {

        val wasFirstNotification =
            connectedDeviceCharacteristics[characteristicId]?.isEmpty() ?: false

        if (wasFirstNotification) {
            setCharNotificationStartTime(
                characteristicId = characteristicId,
                startTime = Calendar.getInstance().time.time
            )
        }

        val currentTime = Calendar.getInstance().time.time

        val timeDiffInSeconds =
            getTimeDiffInSeconds(characteristicId = characteristicId, currentTime = currentTime)

        connectedDeviceCharacteristics.saveCharacteristicDataPerTime(
            characteristicId = characteristicId,
            data = data.toFloat(),
            timeDiffInSeconds = timeDiffInSeconds
        )
    }

    private fun setCharNotificationStartTime(characteristicId: UUID, startTime: Long) = when (characteristicId) {
        BLE_VELOCITY_CHARACTERISTIC_UUID -> velocityNotificationStartTime = startTime
        BLE_OFFSET_CHARACTERISTIC_UUID -> offsetNotificationStartTime = startTime
        BLE_FORCE_CHARACTERISTIC_UUID -> forceNotificationStartTime = startTime
        BLE_POWER_CHARACTERISTIC_UUID -> powerNotificationStartTime = startTime
        else -> accelerationNotificationStartTime = startTime
    }

    private fun getTimeDiffInSeconds(characteristicId: UUID, currentTime: Long): Long =
        when (characteristicId) {
            BLE_VELOCITY_CHARACTERISTIC_UUID -> TimeUnit.MILLISECONDS.toSeconds(
                currentTime - velocityNotificationStartTime
            )
            BLE_OFFSET_CHARACTERISTIC_UUID -> TimeUnit.MILLISECONDS.toSeconds(
                currentTime - offsetNotificationStartTime
            )
            BLE_FORCE_CHARACTERISTIC_UUID -> TimeUnit.MILLISECONDS.toSeconds(
                currentTime - forceNotificationStartTime
            )
            BLE_POWER_CHARACTERISTIC_UUID -> TimeUnit.MILLISECONDS.toSeconds(
                currentTime - powerNotificationStartTime
            )
            else -> TimeUnit.MILLISECONDS.toSeconds(
                currentTime - accelerationNotificationStartTime
            )
        }


    private fun MutableMap<UUID, MutableList<Entry>>.saveCharacteristicDataPerTime(
        characteristicId: UUID,
        data: Float,
        timeDiffInSeconds: Long,
    ) = this[characteristicId]
        ?.apply { add(Entry(timeDiffInSeconds.toFloat(), data)) }
        ?.also {
            when (characteristicId) {
                BLE_VELOCITY_CHARACTERISTIC_UUID -> _velocityEntries.value = it
                BLE_OFFSET_CHARACTERISTIC_UUID -> _offsetEntries.value = it
                BLE_ACCELERATION_CHARACTERISTIC_UUID -> _accelerationEntries.value = it
                BLE_POWER_CHARACTERISTIC_UUID -> _powerEntries.value = it
                BLE_FORCE_CHARACTERISTIC_UUID -> _forceEntries.value = it
            }
        }

    private fun getVelocityColorWheelColor(velocity: Float) = when (velocity) {
        in 0.0..0.25 -> ContextCompat.getColor(application, R.color.red_primary)
        in 0.25..0.50 -> ContextCompat.getColor(application, R.color.yellow_primary)
        else -> ContextCompat.getColor(application, R.color.green_secondary)
    }

    fun navigateToDetailedReportFragment() = viewModelScope.launch {
        _navigateToDetailedReportEvent.emit(
            ReportModel(
                exercise = workoutConfigModel.exercise,
                trainingMethodology = workoutConfigModel.trainingMethodology,
                weight = weightData.value ?: 0,
                offsetMovements = connectedDeviceCharacteristics[BLE_VELOCITY_CHARACTERISTIC_UUID]?.map {
                    Offset(
                        timestamp = it.x.toLong(),
                        value = it.y
                    )
                } ?: listOf(),
                meanVelocity = connectedDeviceCharacteristics[BLE_VELOCITY_CHARACTERISTIC_UUID]?.calculateMean()
                    ?: 0f,
                meanPower = connectedDeviceCharacteristics[BLE_POWER_CHARACTERISTIC_UUID]?.calculateMean()
                    ?: 0f,
                meanForce = connectedDeviceCharacteristics[BLE_FORCE_CHARACTERISTIC_UUID]?.calculateMean()
                    ?: 0f,
                userId = authService.getCurrentUserId().orEmpty()
            )
        )
    }

    private fun List<Entry>.calculateMean(): Float {

        var valuesSum = 0f
        var mean = 0f

        forEach { entry ->
            valuesSum += entry.y
        }

        if (isNotEmpty())
            mean = valuesSum / size

        return mean
    }
}