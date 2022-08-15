package com.example.pushapp.models.detailed_report

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class AccesedBy : Parcelable {
    HISTORY_FRAGMENT,
    WORKOUT_FRAGMENT
}