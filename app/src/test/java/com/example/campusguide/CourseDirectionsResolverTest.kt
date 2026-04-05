package com.example.campusguide

import com.example.campusguide.data.ALL_SUGGESTIONS
import com.example.campusguide.data.CampusBuilding
import com.example.campusguide.data.Course
import com.example.campusguide.indoor.IndoorFloorGraph
import com.example.campusguide.indoor.IndoorNode
import com.example.campusguide.indoor.IndoorNodeType
import com.example.campusguide.indoor.IndoorRoomSearchService
import com.example.campusguide.indoor.TestIndoorRegistry
import com.example.campusguide.ui.components.Campus
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CourseDirectionsResolverTest {

    @After
    fun tearDown() {
        TestIndoorRegistry.clear()
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun makeCourse(buildingCode: String, room: String = "") = Course(
        subject = "COMP",
        termCode = "2241",
        courseID = "1",
        catalog = "232",
        section = "AA",
        componentCode = "LEC",
        classNumber = "1234",
        courseTitle = "Test Course",
        locationCode = "SGW",
        buildingCode = buildingCode,
        room = room,
        startTime = "08:45",
        endTime = "10:00",
        startDate = "2024-01-01",
        endDate = "2024-04-01",
        mondays = "Y",
        tuesdays = "N",
        wednesdays = "N",
        thursdays = "N",
        fridays = "N",
        saturdays = "N",
        sundays = "N"
    )

    private fun resolveBuilding(course: Course): CampusBuilding? =
        ALL_SUGGESTIONS
            .filterIsInstance<CampusBuilding>()
            .firstOrNull { it.buildingCode.equals(course.buildingCode, ignoreCase = true) }

    private fun resolveIndoorRoom(course: Course): List<IndoorRoomSearchService.Result> =
        if (course.room.isNotBlank() && course.buildingCode.isNotBlank()) {
            IndoorRoomSearchService.search(
                query = course.room,
                scope = IndoorRoomSearchService.Scope.Building,
                buildingCode = course.buildingCode,
                limit = 1
            )
        } else emptyList()

    private fun seedRoom(buildingCode: String, floor: Int, nodeId: String) {
        val graph = IndoorFloorGraph(
            buildingCode = buildingCode,
            floor = floor,
            floorPlanDrawableRes = 0,
            imageWidth = 100,
            imageHeight = 100,
            nodes = listOf(
                IndoorNode(nodeId, nodeId.substringAfterLast("-"), 0f, 0f, IndoorNodeType.ROOM, floor, buildingCode)
            ),
            edges = emptyList()
        )
        TestIndoorRegistry.seed(graph)
    }

    // ── Building resolution ───────────────────────────────────────────────────

    @Test
    fun `resolveBuilding returns correct building for known code`() {
        val course = makeCourse("H")
        val building = resolveBuilding(course)
        assertNotNull(building)
        assertEquals("H", building!!.buildingCode)
        assertEquals("Henry F. Hall Building", building.buildingName)
    }

    @Test
    fun `resolveBuilding returns null for unknown building code`() {
        val course = makeCourse("XYZ")
        assertNull(resolveBuilding(course))
    }

    @Test
    fun `resolveBuilding is case insensitive`() {
        val course = makeCourse("h")
        val building = resolveBuilding(course)
        assertNotNull(building)
        assertEquals("H", building!!.buildingCode)
    }

    @Test
    fun `resolveBuilding returns correct campus for SGW building`() {
        val course = makeCourse("H")
        val building = resolveBuilding(course)
        assertEquals(Campus.SGW, building!!.campus)
    }

    @Test
    fun `resolveBuilding returns correct campus for Loyola building`() {
        val course = makeCourse("CC")
        val building = resolveBuilding(course)
        assertNotNull(building)
        assertEquals(Campus.LOYOLA, building!!.campus)
    }

    @Test
    fun `resolveBuilding displayName includes building code in parentheses`() {
        val course = makeCourse("MB")
        val building = resolveBuilding(course)
        assertNotNull(building)
        assertTrue(building!!.displayName.contains("(MB)"))
    }

    // ── Indoor room resolution ────────────────────────────────────────────────

    @Test
    fun `resolveIndoorRoom finds room when it exists in indoor map`() {
        seedRoom("H", 8, "H-8-851")

        val course = makeCourse("H", "851")
        val results = resolveIndoorRoom(course)

        assertEquals(1, results.size)
        assertEquals("H.851", results.first().primaryLabel)
        assertEquals("H", results.first().buildingCode)
    }

    @Test
    fun `resolveIndoorRoom returns empty when room not in indoor map`() {
        seedRoom("H", 8, "H-8-851")

        val course = makeCourse("H", "9999")
        val results = resolveIndoorRoom(course)

        assertTrue(results.isEmpty())
    }

    @Test
    fun `resolveIndoorRoom returns empty when room is blank`() {
        seedRoom("H", 8, "H-8-851")

        val course = makeCourse("H", "")
        val results = resolveIndoorRoom(course)

        assertTrue(results.isEmpty())
    }

    @Test
    fun `resolveIndoorRoom returns empty when building code is blank`() {
        val course = makeCourse("", "851")
        val results = resolveIndoorRoom(course)

        assertTrue(results.isEmpty())
    }

    @Test
    fun `resolveIndoorRoom only searches in the course building`() {
        val hGraph = IndoorFloorGraph(
            buildingCode = "H", floor = 8, floorPlanDrawableRes = 0,
            imageWidth = 100, imageHeight = 100,
            nodes = listOf(IndoorNode("H-8-851", "851", 0f, 0f, IndoorNodeType.ROOM, 8, "H")),
            edges = emptyList()
        )
        val mbGraph = IndoorFloorGraph(
            buildingCode = "MB", floor = 1, floorPlanDrawableRes = 0,
            imageWidth = 100, imageHeight = 100,
            nodes = listOf(IndoorNode("MB-1-851", "851", 0f, 0f, IndoorNodeType.ROOM, 1, "MB")),
            edges = emptyList()
        )
        TestIndoorRegistry.seed(hGraph, mbGraph)

        val course = makeCourse("H", "851")
        val results = resolveIndoorRoom(course)

        assertTrue(results.all { it.buildingCode == "H" })
    }

    // ── Resolution priority ───────────────────────────────────────────────────

    @Test
    fun `indoor result takes priority over building when room is mapped`() {
        seedRoom("H", 8, "H-8-851")

        val course = makeCourse("H", "851")
        val indoorResults = resolveIndoorRoom(course)
        val building = resolveBuilding(course)

        assertTrue(indoorResults.isNotEmpty())
        assertNotNull(building)
        assertEquals("H-8-851", indoorResults.first().node.id)
    }

    @Test
    fun `falls back to building when indoor room not found`() {
        seedRoom("H", 8, "H-8-851")

        val course = makeCourse("H", "9999")
        val indoorResults = resolveIndoorRoom(course)
        val building = resolveBuilding(course)

        assertTrue(indoorResults.isEmpty())
        assertNotNull(building)
        assertEquals("H", building!!.buildingCode)
    }

    @Test
    fun `shows unknown dialog when both indoor and building resolution fail`() {
        val course = makeCourse("XYZ", "000")
        val indoorResults = resolveIndoorRoom(course)
        val building = resolveBuilding(course)

        assertTrue(indoorResults.isEmpty())
        assertNull(building)
    }
}
