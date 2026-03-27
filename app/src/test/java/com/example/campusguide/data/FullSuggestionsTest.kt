package com.example.campusguide.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.campusguide.ui.components.Campus
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.collections.emptyList
import kotlin.intArrayOf


@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class FullSuggestionsTest {


    val fbBuilding = ALL_SUGGESTIONS
        .filterIsInstance<CampusBuilding>()
        .first { it.buildingCode == "FB" }

    val hBuilding = ALL_SUGGESTIONS
        .filterIsInstance<CampusBuilding>()
        .first { it.buildingCode == "H" }

    val hingstonBuildingHAWing = ALL_SUGGESTIONS
        .filterIsInstance<CampusBuilding>()
        .first { it.buildingCode == "HA" }

    val zAnnex = ALL_SUGGESTIONS
        .filterIsInstance<CampusBuilding>()
        .first { it.buildingCode == "Z" }

    val sgwShuttleStop = ALL_SUGGESTIONS
        .filterIsInstance<ShuttleStop>()
        .first { it.id == "sgw_shuttle" }

    @Test
    fun campusBuildingScoreTest() {

        val testList = listOf(
            arrayOf("FB", "100"),
            arrayOf("F", "80"),
            arrayOf("Faubourg Building", "70"),
            arrayOf("Faubourg", "60"),
            arrayOf("Building", "50"),
            arrayOf("uilding", "30"),
            arrayOf("1250 Guy St.", "10"),
            arrayOf("zzz", "0")
        )

        for (test in testList) {
            assertEquals(test[1].toInt(), fbBuilding.score(test[0]))
        }
    }

    @Test
    fun shuttleStopScoreTest() {

        val testList = listOf(
            arrayOf("SGW Shuttle Stop", "100"),    // query, expected score
            arrayOf("SGW", "91"),
            arrayOf("Shu", "50"),
            arrayOf("ttle", "30"),
            arrayOf("Hall Building", "20"),
            arrayOf("zzz", "0")
        )


        for (test in testList) {
            assertEquals(test[1].toInt(), sgwShuttleStop.score(test[0]))
        }
    }

    @Test
    fun `fullSuggestions() Returns Empty If Query Empty`() {

        val testList = listOf(
            arrayOf<Any>(Campus.LOYOLA, true),    // activeCampus, crossCampus
            arrayOf<Any>(Campus.LOYOLA, false),
            arrayOf<Any>(Campus.SGW, true),
            arrayOf<Any>(Campus.SGW, false)
        )

        for (test in testList) {
            assertEquals(emptyList<Suggestion>(),
                fullSuggestions("",test[0] as Campus,test[1] as Boolean))
        }
    }

    @Test
    fun `First item in fullSuggestion() return is as expected`(){

        val testList = listOf(
            arrayOf(hBuilding,"H",Campus.LOYOLA, true),    //expected result,  query, activeCampus, crossCampus
            arrayOf(hingstonBuildingHAWing,"H",Campus.LOYOLA, false),
            arrayOf(zAnnex,"Z",Campus.SGW, false),
        )

        for (test in testList) {
            assertEquals(test[0] as Suggestion,
                fullSuggestions(test[1] as String,test[2] as Campus,test[3] as Boolean).firstOrNull())
        }



    }


}