package com.example.campusguide

import android.os.Environment
import android.util.Log
import java.io.File

object UsabilityTrackerIRLUsers {

    private val results = mutableListOf<String>()

    fun userInteractionRecord(userInteraction: String){
        val time = System.currentTimeMillis()
        results.add("$userInteraction,$time")
        Log.d("USABILITY", "$userInteraction recorded on time $time")
    }

    fun dumpResults() {
        results.forEach {
            Log.d("USABILITY_RESULT", it)
        }
    }

    fun dumpToFile() {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "usability_results_${System.currentTimeMillis()}.csv"
        )

        file.bufferedWriter().use { writer ->
            writer.write("UserInteraction,TimeSpotted\n")
            results.forEach { result ->
                writer.write("${result}\n")
            }
        }

        Log.d("UsabilityTracker", "CSV saved to: ${file.absolutePath}")
        results.clear()
    }
}