package com.example.campusguide.usability

class SimulatedUser(val profile: UserProfile) {

    fun pause() {
        PersonaBehavior.pauseFor(profile)
    }

    fun maybeMakeTypingError(text: String): String {
        if ((profile == UserProfile.JORDAN_LEE || profile == UserProfile.SOFIA_LOPEZ)&& (0..4).random() == 0) {
            return text.dropLast(1) + "x" // simple typo
        }
        return text
    }
}