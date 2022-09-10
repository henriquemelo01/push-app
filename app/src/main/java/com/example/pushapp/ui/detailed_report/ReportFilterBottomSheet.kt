package com.example.pushapp.ui.detailed_report

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.pushapp.databinding.BottomSheetReportFilterBinding
import com.example.pushapp.models.detailed_report.ReportVariables
import com.example.pushapp.utils.flowObserver
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.lang.IllegalStateException

class ReportFilterBottomSheet : BottomSheetDialogFragment() {

    private val args by lazy {
        ReportFilterArgs.fromBundle(arguments)
    }

    private val viewModel: ReportFilterBottomSheetViewModel by viewModel {
        parametersOf(
            args.selectedFilters
        )
    }

    private lateinit var binding: BottomSheetReportFilterBinding

    private var onApplyFilters: ((filters: Set<ReportVariables>) -> Unit)? =
        null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = BottomSheetReportFilterBinding.inflate(inflater, container, false).apply {
        lifecycle.addObserver(viewModel)
        binding = this
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupListeners()
    }

    private fun setupListeners() = with(viewModel) {

        binding.cbOffsetFilter.setOnCheckedChangeListener { _, isSelected ->
            onSelection(
                isSelected = isSelected,
                reportVariable = ReportVariables.OFFSET
            )
        }

        binding.cbAccelerationFilter.setOnCheckedChangeListener { _, isSelected ->
            onSelection(
                isSelected = isSelected,
                reportVariable = ReportVariables.ACCELERATION
            )
        }

        binding.cbVelocityFilter.setOnCheckedChangeListener { _, isSelected ->
            onSelection(
                isSelected = isSelected,
                reportVariable = ReportVariables.VELOCITY
            )
        }

        binding.cbForceFilter.setOnCheckedChangeListener { _, isSelected ->
            onSelection(
                isSelected = isSelected,
                reportVariable = ReportVariables.FORCE
            )
        }

        binding.cbPowerFilter.setOnCheckedChangeListener { _, isSelected ->
            onSelection(
                isSelected = isSelected,
                reportVariable = ReportVariables.POWER
            )
        }

        binding.btApplyFilters.setOnClickListener {
            applyFilters()
        }


        flowObserver(applyFiltersEvent) {
            onApplyFilters?.invoke(it)
            dismiss()
        }

        offsetFilterState.observe(viewLifecycleOwner) {
            binding.cbOffsetFilter.isChecked = it
        }

        accelerationFilterState.observe(viewLifecycleOwner) {
            binding.cbAccelerationFilter.isChecked = it
        }

        velocityFilterState.observe(viewLifecycleOwner) {
            binding.cbVelocityFilter.isChecked = it
        }

        forceFilterState.observe(viewLifecycleOwner) {
            binding.cbForceFilter.isChecked = it
        }

        powerFilterState.observe(viewLifecycleOwner) {
            binding.cbPowerFilter.isChecked = it
        }

        onSelectionEvent.observe(viewLifecycleOwner) {
            setCorrespondingFilter(it)
        }
    }


    private fun setCorrespondingFilter(filterState: ReportFilterBottomSheetViewModel.ReportVariableState) =
        when (filterState.reportVariable) {
            ReportVariables.OFFSET -> binding.cbOffsetFilter.isChecked = filterState.isSelected
            ReportVariables.ACCELERATION -> binding.cbAccelerationFilter.isChecked = filterState.isSelected
            ReportVariables.VELOCITY -> binding.cbVelocityFilter.isChecked = filterState.isSelected
            ReportVariables.FORCE -> binding.cbForceFilter.isChecked = filterState.isSelected
            ReportVariables.POWER -> binding.cbPowerFilter.isChecked = filterState.isSelected
        }

    fun setOnApplyFilters(
        callback: (filters: Set<ReportVariables>) -> Unit
    ) {
        onApplyFilters = callback
    }

    @Parcelize
    data class ReportFilterArgs(
        val selectedFilters: Set<ReportVariables>
    ) : Parcelable {

        fun toBundle() = Bundle().apply {
            putParcelable(
                REPORT_FILTER_ARGS_KEY,
                this@ReportFilterArgs
            )
        }

        companion object {
            const val REPORT_FILTER_ARGS_KEY = "ARGS"

            fun fromBundle(args: Bundle?): ReportFilterArgs =
                args?.getParcelable(REPORT_FILTER_ARGS_KEY) ?: throw IllegalStateException(
                    "Fragment $this has null arguments"
                )
        }
    }

    companion object {
        fun newInstance(
            reportFilterArgs: ReportFilterArgs
        ) = ReportFilterBottomSheet().apply { arguments = reportFilterArgs.toBundle() }
    }
}