package com.example.pushapp.ui.detailed_report

import androidx.lifecycle.*
import com.example.pushapp.models.detailed_report.ReportVariables
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class ReportFilterBottomSheetViewModel(
    filters: Set<ReportVariables>
) : ViewModel(), DefaultLifecycleObserver {

    private val _offsetFilterState =
        MutableLiveData(filters.contains(ReportVariables.OFFSET))

    val offsetFilterState: LiveData<Boolean> get() = _offsetFilterState

    private val _accelerationFilterState =
        MutableLiveData(filters.contains(ReportVariables.ACCELERATION))

    val accelerationFilterState: LiveData<Boolean> get() = _accelerationFilterState

    private val _velocityFilterState =
        MutableLiveData(filters.contains(ReportVariables.VELOCITY))

    val velocityFilterState: LiveData<Boolean> get() = _velocityFilterState

    private val _powerFilterState =
        MutableLiveData(filters.contains(ReportVariables.POWER))

    val powerFilterState: LiveData<Boolean> get() = _powerFilterState

    private val _forceFilterState =
        MutableLiveData(filters.contains(ReportVariables.FORCE))

    val forceFilterState: LiveData<Boolean> get() = _forceFilterState

    private val selectedFilters: MutableSet<ReportVariables> =
        filters.toMutableSet()

    override fun onCreate(owner: LifecycleOwner) {
        selectedFilters.forEach {
            onSelection(true, it)
        }
    }

    private val _applyFiltersEvent =
        MutableSharedFlow<Set<ReportVariables>>()

    val applyFiltersEvent get() = _applyFiltersEvent.asSharedFlow()

    private val _onSelectionEvent = MutableLiveData<ReportVariableState>()
    val onSelectionEvent: LiveData<ReportVariableState> get() = _onSelectionEvent

    fun onSelection(isSelected: Boolean, reportVariable: ReportVariables) {
        viewModelScope.launch {
            if (isSelected) {
                selectedFilters.add(reportVariable)
                _onSelectionEvent.value = ReportVariableState(
                    isSelected = isSelected,
                    reportVariable = reportVariable
                )
            } else {
                selectedFilters.remove(reportVariable)
                _onSelectionEvent.value = ReportVariableState(
                    isSelected = isSelected,
                    reportVariable = reportVariable
                )
            }
        }
    }

    fun applyFilters() = viewModelScope.launch {
        _applyFiltersEvent.emit(selectedFilters)
    }

    data class ReportVariableState(
        val isSelected: Boolean,
        val reportVariable: ReportVariables
    )
}