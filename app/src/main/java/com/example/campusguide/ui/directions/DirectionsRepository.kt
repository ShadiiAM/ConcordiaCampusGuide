package com.example.campusguide.ui.directions

interface DirectionsRepository {
    suspend fun getRoute(request: RouteRequest): RouteResult
}
