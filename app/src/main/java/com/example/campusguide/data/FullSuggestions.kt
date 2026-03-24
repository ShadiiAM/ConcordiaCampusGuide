package com.example.campusguide.data

import com.example.campusguide.indoor.IndoorNode
import com.example.campusguide.ui.components.Campus
import com.google.android.gms.maps.model.LatLng


sealed class Suggestion(
    open val campus: Campus?

) {

    /** True if the suggestion is relevant for the given query. */
    fun matches(query: String): Boolean = score(query) > 0          // same for all and doesn't change for children

    open fun score(query: String): Int {
        return -1
    }
}


/**
 * A campus building entry used for autocomplete suggestions.
 * LatLng is NOT stored here — the caller resolves it from the GeoJSON overlay
 * using [buildingCode] as the feature ID, exactly like the polygon click handler does.
 */
class CampusBuilding(
    val buildingCode: String,
    val buildingName: String,
    val address: String,
    override val campus: Campus,
) : Suggestion(campus) {
    /** Shown in the text field after selection: "Henry F. Hall Building (H)" */
    val displayName: String get() = "$buildingName ($buildingCode)"

    /**
     * Relevance score for ranking search results.
     * Higher = better match. Returns 0 if not relevant.
     */
    override fun score(query: String): Int {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return 0
        val code = buildingCode.lowercase()
        val name = buildingName.lowercase()
        val addr = address.lowercase()
        val nameWords = name.split(Regex("\\s+"))
        return when {
            code == q                                        -> 100  // exact code  e.g. "h"  → H
            code.startsWith(q)                               -> 80   // code prefix e.g. "fb" → FB
            name == q                                        -> 70   // exact full name
            name.startsWith(q)                               -> 60   // name starts with query
            nameWords.any { it.startsWith(q) }               -> 50   // any word in name starts with query
            name.contains(q)                                 -> 30   // substring anywhere in name
            addr.contains(q)                                 -> 10   // match in address
            else                                             -> 0
        }
    }
}


data class ShuttleStop(
    val id: String,
    val name: String,
    val description: String = "Concordia Shuttle Service",
    override val campus: Campus,
    val latLng: LatLng,
    ) : Suggestion(campus) {

    override fun score(query: String): Int {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return 0
        val description = description.lowercase()
        val name = name.lowercase()
        val nameWords = name.split(Regex("\\s+"))
        return when {
            name == q                                        -> 100   // exact full name
            name.startsWith(q)                               -> 91   // name starts with query
            nameWords.any { it.startsWith(q) }               -> 50   // any word in name starts with query
            name.contains(q)                                 -> 30   // substring anywhere in name
            description.contains(q)                          -> 20
            else                                             -> 0
        }
    }
}

data class Indoor(
    val node: IndoorNode,
    val buildingCode: String,
    val primaryLabel: String,
    val secondaryLabel: String,
    val tertiaryLabel: String,
    override val campus: Campus? = null
) : Suggestion(campus)

private val SGW_STOP_LAT_LNG      = LatLng(45.4971, -73.5785)  // Hall Building front door, De Maisonneuve Blvd W
private val LOYOLA_ARRIVAL_LAT_LNG   = LatLng(45.4579, -73.6389)  // Loyola stop — arriving from downtown
private val LOYOLA_DEPARTURE_LAT_LNG = LatLng(45.4576, -73.6390)  // Loyola stop — departing to downtown

