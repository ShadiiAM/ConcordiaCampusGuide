package com.example.campusguide.map.geoJson

import android.content.Context
import androidx.annotation.RawRes

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext



/**
 * Dynamic GeoJSON overlay that draws real Polygons/Markers and keeps references.
 * Supports Polygon, MultiPolygon, Point.
 *
 */
class GeoJsonOverlay(
private val context: Context,
@RawRes private val geoJsonRawRes: Int? = null,
private val idPropertyName: String = "id" // fallback to feature "id" field
) {
    private var map: GoogleMap? = null

    private val polygonsById = ConcurrentHashMap<String, MutableList<Polygon>>() // MultiPolygon = multiple
    private val markersById = ConcurrentHashMap<String, Marker>()

    private val featurePropsById = ConcurrentHashMap<String, JSONObject>()

    private val appContext = context.applicationContext

    fun attachToMap(googleMap: GoogleMap, geoJson: JSONObject? = null) {
        clear()
        map = googleMap

        val json = geoJson ?: loadFromRawOrThrow()
        val features = json.optJSONArray("features") ?: return

        for (i in 0 until features.length()) {
            val feature = features.optJSONObject(i) ?: continue
            addFeature(feature)
        }
    }
    /**
     * Async variant: coordinate parsing runs on the calling thread (intended: IO);
     * only addPolygon / addMarker are dispatched to Main.  Keeps the main-thread
     * block short — critical for avoiding ANR on map load.
     */
    suspend fun attachToMapAsync(googleMap: GoogleMap, geoJson: JSONObject) {
        val features = geoJson.optJSONArray("features") ?: return

        // --- Phase 1: build options on calling thread — zero Map API calls ---
        class PendingPoly  (val id: String, val options: PolygonOptions,  val props: JSONObject)
        class PendingMarker(val id: String, val options: MarkerOptions,   val props: JSONObject)

        val pendingPolys   = ArrayList<PendingPoly>(features.length())
        val pendingMarkers = ArrayList<PendingMarker>(features.length())

        for (i in 0 until features.length()) {
            val feature = features.optJSONObject(i) ?: continue
            val id      = stableIdFor(feature)
            val props   = feature.optJSONObject("properties") ?: JSONObject()
            val geom    = feature.optJSONObject("geometry")   ?: continue

            when (geom.optString("type")) {
                "Polygon" -> {
                    val coords = geom.optJSONArray("coordinates") ?: continue
                    pendingPolys.add(PendingPoly(id, buildPolygonOptions(coords), props))
                }
                "MultiPolygon" -> {
                    val coords = geom.optJSONArray("coordinates") ?: continue
                    for (p in 0 until coords.length()) {
                        val polyCoords = coords.optJSONArray(p) ?: continue
                        pendingPolys.add(PendingPoly(id, buildPolygonOptions(polyCoords), props))
                    }
                }
                "Point" -> {
                    val coords = geom.optJSONArray("coordinates") ?: continue
                    if (coords.length() < 2) continue
                    val title = props.optString("building-name", "").ifBlank { null }
                    val opts  = MarkerOptions().position(LatLng(coords.optDouble(1), coords.optDouble(0)))
                    if (title != null) opts.title(title)
                    pendingMarkers.add(PendingMarker(id, opts, props))
                }
            }
        }

        // --- Phase 2: Map API calls on Main only (fast: no parsing) ---
        withContext(Dispatchers.Main) {
            clear()
            map = googleMap

            for (p in pendingPolys) {
                featurePropsById[p.id] = p.props
                val poly = googleMap.addPolygon(p.options)
                applyStyleToPolygon(poly, styleFromProperties(p.props))
                polygonsById.getOrPut(p.id) { mutableListOf() }.add(poly)
            }
            for (m in pendingMarkers) {
                featurePropsById[m.id] = m.props
                val marker = googleMap.addMarker(m.options) ?: continue
                applyStyleToMarker(marker, styleFromProperties(m.props))
                markersById[m.id] = marker
            }
        }
    }

    fun clear() {
        polygonsById.values.flatten().forEach { it.remove() }
        markersById.values.forEach { it.remove() }

        polygonsById.clear()
        markersById.clear()
        featurePropsById.clear()
    }

    fun removeFeature(featureId: String) {
        polygonsById.remove(featureId)?.forEach { it.remove() }
        markersById.remove(featureId)?.remove()
        featurePropsById.remove(featureId)
    }

    fun reapplyPropertiesStyles() {
        polygonsById.forEach { (id, polys) ->
            val props = featurePropsById[id] ?: return@forEach
            val style = styleFromProperties(props)
            applyStyleToPolygons(polys, style)
        }
    }

    //--------------------
    // Style manipulation
    //--------------------

    fun setStyleForFeature(featureId: String, style: GeoJsonStyle) {
        polygonsById[featureId]?.let { applyStyleToPolygons(it, style) }
        markersById[featureId]?.let { applyStyleToMarker(it, style) }
    }

    fun setAllStyles(style: GeoJsonStyle) {
        polygonsById.values.forEach { applyStyleToPolygons(it, style) }
        markersById.values.forEach { applyStyleToMarker(it, style) }
    }

    /** Opacity only (0..1). Keeps each polygon’s current RGB. */
    fun setFillOpacityForAll(opacity: Float) {
        val op = opacity.coerceIn(0f, 1f)
        polygonsById.values.flatten().forEach { poly ->
            poly.fillColor = GeoJsonColorUtils.withOpacity(poly.fillColor, op)
        }
    }

    fun setVisibleAll(visible: Boolean) {
        polygonsById.values.flatten().forEach { it.isVisible = visible }
        markersById.values.forEach { it.isVisible = visible }
    }
    // --------- Markers ---------
    fun setMarkersVisible(visible: Boolean) {
        markersById.values.forEach { it.isVisible = visible }
    }
    fun setMarkerVisible(featureId: String, visible: Boolean) {
        markersById[featureId]?.isVisible = visible
    }


    // ---------- BUILDINGS (POLYGONS) ----------

    fun setBuildingsVisible(visible: Boolean) {
        polygonsById.values.flatten().forEach { it.isVisible = visible }
    }

    fun setBuildingVisible(featureId: String, visible: Boolean) {
        polygonsById[featureId]?.forEach { it.isVisible = visible }
    }

    //--------- Getters ---------

    fun getBuildings(): Map<String, List<Polygon>> = polygonsById
    fun getBuildingProps(): Map<String, JSONObject> = featurePropsById




    // --------------------
    // Internals
    // --------------------

    private fun addFeature(feature: JSONObject) {
        val googleMap = map ?: return

        val id = stableIdFor(feature)
        val props = feature.optJSONObject("properties") ?: JSONObject()
        featurePropsById[id] = props

        val geom = feature.optJSONObject("geometry") ?: return
        val type = geom.optString("type")

        when (type) {
            "Polygon" -> {
                val coords = geom.optJSONArray("coordinates") ?: return
                val poly = buildPolygonFromCoordinates(googleMap, coords, props)
                polygonsById[id] = mutableListOf(poly)
            }
            "MultiPolygon" -> {
                val coords = geom.optJSONArray("coordinates") ?: return
                val polys = mutableListOf<Polygon>()
                for (p in 0 until coords.length()) {
                    val polygonCoords = coords.optJSONArray(p) ?: continue
                    polys += buildPolygonFromCoordinates(googleMap, polygonCoords, props)
                }
                polygonsById[id] = polys
            }
            "Point" -> {
                val coords = geom.optJSONArray("coordinates") ?: return
                val marker = buildMarkerFromPoint(googleMap, coords, props)
                if (marker != null) markersById[id] = marker
            }
        }
    }

    /** Build PolygonOptions from a GeoJSON coordinate array — no Map API calls, safe on any thread. */
    private fun buildPolygonOptions(coords: JSONArray): PolygonOptions {
        val outerRing = coords.optJSONArray(0) ?: JSONArray()
        val options = PolygonOptions()
            .addAll(parseLngLatRing(outerRing))
            .clickable(true)
        for (h in 1 until coords.length()) {
            val hole = coords.optJSONArray(h) ?: continue
            options.addHole(parseLngLatRing(hole))
        }
        return options
    }

    private fun buildPolygonFromCoordinates(
        googleMap: GoogleMap,
        coords: JSONArray,
        props: JSONObject
    ): Polygon {
        val poly  = googleMap.addPolygon(buildPolygonOptions(coords))
        applyStyleToPolygon(poly, styleFromProperties(props))
        return poly
    }

    private fun buildMarkerFromPoint(
        googleMap: GoogleMap,
        coords: JSONArray,
        props: JSONObject
    ): Marker? {
        if (coords.length() < 2) return null
        val lng = coords.optDouble(0)
        val lat = coords.optDouble(1)

        val title = props.optString("building-name", "").ifBlank { null }

        val marker = googleMap.addMarker(
            MarkerOptions()
                .position(LatLng(lat, lng))
                .title(title)
        ) ?: return null

        val style = styleFromProperties(props)
        applyStyleToMarker(marker, style)

        return marker
    }

    private fun parseLngLatRing(ring: JSONArray): List<LatLng> {
        val pts = ArrayList<LatLng>(ring.length())
        for (i in 0 until ring.length()) {
            val pair = ring.optJSONArray(i) ?: continue
            val lng = pair.optDouble(0)
            val lat = pair.optDouble(1)
            pts.add(LatLng(lat, lng))
        }
        return pts
    }

    private fun styleFromProperties(props: JSONObject): GeoJsonStyle {

        val strokeHex = GeoJsonColorUtils.stringOrNull(props.opt("stroke"))
        val fillHex = GeoJsonColorUtils.stringOrNull(props.opt("fill"))

        val strokeOpacity = GeoJsonColorUtils.floatOrNull(props.opt("stroke-opacity")) ?: 1f
        val fillOpacity = GeoJsonColorUtils.floatOrNull(props.opt("fill-opacity")) ?: 1f

        val strokeWidth = GeoJsonColorUtils.floatOrNull(props.opt("stroke-width"))

        val strokeColor = strokeHex?.let { safeParse(it) }?.let { GeoJsonColorUtils.withOpacity(it, strokeOpacity) }
        val fillColor = fillHex?.let { safeParse(it) }?.let { GeoJsonColorUtils.withOpacity(it, fillOpacity) }

        val markerHex = props.optString("marker-color", null)
        val markerColor = markerHex?.takeIf { it.isNotBlank() }?.let { safeParse(it) }

        val markerSize = props.optString("marker-size", "medium")
        val markerScale = when (markerSize) {
            "small" -> 0.75f
            "large" -> 1.4f
            else -> 1f
        }

        return GeoJsonStyle(
            fillColor = fillColor,
            strokeColor = strokeColor,
            strokeWidth = strokeWidth,
            clickable = true,

            markerColor = markerColor,
            markerScale = markerScale,
            markerAlpha = 1f
        )
    }

    private fun safeParse(hex: String): Int? =
        try { GeoJsonColorUtils.parse(hex) } catch (_: Throwable) { null }

    private fun applyStyleToPolygons(polys: List<Polygon>, style: GeoJsonStyle) {
        polys.forEach { applyStyleToPolygon(it, style) }
    }

    private fun applyStyleToPolygon(poly: Polygon, style: GeoJsonStyle) {
        style.fillColor?.let { poly.fillColor = it }
        style.strokeColor?.let { poly.strokeColor = it }
        style.strokeWidth?.let { poly.strokeWidth = it }
        style.zIndex?.let { poly.zIndex = it }
        style.visible?.let { poly.isVisible = it }
        style.clickable?.let { poly.isClickable = it }
    }

    private fun applyStyleToMarker(marker: Marker, style: GeoJsonStyle) {
        style.visible?.let { marker.isVisible = it }
        style.zIndex?.let { marker.zIndex = it }

        // Apply alpha directly to the marker object (Google Maps supports this)
        style.markerAlpha?.let { marker.alpha = it.coerceIn(0f, 1f) }

        // If you pass markerColor, we always set a new icon (no guard that can fail silently)
        style.markerColor?.let { color ->
            val scale = (style.markerScale ?: 1f).coerceIn(0.4f, 3f)
            marker.setIcon(
                MarkerIconFactory.create(
                    context = appContext,
                    color = color,
                    scale = scale,
                    alpha = style.markerAlpha ?: 1f
                )
            )
        }
    }

    private fun stableIdFor(feature: JSONObject): String {
        // Prefer properties[idPropertyName]
        val props = feature.optJSONObject("properties")
        val fromProp = props?.opt(idPropertyName)?.toString()?.takeIf { it.isNotBlank() }
        if (fromProp != null) return fromProp

        // Then feature-level "id"
        val id = feature.opt("id")?.toString()?.takeIf { it.isNotBlank() }
        if (id != null) return id

        // fallback
        return feature.toString().hashCode().toString()
    }

    private fun loadFromRawOrThrow(): JSONObject {
        val resId = geoJsonRawRes
            ?: throw IllegalStateException("Provide JSONObject or raw resource id.")
        val input = context.resources.openRawResource(resId)
        val text = BufferedReader(InputStreamReader(input)).use { it.readText() }
        return JSONObject(text)
    }
}
