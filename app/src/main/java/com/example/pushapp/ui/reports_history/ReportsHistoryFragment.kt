package com.example.pushapp.ui.reports_history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pushapp.NavigationDirections
import com.example.pushapp.databinding.FragmentReportsHistoryBinding
import com.example.pushapp.models.detailed_report.AccesedBy
import com.example.pushapp.utils.flowObserver
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReportsHistoryFragment : Fragment() {

    private lateinit var binding: FragmentReportsHistoryBinding

    private val viewModel: ReportsHistoryViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentReportsHistoryBinding.inflate(inflater, container, false).apply {
        lifecycle.addObserver(viewModel)
        binding = this
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupBind()
    }

    private fun setupBind() = with(binding) {

        rvUserReports.adapter = GroupAdapter<GroupieViewHolder>().apply {

            viewModel.reports.observe(viewLifecycleOwner) { reportsModel ->
                clear()
                addAll(
                    reportsModel.mapIndexed { index, report ->
                        ReportHistoryItem(
                            model = report,
                            title = "Report",
                            onItemClick = { reportModel ->
                                findNavController().navigate(
                                    NavigationDirections.actionGlobalToDetailedReportFragment(
                                        reportModel,
                                        AccesedBy.HISTORY_FRAGMENT
                                    )
                                )
                            },
                            onGarbageClick = { reportId ->
                                viewModel.deleteReportById(reportId)
                            }
                        )
                    }
                )
            }
        }

        flowObserver(viewModel.onGetReportsFailureEvent) {
            Toast.makeText(requireContext(), "Xiiii falhou - ${it.message}", Toast.LENGTH_SHORT)
                .show()
        }

        flowObserver(viewModel.onDeleteReportFailureEvent) {
            Toast.makeText(requireContext(), "Xiiii falhou - ${it.message}", Toast.LENGTH_SHORT)
                .show()
        }
    }

}