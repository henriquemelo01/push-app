package com.example.pushapp.ui.detailed_report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.pushapp.NavigationDirections
import com.example.pushapp.R
import com.example.pushapp.databinding.FragmentDetailedReportBinding
import com.example.pushapp.models.detailed_report.ReportVariables
import com.example.pushapp.utils.flowObserver
import com.example.pushapp.utils.setupStyle
import com.example.pushapp.utils.showOnce
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class DetailedReportFragment : Fragment() {

    private lateinit var binding: FragmentDetailedReportBinding

    private val arguments: DetailedReportFragmentArgs by navArgs()

    private val viewModel: DetailedReportViewModel by viewModel {
        parametersOf(arguments.reportModel, arguments.accesedBy)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentDetailedReportBinding.inflate(inflater, container, false).apply {
        lifecycle.addObserver(viewModel)
        binding = this
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupBind()
        setupLiveData()
        setupListeners()
    }

    private fun setupBind() = with(binding) {

        ivArrowLeft.setOnClickListener { requireActivity().onBackPressed() }

        ivReportGraphFilter.setOnClickListener {
            viewModel.triggerOpenFilterBottomSheetEvent()
        }
    }

    private fun setupLiveData() = with(viewModel) {

        trainingMethod.observe(viewLifecycleOwner) {
            binding.tvHeader.text = getString(R.string.detailed_report_title, it)
        }

        workoutWeight.observe(viewLifecycleOwner) {
            binding.tvWorkoutWeight.text =
                getString(R.string.workout_weight_detailed_report_label, it)
        }

        showSaveReportButton.observe(viewLifecycleOwner) { showSaveButton ->
            binding.btSaveReport.visibility = if (showSaveButton) View.VISIBLE else View.GONE
        }

        showDiscardReportButton.observe(viewLifecycleOwner) { showDiscardButton ->
            binding.btDiscardReport.visibility = if (showDiscardButton) View.VISIBLE else View.GONE
        }

        selectedFilterEntries.observe(viewLifecycleOwner) {

            binding.lcVelocityData.apply {

                setupStyle(
                    minValue = getSelectedFilterEntriesMinValue(),
                    maxValue = getSelectedFilterEntriesMaxValue()
                )

                if (it.entries.isEmpty())
                    clear()

                it.entries.forEachIndexed { index, entry ->

                    val chartId = entry.key.chartLabel

                    val lineColor = entry.key.graphLineColor

                    // Inverter Velocidade, Offset e Potencia
                    val signalInversionStatement =
                        entry.key == ReportVariables.VELOCITY || entry.key == ReportVariables.POWER

                    val entryValues =
                        if (signalInversionStatement) entry.value.map {
                            Entry(
                                it.x,
                                -it.y
                            )
                        } else entry.value

                    val dataSet = LineDataSet(entryValues, chartId).setupStyle(
                        context = context,
                        lineColor = lineColor,
                        containsGradient = false
                    )

//                    val dataSet = LineDataSet(entry.value, chartId).setupStyle(
//                        context = context,
//                        lineColor = lineColor,
//                        containsGradient = false
//                    )

                    if (index == 0)
                        data = LineData(dataSet)
                    else
                        data.addDataSet(dataSet)

                    data.notifyDataChanged()

                    notifyDataSetChanged()

                    invalidate()
                }
            }
        }

        exercise.observe(viewLifecycleOwner) {
            binding.tvExerciseData.text = it
        }

        meanVelocity.observe(viewLifecycleOwner) {
            binding.tvMeanVelocityData.text = getString(R.string.velocity_data_label, it)
        }

        meanPower.observe(viewLifecycleOwner) {
            binding.tvMeanPowerData.text = getString(R.string.mean_power_data_label, it)
        }

        meanForce.observe(viewLifecycleOwner) {
            binding.tvMeanForceData.text = getString(R.string.mean_force_data_label, it)
        }

        flowObserver(openFilterBottomSheetEvent) {
            ReportFilterBottomSheet.newInstance(
                reportFilterArgs = ReportFilterBottomSheet.ReportFilterArgs(
                    selectedFilters = viewModel.selectedFilters
                )
            ).apply {
                setOnApplyFilters {
                    viewModel.onApplyFilter(it.toMutableSet())
                }
            }.showOnce(childFragmentManager)
        }
    }

    private fun setupListeners() = with(viewModel) {

        flowObserver(onSaveReportSuccessEvent) {
            Toast
                .makeText(requireContext(), "Relatório foi salvo com sucesso", Toast.LENGTH_SHORT)
                .show()

            findNavController().navigate(NavigationDirections.actionGlobalToTrainingConfigurationFragment())
        }

        flowObserver(onSaveReportFailureEvent) {
            Toast.makeText(
                requireContext(),
                "Xiiii houve uma falha - ${it.message}",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.btSaveReport.setOnClickListener { saveReport() }

        binding.btDiscardReport.setOnClickListener {
            findNavController().navigate(NavigationDirections.actionGlobalToTrainingConfigurationFragment())
        }
    }

    private companion object {
        const val LINE_CHART_OFFSET_LABEL = "OFFSET"
    }
}