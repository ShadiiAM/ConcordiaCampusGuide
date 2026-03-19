package com.example.campusguide.data

import com.example.campusguide.ui.components.Campus
import com.google.android.gms.maps.model.LatLng


sealed class Suggestion(
    val campus: Campus

) {

    /** True if the suggestion is relevant for the given query. */
    fun matches(query: String): Boolean = score(query) > 0          // same for all and doesn't change for children

    open fun score(query: String): Int {
        return -1
    }
}

data class SuggestionData(
    val suggestion: Suggestion
    ){}


/**
 * A campus building entry used for autocomplete suggestions.
 * LatLng is NOT stored here — the caller resolves it from the GeoJSON overlay
 * using [buildingCode] as the feature ID, exactly like the polygon click handler does.
 */
class CampusBuilding(
    val buildingCode: String,
    val buildingName: String,
    val address: String,
    campus: Campus,
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


class ShuttleStop(
    val id: String,
    val name: String,
    val description: String = "Concordia Shuttle Service",
    campus: Campus,
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

private val SGW_STOP_LAT_LNG      = LatLng(45.4971, -73.5785)  // Hall Building front door, De Maisonneuve Blvd W
private val LOYOLA_ARRIVAL_LAT_LNG   = LatLng(45.4579, -73.6389)  // Loyola stop — arriving from downtown
private val LOYOLA_DEPARTURE_LAT_LNG = LatLng(45.4576, -73.6390)  // Loyola stop — departing to downtown

val ALL_SUGGESTIONS: List<SuggestionData> = listOf(
    // SGW
    SuggestionData(suggestion = CampusBuilding("B", "B Annex", "2160 Bishop St.", Campus.SGW) as Suggestion),
    SuggestionData(suggestion = CampusBuilding("CI", "CI Annex",                                                       "2149 Mackay St.",              Campus.SGW)),
    SuggestionData(suggestion = CampusBuilding("CL", "CL Annex",                                                       "1665 Ste-Catherine St. W.",    Campus.SGW)),
    SuggestionData(suggestion = CampusBuilding("D",  "D Annex",                                                        "2140 Bishop St.",              Campus.SGW)),
    SuggestionData(suggestion = CampusBuilding("EN", "EN Annex",                                                       "2070 Mackay St.",              Campus.SGW)),
    SuggestionData(suggestion = CampusBuilding("ER", "ER Building",                                                    "2155 Guy St.",                 Campus.SGW)),
    SuggestionData(suggestion = CampusBuilding("EV", "Engineering, Computer Science and Visual Arts Integrated Complex","1515 Ste-Catherine St. W.",    Campus.SGW)),
    SuggestionData(suggestion = CampusBuilding("FA", "FA Annex",                                                       "2060 Mackay St.",              Campus.SGW)),
    SuggestionData(suggestion = CampusBuilding("FB", "Faubourg Building",                                              "1250 Guy St.",                 Campus.SGW)),
    SuggestionData(suggestion = CampusBuilding("FG", "Faubourg Ste-Catherine Building",                                "1610 Ste-Catherine St. W.",    Campus.SGW)),
    SuggestionData(suggestion = CampusBuilding("GA", "Grey Nuns Annex",                                                "1211-1215 St-Mathieu St.",     Campus.SGW)),
    SuggestionData(suggestion = CampusBuilding("GM", "Guy-De Maisonneuve Building",                                    "1550 De Maisonneuve Blvd. W.", Campus.SGW)),
    SuggestionData(suggestion = CampusBuilding("GN", "Grey Nuns Building",                                             "1190 Guy St.",                 Campus.SGW)),
    SuggestionData(suggestion = CampusBuilding("GS", "GS Building",                                                    "1538 Sherbrooke St. W.",       Campus.SGW)),
    SuggestionData(suggestion = CampusBuilding("H",  "Henry F. Hall Building",                                         "1455 De Maisonneuve Blvd. W.", Campus.SGW)),
    SuggestionData(suggestion = CampusBuilding("K",  "K Annex",                                                        "2150 Bishop St.",              Campus.SGW)),
    SuggestionData(suggestion = CampusBuilding("LB", "J.W. McConnell Building",                                        "1400 De Maisonneuve Blvd. W.", Campus.SGW)),
    SuggestionData(suggestion = CampusBuilding("LD", "LD Building",                                                    "1424 Bishop St.",              Campus.SGW)),
    SuggestionData(suggestion = CampusBuilding("LS", "Learning Square",                                                "1535 De Maisonneuve Blvd. W.", Campus.SGW)),
    SuggestionData(suggestion = CampusBuilding("M",  "M Annex",                                                        "2135 Mackay St.",              Campus.SGW)),
    SuggestionData(suggestion = CampusBuilding("MB", "John Molson Building",                                           "1450 Guy St.",                 Campus.SGW)),
    SuggestionData(suggestion = CampusBuilding("MI", "MI Annex",                                                       "2130 Bishop St.",              Campus.SGW)),
    SuggestionData(suggestion = CampusBuilding("MU", "MU Annex",                                                       "2170 Bishop St.",              Campus.SGW)),
    SuggestionData(suggestion = CampusBuilding("P",  "P Annex",                                                        "2020 Mackay St.",              Campus.SGW)),
    SuggestionData(suggestion = CampusBuilding("PR", "PR Annex",                                                       "2100 Mackay St.",              Campus.SGW)),
    SuggestionData(suggestion = CampusBuilding("Q",  "Q Annex",                                                        "2010 Mackay St.",              Campus.SGW)),
    SuggestionData(suggestion = CampusBuilding("R",  "R Annex",                                                        "2050 Mackay St.",              Campus.SGW)),
    SuggestionData(suggestion = CampusBuilding("RR", "RR Annex",                                                       "2040 Mackay St.",              Campus.SGW)),
    SuggestionData(suggestion = CampusBuilding("S",  "S Annex",                                                        "2145 Mackay St.",              Campus.SGW)),
    SuggestionData(suggestion = CampusBuilding("SB", "Samuel Bronfman Building",                                       "1590 Docteur-Penfield",        Campus.SGW)),
    SuggestionData(suggestion = CampusBuilding("T",  "T Annex",                                                        "2030 Mackay St.",              Campus.SGW)),
    SuggestionData(suggestion = CampusBuilding("TD", "Toronto-Dominion Building",                                      "1410 Guy St.",                 Campus.SGW)),
    SuggestionData(suggestion = CampusBuilding("V",  "V Annex",                                                        "2110 Mackay St.",              Campus.SGW)),
    SuggestionData(suggestion = CampusBuilding("VA", "Visual Arts Building",                                           "1395 Rene-Levesque Blvd. W.", Campus.SGW)),
    SuggestionData(suggestion = CampusBuilding("X",  "X Annex",                                                        "2080 Mackay St.",              Campus.SGW)),
    SuggestionData(suggestion = CampusBuilding("Z",  "Z Annex",                                                        "2090 Mackay St.",              Campus.SGW)),
    // Loyola
    SuggestionData(suggestion = CampusBuilding("AD", "Administration Building",                          "7141 Sherbrooke St. W.", Campus.LOYOLA)),
    SuggestionData(suggestion = CampusBuilding("BB", "BB Annex",                                         "3502 Belmore Ave.",      Campus.LOYOLA)),
    SuggestionData(suggestion = CampusBuilding("BH", "BH Annex",                                         "3500 Belmore Ave.",      Campus.LOYOLA)),
    SuggestionData(suggestion = CampusBuilding("CC", "Central Building",                                 "7141 Sherbrooke St. W.", Campus.LOYOLA)),
    SuggestionData(suggestion = CampusBuilding("CJ", "Communication Studies and Journalism Building",    "7141 Sherbrooke St. W.", Campus.LOYOLA)),
    SuggestionData(suggestion = CampusBuilding("DO", "Stinger Dome",                                     "7141 Sherbrooke St. W.", Campus.LOYOLA)),
    SuggestionData(suggestion = CampusBuilding("FC", "F.C. Smith Building",                              "7141 Sherbrooke St. W.", Campus.LOYOLA)),
    SuggestionData(suggestion = CampusBuilding("GE", "Centre for Structural and Functional Genomics",    "7141 Sherbrooke St. W.", Campus.LOYOLA)),
    SuggestionData(suggestion = CampusBuilding("HA", "Hingston Hall, wing HA",                           "7141 Sherbrooke St. W.", Campus.LOYOLA)),
    SuggestionData(suggestion = CampusBuilding("HB", "Hingston Hall, wing HB",                           "7141 Sherbrooke St. W.", Campus.LOYOLA)),
    SuggestionData(suggestion = CampusBuilding("HC", "Hingston Hall, wing HC",                           "7141 Sherbrooke St. W.", Campus.LOYOLA)),
    SuggestionData(suggestion = CampusBuilding("HU", "Applied Science Hub",                              "7141 Sherbrooke St. W.", Campus.LOYOLA)),
    SuggestionData(suggestion = CampusBuilding("JR", "Jesuit Residence",                                 "7141 Sherbrooke St. W.", Campus.LOYOLA)),
    SuggestionData(suggestion = CampusBuilding("PC", "PERFORM Centre",                                   "7200 Sherbrooke St. W.", Campus.LOYOLA)),
    SuggestionData(suggestion = CampusBuilding("PS", "Physical Services Building",                       "7141 Sherbrooke St. W.", Campus.LOYOLA)),
    SuggestionData(suggestion = CampusBuilding("PT", "Oscar Peterson Concert Hall",                      "7141 Sherbrooke St. W.", Campus.LOYOLA)),
    SuggestionData(suggestion = CampusBuilding("PY", "Psychology Building",                              "7141 Sherbrooke St. W.", Campus.LOYOLA)),
    SuggestionData(suggestion = CampusBuilding("QA", "Quadrangle",                                       "7141 Sherbrooke St. W.", Campus.LOYOLA)),
    SuggestionData(suggestion = CampusBuilding("RA", "Recreation and Athletics Complex",                 "7200 Sherbrooke St. W.", Campus.LOYOLA)),
    SuggestionData(suggestion = CampusBuilding("RF", "Loyola Jesuit Hall and Conference Centre",         "7141 Sherbrooke St. W.", Campus.LOYOLA)),
    SuggestionData(suggestion = CampusBuilding("SC", "Student Centre",                                   "7141 Sherbrooke St. W.", Campus.LOYOLA)),
    SuggestionData(suggestion = CampusBuilding("SH", "Future Buildings Laboratory",                      "7141 Sherbrooke St. W.", Campus.LOYOLA)),
    SuggestionData(suggestion = CampusBuilding("SI", "St. Ignatius of Loyola Church",                    "4455 West Broadway St.", Campus.LOYOLA)),
    SuggestionData(suggestion = CampusBuilding("SP", "Richard J. Renaud Science Complex",                "7141 Sherbrooke St. W.", Campus.LOYOLA)),
    SuggestionData(suggestion = CampusBuilding("TA", "Terrebonne Building",                              "7079 de Terrebonne St.", Campus.LOYOLA)),
    SuggestionData(suggestion = CampusBuilding("VE", "Vanier Extension",                                 "7141 Sherbrooke St. W.", Campus.LOYOLA)),
    SuggestionData(suggestion = CampusBuilding("VL", "Vanier Library Building",                          "7141 Sherbrooke St. W.", Campus.LOYOLA)),

    // Shuttle Stops

    SuggestionData(suggestion = ShuttleStop("sgw_shuttle",    "SGW Shuttle Stop",                                       "Henry F. Hall Building, 1455 De Maisonneuve Blvd. W.",
        Campus.SGW,SGW_STOP_LAT_LNG,) as Suggestion),
    SuggestionData(suggestion = ShuttleStop("loyola_shuttle_arrival",    "Loyola Shuttle Stop (Arrival)",               "Loyola Chapel, 7137 Sherbrooke St. W. — Drop-off from downtown" ,
        Campus.LOYOLA,LOYOLA_ARRIVAL_LAT_LNG,) as Suggestion),
    SuggestionData(suggestion = ShuttleStop("loyola_shuttle_departure",    "Loyola Shuttle Stop (Departure)",           "Loyola Chapel, 7137 Sherbrooke St. W. — Pick-up to downtown",
        Campus.LOYOLA,LOYOLA_DEPARTURE_LAT_LNG,) as Suggestion),

    )


fun fullSuggestions(
    query: String,
    activeCampus: Campus,
    crossCampus: Boolean,
    max: Int = 8,
): List<SuggestionData> {
    val q = query.trim()
    if (q.isEmpty()) return emptyList()
    val pool = if (crossCampus) ALL_SUGGESTIONS
    else ALL_SUGGESTIONS.filter { it.suggestion.campus == activeCampus }
    return pool
        .mapNotNull { suggestions -> val s = suggestions.suggestion.score(q); if (s > 0) suggestions to s else null }
        .sortedByDescending { (_, s) -> s }
        .map { (suggestions, _) -> suggestions }
        .take(max)
}