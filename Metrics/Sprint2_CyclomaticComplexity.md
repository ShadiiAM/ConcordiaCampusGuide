### Sprint 2 - Cyclomatic Complexity Analysis

**Methodology:** CC = 1 + (decision points). Decision points counted: `if`, `else if`, `for`/`while` loops, `when` branches (N-1), `&&`/`||`, `catch`, `?: return/continue/throw`, `?.let{}` blocks.

---

#### Very High Complexity (CC >= 11) — Refactoring recommended

| Function | File | CC |
|----------|------|----|
| `HoursLine()` | BuildingDetailsBottomSheet.kt:330 | **18** |
| `attachToMapAsync()` | GeoJsonOverlay.kt:58 | **16** |
| `BuildingDetailsBottomSheet()` | BuildingDetailsBottomSheet.kt:80 | **12** |
| `checkIfOpen()` | BuildingDetailsBottomSheet.kt:471 | **12** |
| `executeSwitchCampus()` | MapsActivity.kt:343 | **11** |
| `addFeature()` | GeoJsonOverlay.kt:205 | **11** |
| `CampusToggle()` | CampusToggle.kt:35 | **11** |
| `CompactHoursDisplay()` | BuildingDetailsBottomSheet.kt:266 | **11** |

#### High Complexity (CC 8-10)

| Function | File | CC |
|----------|------|----|
| `styleFromProperties()` | GeoJsonOverlay.kt:295 | **9** |
| `getCurrentTime()` | BuildingDetailsBottomSheet.kt:440 | **8** |

#### Moderate Complexity (CC 5-7)

| Function | File | CC |
|----------|------|----|
| `styleFor()` | GeoJsonStyleMapper.kt:10 | **7** |
| `CalendarScreen()` | CalendarScreen.kt:47 | **7** |
| `cleanText()` | BuildingDetailsBottomSheet.kt:45 | **7** |
| `applyStyleToPolygon()` | GeoJsonOverlay.kt:337 | **7** |
| `ConcordiaCampusGuideApp()` | MainActivity.kt:65 | **6** |
| `onMapReady()` | MapsActivity.kt:233 | **6** |
| `showBuildingDetails()` | MapsActivity.kt:426 | **6** |
| `parseCoordinateRing()` | GeoJsonParser.kt:61 | **6** |
| `attachToMap()` | GeoJsonOverlay.kt:41 | **5** |
| `onRequestPermissionsResult()` | MapsActivity.kt:508 | **5** |
| `ProfileOverlayContent()` | MapsActivity.kt:635 | **5** |
| `AccessibilityScreen()` | AccessibilityScreen.kt:49 | **5** |
| `applyStyleToMarker()` | GeoJsonOverlay.kt:346 | **5** |
| `stableIdFor()` | GeoJsonOverlay.kt:367 | **5** |
| `parseCoordinates()` | GeoJsonParser.kt:17 | **5** |

#### Low Complexity (CC 1-4)

