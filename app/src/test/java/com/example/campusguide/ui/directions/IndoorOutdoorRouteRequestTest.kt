package com.example.campusguide.ui.directions

import com.example.campusguide.indoor.IndoorNode
import com.example.campusguide.indoor.IndoorNodeType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class IndoorOutdoorRouteRequestTest {

    private fun node(id: String, building: String, floor: Int): IndoorNode = IndoorNode(
        id = id,
        label = id,
        x = 0f,
        y = 0f,
        type = IndoorNodeType.ROOM,
        floor = floor,
        buildingCode = building,
    )

    @Test
    fun request_holdsStartAndDestinationNodes() {
        val start = node("H-8-801", "H", 8)
        val destination = node("MB-1-102", "MB", 1)

        val request = IndoorOutdoorRouteRequest(
            startNode = start,
            destinationNode = destination,
        )

        assertEquals(start, request.startNode)
        assertEquals(destination, request.destinationNode)
    }

    @Test
    fun request_copyAndEquality_behaveAsDataClass() {
        val start = node("H-8-801", "H", 8)
        val destination = node("MB-1-102", "MB", 1)
        val request = IndoorOutdoorRouteRequest(start, destination)

        val same = request.copy()
        val changed = request.copy(destinationNode = node("MB-2-210", "MB", 2))

        assertEquals(request, same)
        assertEquals(request.hashCode(), same.hashCode())
        assertNotEquals(request, changed)
    }
}
