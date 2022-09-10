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
    val velocityPerTime: List<Offset> = listOf(),
    val powerPerTime: List<Offset> = listOf(),
    val forcePerTime: List<Offset> = listOf(),
    val accelerationPerTime: List<Offset> = listOf(),
    val meanVelocity: Float = 0f,
    val meanPower: Float = 0f,
    val meanForce: Float = 0f,
    val userId: String = "",
    val createdAt: Long? = null
) : Parcelable

@Parcelize
data class Offset(
    val value: Float = 0f,
    val timestamp: Float = 0f
) : Parcelable