| Function | File | CC |
|----------|------|----|
| `onCreate()` | MainActivity.kt:41 | 1 |
| `Greeting()` | MainActivity.kt:129 | 1 |
| `PlaceholderScreen()` | MainActivity.kt:146 | 1 |
| `onCreate()` | MapsActivity.kt:104 | 1 |
| `loadGeoJson()` | MapsActivity.kt:289 | 1 |
| `defaultOverlayStyle()` | MapsActivity.kt:317 | 1 |
| `showProfileOverlay()` | MapsActivity.kt:410 | 1 |
| `saveCampus()` | MapsActivity.kt:477 | 1 |
| `requestLocationUpdates()` | MapsActivity.kt:522 | 1 |
| `highlightedOverlayStyle()` | MapsActivity.kt:607 | 1 |
| `onDestroy()` | MapsActivity.kt:620 | 1 |
| `parse()` | GeoJsonColorUtils.kt:10 | 1 |
| `withOpacity()` | GeoJsonColorUtils.kt:14 | 1 |
| `clear()` | GeoJsonOverlay.kt:118 | 1 |
| `removeFeature()` | GeoJsonOverlay.kt:128 | 1 |
| `setAllStyles()` | GeoJsonOverlay.kt:151 | 1 |
| `setVisibleAll()` | GeoJsonOverlay.kt:164 | 1 |
| `setMarkersVisible()` | GeoJsonOverlay.kt:169 | 1 |
| `setMarkerVisible()` | GeoJsonOverlay.kt:172 | 1 |
| `setBuildingsVisible()` | GeoJsonOverlay.kt:179 | 1 |
| `setBuildingVisible()` | GeoJsonOverlay.kt:183 | 1 |
| `getBuildings()` | GeoJsonOverlay.kt:189 | 1 |
| `getBuildingProps()` | GeoJsonOverlay.kt:190 | 1 |
| `getPolygonId()` | GeoJsonOverlay.kt:196 | 1 |
| `buildPolygonFromCoordinates()` | GeoJsonOverlay.kt:251 | 1 |
| `applyStyleToPolygons()` | GeoJsonOverlay.kt:333 | 1 |
| `MapScreen()` | MapScreen.kt:22 | 1 |
| `ProfileScreen()` | ProfileScreen.kt:43 | 1 |
| `ProfileItem()` | ProfileScreen.kt:96 | 1 |
| `AccessibilityItem()` | ProfileScreen.kt:154 | 1 |
| `SettingRow()` | AccessibilityScreen.kt:237 | 1 |
| `AccessibleAppRoot()` | ColorFilterOverlay.kt:11 | 1 |
| `CompactInfoRow()` | BuildingDetailsBottomSheet.kt:241 | 1 |
| `InfoSection()` | BuildingDetailsBottomSheet.kt:524 | 1 |
| `setBold()` | AccessibilityState.kt:40 | 1 |
| `pointInBuilding()` | BuildingLocator.kt:27 | 1 |
| `resetForTests()` | MarkerIconFactory.kt:31 | 1 |
| `setFillOpacityForAll()` | GeoJsonOverlay.kt:157 | 1 |
| `saveCampus()` | CampusPreferences.kt:18 | 1 |
| `rememberAccessibilityState()` | AccessibilityState.kt:61 | 1 |
| `initializeOverlays()` | MapsActivity.kt:299 | 2 |
| `switchCampus()` | MapsActivity.kt:331 | 2 |
| `getSavedCampus()` | MapsActivity.kt:467 | 2 |
| `requestLocation()` | MapsActivity.kt:534 | 2 |
| `setLocation()` | MapsActivity.kt:545 | 2 |
| `isLocationEnabled()` | MapsActivity.kt:555 | 2 |
| `isPermissionsGranted()` | MapsActivity.kt:560 | 2 |
| `generateCallback()` | MapsActivity.kt:565 | 2 |
| `safeParseColor()` | GeoJsonStyleMapper.kt:42 | 2 |
| `stringOrNull()` | GeoJsonColorUtils.kt:26 | 2 |
| `reapplyPropertiesStyles()` | GeoJsonOverlay.kt:134 | 2 |
| `loadFromRawOrThrow()` | GeoJsonOverlay.kt:381 | 2 |
| `SearchBarWithProfile()` | SearchBar.kt:39 | 2 |
| `parsePolygonCoordinates()` | GeoJsonParser.kt:31 | 2 |
| `parseMultiPolygonCoordinates()` | GeoJsonParser.kt:39 | 2 |
| `extractTitle()` | GeoJsonParser.kt:93 | 2 |
| `getSavedCampus()` | CampusPreferences.kt:25 | 2 |
| `increaseTextSize()` | AccessibilityState.kt:32 | 2 |
| `decreaseTextSize()` | AccessibilityState.kt:36 | 2 |
| `floatOrNull()` | GeoJsonColorUtils.kt:20 | 3 |
| `startLocationTracking()` | MapsActivity.kt:499 | 3 |
| `highlightBuildingUserIsIn()` | MapsActivity.kt:578 | 3 |
| `setStyleForFeature()` | GeoJsonOverlay.kt:146 | 3 |
| `buildPolygonOptions()` | GeoJsonOverlay.kt:239 | 3 |
| `buildMarkerFromPoint()` | GeoJsonOverlay.kt:261 | 3 |
| `parseLngLatRing()` | GeoJsonOverlay.kt:284 | 3 |
| `fromJson()` | BuildingInfo.kt:26 | 3 |
| `onGPS()` | MapsActivity.kt:486 | 4 |
| `ConcordiaCampusGuideTheme()` | Theme.kt:30 | 4 |
| `AccessibleText()` | AccessibleText.kt:12 | 4 |
| `overlayColorForMode()` | ColorFilterOverlay.kt:27 | 4 |
| `cycleColorBlindMode()` | AccessibilityState.kt:44 | 4 |
| `findBuilding()` | BuildingLocator.kt:15 | 4 |
| `polygonContainsPoint()` | BuildingLocator.kt:32 | 4 |
| `create()` | MarkerIconFactory.kt:39 | 4 |
| `NavigationBar()` | NavigationBar.kt:26 | 4 |
| `parsePointCoordinates()` | GeoJsonParser.kt:50 | 4 |
| `hasValidGeometry()` | GeoJsonParser.kt:82 | 4 |
| `isLineForCurrentDay()` | BuildingDetailsBottomSheet.kt:459 | 4 |

