package com.example.pushapp.ui.detailed_report

import androidx.lifecycle.*
import com.example.pushapp.models.Offset
import com.example.pushapp.models.ReportModel
import com.example.pushapp.models.detailed_report.AccesedBy
import com.example.pushapp.models.detailed_report.ReportVariables
import com.example.pushapp.services.PushAppRepository
import com.example.pushapp.services.ReportCsvFile
import com.example.pushapp.utils.toOffsetList
import com.github.mikephil.charting.data.Entry
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.abs

class DetailedReportViewModel(
    private val reportModel: ReportModel,
    private val accessedBy: AccesedBy,
    private val repository: PushAppRepository
) : ViewModel(), DefaultLifecycleObserver {

    val workoutWeight = liveData {
        emit(reportModel.weight)
    }

    val downloadIconVisibility = liveData {
        emit(accessedBy == AccesedBy.HISTORY_FRAGMENT)
    }

    private val offsetEntries = reportModel.offsetMovements.toEntries()

    private val velocityEntries = reportModel.velocityPerTime.toEntries()

    private val forceEntries = reportModel.forcePerTime.toEntries()

    private val powerEntries = reportModel.powerPerTime.toEntries()

    private val accelerationEntries = reportModel.accelerationPerTime.toEntries()

    val exercise = liveData {
        emit(reportModel.exercise.value)
    }

    val trainingMethod = liveData {
        emit(reportModel.trainingMethodology.value)
    }

    val meanVelocity = liveData {
        emit(
            abs(reportModel.meanVelocity)
        )
    }

    val meanPower = liveData {
        emit(
            abs(reportModel.meanPower)
        )
    }

    val meanForce = liveData {
        emit(reportModel.meanForce)
    }

    private val _selectedFilterEntries = MutableLiveData<Map<ReportVariables, List<Entry>>>()
    val selectedFilterEntries: LiveData<Map<ReportVariables, List<Entry>>> get() = _selectedFilterEntries

    var selectedFilters = mutableSetOf<ReportVariables>()

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


    override fun onCreate(owner: LifecycleOwner) {
        // Graficos que serão exibidos inicialmente
        _selectedFilterEntries.value = mutableMapOf<ReportVariables, List<Entry>>().apply {
            put(ReportVariables.OFFSET, offsetEntries)
            put(ReportVariables.ACCELERATION, accelerationEntries)
            put(ReportVariables.VELOCITY, velocityEntries)
        }

        selectedFilters = mutableSetOf(
            ReportVariables.OFFSET,
            ReportVariables.ACCELERATION,
            ReportVariables.VELOCITY,
            ReportVariables.FORCE,
            ReportVariables.POWER
        )
    }

    private val _onDownloadIconClickEvent = MutableSharedFlow<ReportCsvFile>()
    val onDownloadIconClickEvent get() = _onDownloadIconClickEvent.asSharedFlow()

    fun onDownloadIconClick() {
        viewModelScope.launch {
            _onDownloadIconClickEvent.emit(
                ReportCsvFile(
                    filename = "report_${reportModel.id}",
                    url = "https://push-app-api.onrender.com/api/v1/reportCsvGenerator/" + reportModel.id
                )
            )
        }
    }

    fun onApplyFilter(reportVariables: MutableSet<ReportVariables>) {

        selectedFilters = reportVariables

        val filtersMap = mutableMapOf<ReportVariables, List<Entry>>().apply {

            if (reportVariables.contains(ReportVariables.OFFSET))
                put(ReportVariables.OFFSET, offsetEntries)

            if (reportVariables.contains(ReportVariables.ACCELERATION))
                put(ReportVariables.ACCELERATION, accelerationEntries)

            if (reportVariables.contains(ReportVariables.VELOCITY))
                put(ReportVariables.VELOCITY, velocityEntries)

            if (reportVariables.contains(ReportVariables.FORCE))
                put(ReportVariables.FORCE, forceEntries)

            if (reportVariables.contains(ReportVariables.POWER))
                put(ReportVariables.POWER, powerEntries)
        }

        _selectedFilterEntries.value = filtersMap
    }

    fun saveReport() = viewModelScope.launch {
        repository.saveReport(reportModel.copy(createdAt = Calendar.getInstance().time.time))
            .onSuccess { report ->
                _onSaveReportSuccessEvent.emit(report)
            }.onFailure { failure ->
                _onSaveReportFailureEvent.emit(failure)
            }
    }

    private val _openFilterBottomSheetEvent = MutableSharedFlow<Unit>()
    val openFilterBottomSheetEvent get() = _openFilterBottomSheetEvent.asSharedFlow()

    fun triggerOpenFilterBottomSheetEvent() = viewModelScope.launch {
        _openFilterBottomSheetEvent.emit(Unit)
    }

    private fun List<Offset>.toEntries(): List<Entry> = map {
        Entry(it.timestamp, it.value)
    }

    private fun List<Offset>.maxValue(): Float = map { it.value }.maxOrNull() ?: 0f

    private fun List<Offset>.minValue(): Float = map { it.value }.minOrNull() ?: 0f


    fun getSelectedFilterEntriesMaxValue(): Float {
        var maxValue = 0f
        _selectedFilterEntries.value?.entries?.forEach {

            val signalInversionStatement =
                it.key == ReportVariables.VELOCITY || it.key == ReportVariables.POWER

            val offsetList = it.value.toOffsetList()

            val charMaxValue =
                if (signalInversionStatement) offsetList.map { it.copy(value = -it.value) }
                    .maxValue() else offsetList.maxValue()

            if (charMaxValue > maxValue)
                maxValue = charMaxValue
        }
        return maxValue
    }

    fun getSelectedFilterEntriesMinValue(): Float {
        var minValue = 0f
        _selectedFilterEntries.value?.entries?.forEach {

            val signalInversionStatement =
                it.key == ReportVariables.VELOCITY || it.key == ReportVariables.POWER

            val offsetList = it.value.toOffsetList()

            val charMinValue =
                if (signalInversionStatement) offsetList.map { it.copy(value = -it.value) }
                    .minValue() else offsetList.minValue()

            if (charMinValue < minValue)
                minValue = charMinValue
        }
        return minValue
    }

//    fun getSelectedFilterEntriesMaxValue(): Float {
//        var maxValue = 0f
//        _selectedFilterEntries.value?.entries?.forEach {
//            val charMaxValue = it.value.toOffsetList().maxValue()
//            if (charMaxValue > maxValue)
//                maxValue = charMaxValue
//        }
//        return maxValue
//    }
//
//    fun getSelectedFilterEntriesMinValue(): Float {
//        var minValue = 0f
//        _selectedFilterEntries.value?.entries?.forEach {
//            val charMinValue = it.value.toOffsetList().minValue()
//            if (charMinValue < minValue)
//                minValue = charMinValue
//        }
//        return minValue
//    }
}