package com.example.pushapp.ui.reports_history

import android.view.View
import com.example.pushapp.R
import com.example.pushapp.databinding.ItemReportsHistoryReportBinding
import com.example.pushapp.models.ReportModel
import com.xwray.groupie.viewbinding.BindableItem
import java.text.SimpleDateFormat

class ReportHistoryItem(
    private val model: ReportModel,
    private val title: String,
    private val onItemClick: (report: ReportModel) -> Unit,
    private val onGarbageClick: (reportId: String) -> Unit
) : BindableItem<ItemReportsHistoryReportBinding>() {

    override fun bind(viewBinding: ItemReportsHistoryReportBinding, position: Int): Unit =
        with(viewBinding) {

            val createdAt = if (model.createdAt != null) "- ${
                SimpleDateFormat.getDateTimeInstance()
                    .format(model.createdAt)
            }" else ""

            tvReportItem.apply {
                text = "$title $createdAt"
                setOnClickListener { onItemClick(model) }
            }

            ivGarbage.setOnClickListener {
                if (model.id.isNotEmpty())
                    onGarbageClick(model.id)
            }

        }

    override fun getLayout() = R.layout.item_reports_history_report

    override fun initializeViewBinding(view: View) = ItemReportsHistoryReportBinding.bind(view)
}
