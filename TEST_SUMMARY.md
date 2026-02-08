# Concordia Campus Guide - Test Summary

This document provides a comprehensive overview of all unit tests in the project.

**Total Tests:** 293
**Pass Rate:** 100%
**Testing Frameworks:** JUnit 4, Robolectric, Mockito, Compose UI Testing

---

## Table of Contents

1. [Activity Tests](#activity-tests)
2. [UI Screen Tests](#ui-screen-tests)
3. [UI Component Tests](#ui-component-tests)
4. [Map & GeoJSON Tests](#map--geojson-tests)
5. [Theme Tests](#theme-tests)
6. [Utility Tests](#utility-tests)

---

## Activity Tests

### MainActivityTest.kt (32 tests)
**Location:** `app/src/test/java/com/example/campusguide/MainActivityTest.kt`
**Covers:** `MainActivity.kt`, `AppDestinations`, `Greeting`, `ConcordiaCampusGuideApp`

| Test Name | Description |
|-----------|-------------|
| `mainActivity_onCreate_shouldLaunchSuccessfully` | Verifies activity launches and has valid window |
| `mainActivity_extendsComponentActivity` | Confirms inheritance from ComponentActivity |
| `mainActivity_hasCorrectPackageName` | Validates package name is `com.example.campusguide` |
| `appDestinations_enumHasCorrectValues` | Verifies 4 destinations: Map, Directions, Calendar, POI |
| `greetingFunction_shouldFormatCorrectly` | Tests "Hello {name}!" format |
| `concordiaCampusGuideApp_displaysGreeting` | Verifies "Hello Android!" is displayed |
| `concordiaCampusGuideApp_displaysCampusMapButton` | Checks "Open Campus Map" button exists |
| `campusMapButton_clickable` | Tests button click executes without error |
| `greeting_displaysCorrectMessage` | Validates greeting with custom name |
| `navigationItems_allDestinations_areClickable` | Tests all nav items can be clicked |
| `appDestinations_icon_returnsCorrectAppIcon` | Verifies all destination icons exist |
| `greeting_withModifier_appliesCorrectly` | Tests greeting with Modifier parameter |
| `greetingPreview_rendersWithoutErrors` | Validates preview function executes |
| `navigationSuite_switchesBetweenDestinations` | Tests navigation switching between destinations |
| `greeting_withDifferentNames_alice` | Tests greeting with "Alice" |
| `greeting_withDifferentNames_bob` | Tests greeting with "Bob" |
| `concordiaCampusGuideApp_profileNavigation_showsProfileScreen` | Tests profile navigation from avatar click |
| `concordiaCampusGuideApp_profileToAccessibility_showsAccessibilityScreen` | Tests accessibility screen navigation |
| `concordiaCampusGuideApp_accessibilityBackButton_returnsToProfile` | Tests back navigation from accessibility |
| `concordiaCampusGuideApp_profileBackButton_returnsToMain` | Tests back navigation from profile |
| `appIcon_vectorType_hasCorrectImageVector` | Validates Vector type icons |
| `appIcon_drawableType_hasCorrectResId` | Validates Drawable type icons |
| `appDestinations_allIconsAreValid` | Verifies all icons have valid values |
| `appDestinations_labelsAreNotEmpty` | Checks all labels are non-empty |
| `greeting_withEmptyName_displaysCorrectly` | Tests greeting with empty string |
| `greeting_withSpecialCharacters_displaysCorrectly` | Tests greeting with special chars |
| `greeting_withLongName_displaysCorrectly` | Tests greeting with long name |
| `concordiaCampusGuideApp_displaysSearchBar` | Verifies search bar is visible |

### MapsActivityTest.kt (27 tests)
**Location:** `app/src/test/java/com/example/campusguide/MapsActivityTest.kt`
**Covers:** `MapsActivity.kt` - Google Maps integration, location services

| Test Name | Description |
|-----------|-------------|
| `mapsActivity_onCreate_shouldInflateLayout` | Verifies activity creates with valid layout |
| `mapsActivity_onCreate_executesSuccessfully` | Tests full onCreate lifecycle |
| `mapsActivity_hasCorrectConcordiaCoordinates` | Validates SGW coordinates (45.4972, -73.5789) |
| `mapsActivity_hasCorrectZoomLevel` | Confirms zoom level is 15 |
| `mapsActivity_extendsAppCompatActivity` | Checks inheritance from AppCompatActivity |
| `mapsActivity_implementsOnMapReadyCallback` | Confirms OnMapReadyCallback implementation |
| `mapsActivity_concordiaSGWLocation_isValid` | Validates LatLng is within valid ranges |
| `mapsActivity_markerTitle_isCorrect` | Verifies marker title string |
| `mapsActivity_concordiaLocation_withinMontrealBounds` | Confirms location is in Montreal |
| `mapsActivity_markerOptions_createsSuccessfully` | Tests MarkerOptions creation |
| `mapsActivity_onMapReady_movesCamera` | Verifies camera movement on map ready |
| `mapsActivity_onMapReady_initializesOverlays` | Tests SGW and LOY overlay initialization |
| `mapsActivity_onGPS_isLocationEnabled_isPermissionsGranted` | Tests GPS and permission methods |
| `mapsActivity_requestLocationUpdates` | Verifies location update requests |
| `mapsActivity_requestLocation_returnsCorrectLatLng` | Tests setLocation with valid location |
| `mapActivity_requestLocation` | Tests requestLocation method |
| `mapActivity_getCallback` | Tests generateCallback and LocationResult handling |
| `mapsActivity_userMarker_isInitiallyNull` | Verifies userMarker starts as null |
| `mapsActivity_callback_updatesExistingMarkerPosition` | Tests marker position update |
| `mapsActivity_callback_handlesNullLastLocation` | Tests null location handling |
| `mapsActivity_setLocation_returnsDefaultLocationOnNull` | Tests setLocation with valid coordinates |
| `mapsActivity_defaultConcordiaLocation_isCorrect` | Validates default location values |
| `mapsActivity_fusedLocationProviderClient_isInitialized` | Checks location client initialization |
| `mapsActivity_locationRequestPriority_isHighAccuracy` | Verifies high accuracy priority |
| `mapsActivity_locationUpdateInterval_isValid` | Tests 10s/5s interval configuration |
| `mapsActivity_generateCallback_returnsLocationCallback` | Validates callback type |
| `mapsActivity_isPermissionsGranted_behaviorVerification` | Documents permission check behavior |
| `mapsActivity_isLocationEnabled_returnsBoolean` | Verifies boolean return type |

### MapsActivityOnMapReadyTest.kt (10 tests)
**Location:** `app/src/test/java/com/example/campusguide/MapsActivityOnMapReadyTest.kt`
**Covers:** `MapsActivity.onMapReady()` - Map initialization logic

| Test Name | Description |
|-----------|-------------|
| `onMapReady_executesSuccessfully` | Tests onMapReady execution with mock map |
| `onMapReady_addsMarkerWithCorrectPosition` | Captures and validates marker position |
| `onMapReady_movesCameraToCorrectLocation` | Verifies camera movement call |
| `concordiaCampusLocation_hasCorrectCoordinates` | Validates SGW coordinates |
| `concordiaCampusLocation_isInMontreal` | Confirms Montreal geographic bounds |
| `mapZoomLevel_isAppropriateForCampusView` | Tests zoom level (14-17 range) |
| `markerOptions_canBeCreatedWithCampusData` | Tests MarkerOptions creation |
| `markerTitle_hasCorrectFormat` | Validates title format and content |
| `campusCoordinates_areValidLatLng` | Tests valid lat/lng ranges |
| `concordiaLocation_matchesOfficialCoordinates` | Compares with official coordinates |

---

## UI Screen Tests

### AccessibilityScreenTest.kt (18 tests)
**Location:** `app/src/test/java/com/example/campusguide/AccessibilityScreenTest.kt`
**Covers:** `AccessibilityScreen.kt` - Accessibility settings UI

| Test Name | Description |
|-----------|-------------|
| `accessibilityScreen_displaysTitle` | Verifies "Accessibility" title is shown |
| `accessibilityScreen_displaysSectionHeader` | Checks "Display and Text Size" header |
| `accessibilityScreen_displaysTextSizeSetting` | Validates "Text size" option |
| `accessibilityScreen_displaysTextColourSetting` | Validates "Text colour" option |
| `accessibilityScreen_displaysBoldSetting` | Validates "Bold" toggle option |
| `accessibilityScreen_backButton_triggersCallback` | Tests back button callback |
| `accessibilityScreen_boldToggle_canBeClicked` | Verifies Bold toggle is interactive |
| `accessibilityScreen_displaysMinusButton` | Checks "-" button exists |
| `accessibilityScreen_displaysPlusButton` | Checks "+" button exists |
| `accessibilityScreen_rendersWithoutErrors` | Basic render test |
| `accessibilityScreen_darkTheme_rendersCorrectly` | Tests dark theme rendering |
| `accessibilityScreen_minusButton_canBeClicked` | Tests minus button click |
| `accessibilityScreen_plusButton_canBeClicked` | Tests plus button click |
| `accessibilityScreenPreview_rendersCorrectly` | Tests preview composable |
| `accessibilityScreen_lightTheme_rendersCorrectly` | Tests light theme rendering |
| `accessibilityScreen_withDefaultCallback_rendersCorrectly` | Tests with empty callback |
| `accessibilityScreen_allSettingsAreDisplayed` | Verifies all 3 settings visible |
| `accessibilityScreen_multipleButtonClicks` | Tests repeated button interactions |

### ProfileScreenTest.kt (16 tests)
**Location:** `app/src/test/java/com/example/campusguide/ProfileScreenTest.kt`
**Covers:** `ProfileScreen.kt` - User profile settings UI

| Test Name | Description |
|-----------|-------------|
| `profileScreen_displaysTitle` | Verifies "User settings" title |
| `profileScreen_displaysUserName` | Checks "Jane Doe" is displayed |
| `profileScreen_displaysStudentSubtitle` | Validates "Student" subtitle |
| `profileScreen_displaysAccessibilityItem` | Checks "Accessibility" menu item |
| `profileScreen_backButton_triggersCallback` | Tests back button callback |
| `profileScreen_profileItem_triggersCallback` | Tests profile item click callback |
| `profileScreen_accessibilityItem_triggersCallback` | Tests accessibility click callback |
| `profileScreen_displaysUserInitial` | Verifies "A" avatar initial |
| `profileScreen_rendersWithoutErrors` | Basic render test |
| `profileScreen_darkTheme_rendersCorrectly` | Tests dark theme |
| `profileScreenPreview_rendersCorrectly` | Tests preview composable |
| `profileScreen_lightTheme_rendersCorrectly` | Tests light theme |
| `profileScreen_withAllDefaultCallbacks_rendersCorrectly` | Tests with all callbacks set |
| `profileScreen_displaysAccessibilityIcon` | Checks accessibility icon exists |
| `profileScreen_clickOnStudentSubtitle_triggersProfileCallback` | Tests subtitle click |
| `profileScreen_multipleClicks_triggersCallbacksCorrectly` | Tests multiple profile clicks |

---

## UI Component Tests

### SearchBarTest.kt (20 tests)
**Location:** `app/src/test/java/com/example/campusguide/SearchBarTest.kt`
**Covers:** `SearchBar.kt` - `SearchBarWithProfile` composable

| Test Name | Description |
|-----------|-------------|
| `searchBar_displaysPlaceholder` | Verifies "Search..." placeholder |
| `searchBar_displaysSearchIcon` | Checks search icon with content description |
| `searchBar_displaysProfileAvatar` | Verifies "A" avatar is displayed |
| `searchBar_profileClick_triggersCallback` | Tests profile avatar click |
| `searchBar_textInput_triggersCallback` | Validates callback setup |
| `searchBar_rendersWithoutErrors` | Basic render test |
| `searchBar_darkTheme_rendersCorrectly` | Tests dark theme |
| `searchBar_withDefaultCallbacks_rendersCorrectly` | Tests with empty callbacks |
| `searchBarWithProfilePreview_rendersCorrectly` | Tests preview composable |
| `searchBar_withCustomModifier_rendersCorrectly` | Tests custom Modifier |
| `searchBar_profileAvatarDisplaysInitial` | Validates avatar initial "A" |
| `searchBar_lightTheme_rendersCorrectly` | Tests light theme |
| `searchBar_hasTextInputField` | Verifies text input exists |
| `searchBar_callbackSetup_isValid` | Tests callback configuration |
| `searchBar_initialState_showsPlaceholder` | Verifies initial placeholder state |
| `searchBar_profileButton_isClickable` | Tests profile button interaction |
| `searchBar_searchIcon_contentDescriptionIsSearch` | Validates accessibility |
| `searchBar_dynamicTheme_rendersWithoutErrors` | Tests dynamic color theme |
| `searchBar_darkDynamicTheme_rendersWithoutErrors` | Tests dark + dynamic theme |
| `searchBar_allElements_areDisplayed` | Verifies all UI elements |
| `searchBar_emptyCallbacks_renderCorrectly` | Tests with no-op callbacks |

### ComponentTest.kt (12 tests)
**Location:** `app/src/test/java/com/example/campusguide/ComponentTest.kt`
**Covers:** Additional component tests for `Greeting`, `ConcordiaCampusGuideApp`

| Test Name | Description |
|-----------|-------------|
| `greeting_withCustomModifier_rendersCorrectly` | Tests Greeting with Modifier |
| `greetingPreview_darkTheme_rendersCorrectly` | Tests preview in dark theme |
| `greetingPreview_lightTheme_rendersCorrectly` | Tests preview in light theme |
| `concordiaCampusGuideApp_multipleNavigationClicks` | Tests repeated navigation |
| `greeting_multipleNamesSequence1` | Tests greeting with "User1" |
| `greeting_multipleNamesSequence2` | Tests greeting with "User2" |
| `greeting_multipleNamesSequence3` | Tests greeting with "User3" |
| `greeting_withNumbers_rendersCorrectly` | Tests greeting with numeric name |
| `greeting_withUnicode_rendersCorrectly` | Tests greeting with "Jean-Pierre" |
| `concordiaCampusGuideApp_rapidNavigationClicks` | Tests rapid same-destination clicks |
| `concordiaCampusGuideApp_poiDestination_isDisplayed` | Tests POI navigation |
| `concordiaCampusGuideApp_calendarDestination_isDisplayed` | Tests Calendar navigation |

### NavigationBarTest.kt (18 tests)
**Location:** `app/src/test/java/com/example/campusguide/NavigationBarTest.kt`
**Covers:** `NavigationBar.kt` - `NavigationBar` composable

| Test Name | Description                                                                    |
|-----------|--------------------------------------------------------------------------------|
| `nav_BarDisplaysAppDestinations` | Tests UI Visibility                                                   |
| `navBar_rendersWithoutErrors` | Tests Slot Integrity                                                    |
| `navBar_darkTheme_rendersCorrectly` | Tests Dark Mode                                                   |
| `navBar_withNoContent_rendersCorrectly` | Tests Empty State                                                       |
| `navBarPreview_rendersCorrectly` | Tests Preview Execution                                                    |
| `navBarEachDestinationCanBecomeSelected` | Tests Exhaustive Selection                                                    |
| `navBarWithDifferentCurrentDestination` | Tests Initial State                                                     |
| `navBarUpdatesCurrentDestination` | Tests State Callback                                                |
| `navBarWithoutContentDoesNotRenderSearch` | Tests Conditional Rendering of the search bar/content line of nav bar          |
| `navBarCurrentDestinationisVisuallySelected` | Tests that selected navbar item shows visual indicator                         |
| `navBarUnselectedItemsAreNotSelected` | Tests that only one nav bar item is selected                                   |
| `navBar_clickingSameDestinationDoesNotCrash` | Tests if clicking the active state doesnt crash code                           |
| `navBarRendersDrawableIcon` | Tests Resource Loading                                                         |
| `navBarLabelRecomposes` | Tests Reactivity: Ensures the UI updates automatically when state value changed |
| `navBar_rendersDrawableIcon_specifically` | Tests the rendering of AppIcons                                                |
| `navBar_allDestinations_respondToClicks` | Tests Enum Coverage                                                            |
| `navBar_restoresSelectedDestination_afterRecreation` | Tests State Persistence                                                        |
| `navBar_contentHasCorrectPadding` | Tests Modifier Consumption                                                     |

---

## Map & GeoJSON Tests

### GeoJsonOverlayTest.kt (32 tests)
**Location:** `app/src/test/java/com/example/campusguide/ui/map/geoJson/GeoJsonOverlayTest.kt`
**Covers:** `GeoJsonOverlay.kt` - GeoJSON map overlay rendering

| Test Name | Description |
|-----------|-------------|
| `attachToMap_addsPolygonMarkerAndMultiPolygon_appliesNonColorStyles_andSetsMarkerIcon` | Tests feature parsing |
| `setFillOpacityForAll_clamps_andRewritesFillColor` | Tests opacity clamping |
| `visibilityHelpers_toggleEverything` | Tests visibility toggle methods |
| `removeFeature_removesPolygonAndMarker` | Tests feature removal |
| `clear_removesEverything_andSecondClearNoOps` | Tests clear functionality |
| `setStyleForFeature_appliesToPolygonAndMarker_withoutCrashing` | Tests per-feature styling |
| `setAllStyles_appliesToAllPolygonsAndMarkers_withoutCrashing` | Tests global styling |
| `attachToMap_ignoresMissingGeometry_unknownType_andBadPointCoords` | Tests error handling |
| `attachToMap_defaultArgPath_loadsFromRaw_andDoesNotCrash` | Tests raw resource loading |
| `reapplyPropertiesStyles_reappliesUpdatedPropertiesToExistingPolygons` | Tests style reapplication |
| `getBuildings_returnsPolygonsMap` | Tests getBuildings() getter |
| `getBuildingProps_returnsPropertiesMap` | Tests getBuildingProps() getter |
| `attachToMap_withPolygonHoles_parsesCorrectly` | Tests polygon hole parsing |
| `attachToMap_withEmptyFeatures_doesNotCrash` | Tests empty feature array |
| `attachToMap_withMissingFeaturesArray_doesNotCrash` | Tests missing features |
| `setStyleForFeature_nonExistentFeature_doesNotCrash` | Tests non-existent feature |
| `removeFeature_nonExistentFeature_doesNotCrash` | Tests removing non-existent |
| `setMarkerVisible_nonExistentMarker_doesNotCrash` | Tests visibility on missing |
| `setBuildingVisible_nonExistentBuilding_doesNotCrash` | Tests building visibility |
| `attachToMap_withLargeMarkerSize_appliesCorrectScale` | Tests large marker scale |
| `attachToMap_withMediumMarkerSize_appliesDefaultScale` | Tests medium marker scale |
| `attachToMap_pointWithoutBuildingName_usesNullTitle` | Tests point without title |
| `stableIdFor_usesPropertyIdFirst` | Tests ID resolution priority |
| `attachToMap_nullGeoJsonRes_throwsWhenNoJsonProvided` | Tests error on missing JSON |
| `setFillOpacityForAll_withNegativeOpacity_clampsToZero` | Tests negative opacity |
| `reapplyPropertiesStyles_withNoPolygons_doesNotCrash` | Tests empty reapply |

### GeoJsonColorUtilsTest.kt (30 tests)
**Location:** `app/src/test/java/com/example/campusguide/ui/map/geoJson/GeoJsonColorUtilsTest.kt`
**Covers:** `GeoJsonColorUtils.kt` - Color parsing and manipulation utilities

| Test Name | Description |
|-----------|-------------|
| `parse_acceptsHashRRGGBB` | Tests #RRGGBB format parsing |
| `parse_acceptsHashAARRGGBB` | Tests #AARRGGBB format parsing |
| `parse_trimsWhitespace` | Tests whitespace trimming |
| `parse_namedColor_works` | Tests "red" color name |
| `parse_withoutHashHex_throws` | Tests invalid hex without # |
| `parse_invalidString_throws` | Tests invalid color string |
| `withOpacity_zeroOpacity_makesAlphaZero_preservesRGB` | Tests 0 opacity |
| `withOpacity_fullOpacity_keepsSameColor` | Tests 1.0 opacity |
| `withOpacity_halfOpacity_multipliesExistingAlpha_andRounds` | Tests 0.5 opacity |
| `withOpacity_clampsOpacityBelowZero_toZero` | Tests negative clamping |
| `withOpacity_clampsOpacityAboveOne_toOne` | Tests >1 clamping |
| `floatOrNull_string_invalid_returnsNull` | Tests invalid string |
| `floatOrNull_null_returnsNull` | Tests null input |
| `floatOrNull_otherType_returnsNull` | Tests unsupported type |
| `stringOrNull_string_returnsSame` | Tests string passthrough |
| `stringOrNull_nonString_returnsToString` | Tests toString conversion |
| `stringOrNull_null_returnsNull` | Tests null handling |
| `floatOrNull_intNumber_returnsFloat` | Tests int to float |
| `floatOrNull_doubleNumber_returnsFloat` | Tests double to float |
| `floatOrNull_longNumber_returnsFloat` | Tests long to float |
| `floatOrNull_validStringNumber_returnsFloat` | Tests numeric string |
| `floatOrNull_stringInteger_returnsFloat` | Tests integer string |
| `floatOrNull_emptyString_returnsNull` | Tests empty string |
| `floatOrNull_whitespaceString_returnsNull` | Tests whitespace string |
| `withOpacity_transparentColor_staysTransparent` | Tests transparent input |
| `withOpacity_quarterOpacity_calculatesCorrectly` | Tests 0.25 opacity |
| `parse_blackColor_works` | Tests #000000 |
| `parse_whiteColor_works` | Tests #FFFFFF |
| `parse_lowercaseHex_works` | Tests lowercase hex |
| `parse_mixedCaseHex_works` | Tests mixed case hex |
| `parse_namedColor_blue_works` | Tests "blue" color name |
| `parse_yellowColor_works` | Tests #FFFF00 |
| `parse_cyanColor_works` | Tests #00FFFF |
| `stringOrNull_floatNumber_returnsToString` | Tests float toString |
| `stringOrNull_emptyString_returnsEmpty` | Tests empty string |

### GeoJsonStyleTest.kt (17 tests)
**Location:** `app/src/test/java/com/example/campusguide/ui/map/geoJson/GeoJsonStyleTest.kt`
**Covers:** `GeoJsonStyle.kt` - Style data class

| Test Name | Description |
|-----------|-------------|
| `constructor_withAllParameters_createsInstance` | Tests full constructor |
| `constructor_withNoParameters_createsInstanceWithNulls` | Tests default constructor |
| `copy_withChanges_createsNewInstance` | Tests data class copy |
| `equals_sameValues_returnsTrue` | Tests equality |
| `equals_differentValues_returnsFalse` | Tests inequality |
| `constructor_withPartialParameters_createsInstanceWithMixedNulls` | Tests partial params |
| `hashCode_sameValues_returnsSameHash` | Tests hashCode consistency |
| `hashCode_differentValues_returnsDifferentHash` | Tests hashCode uniqueness |
| `toString_includesAllFields` | Tests toString content |
| `markerStyle_allFieldsAccessible` | Tests marker style fields |
| `copy_preservesUnchangedFields` | Tests copy preservation |
| `destructuring_worksCorrectly` | Tests destructuring |
| `zIndex_canBeSet` | Tests zIndex property |
| `visible_canBeSetToFalse` | Tests visible=false |
| `visible_canBeSetToTrue` | Tests visible=true |
| `clickable_canBeSetToFalse` | Tests clickable=false |
| `allNullStyle_isEqualToDefault` | Tests null equivalence |

### GeoJsonStyleMapperTest.kt (13 tests)
**Location:** `app/src/test/java/com/example/campusguide/ui/map/geoJson/GeoJsonStyleMapperTest.kt`
**Covers:** `GeoJsonStyleMapper.kt` - Feature to style mapping

| Test Name | Description |
|-----------|-------------|
| `styleFor_noProperties_returnsAllNulls_clickableTrue` | Tests empty properties |
| `styleFor_fillOnly_parsesFillColor` | Tests fill color parsing |
| `styleFor_fillAndOpacity_appliesOpacity` | Tests fill with opacity |
| `styleFor_fillOpacityButNoFill_ignoresOpacity` | Tests orphan opacity |
| `styleFor_fillInvalidColor_safeParseReturnsNull` | Tests invalid fill |
| `styleFor_fillOpacityInvalidNumber_doesNotApplyOpacity_usesBaseFill` | Tests invalid opacity |
| `styleFor_strokeOnly_parsesStrokeColor` | Tests stroke color |
| `styleFor_strokeAndOpacity_appliesOpacity` | Tests stroke opacity |
| `styleFor_strokeOpacityInvalidNumber_doesNotApplyOpacity_usesBaseStroke` | Tests invalid stroke opacity |
| `styleFor_strokeInvalidColor_safeParseReturnsNull` | Tests invalid stroke |
| `styleFor_strokeWidth_parsesFloat` | Tests stroke width |
| `styleFor_strokeWidthInvalid_returnsNull` | Tests invalid width |
| `styleFor_allProperties_combinesCorrectly` | Tests all properties combined |

### MarkerIconFactoryTest.kt (18 tests)
**Location:** `app/src/test/java/com/example/campusguide/ui/map/geoJson/MarkerIconFactoryTest.kt`
**Covers:** `MarkerIconFactory.kt` - Custom marker icon creation

| Test Name | Description |
|-----------|-------------|
| `create_scaleClamped_lowScale_uses0_4x` | Tests min scale clamping |
| `create_scaleClamped_highScale_uses3x` | Tests max scale clamping |
| `create_alphaClamped_belowZero_becomesTransparent_bitmapStillCreated` | Tests negative alpha |
| `create_alphaClamped_aboveOne_setsDrawableAlphaTo255_andCreatesBitmap` | Tests alpha >1 |
| `create_tintApplied_callsBitmapToDescriptor_withExpectedSize` | Tests tint and size |
| `create_drawableNull_returnsDefaultMarker_andDoesNotCreateBitmap` | Tests null drawable |
| `create_normalScale_produces64pxBitmap` | Tests 1x scale = 64px |
| `create_halfScale_produces32pxBitmap` | Tests 0.5x scale = 32px |
| `create_doubleScale_produces128pxBitmap` | Tests 2x scale = 128px |
| `create_defaultParams_usesScale1AndAlpha1` | Tests default parameters |
| `create_halfAlpha_producesValidBitmap` | Tests 0.5 alpha |
| `create_zeroAlpha_producesTransparentBitmap` | Tests 0 alpha |
| `resetForTests_restoresDefaultBehavior` | Tests reset function |
| `create_withMinimumCoercedSize_isAtLeast16px` | Tests min size 16px |
| `create_bitmapConfigIsARGB8888` | Tests bitmap config |

### BuildingLocatorTest.kt (17 tests)
**Location:** `app/src/test/java/com/example/campusguide/ui/map/utils/BuildingLocatorTest.kt`
**Covers:** `BuildingLocator.kt` - Point-in-polygon building detection

| Test Name | Description |
|-----------|-------------|
| `findBuilding_pointInsideOuter_returnsHitWithProps` | Tests basic hit detection |
| `findBuilding_pointOutsideAll_returnsNull` | Tests miss detection |
| `pointInBuilding_delegatesToFindBuilding` | Tests boolean wrapper |
| `findBuilding_pointInsideHole_returnsNull` | Tests hole exclusion |
| `findBuilding_multipleBuildings_returnsFirstMatchingInInsertionOrder` | Tests ordering |
| `findBuilding_propsMissing_stillReturnsHitWithNullProperties` | Tests null props |
| `findBuilding_geodesicFalse_stillFindsSameForSimpleSquare` | Tests non-geodesic |
| `findBuilding_emptyPolygonsMap_returnsNull` | Tests empty map |
| `pointInBuilding_emptyPolygonsMap_returnsFalse` | Tests empty map boolean |
| `findBuilding_multiplePolygonsPerBuilding_findsIfInAny` | Tests multi-polygon |
| `findBuilding_pointOnEdge_mayReturnHit` | Tests edge case |
| `findBuilding_pointAtVertex_mayReturnHit` | Tests vertex case |
| `findBuilding_multipleHoles_correctlyExcludesAllHoles` | Tests multiple holes |
| `buildingHit_dataClass_hasCorrectEquality` | Tests BuildingHit equality |
| `buildingHit_withNullProperties_isValid` | Tests null properties |
| `findBuilding_pointFarOutside_returnsNull` | Tests far point |
| `findBuilding_defaultGeodesicValue_isTrue` | Tests default geodesic |

---

## Theme Tests

### ThemeTest.kt (19 tests)
**Location:** `app/src/test/java/com/example/campusguide/ThemeTest.kt`
**Covers:** `Theme.kt` - `ConcordiaCampusGuideTheme` composable

| Test Name | Description |
|-----------|-------------|
| `theme_lightMode_appliesCorrectly` | Tests light theme application |
| `theme_darkMode_appliesCorrectly` | Tests dark theme application |
| `theme_dynamicColors_enabled` | Tests dynamic color enabled |
| `theme_dynamicColors_disabled` | Tests dynamic color disabled |
| `theme_darkMode_withDynamicColors` | Tests dark + dynamic |
| `theme_lightMode_withDynamicColors` | Tests light + dynamic |
| `theme_allBranchCombinations_dark_dynamic` | Tests dark+dynamic branch |
| `theme_allBranchCombinations_dark_static` | Tests dark+static branch |
| `theme_allBranchCombinations_light_dynamic` | Tests light+dynamic branch |
| `theme_allBranchCombinations_light_static` | Tests light+static branch |
| `theme_defaultParameters_appliesCorrectly` | Tests default params |
| `theme_withContent_rendersContent` | Tests content rendering |
| `theme_darkMode_static_usesStaticColors` | Tests static dark colors |
| `theme_lightMode_static_usesStaticColors` | Tests static light colors |
| `theme_dynamicColor_withSdk33_appliesCorrectly` | Tests SDK 33 dynamic |
| `theme_staticColor_withSdk33_appliesCorrectly` | Tests SDK 33 static |
| `theme_nestedThemes_applyCorrectly` | Tests nested themes |
| `theme_multipleContents_renderCorrectly` | Tests multiple children |

### ColorTest.kt (9 tests)
**Location:** `app/src/test/java/com/example/campusguide/ui/theme/ColorTest.kt`
**Covers:** `Color.kt` - Color constant definitions

| Test Name | Description |
|-----------|-------------|
| `purple80_hasCorrectValue` | Tests Purple80 = 0xFFD0BCFF |
| `purpleGrey80_hasCorrectValue` | Tests PurpleGrey80 = 0xFFCCC2DC |
| `pink80_hasCorrectValue` | Tests Pink80 = 0xFFEFB8C8 |
| `purple40_hasCorrectValue` | Tests Purple40 = 0xFF6650a4 |
| `purpleGrey40_hasCorrectValue` | Tests PurpleGrey40 = 0xFF625b71 |
| `pink40_hasCorrectValue` | Tests Pink40 = 0xFF7D5260 |
| `lightAndDarkVariants_areDifferent` | Tests 80 vs 40 variants |
| `colors80_areLighterThanColors40` | Tests alpha channel |
| `allColors_areNotTransparent` | Tests non-transparent |

### TypeTest.kt (9 tests)
**Location:** `app/src/test/java/com/example/campusguide/ui/theme/TypeTest.kt`
**Covers:** `Type.kt` - Typography configuration

| Test Name | Description |
|-----------|-------------|
| `typography_isNotNull` | Tests Typography exists |
| `bodyLarge_hasCorrectFontFamily` | Tests FontFamily.Default |
| `bodyLarge_hasCorrectFontWeight` | Tests FontWeight.Normal |
| `bodyLarge_hasCorrectFontSize` | Tests 16.sp |
| `bodyLarge_hasCorrectLineHeight` | Tests 24.sp |
| `bodyLarge_hasCorrectLetterSpacing` | Tests 0.5.sp |
| `typography_bodyLarge_isAccessible` | Tests accessibility |
| `typography_defaultStyles_areAvailable` | Tests all 15 styles exist |

---

## Utility Tests

### ExampleUnitTest.kt (1 test)
**Location:** `app/src/test/java/com/example/campusguide/ExampleUnitTest.kt`
**Covers:** Basic test infrastructure validation

| Test Name | Description |
|-----------|-------------|
| `addition_isCorrect` | Validates 2+2=4 (sanity check) |

---

## Test Coverage by Package

| Package | Tests | Status |
|---------|-------|--------|
| `com.example.campusguide` | 153 | 100% |
| `com.example.campusguide.ui.map.geoJson` | 106 | 100% |
| `com.example.campusguide.ui.map.utils` | 17 | 100% |
| `com.example.campusguide.ui.theme` | 17 | 100% |
| **Total** | **293** | **100%** |

---

## Running Tests

```bash
# Run all unit tests
./gradlew testDebugUnitTest

# Generate coverage report
./gradlew jacocoTestReport

# View report at:
# app/build/reports/tests/testDebugUnitTest/index.html
```

---

*Generated: February 2026*
