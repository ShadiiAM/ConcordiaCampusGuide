package com.example.campusguide.ui.map.geoJson

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.graphics.createBitmap
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.drawable.DrawableCompat
import com.example.campusguide.R
import com.example.campusguide.data.POIType
import com.example.campusguide.ui.components.getPOIColorAndDrawable
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

// Applies "Replace Magic Literal" refactoring — named constants instead of raw numbers
private val SHUTTLE_MARKER_COLOR = Color(0xFF1565C0)  // blue — distinct from building red 0xFFbc4949
private const val MARKER_WIDTH_PX    = 120
private const val RECT_HEIGHT_PX     = 95
private const val TRIANGLE_HEIGHT_PX = 30
private const val MARKER_HEIGHT_PX   = RECT_HEIGHT_PX + TRIANGLE_HEIGHT_PX
private const val CORNER_RADIUS      = 12f
private const val ICON_SIZE_PX       = 45
private const val TEXT_SIZE_PX       = 24f

/**
 * Factory object for shuttle stop markers.
 * Extends the Factory pattern established by [MarkerIconFactory] in Release 1.
 *
 * Renders a blue rounded-rectangle badge (bus icon + "Shuttle" label) with a
 * downward triangle tip anchored at the stop coordinate.
 */
object MapMarkerFactory {

    // Injectable hooks for tests (mirrors MarkerIconFactory pattern)
    internal var bitmapToDescriptor: (Bitmap) -> BitmapDescriptor =
        { bmp -> BitmapDescriptorFactory.fromBitmap(bmp) }

    internal var defaultMarker: () -> BitmapDescriptor =
        { BitmapDescriptorFactory.defaultMarker() }

    internal fun resetForTests() {
        bitmapToDescriptor = { bmp -> BitmapDescriptorFactory.fromBitmap(bmp) }
        defaultMarker = { BitmapDescriptorFactory.defaultMarker() }
    }

    fun makePaint(color: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.FILL
    }

    fun getDrawable(context: Context, res: Int) =
        AppCompatResources.getDrawable(context, res)



    fun create(context: Context, type: String): BitmapDescriptor {

        lateinit var iconDetails: Pair<Color, Int>
        // Draw white bus icon centered in upper portion of rectangle
        val poiType = enumValues<POIType>().firstOrNull { it.name == type }
        if(poiType != null){
            iconDetails = getPOIColorAndDrawable(poiType)
        }
        else{
            iconDetails = Pair(SHUTTLE_MARKER_COLOR, R.drawable.ic_directions_bus)
        }

        val iconDetailsTransform = Pair(makePaint(iconDetails.first.toArgb()),getDrawable(context, iconDetails.second) ?: return defaultMarker())


        val bitmap = createBitmap(MARKER_WIDTH_PX, MARKER_HEIGHT_PX)
        val canvas = Canvas(bitmap)



        // Draw rounded rectangle (top badge)
        val rect = RectF(0f, 0f, MARKER_WIDTH_PX.toFloat(), RECT_HEIGHT_PX.toFloat())
        canvas.drawRoundRect(rect, CORNER_RADIUS, CORNER_RADIUS, iconDetailsTransform.first)

        // Draw triangle tip pointing down, seamlessly below the rectangle
        val cx = MARKER_WIDTH_PX / 2f
        val tip = Path().apply {
            moveTo(cx - 24f, RECT_HEIGHT_PX.toFloat())
            lineTo(cx + 24f, RECT_HEIGHT_PX.toFloat())
            lineTo(cx, MARKER_HEIGHT_PX.toFloat())
            close()
        }
        canvas.drawPath(tip, iconDetailsTransform.first)


        val wrapped = DrawableCompat.wrap(iconDetailsTransform.second).mutate()
        DrawableCompat.setTint(wrapped, if(type == "Shuttle") Color.White.toArgb() else Color.Black.toArgb())

        val iconLeft = ((MARKER_WIDTH_PX - ICON_SIZE_PX) / 2f).toInt()
        val iconTop  = 10
        wrapped.setBounds(iconLeft, iconTop, iconLeft + ICON_SIZE_PX, iconTop + ICON_SIZE_PX)
        wrapped.draw(canvas)

        // Draw "Shuttle" text centered below the icon
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if(type == "Shuttle") Color.White.toArgb() else Color.Black.toArgb()
            textSize = TEXT_SIZE_PX
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        val textY = iconTop + ICON_SIZE_PX + TEXT_SIZE_PX + 2f
        canvas.drawText(type, cx, textY, textPaint)

        return bitmapToDescriptor(bitmap)
    }


}

