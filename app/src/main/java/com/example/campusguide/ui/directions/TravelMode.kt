package com.example.campusguide.ui.directions

enum class TravelMode {
    DRIVE,
    DRIVING,
    TRANSIT,
    WALK,
    WALKING,
    BICYCLING;

    val label: String get() = when (this) {
        DRIVE, DRIVING   -> "Drive"
        TRANSIT -> "Transit"
        WALK, WALKING    -> "Walk"
        BICYCLING -> "Bicycle"
    }

    val apiValue: String get() = when (this) {
        DRIVE, DRIVING   -> "DRIVE"
        TRANSIT -> "TRANSIT"
        WALK, WALKING    -> "WALK"
        BICYCLING -> "BICYCLE"
    }

    val contentDescription: String get() = when (this) {
        DRIVE, DRIVING   -> "Drive: car route"
        TRANSIT -> "Transit: bus or metro route"
        WALK, WALKING    -> "Walk: pedestrian route"
        BICYCLING -> "Bicycle: bike route"
    }
}