---

#### Summary Statistics

| Metric | Value |
|--------|-------|
| Total source files | 25 |
| Total functions analyzed | ~85 |
| Average CC | ~3.5 |
| Median CC | 2 |
| Max CC | 18 (`HoursLine()`) |
| Functions with CC > 10 | 8 |
| Functions with CC 5-10 | 15 |
| Functions with CC 1-4 | ~62 |

#### Complexity Distribution by File

| File | Functions | Max CC | Avg CC |
|------|-----------|--------|--------|
| BuildingDetailsBottomSheet.kt | 10 | 18 | 7.4 |
| GeoJsonOverlay.kt | 22 | 16 | 3.5 |
| MapsActivity.kt | 24 | 11 | 2.8 |
| CampusToggle.kt | 2 | 11 | 6.0 |
| CalendarScreen.kt | 2 | 7 | 4.0 |
| GeoJsonStyleMapper.kt | 2 | 7 | 4.5 |
| GeoJsonParser.kt | 7 | 6 | 3.6 |
| MainActivity.kt | 5 | 6 | 2.0 |
| AccessibilityScreen.kt | 3 | 5 | 2.3 |
| AccessibleText.kt | 1 | 4 | 4.0 |
| ColorFilterOverlay.kt | 2 | 4 | 2.5 |
| BuildingLocator.kt | 3 | 4 | 3.0 |
| MarkerIconFactory.kt | 2 | 4 | 2.5 |
| NavigationBar.kt | 2 | 4 | 2.5 |
| GeoJsonColorUtils.kt | 4 | 3 | 1.8 |
| Theme.kt | 1 | 4 | 4.0 |
| AccessibilityState.kt | 5 | 4 | 2.2 |
| CampusPreferences.kt | 2 | 2 | 1.5 |
| BuildingInfo.kt | 1 | 3 | 3.0 |
| SearchBar.kt | 2 | 2 | 1.5 |
| ProfileScreen.kt | 4 | 1 | 1.0 |
| MapScreen.kt | 2 | 1 | 1.0 |
| GeoJsonStyle.kt | 0 | — | — |
| Color.kt | 0 | — | — |
| Type.kt | 0 | — | — |

#### Refactoring Recommendations

1. **`HoursLine()` (CC=18)** — Extract URL-parsing logic into a separate `buildAnnotatedUrl()` function. Extract day-detection and open/closed styling into reusable helpers shared with `CompactHoursDisplay()`.

2. **`attachToMapAsync()` (CC=16)** — Extract per-geometry-type parsing (`Polygon`, `MultiPolygon`, `Point`) into three separate private functions. The Phase 1 / Phase 2 structure already exists but the Phase 1 loop body is too dense.

3. **`checkIfOpen()` (CC=12)** — Extract AM/PM-to-24h conversion into a standalone `parseTo24Hour()` utility. Separate 24/7 detection, closed detection, and time-range parsing into distinct functions.

4. **`BuildingDetailsBottomSheet()` (CC=12)** — Extract the expanded details section into a `BuildingExpandedDetails()` composable. Group nullable field rendering into a list-driven approach.

5. **`executeSwitchCampus()` (CC=11)** — The 7+ `when(campus)` blocks repeat the SGW/LOYOLA dispatch pattern. Create a helper like `overlayFor(campus)` and `rawResFor(campus)` to eliminate repetition.

6. **`addFeature()` (CC=11)** — Similar to `attachToMapAsync()`, extract geometry-type handlers into separate functions.

7. **`CampusToggle()` (CC=11)** — The 10 inline `if(selectedCampus == ...)` conditionals for color theming can be replaced by precomputing color variables at the top of the function.

8. **`CompactHoursDisplay()` (CC=11)** — Shares logic with `HoursLine()`. Extract shared day-detection and status-coloring into common utilities.
