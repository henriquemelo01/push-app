package com.example.pushapp.utils

import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.example.pushapp.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

//fun <T> Fragment.flowObserver(flow: Flow<T>, observer: (value: T) -> Unit) {
//    lifecycleScope.launch {
//        flow.collect {
//            observer(it)
//        }
//    }
//}

fun <T> Fragment.flowObserver(flow: Flow<T>, observer: (value: T) -> Unit) {
    lifecycleScope.launchWhenStarted {
        flow.collectLatest {
            observer(it)
        }
    }
}

fun DialogFragment.showOnce(fragmentManager: FragmentManager) {
    try {
        val tag = this::class.java.simpleName

        if (fragmentManager.findFragmentByTag(tag) == null) {
            show(fragmentManager, tag)
        }
    } catch (error: Throwable) {
    }
}

fun BluetoothGattCharacteristic.isReadable(): Boolean =
    containsProperty(BluetoothGattCharacteristic.PROPERTY_READ)

fun BluetoothGattCharacteristic.isNotifyCharacteristic(): Boolean =
    containsProperty(BluetoothGattCharacteristic.PROPERTY_NOTIFY)

fun BluetoothGattCharacteristic.containsProperty(property: Int): Boolean =
    properties and property != 0

fun ByteArray.toHexString(): String =
    joinToString(separator = "", prefix = "0x") { String.format("%02X", it) }

fun ByteArray.processIntegerData(): Int = processData().toInt()

fun ByteArray.processFloatData() = processData().toFloat()

fun ByteArray.processData(): String {
    val hexStringData = toHexString().split("0x")[1]
    return hexStringData.decodeHex().trim { it == ' ' }
}

fun String.decodeHex(): String {
    require(length % 2 == 0) {"Must have an even length"}
    return String(
        chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    )
}

fun LineChart.setupStyle(
    minValue: Float = 0f,
    maxValue: Float = 1.2f
) = this.apply {

    axisRight.isEnabled = false

    axisLeft.apply {
        isEnabled = true
        axisMinimum = minValue
        axisMaximum = maxValue
    }

    xAxis.apply {
        position = XAxis.XAxisPosition.BOTTOM
        setDrawGridLines(true)
    }

    setTouchEnabled(true)

    isDragEnabled = true

    description = null

    enableScroll()
}

fun LineDataSet.setupStyle(
    context: Context, @ColorRes
    lineColor: Int = R.color.blue_primary
) =
    this.apply {

//        mode = LineDataSet.Mode.CUBIC_BEZIER

        setDrawValues(false)

        lineWidth = 3f

        isHighlightEnabled = true

        setDrawHighlightIndicators(false)

        setDrawCircles(false)

        color = ContextCompat.getColor(context, lineColor)

        setDrawFilled(true)
        fillDrawable = ContextCompat.getDrawable(context, R.drawable.shape_blue_primary)
    }

fun TextInputLayout.checkErrorState(wasError: Boolean) = apply {
    if (wasError)
        error = "Required *"
    else {
        error = null
        helperText = null
    }
}