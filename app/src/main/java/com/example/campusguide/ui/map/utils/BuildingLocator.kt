package com.example.campusguide.ui.map.geoJson

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polygon
import com.google.maps.android.PolyUtil
import org.json.JSONObject

class BuildingLocator(
    private val polygonsById: Map<String, List<Polygon>>,
    private val propsById: Map<String, JSONObject>,
    private val geodesic: Boolean = true
) {


    fun findBuilding(point: LatLng): BuildingHit? {
        for ((id, polygons) in polygonsById) {
            for (poly in polygons) {
                if (polygonContainsPoint(poly, point)) {
                    val props = propsById[id]
                    return BuildingHit(id, props)
                }
            }
        }
        return null
    }

    fun pointInBuilding(point: LatLng): Boolean {
        return findBuilding(point) != null
    }


    private fun polygonContainsPoint(polygon: Polygon, point: LatLng): Boolean {
        val outer = polygon.points
        if (!PolyUtil.containsLocation(point, outer, geodesic)) return false

        // Check holes
        polygon.holes.forEach { hole ->
            if (PolyUtil.containsLocation(point, hole, geodesic)) return false
        }
        return true
    }
}

data class BuildingHit(
    val id: String,
    val properties: JSONObject?
)
