package com.example.campusguide.ui.map.geoJson

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import com.example.campusguide.R
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

// Applies "Replace Magic Literal" refactoring — named constants instead of raw numbers
private const val SHUTTLE_MARKER_COLOR = 0xFF1565C0.toInt()  // blue — distinct from building red 0xFFbc4949
private const val MARKER_WIDTH_PX    = 120
private const val RECT_HEIGHT_PX     = 82
private const val TRIANGLE_HEIGHT_PX = 30
private const val MARKER_HEIGHT_PX   = RECT_HEIGHT_PX + TRIANGLE_HEIGHT_PX
private const val CORNER_RADIUS      = 12f
private const val ICON_SIZE_PX       = 38
private const val TEXT_SIZE_PX       = 20f

/**
 * Factory object for shuttle stop markers.
 * Extends the Factory pattern established by [MarkerIconFactory] in Release 1.
 *
 * Renders a blue rounded-rectangle badge (bus icon + "Shuttle" label) with a
 * downward triangle tip anchored at the stop coordinate.
 */
object ShuttleMarkerFactory {

    // Injectable hooks for tests (mirrors MarkerIconFactory pattern)
    internal var bitmapToDescriptor: (Bitmap) -> BitmapDescriptor =
        { bmp -> BitmapDescriptorFactory.fromBitmap(bmp) }

    internal var defaultMarker: () -> BitmapDescriptor =
        { BitmapDescriptorFactory.defaultMarker() }

    internal fun resetForTests() {
        bitmapToDescriptor = { bmp -> BitmapDescriptorFactory.fromBitmap(bmp) }
        defaultMarker = { BitmapDescriptorFactory.defaultMarker() }
    }

    fun create(context: Context): BitmapDescriptor {
        val bitmap = Bitmap.createBitmap(MARKER_WIDTH_PX, MARKER_HEIGHT_PX, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bluePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = SHUTTLE_MARKER_COLOR
            style = Paint.Style.FILL
        }

        // Draw rounded rectangle (top badge)
        val rect = RectF(0f, 0f, MARKER_WIDTH_PX.toFloat(), RECT_HEIGHT_PX.toFloat())
        canvas.drawRoundRect(rect, CORNER_RADIUS, CORNER_RADIUS, bluePaint)

        // Draw triangle tip pointing down, seamlessly below the rectangle
        val cx = MARKER_WIDTH_PX / 2f
        val tip = Path().apply {
            moveTo(cx - 24f, RECT_HEIGHT_PX.toFloat())
            lineTo(cx + 24f, RECT_HEIGHT_PX.toFloat())
            lineTo(cx, MARKER_HEIGHT_PX.toFloat())
            close()
        }
        canvas.drawPath(tip, bluePaint)

        // Draw white bus icon centered in upper portion of rectangle
        val drawable = AppCompatResources.getDrawable(context, R.drawable.ic_directions_bus)
            ?: return defaultMarker()
        val wrapped = DrawableCompat.wrap(drawable).mutate()
        DrawableCompat.setTint(wrapped, Color.WHITE)

        val iconLeft = ((MARKER_WIDTH_PX - ICON_SIZE_PX) / 2f).toInt()
        val iconTop  = 10
        wrapped.setBounds(iconLeft, iconTop, iconLeft + ICON_SIZE_PX, iconTop + ICON_SIZE_PX)
        wrapped.draw(canvas)

        // Draw "Shuttle" text centered below the icon
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = TEXT_SIZE_PX
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        val textY = iconTop + ICON_SIZE_PX + TEXT_SIZE_PX + 2f
        canvas.drawText("Shuttle", cx, textY, textPaint)

        return bitmapToDescriptor(bitmap)
    }
}
