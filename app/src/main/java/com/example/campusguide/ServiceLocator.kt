package com.example.campusguide

import com.example.campusguide.data.CalendarRepository
import com.example.campusguide.data.CalendarRepositoryImpl
import com.example.campusguide.data.ConcordiaScheduleRepository

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
}
