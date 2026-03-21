package com.example.campusguide

import com.example.campusguide.data.CalendarRepository
import com.example.campusguide.data.CalendarRepositoryImpl

/**
 * A Service Locator to provide singleton instances of repositories.
 */
object ServiceLocator {
    
    // Lazy singleton instance of the repository
    val calendarRepository: CalendarRepository by lazy {
        CalendarRepositoryImpl()
    }
}
