package com.example.pushapp.ui.workout

import android.app.Application
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.example.pushapp.R
import com.example.pushapp.models.ReportModel
import com.example.pushapp.models.WorkoutConfigurationModel
import com.example.pushapp.models.training_configuration.Exercise
import com.example.pushapp.models.training_configuration.TrainingMethodology
import com.example.pushapp.services.PushAppAuthService
import com.example.pushapp.utils.toOffsetList
import com.github.mikephil.charting.data.Entry
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class WorkoutViewModel(
    private val workoutConfigModel: WorkoutConfigurationModel,
    private val application: Application,
    private val authService: PushAppAuthService
) : BluetoothHandlerViewModel() {

    val title = liveData {
        emit(workoutConfigModel.trainingMethodology.value)
    }

    val showBarPositionContainer = liveData {
        emit(workoutConfigModel.exercise == Exercise.SMITH_MACHINE)
    }

    val showVelocityWheel = liveData {
        emit(workoutConfigModel.trainingMethodology == TrainingMethodology.VELOCITY_BASED_TRAINING)
    }

    val velocityWheelColor = _velocityData.map { getVelocityColorWheelColor(it) }

    private var velocityNotificationStartTime = 0L

    var velocityMinValue = 0f

    var velocityMaxValue = 0f

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

        setCharMinValue(
            characteristicId = characteristicId,
            data = data,
            wasFirstNotification = wasFirstNotification
        )

        setCharMaxValue(
            characteristicId = characteristicId,
            data = data,
            wasFirstNotification = wasFirstNotification
        )

        val currentTime = Calendar.getInstance().time.time

        val timeDiffInMilliSeconds =
            getTimeDiffInMilliseconds(
                characteristicId = characteristicId,
                currentTime = currentTime
            )

        connectedDeviceCharacteristics.saveCharacteristicDataPerTime(
            characteristicId = characteristicId,
            data = data.toFloat(),
            timeDiffInMilliSeconds = timeDiffInMilliSeconds
        )
    }

    private fun setCharNotificationStartTime(characteristicId: UUID, startTime: Long) =
        when (characteristicId) {
            BLE_VELOCITY_CHARACTERISTIC_UUID -> velocityNotificationStartTime = startTime
            BLE_OFFSET_CHARACTERISTIC_UUID -> offsetNotificationStartTime = startTime
            BLE_FORCE_CHARACTERISTIC_UUID -> forceNotificationStartTime = startTime
            BLE_POWER_CHARACTERISTIC_UUID -> powerNotificationStartTime = startTime
            else -> accelerationNotificationStartTime = startTime
        }

    private fun setCharMinValue(
        characteristicId: UUID,
        data: Number,
        wasFirstNotification: Boolean
    ) {
        when (characteristicId) {
            BLE_VELOCITY_CHARACTERISTIC_UUID -> {
                if (wasFirstNotification)
                    velocityMinValue = data.toFloat()
                else
                    data.toFloat().takeIf { it < velocityMinValue }?.let { velocityData ->
                        velocityMinValue = velocityData
                    }
            }
        }
    }

    private fun setCharMaxValue(
        characteristicId: UUID,
        data: Number,
        wasFirstNotification: Boolean
    ) {
        when (characteristicId) {
            BLE_VELOCITY_CHARACTERISTIC_UUID -> {
                if (wasFirstNotification)
                    velocityMaxValue = data.toFloat()
                else
                    data.toFloat().takeIf { it > velocityMaxValue }?.let { velocityData ->
                        velocityMaxValue = velocityData
                    }
            }
        }
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

    private fun getTimeDiffInMilliseconds(characteristicId: UUID, currentTime: Long): Long =
        when (characteristicId) {
            BLE_VELOCITY_CHARACTERISTIC_UUID -> TimeUnit.MILLISECONDS.toMillis(
                currentTime - velocityNotificationStartTime
            )
            BLE_OFFSET_CHARACTERISTIC_UUID -> TimeUnit.MILLISECONDS.toMillis(
                currentTime - offsetNotificationStartTime
            )
            BLE_FORCE_CHARACTERISTIC_UUID -> TimeUnit.MILLISECONDS.toMillis(
                currentTime - forceNotificationStartTime
            )
            BLE_POWER_CHARACTERISTIC_UUID -> TimeUnit.MILLISECONDS.toMillis(
                currentTime - powerNotificationStartTime
            )
            else -> TimeUnit.MILLISECONDS.toMillis(
                currentTime - accelerationNotificationStartTime
            )
        }

    private fun MutableMap<UUID, MutableList<Entry>>.saveCharacteristicDataPerTime(
        characteristicId: UUID,
        data: Float,
        timeDiffInMilliSeconds: Long,
    ) = this[characteristicId]
        ?.apply { add(Entry(timeDiffInMilliSeconds.toFloat() / 1000, data)) }
        ?.also {
            when (characteristicId) {
                BLE_VELOCITY_CHARACTERISTIC_UUID -> _velocityEntries.value = it
                BLE_OFFSET_CHARACTERISTIC_UUID -> _offsetEntries.value = it
                BLE_ACCELERATION_CHARACTERISTIC_UUID -> _accelerationEntries.value = it
                BLE_POWER_CHARACTERISTIC_UUID -> _powerEntries.value = it
                BLE_FORCE_CHARACTERISTIC_UUID -> _forceEntries.value = it
            }
        }

    private fun getVelocityColorWheelColor(velocity: Float): Int {

        val velocityColor = when (velocity) {
            in 0.7..1.3 -> ContextCompat.getColor(application, R.color.green_primary)
            in -0.7..-1.3 -> ContextCompat.getColor(application, R.color.green_primary)
            else -> ContextCompat.getColor(application, R.color.red_primary)
        }

        return if (workoutConfigModel.trainingMethodology == TrainingMethodology.FREE_TRAINING)
            ContextCompat.getColor(application, R.color.gray) else velocityColor
    }

    private val offsetMovements
        get() = connectedDeviceCharacteristics[BLE_OFFSET_CHARACTERISTIC_UUID]?.toOffsetList()
            ?: listOf()

    private val velocityPerTime
        get() = connectedDeviceCharacteristics[BLE_VELOCITY_CHARACTERISTIC_UUID]?.toOffsetList()
            ?: listOf()

    private val meanVelocity
        get() = connectedDeviceCharacteristics[BLE_VELOCITY_CHARACTERISTIC_UUID]?.calculateMeanBasedOnTrainingMethod(
            trainingMethodology = workoutConfigModel.trainingMethodology,
            uuidCharacteristic = BLE_VELOCITY_CHARACTERISTIC_UUID
        ) ?: 0f

    private val powerPerTime
        get() = connectedDeviceCharacteristics[BLE_POWER_CHARACTERISTIC_UUID]?.toOffsetList()
            ?: listOf()

    private val meanPower
        get() = connectedDeviceCharacteristics[BLE_POWER_CHARACTERISTIC_UUID]?.calculateMeanBasedOnTrainingMethod(
            trainingMethodology = workoutConfigModel.trainingMethodology,
            uuidCharacteristic = BLE_POWER_CHARACTERISTIC_UUID
        ) ?: 0f

    private val forcePerTime
        get() = connectedDeviceCharacteristics[BLE_FORCE_CHARACTERISTIC_UUID]?.toOffsetList()
            ?: listOf()

    private val meanForce
        get() = connectedDeviceCharacteristics[BLE_FORCE_CHARACTERISTIC_UUID]?.calculateMeanBasedOnTrainingMethod(
            trainingMethodology = workoutConfigModel.trainingMethodology,
            uuidCharacteristic = BLE_FORCE_CHARACTERISTIC_UUID
        ) ?: 0f

    private val accelerationPerTime
        get() = connectedDeviceCharacteristics[BLE_ACCELERATION_CHARACTERISTIC_UUID]?.toOffsetList()
            ?: listOf()

    fun navigateToDetailedReportFragment() = viewModelScope.launch {
        _navigateToDetailedReportEvent.emit(
            ReportModel(
                exercise = workoutConfigModel.exercise,
                trainingMethodology = workoutConfigModel.trainingMethodology,
                weight = weightData.value ?: 0f,
                offsetMovements = offsetMovements,
                velocityPerTime = velocityPerTime,
                powerPerTime = powerPerTime,
                forcePerTime = forcePerTime,
                accelerationPerTime = accelerationPerTime,
                meanVelocity = meanVelocity,
                meanPower = meanPower,
                meanForce = meanForce,
                userId = authService.getCurrentUserId().orEmpty()
            )
        )
    }

    // Treino Livre
    private fun List<Entry>.calculateMean(isMeanWithAbsValue: Boolean = false): Float {

        var valuesSum = 0f
        var mean = 0f

        map { if (isMeanWithAbsValue) abs(it.y) else it.y }.forEach { values ->
            valuesSum += values
        }

        if (isNotEmpty())
            mean = valuesSum / size

        return mean
    }

    private fun List<Entry>.calculateMeanBasedOnTrainingMethod(
        trainingMethodology: TrainingMethodology,
        uuidCharacteristic: UUID
    ) = if (trainingMethodology == TrainingMethodology.FREE_TRAINING)
        this.calculateMean(isMeanWithAbsValue = true)
    else this.calculateMeanVBT(uuidCharacteristic)


    private fun List<Entry>.calculateMeanVBT(
        uuidCharacteristic: UUID
    ) = when (uuidCharacteristic) {
        BLE_VELOCITY_CHARACTERISTIC_UUID -> this.calculateVelocityOrPowerMeanConcentrica()
        BLE_FORCE_CHARACTERISTIC_UUID -> this.calculateMeanForceConcentrica(
            weightData.value ?: 0f
        )
        else -> this.calculateVelocityOrPowerMeanConcentrica()
    }

    private fun List<Entry>.calculateVelocityOrPowerMeanConcentrica(): Float {

        var meanValue = 0f

        val filteredValues = filter { it.y <= 0f }.map { it.y }
        val totalValues = filteredValues.sum()

        if (filteredValues.isNotEmpty())
            meanValue = totalValues / filteredValues.size

        return abs(meanValue)
    }

    private fun List<Entry>.calculateMeanForceConcentrica(weight: Float): Float {

        val peso = weight * 9.81f

        var meanForce = 0f

        val filteredForce = map { it.y - peso }.filter { it >= 0f }.map { it + peso }
        val totalForce = filteredForce.sum()

        if (filteredForce.isNotEmpty())
            meanForce = totalForce / filteredForce.size

        return meanForce
    }
}