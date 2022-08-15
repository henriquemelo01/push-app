package com.example.pushapp.models

enum class BluetoothConnectionStatus(val value: String) {
    CONNECTED("CONNECTED"),
    DISCONNECTED("DISCONNECTED");

    companion object {
        fun getByValue(value: String?) =
            values().firstOrNull { it.value == value } ?: DISCONNECTED
    }
}
