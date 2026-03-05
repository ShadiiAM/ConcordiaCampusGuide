package com.example.campusguide.ui.directions

enum class TravelMode(val apiValue: String, val label: String, val contentDescription: String) {
    DRIVE("DRIVE", "Drive", "Drive: car route"),
    WALK("WALK", "Walk", "Walk: pedestrian route"),
    TRANSIT("TRANSIT", "Transit", "Transit: bus or metro route")
}