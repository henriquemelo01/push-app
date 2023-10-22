package com.example.pushapp.services

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.ContextCompat

class DownloadReportCsvHelper(
    private val context: Context
) {
   fun downloadReportCsvFile(reportCsvFile: ReportCsvFile) {

       val request = DownloadManager.Request(Uri.parse(reportCsvFile.url)).apply {
           setTitle(reportCsvFile.filename)
           setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
           setDestinationInExternalPublicDir(
               Environment.DIRECTORY_DOWNLOADS,
               reportCsvFile.filename + CSV_FILE
           )
       }

       val manager = ContextCompat.getSystemService(context, DownloadManager::class.java)
       manager?.enqueue(request)
   }

    private companion object {
        const val CSV_FILE = ".csv"
    }
}

data class ReportCsvFile(
    val filename: String,
    val url: String
)