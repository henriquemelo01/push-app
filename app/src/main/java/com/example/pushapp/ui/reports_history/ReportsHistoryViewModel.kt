package com.example.pushapp.ui.reports_history

import androidx.lifecycle.*
import com.example.pushapp.models.ReportModel
import com.example.pushapp.services.PushAppAuthService
import com.example.pushapp.services.PushAppRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class ReportsHistoryViewModel(
    private val repository: PushAppRepository,
    private val authService: PushAppAuthService
) : ViewModel(), DefaultLifecycleObserver {

    private val _reports = MutableLiveData<List<ReportModel>>()
    val reports: LiveData<List<ReportModel>> get() = _reports

    private val _onGetReportsFailureEvent = MutableSharedFlow<Throwable>()
    val onGetReportsFailureEvent get() = _onGetReportsFailureEvent.asSharedFlow()

    override fun onCreate(owner: LifecycleOwner) {
        getReports()
    }

    private fun getReports() = viewModelScope.launch {

        val currentUserID = authService.getCurrentUserId()

        currentUserID?.let { userID ->

            repository.getUserReports(userID)
                .onSuccess { reportsModel ->
                    _reports.value = reportsModel
                }
                .onFailure { failure ->
                    _onGetReportsFailureEvent.emit(failure)
                }
        }
    }

    private val _onDeleteReportFailureEvent = MutableSharedFlow<Throwable>()
    val onDeleteReportFailureEvent get() = _onDeleteReportFailureEvent.asSharedFlow()

    fun deleteReportById(reportId: String) = viewModelScope.launch {
        repository.deleteReportById(reportId)
            .onSuccess {
                getReports()
            }.onFailure { failure ->
                _onDeleteReportFailureEvent.emit(failure)
            }
    }
}