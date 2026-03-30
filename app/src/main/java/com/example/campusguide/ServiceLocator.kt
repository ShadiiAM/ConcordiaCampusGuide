package com.example.campusguide

import android.content.Context
import com.example.campusguide.data.CalendarRepository
import com.example.campusguide.data.CalendarRepositoryImpl
import com.example.campusguide.data.CalendarStorage
import com.example.campusguide.data.ConcordiaScheduleRepository
import com.example.campusguide.data.calendarDataStore

/**
 * A Service Locator to provide singleton instances of repositories.
 */
object ServiceLocator {
    
    // Lazy singleton instance of the repository
    val calendarRepository: CalendarRepository by lazy {
        CalendarRepositoryImpl()
    }


    val concordiaScheduleRepository: ConcordiaScheduleRepository by lazy {
        ConcordiaScheduleRepository()
    }

    private var calendarStorage: CalendarStorage? = null

    fun getCalendarStorage(context: Context): CalendarStorage {
        return calendarStorage ?: synchronized(this) {
            calendarStorage ?: CalendarStorage(context.applicationContext.calendarDataStore).also { calendarStorage = it }
        }
    }
}
