package com.example.campusguide.usability

object PersonaBehavior {

    fun pauseFor(user: UserProfile) {
        val delay = when (user) {
            UserProfile.LIAM_DUBOIS -> (50..200)     // fast
            UserProfile.EMILY_NGUYEN -> (200..800)   // average
            UserProfile.ALEXIA_MARTIN -> (800..2000)  // slow
            UserProfile.JORDAN_LEE -> (400..1600)  // error prone (likely to reselect, twice as much as average)
            UserProfile.SOFIA_LOPEZ -> (1500..3000)  // distracted (long needless waits between button clicks)
        }.random()

        Thread.sleep(delay.toLong())
    }
}