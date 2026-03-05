package com.example.campusguide.usability

import android.util.Log

object UsabilityTracker {

    private val startTimes = mutableMapOf<String, Long>()
    private val results = mutableListOf<String>()

    fun start(task: String) {
        startTimes[task] = System.currentTimeMillis()
    }

    fun end(task: String) {
        val start = startTimes[task] ?: return
        val duration = System.currentTimeMillis() - start
        results.add("$task,$duration")
        Log.d("USABILITY", "$task took ${duration}ms")
    }

    fun dumpResults() {
        results.forEach {
            Log.d("USABILITY_RESULT", it)
        }
    }
}