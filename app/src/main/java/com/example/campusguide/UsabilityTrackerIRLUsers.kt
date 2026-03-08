package com.example.campusguide

import android.os.Environment
import android.util.Log
import java.io.File

object UsabilityTrackerIRLUsers {

    private val results = mutableListOf<String>()
    private val logResults = mutableListOf<String>()

    private var lastTime: Long? = null
    fun userInteractionRecord(userInteraction: String){

        val now = System.currentTimeMillis()

        if (lastTime != null) {
            val diff = now - lastTime!!
            val record = "$userInteraction,${diff / 1000.0}"
            Log.d("USABILITY", record)
            logResults.add(record)
        }
        results.add("$userInteraction,$now")

        lastTime = now
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