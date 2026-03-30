package com.example.campusguide.data

import com.example.campusguide.ui.components.Campus
import com.example.campusguide.ui.components.DayOfWeek
import com.example.campusguide.ui.shuttle.NearestShuttleStopFinder.distanceBetween
import com.google.android.gms.maps.model.LatLng

data class OutsidePOI(
    val name: String,
    val description: String = "Place of interest",
    val category: POIType,
    val workingHours: WorkingHours,
    val rating: Double,
    val latLng: LatLng,
    val address: String
): Suggestion() {

    fun filterPOI(userLocation: LatLng, distanceLimit: Float, categoriesIncluded: List<POIType>, rating: Double ): Boolean{

        val passDistanceCheck = distanceBetween(userLocation, this.latLng) <= distanceLimit
        val passCategoryCheck = this.category in categoriesIncluded
        val passRatingCheck = this.rating >= rating

        return passDistanceCheck && passCategoryCheck && passRatingCheck
    }




    override fun score(query: String): Int {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return 0
        val name = name.lowercase()
        val addr = address.lowercase()
        val type = category.toString().lowercase()
        val nameWords = name.split(Regex("\\s+"))
        return when {
            name == q                                        -> 100   // exact full name
            name.startsWith(q)                               -> 80   // name starts with query
            nameWords.any { it.startsWith(q) }               -> 60   // any word in name starts with query
            type.contains(q)                                 -> 50
            name.contains(q)                                 -> 40   // substring anywhere in name
            addr.contains(q)                                 -> 30   // match in address
            q.contains(type)                                 -> 20
            else                                             -> 0
        }
    }




}

enum class POIType {
    Cafe,
    Metro,
    Restaurant,
    Museum,
    Grocery,
    Park
}

data class WorkingHours(
    val openingHour: Double,
    val closingHour: Double,
    val closedDays: List<DayOfWeek>
)



