package com.example.campusguide.map.geoJson

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.example.campusguide.R

object MarkerIconFactory {

    // --- injectable hooks for tests ---
    internal var bitmapToDescriptor: (Bitmap) -> BitmapDescriptor =
        { bmp -> BitmapDescriptorFactory.fromBitmap(bmp) }

    internal var defaultMarker: () -> BitmapDescriptor =
        { BitmapDescriptorFactory.defaultMarker() }

    internal var drawableProvider: (Context) -> Drawable? =
        { ctx -> AppCompatResources.getDrawable(ctx, R.drawable.ic_poi) }

    // Cache: avoids re-creating the same bitmap when setAllStyles applies
    // the same color/scale/alpha to dozens of markers in a row.
    private var cachedParams: Triple<Int, Float, Float>? = null
    private var cachedDescriptor: BitmapDescriptor? = null

    internal fun resetForTests() {
        bitmapToDescriptor = { bmp -> BitmapDescriptorFactory.fromBitmap(bmp) }
        defaultMarker = { BitmapDescriptorFactory.defaultMarker() }
        drawableProvider = { ctx -> AppCompatResources.getDrawable(ctx, R.drawable.ic_poi) }
        cachedParams = null
        cachedDescriptor = null
    }

    fun create(
        context: Context,
        @ColorInt color: Int,
        scale: Float = 1f,
        alpha: Float = 1f
    ): BitmapDescriptor {
        val normalizedScale = scale.coerceIn(0.4f, 3f)
        val normalizedAlpha = alpha.coerceIn(0f, 1f)
        val params = Triple(color, normalizedScale, normalizedAlpha)

        // Return cached descriptor if params haven't changed
        cachedDescriptor?.let { if (cachedParams == params) return it }

        val drawable = AppCompatResources.getDrawable(context, R.drawable.ic_poi)
            ?: return BitmapDescriptorFactory.defaultMarker()

        val wrapped = DrawableCompat.wrap(drawable).mutate()
        DrawableCompat.setTint(wrapped, color)

        val a = (255f * normalizedAlpha).toInt().coerceIn(0, 255)
        wrapped.alpha = a

        val baseSizePx = 64 // bigger than 24dp so it actually looks good on map
        val size = (baseSizePx * normalizedScale).toInt().coerceAtLeast(16)

        wrapped.setBounds(0, 0, size, size)

        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        wrapped.draw(canvas)

        val result = bitmapToDescriptor(bitmap)
        cachedParams = params
        cachedDescriptor = result
        return result
    }
}