package com.example.pushapp.ui.workout

import com.example.pushapp.models.Offset
import com.example.pushapp.models.training_configuration.Exercise
import com.example.pushapp.models.training_configuration.TrainingMethodology

// ? Enviar para tela de DetailReport
data class WorkoutReportModel(
    val id: String = "",
    val exercise: Exercise = Exercise.getByValue(""),
    val trainingMethodology: TrainingMethodology = TrainingMethodology.getByValue(""),
    val weight: Int = 0,
    val offsetMovements: List<Offset> = listOf(),
    val velocityPerUnitTime: List<Offset> = listOf(),
    val accelerationPerUnitTime: List<Offset> = listOf(),
    val forcePerUnitTime: List<Offset> = listOf(),
    val powerPerUnitTime: List<Offset> = listOf(),
    val meanVelocity: Float = 0f,
    val meanPower: Float = 0f,
    val meanForce: Float = 0f,
    val userId: String = "",
    val createdAt: Long? = null
)