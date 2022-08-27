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

    private var integerVariableNotificationStartTime = 0L

    private val _velocityEntries = MutableLiveData<MutableList<Entry>>()
    val velocityEntries: LiveData<MutableList<Entry>> get() = _velocityEntries

    // Será substituido pelo offset
    private val _dummyIntegerEntries = MutableLiveData<MutableList<Entry>>()
    val dummyIntegerEntries get() = MutableLiveData<MutableList<Entry>>()

    private val _navigateToDetailedReportEvent = MutableSharedFlow<ReportModel>()
    val navigateToDetailedReportEvent get() = _navigateToDetailedReportEvent.asSharedFlow()

    fun <T : Number> saveData(characteristicId: UUID, data: T) {

        val wasFirstNotification =
            connectedDeviceCharacteristics[characteristicId]?.isEmpty() ?: false

        if (wasFirstNotification) {
            if (characteristicId == BLE_VELOCITY_CHARACTERISTIC_UUID)
                velocityNotificationStartTime = Calendar.getInstance().time.time
            else
                integerVariableNotificationStartTime = Calendar.getInstance().time.time
        }

        val currentTime = Calendar.getInstance().time.time

        val timeDiffInSeconds =
            if (characteristicId == BLE_VELOCITY_CHARACTERISTIC_UUID) TimeUnit.MILLISECONDS.toSeconds(
                currentTime - velocityNotificationStartTime
            )
            else TimeUnit.MILLISECONDS.toSeconds(currentTime - integerVariableNotificationStartTime)

        connectedDeviceCharacteristics[characteristicId]?.apply {

            if (characteristicId == BLE_VELOCITY_CHARACTERISTIC_UUID) {

                add(Entry(timeDiffInSeconds.toFloat(), data.toFloat()))

                _velocityEntries.value =
                    connectedDeviceCharacteristics[BLE_VELOCITY_CHARACTERISTIC_UUID]

            } else if (characteristicId == BLE_WEIGHT_CHARACTERISTIC_UUID) {

                add(Entry(timeDiffInSeconds.toFloat(), data.toFloat()))

                _dummyIntegerEntries.value =
                    connectedDeviceCharacteristics[BLE_WEIGHT_CHARACTERISTIC_UUID]
            }
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
                weight = workoutConfigModel.weight,
                offsetMovements = connectedDeviceCharacteristics[BLE_VELOCITY_CHARACTERISTIC_UUID]?.map {
                    Offset(
                        timestamp = it.x.toLong(),
                        value = it.y
                    )
                } ?: listOf(),
                meanVelocity = 0.66f, // getMeanVelocity
                meanPower = 550f, // getMeanPower
                meanForce = 550f, // getMeanForce
                userId = authService.getCurrentUserId().orEmpty()
            )
        )
    }
}