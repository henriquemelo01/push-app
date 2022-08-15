package com.example.pushapp.ui.workout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.pushapp.databinding.DialogFragmentWorkoutFinishedBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FinishedWorkoutBottomSheet : BottomSheetDialogFragment() {

    private lateinit var binding: DialogFragmentWorkoutFinishedBinding

    private var onShowReportClickListener: (() -> Unit)? = null

    private var onSeeLaterClickListener: (() -> Unit)? = null

    private var onDiscardMeasuresClickListener: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = DialogFragmentWorkoutFinishedBinding.inflate(inflater, container, false).apply {
        binding = this
        isCancelable = false
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(binding) {

            btShowReport.setOnClickListener { onShowReportClickListener?.invoke() }

            btSeeLater.setOnClickListener { onSeeLaterClickListener?.invoke() }

            btDiscardMeasures.setOnClickListener { onDiscardMeasuresClickListener?.invoke() }
        }
    }

    fun setOnShowReportClickListener(callback: (() -> Unit)) {
        onShowReportClickListener = callback
    }

    fun setOnSeeLaterClickListener(callback: (() -> Unit)) {
        onSeeLaterClickListener = callback
    }

    fun setOnDiscardMeasuresClickListener(callback: (() -> Unit)) {
        onDiscardMeasuresClickListener = callback
    }
}