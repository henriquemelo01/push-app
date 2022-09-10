package com.example.pushapp.models.detailed_report

import androidx.annotation.ColorRes
import com.example.pushapp.R

enum class ReportVariables(val chartLabel: String, @ColorRes val graphLineColor: Int) {
    OFFSET("Posicão", R.color.blue),
    ACCELERATION("Aceleração", R.color.dark_wine),
    VELOCITY("Velocidade", R.color.purple_500),
    FORCE("Força", R.color.slime_green),
    POWER("Potência", R.color.light_red);
}