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


