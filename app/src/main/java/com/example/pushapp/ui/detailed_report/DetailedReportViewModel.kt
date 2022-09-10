package com.example.pushapp.ui.detailed_report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.pushapp.models.Offset
import com.example.pushapp.models.ReportModel
import com.example.pushapp.models.detailed_report.AccesedBy
import com.example.pushapp.services.PushAppRepository
import com.github.mikephil.charting.data.Entry
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

class DetailedReportViewModel(
    private val reportModel: ReportModel,
    private val accessedBy: AccesedBy,
    private val repository: PushAppRepository
) : ViewModel() {

    // OFFSET

    val offsetEntries = liveData {
        val entries = reportModel.offsetMovements.toEntries()
        emit(entries)
    }

    val offsetMovementsMaxValue = reportModel.offsetMovements.maxValue()

    val offsetMovementsMinValue = reportModel.offsetMovements.minValue()

    // VELOCITY

    val velocityEntries = liveData {
        val entries = reportModel.velocityPerTime.toEntries()
        emit(entries)
    }

    val velocitiesMaxValue = reportModel.velocityPerTime.maxValue()

    val velocitiesMinValue = reportModel.velocityPerTime.minValue()


    // FORCE

    val forceEntries = liveData {
        val entries = reportModel.forcePerTime.toEntries()
        emit(entries)
    }

    val forcesMaxValue = reportModel.forcePerTime.maxValue()

    val forcesMinValue = reportModel.forcePerTime.minValue()

    // POWER

    val powerEntries = liveData {
        val entries = reportModel.powerPerTime.toEntries()
        emit(entries)
    }

    val powerEntriesMaxValue = reportModel.powerPerTime.maxValue()

    val powerEntriesMinValue = reportModel.powerPerTime.minValue()

    // ACCELERATION

    val accelerationEntries = liveData {
        val entries = reportModel.accelerationPerTime.toEntries()
        emit(entries)
    }

    val accelerationEntriesMaxValue = reportModel.accelerationPerTime.maxValue()

    val accelerationEntriesValue = reportModel.powerPerTime.minValue()

    val exercise = liveData {
        emit(reportModel.exercise.value)
    }

    val trainingMethod = liveData {
        emit(reportModel.trainingMethodology.value)
    }

    val meanVelocity = liveData {
        emit(reportModel.meanVelocity)
    }

    val meanPower = liveData {
        emit(reportModel.meanPower)
    }

    val meanForce = liveData {
        emit(reportModel.meanForce)
    }

    val showSaveReportButton = liveData {
        emit(accessedBy == AccesedBy.WORKOUT_FRAGMENT)
    }

    val showDiscardReportButton = liveData {
        emit(accessedBy == AccesedBy.WORKOUT_FRAGMENT)
    }

    private val _onSaveReportSuccessEvent = MutableSharedFlow<ReportModel>()
    val onSaveReportSuccessEvent get() = _onSaveReportSuccessEvent.asSharedFlow()

    private val _onSaveReportFailureEvent = MutableSharedFlow<Throwable>()
    val onSaveReportFailureEvent get() = _onSaveReportFailureEvent.asSharedFlow()

    fun saveReport() = viewModelScope.launch {
        repository.saveReport(reportModel.copy(createdAt = Calendar.getInstance().time.time))
            .onSuccess { report ->
                _onSaveReportSuccessEvent.emit(report)
            }.onFailure { failure ->
                _onSaveReportFailureEvent.emit(failure)
            }
    }

    private fun List<Offset>.toEntries(): List<Entry> = map {
        Entry(it.timestamp, it.value)
    }

    private fun  List<Offset>.maxValue() : Float = map { it.value }.maxOrNull() ?: 0f

    private fun  List<Offset>.minValue() : Float = map { it.value }.minOrNull() ?: 0f
}