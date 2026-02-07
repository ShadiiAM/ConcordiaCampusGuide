package com.example.campusguide.ui.map.geoJson
import com.example.campusguide.map.geoJson.GeoJsonStyle
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

    @Test
    fun constructor_withPartialParameters_createsInstanceWithMixedNulls() {
        val style = GeoJsonStyle(
            fillColor = 0xFFFF0000.toInt(),
            strokeWidth = 2.5f,
            clickable = true
        )

        assertEquals(0xFFFF0000.toInt(), style.fillColor)
        assertNull(style.strokeColor)
        assertEquals(2.5f, style.strokeWidth)
        assertNull(style.zIndex)
        assertEquals(true, style.clickable)
        assertNull(style.visible)
        assertNull(style.markerColor)
        assertNull(style.markerAlpha)
        assertNull(style.markerScale)
    }

    @Test
    fun hashCode_sameValues_returnsSameHash() {
        val style1 = GeoJsonStyle(fillColor = 0xFFFF0000.toInt(), strokeWidth = 2f)
        val style2 = GeoJsonStyle(fillColor = 0xFFFF0000.toInt(), strokeWidth = 2f)

        assertEquals(style1.hashCode(), style2.hashCode())
    }

    @Test
    fun hashCode_differentValues_returnsDifferentHash() {
        val style1 = GeoJsonStyle(fillColor = 0xFFFF0000.toInt())
        val style2 = GeoJsonStyle(fillColor = 0xFF0000FF.toInt())

        assertNotEquals(style1.hashCode(), style2.hashCode())
    }

    @Test
    fun toString_includesAllFields() {
        val style = GeoJsonStyle(
            fillColor = 0xFFFF0000.toInt(),
            strokeColor = 0xFF0000FF.toInt(),
            strokeWidth = 2.5f
        )

        val str = style.toString()

        assertTrue(str.contains("fillColor"))
        assertTrue(str.contains("strokeColor"))
        assertTrue(str.contains("strokeWidth"))
    }

    @Test
    fun markerStyle_allFieldsAccessible() {
        val style = GeoJsonStyle(
            markerColor = 0xFF00FF00.toInt(),
            markerAlpha = 0.75f,
            markerScale = 1.5f
        )

        assertEquals(0xFF00FF00.toInt(), style.markerColor)
        assertEquals(0.75f, style.markerAlpha)
        assertEquals(1.5f, style.markerScale)
    }

    @Test
    fun copy_preservesUnchangedFields() {
        val original = GeoJsonStyle(
            fillColor = 0xFFFF0000.toInt(),
            strokeColor = 0xFF0000FF.toInt(),
            strokeWidth = 2f,
            clickable = true
        )

        val modified = original.copy(fillColor = 0xFF00FF00.toInt())

        assertEquals(0xFF00FF00.toInt(), modified.fillColor)
        assertEquals(0xFF0000FF.toInt(), modified.strokeColor)
        assertEquals(2f, modified.strokeWidth)
        assertEquals(true, modified.clickable)
    }

    @Test
    fun destructuring_worksCorrectly() {
        val style = GeoJsonStyle(
            fillColor = 0xFFFF0000.toInt(),
            strokeColor = 0xFF0000FF.toInt(),
            strokeWidth = 2.5f,
            zIndex = 10f,
            clickable = true,
            visible = false,
            markerColor = 0xFF00FF00.toInt(),
            markerAlpha = 0.8f,
            markerScale = 1.5f
        )

        val (fill, stroke, width, zIndex, clickable, visible, markerColor, markerAlpha, markerScale) = style

        assertEquals(0xFFFF0000.toInt(), fill)
        assertEquals(0xFF0000FF.toInt(), stroke)
        assertEquals(2.5f, width)
        assertEquals(10f, zIndex)
        assertEquals(true, clickable)
        assertEquals(false, visible)
        assertEquals(0xFF00FF00.toInt(), markerColor)
        assertEquals(0.8f, markerAlpha)
        assertEquals(1.5f, markerScale)
    }

    @Test
    fun zIndex_canBeSet() {
        val style = GeoJsonStyle(zIndex = 5f)

        assertEquals(5f, style.zIndex)
    }

    @Test
    fun visible_canBeSetToFalse() {
        val style = GeoJsonStyle(visible = false)

        assertEquals(false, style.visible)
    }

    @Test
    fun visible_canBeSetToTrue() {
        val style = GeoJsonStyle(visible = true)

        assertEquals(true, style.visible)
    }

    @Test
    fun clickable_canBeSetToFalse() {
        val style = GeoJsonStyle(clickable = false)

        assertEquals(false, style.clickable)
    }

    @Test
    fun allNullStyle_isEqualToDefault() {
        val style1 = GeoJsonStyle()
        val style2 = GeoJsonStyle(
            fillColor = null,
            strokeColor = null,
            strokeWidth = null,
            zIndex = null,
            clickable = null,
            visible = null,
            markerColor = null,
            markerAlpha = null,
            markerScale = null
        )

        assertEquals(style1, style2)
    }
}
