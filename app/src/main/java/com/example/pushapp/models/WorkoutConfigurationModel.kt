package com.example.pushapp.models

import android.os.Parcelable
import com.example.pushapp.models.training_configuration.Exercise
import com.example.pushapp.models.training_configuration.TrainingMethodology
import kotlinx.parcelize.Parcelize

@Parcelize
data class WorkoutConfigurationModel(
    val exercise: Exercise,
    val trainingMethodology: TrainingMethodology
) : Parcelable