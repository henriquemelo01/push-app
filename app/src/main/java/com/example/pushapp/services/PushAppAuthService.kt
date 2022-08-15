package com.example.pushapp.services

interface PushAppAuthService {

    suspend fun createAccount(email: String, password: String) : Result<String>

    suspend fun signIn(email: String, password: String) : Result<String>

    fun getCurrentUserId() : String?
}