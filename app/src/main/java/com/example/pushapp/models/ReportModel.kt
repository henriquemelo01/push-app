package com.example.pushapp.models

import android.os.Parcelable
import com.example.pushapp.models.training_configuration.Exercise
import com.example.pushapp.models.training_configuration.TrainingMethodology
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReportModel(
    val id: String = "",
    val exercise: Exercise = Exercise.getByValue(""),
    val trainingMethodology: TrainingMethodology = TrainingMethodology.getByValue(""),
    val weight: Int = 0,
    val offsetMovements: List<Offset> = listOf(),
    val meanVelocity: Float = 0f,
    val meanPower: Float = 0f,
    val meanForce: Float = 0f, // + numberOfRepetitions: Int ??
    val userId: String = "",// + createdAt -> timestamp
    val createdAt: Long? = null
) : Parcelable

@Parcelize
data class Offset(
    val value: Float = 0f,
    val timestamp: Long = 0L
) : Parcelable