val ALL_SUGGESTIONS: List<Suggestion> = listOf(
    // SGW
    CampusBuilding("B", "B Annex", "2160 Bishop St.", Campus.SGW),
    CampusBuilding("CI", "CI Annex",                                                       "2149 Mackay St.",              Campus.SGW),
    CampusBuilding("CL", "CL Annex",                                                       "1665 Ste-Catherine St. W.",    Campus.SGW),
    CampusBuilding("D",  "D Annex",                                                        "2140 Bishop St.",              Campus.SGW),
    CampusBuilding("EN", "EN Annex",                                                       "2070 Mackay St.",              Campus.SGW),
    CampusBuilding("ER", "ER Building",                                                    "2155 Guy St.",                 Campus.SGW),
    CampusBuilding("EV", "Engineering, Computer Science and Visual Arts Integrated Complex","1515 Ste-Catherine St. W.",    Campus.SGW),
    CampusBuilding("FA", "FA Annex",                                                       "2060 Mackay St.",              Campus.SGW),
    CampusBuilding("FB", "Faubourg Building",                                              "1250 Guy St.",                 Campus.SGW),
    CampusBuilding("FG", "Faubourg Ste-Catherine Building",                                "1610 Ste-Catherine St. W.",    Campus.SGW),
    CampusBuilding("GA", "Grey Nuns Annex",                                                "1211-1215 St-Mathieu St.",     Campus.SGW),
    CampusBuilding("GM", "Guy-De Maisonneuve Building",                                    "1550 De Maisonneuve Blvd. W.", Campus.SGW),
    CampusBuilding("GN", "Grey Nuns Building",                                             "1190 Guy St.",                 Campus.SGW),
    CampusBuilding("GS", "GS Building",                                                    "1538 Sherbrooke St. W.",       Campus.SGW),
    CampusBuilding("H",  "Henry F. Hall Building",                                         "1455 De Maisonneuve Blvd. W.", Campus.SGW),
    CampusBuilding("K",  "K Annex",                                                        "2150 Bishop St.",              Campus.SGW),
    CampusBuilding("LB", "J.W. McConnell Building",                                        "1400 De Maisonneuve Blvd. W.", Campus.SGW),
    CampusBuilding("LD", "LD Building",                                                    "1424 Bishop St.",              Campus.SGW),
    CampusBuilding("LS", "Learning Square",                                                "1535 De Maisonneuve Blvd. W.", Campus.SGW),
    CampusBuilding("M",  "M Annex",                                                        "2135 Mackay St.",              Campus.SGW),
    CampusBuilding("MB", "John Molson Building",                                           "1450 Guy St.",                 Campus.SGW),
    CampusBuilding("MI", "MI Annex",                                                       "2130 Bishop St.",              Campus.SGW),
    CampusBuilding("MU", "MU Annex",                                                       "2170 Bishop St.",              Campus.SGW),
    CampusBuilding("P",  "P Annex",                                                        "2020 Mackay St.",              Campus.SGW),
    CampusBuilding("PR", "PR Annex",                                                       "2100 Mackay St.",              Campus.SGW),
    CampusBuilding("Q",  "Q Annex",                                                        "2010 Mackay St.",              Campus.SGW),
    CampusBuilding("R",  "R Annex",                                                        "2050 Mackay St.",              Campus.SGW),
    CampusBuilding("RR", "RR Annex",                                                       "2040 Mackay St.",              Campus.SGW),
    CampusBuilding("S",  "S Annex",                                                        "2145 Mackay St.",              Campus.SGW),
    CampusBuilding("SB", "Samuel Bronfman Building",                                       "1590 Docteur-Penfield",        Campus.SGW),
    CampusBuilding("T",  "T Annex",                                                        "2030 Mackay St.",              Campus.SGW),
    CampusBuilding("TD", "Toronto-Dominion Building",                                      "1410 Guy St.",                 Campus.SGW),
    CampusBuilding("V",  "V Annex",                                                        "2110 Mackay St.",              Campus.SGW),
    CampusBuilding("VA", "Visual Arts Building",                                           "1395 Rene-Levesque Blvd. W.", Campus.SGW),
    CampusBuilding("X",  "X Annex",                                                        "2080 Mackay St.",              Campus.SGW),
    CampusBuilding("Z",  "Z Annex",                                                        "2090 Mackay St.",              Campus.SGW),
    // Loyola
    CampusBuilding("AD", "Administration Building",                          "7141 Sherbrooke St. W.", Campus.LOYOLA),
    CampusBuilding("BB", "BB Annex",                                         "3502 Belmore Ave.",      Campus.LOYOLA),
    CampusBuilding("BH", "BH Annex",                                         "3500 Belmore Ave.",      Campus.LOYOLA),
    CampusBuilding("CC", "Central Building",                                 "7141 Sherbrooke St. W.", Campus.LOYOLA),
    CampusBuilding("CJ", "Communication Studies and Journalism Building",    "7141 Sherbrooke St. W.", Campus.LOYOLA),
    CampusBuilding("DO", "Stinger Dome",                                     "7141 Sherbrooke St. W.", Campus.LOYOLA),
    CampusBuilding("FC", "F.C. Smith Building",                              "7141 Sherbrooke St. W.", Campus.LOYOLA),
    CampusBuilding("GE", "Centre for Structural and Functional Genomics",    "7141 Sherbrooke St. W.", Campus.LOYOLA),
    CampusBuilding("HA", "Hingston Hall, wing HA",                           "7141 Sherbrooke St. W.", Campus.LOYOLA),
    CampusBuilding("HB", "Hingston Hall, wing HB",                           "7141 Sherbrooke St. W.", Campus.LOYOLA),
    CampusBuilding("HC", "Hingston Hall, wing HC",                           "7141 Sherbrooke St. W.", Campus.LOYOLA),
    CampusBuilding("HU", "Applied Science Hub",                              "7141 Sherbrooke St. W.", Campus.LOYOLA),
    CampusBuilding("JR", "Jesuit Residence",                                 "7141 Sherbrooke St. W.", Campus.LOYOLA),
    CampusBuilding("PC", "PERFORM Centre",                                   "7200 Sherbrooke St. W.", Campus.LOYOLA),
    CampusBuilding("PS", "Physical Services Building",                       "7141 Sherbrooke St. W.", Campus.LOYOLA),
    CampusBuilding("PT", "Oscar Peterson Concert Hall",                      "7141 Sherbrooke St. W.", Campus.LOYOLA),
    CampusBuilding("PY", "Psychology Building",                              "7141 Sherbrooke St. W.", Campus.LOYOLA),
    CampusBuilding("QA", "Quadrangle",                                       "7141 Sherbrooke St. W.", Campus.LOYOLA),
    CampusBuilding("RA", "Recreation and Athletics Complex",                 "7200 Sherbrooke St. W.", Campus.LOYOLA),
    CampusBuilding("RF", "Loyola Jesuit Hall and Conference Centre",         "7141 Sherbrooke St. W.", Campus.LOYOLA),
    CampusBuilding("SC", "Student Centre",                                   "7141 Sherbrooke St. W.", Campus.LOYOLA),
    CampusBuilding("SH", "Future Buildings Laboratory",                      "7141 Sherbrooke St. W.", Campus.LOYOLA),
    CampusBuilding("SI", "St. Ignatius of Loyola Church",                    "4455 West Broadway St.", Campus.LOYOLA),
    CampusBuilding("SP", "Richard J. Renaud Science Complex",                "7141 Sherbrooke St. W.", Campus.LOYOLA),
    CampusBuilding("TA", "Terrebonne Building",                              "7079 de Terrebonne St.", Campus.LOYOLA),
    CampusBuilding("VE", "Vanier Extension",                                 "7141 Sherbrooke St. W.", Campus.LOYOLA),
    CampusBuilding("VL", "Vanier Library Building",                          "7141 Sherbrooke St. W.", Campus.LOYOLA),

    // Shuttle Stops

    ShuttleStop("sgw_shuttle",    "SGW Shuttle Stop",                                       "Henry F. Hall Building, 1455 De Maisonneuve Blvd. W.", Campus.SGW,SGW_STOP_LAT_LNG),
    ShuttleStop("loyola_shuttle_arrival",    "Loyola Shuttle Stop (Arrival)",                                        "Loyola Chapel, 7137 Sherbrooke St. W. — Drop-off from downtown" ,  Campus.LOYOLA, LOYOLA_ARRIVAL_LAT_LNG),
    ShuttleStop("loyola_shuttle_departure",    "Loyola Shuttle Stop (Departure)",           "Loyola Chapel, 7137 Sherbrooke St. W. — Pick-up to downtown", Campus.LOYOLA, LOYOLA_DEPARTURE_LAT_LNG)
)


fun fullSuggestions(
    query: String,
    activeCampus: Campus,
    crossCampus: Boolean,
    max: Int = 8,
): List<Suggestion> {
    val q = query.trim()
    if (q.isEmpty()) return emptyList()
    val pool = if (crossCampus) ALL_SUGGESTIONS
    else ALL_SUGGESTIONS.filter { it.campus == activeCampus }
    return pool
        .mapNotNull { suggestions -> val s = suggestions.score(q); if (s > 0) suggestions to s else null }
        .sortedByDescending { (_, s) -> s }
        .map { (suggestions, _) -> suggestions }
        .take(max)
}