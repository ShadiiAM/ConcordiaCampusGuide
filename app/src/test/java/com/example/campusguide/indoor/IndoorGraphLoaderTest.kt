package com.example.campusguide.indoor

import android.content.Context
import android.content.res.AssetManager
import com.example.campusguide.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.ByteArrayInputStream

class IndoorGraphLoaderTest {

    @Test
    fun `parse maps every known drawable name to resource id`() {
        val cases = listOf(
            "cc_floor_1" to R.drawable.cc_floor_1,
            "hall_floor_1" to R.drawable.hall_floor_1,
            "hall_floor_2" to R.drawable.hall_floor_2,
            "hall_floor_8" to R.drawable.hall_floor_8,
            "hall_floor_9" to R.drawable.hall_floor_9,
            "lb_floor_2" to R.drawable.lb_floor_2,
            "lb_floor_3" to R.drawable.lb_floor_3,
            "lb_floor_4" to R.drawable.lb_floor_4,
            "lb_floor_5" to R.drawable.lb_floor_5,
            "molson_floor_1" to R.drawable.molson_floor_1,
            "molson_floor_s2" to R.drawable.molson_floor_s2,
            "ve_floor_1" to R.drawable.ve_floor_1,
            "ve_floor_2" to R.drawable.ve_floor_2,
            "vl_floor_1" to R.drawable.vl_floor_1,
            "vl_floor_2" to R.drawable.vl_floor_2
        )

        cases.forEach { (imageBaseName, expectedRes) ->
            val graph = IndoorGraphLoader.parse(minimalGraphJson(imageName = "$imageBaseName.png"))
            assertEquals("Failed on drawable '$imageBaseName'", expectedRes, graph.floorPlanDrawableRes)
        }
    }

    @Test
    fun `parse maps known drawable name to resource id`() {
        val graph = IndoorGraphLoader.parse(minimalGraphJson(imageName = "hall_floor_1.png"))

        assertEquals("H", graph.buildingCode)
        assertEquals(1, graph.floor)
        assertEquals(R.drawable.hall_floor_1, graph.floorPlanDrawableRes)
    }

    @Test
    fun `parse returns zero resource id for unknown drawable name`() {
        val graph = IndoorGraphLoader.parse(minimalGraphJson(imageName = "does_not_exist.png"))

        assertEquals(0, graph.floorPlanDrawableRes)
    }

    @Test
    fun `parse supports image names without extension`() {
        val graph = IndoorGraphLoader.parse(minimalGraphJson(imageName = "hall_floor_8"))

        assertEquals(R.drawable.hall_floor_8, graph.floorPlanDrawableRes)
    }

    @Test
    fun `loadAll returns empty when indoor assets folder is missing`() {
        val context = mock<Context>()
        val assets = mock<AssetManager>()
        whenever(context.assets).thenReturn(assets)
        whenever(assets.list("indoor")).thenReturn(null)

        val result = IndoorGraphLoader.loadAll(context)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `loadAll loads only valid json files and skips malformed json`() {
        val context = mock<Context>()
        val assets = mock<AssetManager>()
        whenever(context.assets).thenReturn(assets)
        whenever(assets.list("indoor")).thenReturn(arrayOf("good.json", "notes.txt", "bad.json"))
        whenever(assets.open(eq("indoor/good.json"))).thenReturn(
            ByteArrayInputStream(minimalGraphJson("hall_floor_2.png").toByteArray())
        )
        whenever(assets.open(eq("indoor/bad.json"))).thenReturn(
            ByteArrayInputStream("{not valid json".toByteArray())
        )

        val result = IndoorGraphLoader.loadAll(context)

        assertEquals(1, result.size)
        assertEquals(R.drawable.hall_floor_2, result.first().floorPlanDrawableRes)
        verify(assets).open(eq("indoor/good.json"))
        verify(assets).open(eq("indoor/bad.json"))
    }

    private fun minimalGraphJson(imageName: String): String =
        """
        {
          "buildingCode": "H",
          "floor": 1,
          "imageName": "$imageName",
          "imageWidth": 1000,
          "imageHeight": 800,
          "nodes": [],
          "edges": []
        }
        """.trimIndent()
}


