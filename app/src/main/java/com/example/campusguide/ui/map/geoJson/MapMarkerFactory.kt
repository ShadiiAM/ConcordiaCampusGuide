package com.example.campusguide.ui.map.geoJson

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.graphics.createBitmap
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

private const val CAFE_POI_MARKER_COLOR = 0xFFFF7FF4.toInt()
private const val METRO_POI_MARKER_COLOR = 0xFF1E88E5.toInt()
private const val RESTAURANT_POI_MARKER_COLOR = 0xFFD32F2F.toInt()

private const val MUSEUM_POI_MARKER_COLOR = 0xFFFFF27F.toInt()
private const val GROCERY_POI_MARKER_COLOR = 0xFFFF9800.toInt()
private const val PARK_POI_MARKER_COLOR = 0xFF4CAF50.toInt()
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

        // Draw white bus icon centered in upper portion of rectangle

        val (paintToUse, drawable) = when(type){
            "Shuttle" -> Pair(makePaint(SHUTTLE_MARKER_COLOR), getDrawable(context, R.drawable.ic_directions_bus) ?: return defaultMarker())
            "Cafe" -> Pair(makePaint(CAFE_POI_MARKER_COLOR), getDrawable(context, R.drawable.cafe_icon) ?: return defaultMarker())
            "Metro" -> Pair(makePaint(METRO_POI_MARKER_COLOR), getDrawable(context, R.drawable.metro_icon) ?: return defaultMarker())
            "Restaurant" -> Pair(makePaint(RESTAURANT_POI_MARKER_COLOR), getDrawable(context, R.drawable.restaurant_icon) ?: return defaultMarker())
            "Museum" -> Pair(makePaint(MUSEUM_POI_MARKER_COLOR), getDrawable(context, R.drawable.museum_icon) ?: return defaultMarker())
            "Grocery" -> Pair(makePaint(GROCERY_POI_MARKER_COLOR), getDrawable(context, R.drawable.grocery_icon) ?: return defaultMarker())
            "Park" -> Pair(makePaint(PARK_POI_MARKER_COLOR), getDrawable(context, R.drawable.park_icon) ?: return defaultMarker())
            else -> Pair(makePaint(SHUTTLE_MARKER_COLOR), getDrawable(context, R.drawable.ic_directions_bus) ?: return defaultMarker())
        }


        val bitmap = createBitmap(MARKER_WIDTH_PX, MARKER_HEIGHT_PX)
        val canvas = Canvas(bitmap)



        // Draw rounded rectangle (top badge)
        val rect = RectF(0f, 0f, MARKER_WIDTH_PX.toFloat(), RECT_HEIGHT_PX.toFloat())
        canvas.drawRoundRect(rect, CORNER_RADIUS, CORNER_RADIUS, paintToUse)

        // Draw triangle tip pointing down, seamlessly below the rectangle
        val cx = MARKER_WIDTH_PX / 2f
        val tip = Path().apply {
            moveTo(cx - 24f, RECT_HEIGHT_PX.toFloat())
            lineTo(cx + 24f, RECT_HEIGHT_PX.toFloat())
            lineTo(cx, MARKER_HEIGHT_PX.toFloat())
            close()
        }
        canvas.drawPath(tip, paintToUse)


        val wrapped = DrawableCompat.wrap(drawable).mutate()
        DrawableCompat.setTint(wrapped, if(type == "Shuttle") Color.WHITE else Color.BLACK)

        val iconLeft = ((MARKER_WIDTH_PX - ICON_SIZE_PX) / 2f).toInt()
        val iconTop  = 10
        wrapped.setBounds(iconLeft, iconTop, iconLeft + ICON_SIZE_PX, iconTop + ICON_SIZE_PX)
        wrapped.draw(canvas)

        // Draw "Shuttle" text centered below the icon
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if(type == "Shuttle") Color.WHITE else Color.BLACK
            textSize = TEXT_SIZE_PX
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        val textY = iconTop + ICON_SIZE_PX + TEXT_SIZE_PX + 2f
        canvas.drawText(type, cx, textY, textPaint)

        return bitmapToDescriptor(bitmap)
    }


}

