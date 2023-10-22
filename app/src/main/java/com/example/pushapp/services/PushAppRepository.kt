package com.example.pushapp.services

import com.example.pushapp.models.CreateUserRequest
import com.example.pushapp.models.ReportModel
import com.example.pushapp.models.UserModel

interface PushAppRepository {

    suspend fun saveCreatedUserData(user: CreateUserRequest): Result<UserModel>

    suspend fun getUser(userId: String): Result<UserModel>

    suspend fun getUserReports(userId: String): Result<List<ReportModel>>

    suspend fun saveReport(report: ReportModel): Result<ReportModel>

    suspend fun deleteReportById(reportId: String): Result<String>

    suspend fun updateReportIdentifierName(
        reportId: String,
        reportIdentifierName: String
    ): Result<Unit>
}