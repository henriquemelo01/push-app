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
import com.example.pushapp.utils.flowObserver
import com.example.pushapp.utils.setupStyle
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
        binding = this
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupBind()
        setupLiveData()
        setupListeners()
    }

    private fun setupBind() = with(binding) {

    }

    private fun setupLiveData() = with(viewModel) {

        trainingMethod.observe(viewLifecycleOwner) {
            binding.ctDetailedReportTitle.title = getString(R.string.detailed_report_title, it)
        }

        showSaveReportButton.observe(viewLifecycleOwner) { showSaveButton ->
            binding.btSaveReport.visibility = if (showSaveButton) View.VISIBLE else View.GONE
        }

        showDiscardReportButton.observe(viewLifecycleOwner) { showDiscardButton ->
            binding.btDiscardReport.visibility = if (showDiscardButton) View.VISIBLE else View.GONE
        }

        offsetEntries.observe(viewLifecycleOwner) { entries ->

            binding.lcOffsetMovements.apply {
                setupStyle()

                visibility = if (entries.isNotEmpty()) View.VISIBLE else View.GONE

                val dataSet =
                    LineDataSet(entries, LINE_CHART_OFFSET_LABEL).setupStyle(context)

                data = LineData(dataSet)

                invalidate()
            }
        }

        exercise.observe(viewLifecycleOwner) {
            binding.tvExerciseData.text = it
        }

        meanVelocity.observe(viewLifecycleOwner) {
            binding.tvMeanVelocityData.text = it.toString()
        }

        meanPower.observe(viewLifecycleOwner) {
            binding.tvMeanPowerData.text = it.toString()
        }

        meanForce.observe(viewLifecycleOwner) {
            binding.tvMeanForceData.text = it.toString()
        }
    }

    private fun setupListeners() = with(viewModel) {

        flowObserver(onSaveReportSuccessEvent) {
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