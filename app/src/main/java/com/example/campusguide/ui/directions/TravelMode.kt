package com.example.campusguide.ui.directions

enum class TravelMode {
    DRIVE,
    TRANSIT,
    WALK;

    val label: String get() = when (this) {
        DRIVE   -> "Drive"
        TRANSIT -> "Transit"
        WALK    -> "Walk"
    }

    val apiValue: String get() = when (this) {
        DRIVE   -> "DRIVE"
        TRANSIT -> "TRANSIT"
        WALK    -> "WALK"
    }

    val contentDescription: String get() = when (this) {
        DRIVE   -> "Drive: car route"
        TRANSIT -> "Transit: bus or metro route"
        WALK    -> "Walk: pedestrian route"
    }
}