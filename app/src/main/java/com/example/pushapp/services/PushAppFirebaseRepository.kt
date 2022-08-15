package com.example.pushapp.services

import com.example.pushapp.models.CreateUserRequest
import com.example.pushapp.models.ReportModel
import com.example.pushapp.models.UserModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.*

class PushAppFirebaseRepository : PushAppRepository {

    private val fireStore = FirebaseFirestore.getInstance()

    private val userCollection = fireStore.collection(USERS_COLLECTION)

    private val reportCollection = fireStore.collection(REPORTS_COLLECTION)

    override suspend fun saveCreatedUserData(user: CreateUserRequest): Result<UserModel> = try {

        val userDocument = userCollection.document(user.userId)

        val userDataMap = mutableMapOf<String, Any>().apply {
            put("email", user.email)
            put("firstName", user.firstName)
        }

        userDocument.set(userDataMap).await()

        val userCreated = userDocument.get().await().toObject(UserModel::class.java)

        userCreated?.let { userCreatedModel ->
            Result.success(userCreatedModel)
        } ?: Result.failure(Exception("Falha ao obter o usuario criado - userCreated null"))

    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getUser(userId: String): Result<UserModel> = try {

        val userDocument = userCollection.document(userId)

        val userModel = userDocument.get().await().toObject(UserModel::class.java)

        userModel?.let { userData ->
            Result.success(userData)
        } ?: Result.failure(Exception("Falha ao obter os dados do usuario - userModel null"))

    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun saveReport(report: ReportModel): Result<ReportModel> = try {

        val randomUUID = UUID.randomUUID().toString()

        val reportDocument = reportCollection.document(randomUUID)

        val reportData = mutableMapOf<String, Any>().apply {
            put("id", randomUUID)
            put("offsetMovements", report.offsetMovements)
            put("userId", report.userId)
            put("exercise", report.exercise)
            put("trainingMethodology", report.trainingMethodology)
            put("weight", report.weight)
            put("meanVelocity", report.meanVelocity)
            put("meanPower", report.meanPower)
            put("meanForce", report.meanForce)
            report.createdAt?.let {
                put("createdAt", report.createdAt)
            }
        }

        reportDocument.set(reportData).await()

        val reportCreated = reportDocument.get().await().toObject(ReportModel::class.java)

        reportCreated?.let { reportCreatedModel ->
            Result.success(reportCreatedModel)
        } ?: Result.failure(Exception("Falha ao obter o report criado - reportCreated null"))

    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getUserReports(userId: String): Result<List<ReportModel>> = try {

        val userReportDocuments =
            reportCollection.whereEqualTo("userId", userId)

        val reportsModel = userReportDocuments.get().await().toObjects(ReportModel::class.java)

        Result.success(reportsModel)

    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteReportById(reportId: String) = try {
        reportCollection.document(reportId).delete().await()
        Result.success(reportId)
    } catch (e: Exception) {
        Result.failure(e)
    }

    private companion object {
        const val USERS_COLLECTION = "users"
        const val REPORTS_COLLECTION = "reports"
    }
}