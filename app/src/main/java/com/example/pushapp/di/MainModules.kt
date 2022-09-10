package com.example.pushapp.di

import com.example.pushapp.models.ReportModel
import com.example.pushapp.models.WorkoutConfigurationModel
import com.example.pushapp.models.detailed_report.AccesedBy
import com.example.pushapp.models.detailed_report.ReportVariables
import com.example.pushapp.services.PushAppAuthService
import com.example.pushapp.services.PushAppFirebaseAuthService
import com.example.pushapp.services.PushAppRepository
import com.example.pushapp.services.PushAppFirebaseRepository
import com.example.pushapp.ui.detailed_report.DetailedReportViewModel
import com.example.pushapp.ui.detailed_report.ReportFilterBottomSheetViewModel
import com.example.pushapp.ui.register.RegisterUserViewModel
import com.example.pushapp.ui.reports_history.ReportsHistoryViewModel
import com.example.pushapp.ui.training_configuration.TrainingConfigurationViewModel
import com.example.pushapp.ui.workout.WorkoutViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mainModules = module {

    single<PushAppRepository> { PushAppFirebaseRepository() }

    single<PushAppAuthService> { PushAppFirebaseAuthService() }

    viewModel {
        TrainingConfigurationViewModel(
            authService = get(),
            repository = get()
        )
    }

    viewModel { (workoutConfigModel: WorkoutConfigurationModel) ->
        WorkoutViewModel(
            workoutConfigModel = workoutConfigModel,
            application = get(),
            authService = get()
        )
    }

    viewModel {
        RegisterUserViewModel(
            pushAppRepository = get(),
            authService = get()
        )
    }

    viewModel { (reportModel: ReportModel, accessedBy: AccesedBy) ->
        DetailedReportViewModel(
            reportModel = reportModel,
            accessedBy = accessedBy,
            repository = get()
        )
    }

    viewModel {
        ReportsHistoryViewModel(
            repository = get(),
            authService = get()
        )
    }

    viewModel { (reportVariables: Set<ReportVariables>) ->
        ReportFilterBottomSheetViewModel(
            filters = reportVariables
        )
    }
}