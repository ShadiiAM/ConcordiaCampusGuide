package com.example.campusguide.ui.map.geoJson
import org.junit.Assert.*
import org.junit.Test
class GeoJsonStyleTest {
    @Test
    fun constructor_withAllParameters_createsInstance() {
        val style = GeoJsonStyle(
            fillColor = 0xFFFF0000.toInt(),
            strokeColor = 0xFF0000FF.toInt(),
            strokeWidth = 2.5f,
            zIndex = 10f,
            clickable = true,
            visible = true,
            markerColor = 0xFF00FF00.toInt(),
            markerAlpha = 0.8f,
            markerScale = 1.5f
        )
        assertEquals(0xFFFF0000.toInt(), style.fillColor)
        assertEquals(0xFF0000FF.toInt(), style.strokeColor)
        assertEquals(2.5f, style.strokeWidth)
    }
    @Test
    fun constructor_withNoParameters_createsInstanceWithNulls() {
        val style = GeoJsonStyle()
        assertNull(style.fillColor)
        assertNull(style.strokeColor)
    }
    @Test
    fun copy_withChanges_createsNewInstance() {
        val original = GeoJsonStyle(fillColor = 0xFFFF0000.toInt(), strokeWidth = 2f)
        val modified = original.copy(fillColor = 0xFF0000FF.toInt(), strokeWidth = 4f)
        assertEquals(0xFF0000FF.toInt(), modified.fillColor)
        assertEquals(4f, modified.strokeWidth)
        assertEquals(0xFFFF0000.toInt(), original.fillColor)
    }
    @Test
    fun equals_sameValues_returnsTrue() {
        val style1 = GeoJsonStyle(fillColor = 0xFFFF0000.toInt(), strokeWidth = 2f)
        val style2 = GeoJsonStyle(fillColor = 0xFFFF0000.toInt(), strokeWidth = 2f)
        assertEquals(style1, style2)
    }
    @Test
    fun equals_differentValues_returnsFalse() {
        val style1 = GeoJsonStyle(fillColor = 0xFFFF0000.toInt())
        val style2 = GeoJsonStyle(fillColor = 0xFF0000FF.toInt())
        assertNotEquals(style1, style2)
    }
}
