package com.example.campusguide.usability

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File

object UsabilityTracker {

    private val startTimes = mutableMapOf<String, Long>()
    private val results = mutableListOf<String>()

    fun start(task: String) {
        startTimes[task] = System.currentTimeMillis()
    }

    fun end(task: String, profile: UserProfile) {
        val start = startTimes[task] ?: return
        val duration = System.currentTimeMillis() - start
        results.add("$profile,$task,$duration")
        Log.d("USABILITY", "$task took ${duration}ms")
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
            writer.write("UserPersona,TaskName,duration_ms\n")
            results.forEach { result ->
                writer.write("${result}\n")
            }
        }

        Log.d("UsabilityTracker", "CSV saved to: ${file.absolutePath}")
        results.clear()
    }
}