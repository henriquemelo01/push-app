package com.example.pushapp.ui.detailed_report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
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

    val offsetEntries = liveData {
        val entries = reportModel.offsetMovements.map {
            Entry(it.timestamp.toFloat(), it.value)
        }
        emit(entries)
    }

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
}