val ALL_POI: List<OutsidePOI> = listOf(


    // SGW LOCATIONS
    // CAFE
    OutsidePOI("Bar Caffettiera", "$1-10 Coffee Shop with a nice outdoor sitting area",
        POIType.Cafe,
        WorkingHours(7.5, 18.0, emptyList()),
        4.7,
        LatLng(45.500802547728085, -73.57620118364804),
"2055 Stanley St, Montreal, H3A 0B7"
    ),

    OutsidePOI("Cafe Aunja", "$10–20 Cozy basement-level Persian tea & coffee spot offering sweets, art on the walls & live music.",
        POIType.Cafe,
        WorkingHours(9.0, 19.5, emptyList()),
        4.6,
        LatLng(45.497801861904286, -73.58028329999999),
"1448 Sherbrooke St W, Montreal, H3G 1K4"
    ),


    OutsidePOI("Cafe Abu El Zulof", "Cafe with nice cozy interior and great food",
        POIType.Cafe,
        WorkingHours(12.0, 2.0, emptyList()),
        4.4,
        LatLng(45.49645508166357, -73.57379645952263),
        "1175 Crescent St, Montreal, H3G 2B1"
    ),


        OutsidePOI("Café Green Drip", "$1-10 Coffee Shop with new modern style and vegan options",
        POIType.Cafe,
        WorkingHours(10.0, 20.0, emptyList()),
        4.3,
        LatLng(45.49433665199065, -73.5803841),
        "1444 Rue St Mathieu, Montréal, H3H 2H9"
    ),
    // METRO

    OutsidePOI("Guy Concordia Metro Station", "Metro station Entrance for quick transport in montreal, Green Line",
        POIType.Metro,
        WorkingHours(5.5, 1.0, emptyList()),
        3.1,
        LatLng(45.495744181373205, -73.57912220415031),
        "Montreal, QC H3G 2J1"
    ),

    OutsidePOI("Guy-Concordia Entrance 2", "Metro station Entrance for quick transport in montreal, Green Line",
        POIType.Metro,
        WorkingHours(5.5, 1.0, emptyList()),
        3.1,
        LatLng(45.49455780859723, -73.58091123758282),
"Montreal, QC H3H 2H9"
    ),

    OutsidePOI("McGill Metro Station", "Metro station Entrance for quick transport in montreal, Green Line",
        POIType.Metro,
        WorkingHours(5.5, 1.0, emptyList()),
        4.2,
        LatLng(45.50415763269394, -73.57159570000002),
        "GC3 HJ9 Montreal, Quebec"
    ),

    // RESTAURANT

    OutsidePOI("Masakali Indian Cuisine", "A highly rated restaurant that offers authentic Indian flavors, a favorite in Ottawa",
        POIType.Restaurant,
        WorkingHours(11.5, 23.0, emptyList()),
        4.9,
        LatLng(45.50271474968275, -73.57652615803725),
        "1015 Sherbrooke St W, Montreal, H3A 1G5"
    ),

    OutsidePOI("Siam Centre-Ville", "Thai restaurant with a private dining room, great cocktails, and outdoor seating",
        POIType.Restaurant,
        WorkingHours(11.5, 22.0, emptyList()),
        4.4,
        LatLng(45.496677503892, -73.57292957301507),
"1325 René-Lévesque Blvd W, Montreal, H3G 0A4"
    ),


    OutsidePOI("Gyu-Kaku Japanese BBQ", "Japanese style restaurant with great BBQ options, cocktails and a cozy fireplace for happy hour",
        POIType.Restaurant,
        WorkingHours(11.5, 22.5, emptyList()),
        4.6,
        LatLng(45.497098125376624, -73.57495095396985),
        "1255 Crescent St, Montreal, H3G 1P6"
    ),


    // MUSEUM

    OutsidePOI("McCord Stewart Museum", "Historical exhibits of costumes, objects & photos on popular subjects from hockey to Inuit art. Might require reservation",
        POIType.Museum,
        WorkingHours(10.0, 17.0, listOf(DayOfWeek.MONDAY)),
        4.4,
        LatLng(45.50436895488742, -73.57341268465662),
        "690 Sherbrooke St W, Montreal, H3A 1E9"
    ),

    OutsidePOI("Montreal Museum of Fine Arts", "Spacious museum showcasing Québec & Canadian visual works, plus international contemporary art. Might require reservation",
        POIType.Museum,
        WorkingHours(10.0, 17.0, listOf(DayOfWeek.MONDAY)),
        4.7,
        LatLng(45.49861214158194, -73.5793786423283),
        "1380 Sherbrooke St W, Montreal, H3G 1J5"
    ),

    OutsidePOI("Montreal Science Centre", "Interactive & educational science & technology exhibits for kids of all ages, with an IMAX theatre. Might require reservation",
        POIType.Museum,
        WorkingHours(9.0, 16.0, emptyList()),
        4.4,
        LatLng(45.504877631539905, -73.55077518650754),
        "2 De la Commune St W, Montreal, H2Y 4B2"
    ),

    // GROCERY_STORE

    OutsidePOI("Provigo avenue des Canadiens de Montréal", "Well Priced grocery store with varied options for all things groceries",
        POIType.Grocery,
        WorkingHours(8.0, 21.0, emptyList()),
        4.0,
        LatLng(45.49651420411009, -73.5706621),
        "1275 Av. des Canadiens-de-Montréal #200, Montreal, H3B 5E8"
    ),

    OutsidePOI("Supermarché PA du Fort", "Branch of a supermarket chain offering produce, meat & fish, plus other staples.",
        POIType.Grocery,
        WorkingHours(8.0, 22.0, emptyList()),
        4.3,
        LatLng(45.49227061965489, -73.5819916367755),
        "1420 Fort St, Montreal, H3H 2C4"
    ),

    OutsidePOI("Adonis Downtown", "Full-range supermarket emphasizing Middle Eastern items, plus prepared foods, pastry & baked goods. Student Discounts available",
        POIType.Grocery,
        WorkingHours(8.0, 22.0, emptyList()),
        3.9,
        LatLng(45.49067142392599, -73.58336814047736),
        "2173 Rue Sainte-Catherine O, Montréal, H3H 1M9"
    ),

    // PARK

    OutsidePOI("Mount Royal Park", "Sizable green space featuring trails, a lake with boat rentals & striking city panoramas.",
        POIType.Park,
        WorkingHours(6.0, 0.0, emptyList()),
        4.8,
        LatLng(45.501746873260345, -73.593078425134),
        "1260 Chem. Remembrance, Montréal, H3H 1A2"
    ),

    OutsidePOI("Dorchester Square", "Sizable green space established in the late 1800s featuring statues & monuments.",
        POIType.Park,
        WorkingHours(6.0, 23.0, emptyList()),
        4.5,
        LatLng(45.49968327987511, -73.5709606269849),
        "2903 Peel St, Montreal, H3B 4P2"
    ),

    OutsidePOI("Oscar Peterson Park", "Park with dogs allowed, picnic tables, and playgrounds.",
        POIType.Park,
        WorkingHours(6.0, 23.0, emptyList()),
        4.3,
        LatLng(45.48922376359782, -73.57415966746228),
        "810 Rue Chatham, Montréal, H3J 0B8"
    ),

)


fun fullPOISuggestions(
    query: String,
    max: Int = 8,
): List<Suggestion> {
    val q = query.trim()
    if (q.isEmpty()) return emptyList()
    return ALL_POI
        .mapNotNull { poi -> val s = poi.score(q); if (s > 0) poi to s else null }
        .sortedByDescending { (_, s) -> s }
        .map { (suggestions, _) -> suggestions }
        .take(max)
}
