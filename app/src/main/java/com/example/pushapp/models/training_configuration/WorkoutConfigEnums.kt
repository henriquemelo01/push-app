package com.example.pushapp.models.training_configuration

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class TrainingMethodology(val value: String) : Parcelable {
    FREE_TRAINING("Treino livre"),
    VELOCITY_BASED_TRAINING("VBT");

    companion object {

        fun getByValue(value: String?) = values()
            .firstOrNull { it.value == value } ?: FREE_TRAINING
    }
}

@Parcelize
enum class Exercise(val value: String) : Parcelable {
    SMITH_MACHINE("Barra guiada"),
    NONE("Livre");

    companion object {
        fun getByValue(value: String?) = values().firstOrNull { it.value == value } ?: NONE
    }
}