package com.example.pushapp.services

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class PushAppFirebaseAuthService : PushAppAuthService {

    private val auth = FirebaseAuth.getInstance()

    override suspend fun createAccount(email: String, password: String): Result<String> = try {

        val authResult = auth.createUserWithEmailAndPassword(email, password).await()

        Result.success(authResult?.user?.uid.orEmpty())

    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun signIn(email: String, password: String) = try {

        val authResult = auth.createUserWithEmailAndPassword(email, password).await()

        authResult?.user?.let {
            return@let Result.success(it.uid)
        } ?: Result.failure(Exception("Usuario nulo"))

    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun getCurrentUserId(): String? = auth.currentUser?.uid
}