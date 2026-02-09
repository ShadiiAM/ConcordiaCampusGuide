### Sprint 2

#### Quality Metrics - Sprint 2

| Metric | Value | Target | Status |
|--------|-------|--------|--------|
| Lines of Code | 2.9k | N/A | - |
| Code Duplication | 0% | <3% | PASS |
| Comment Density | 6.4% | >10% | FAIL |
| Cyclomatic Complexity | 3.5 avg | <10 avg | PASS |
| Technical Debt | 0.25% | <5% | PASS |
| Security Vulnerabilities | 0 | 0 | PASS |
| Code Smells | 53 (3h 38min effort) | <100 | PASS |
| Convention Violations | 43 | <50 | PASS |

#### Key Findings

1. **Comment Density is below target (6.4% vs >10%).** Most files lack KDoc on public APIs. UI composables (`AccessibleText.kt`, `ColorFilterOverlay.kt`, `NavigationBar.kt`, `MapScreen.kt`, `CalendarScreen.kt`) have almost zero documentation. The bulk of existing comments are concentrated in `MapsActivity.kt`, `GeoJsonOverlay.kt`, and `BuildingDetailsBottomSheet.kt`.

2. **8 functions exceed CC > 10 (very high complexity).** The worst offenders are `HoursLine()` (CC=18), `attachToMapAsync()` (CC=16), `BuildingDetailsBottomSheet()` (CC=12), `checkIfOpen()` (CC=12), and `executeSwitchCampus()` (CC=11). These are driven by deeply nested `when` branches, time-parsing logic, and multi-geometry JSON handling. However, the overall average of 3.5 is well within target.

3. **Convention violations are close to the threshold (43/50).** The most prevalent categories are magic numbers (12), `var` instead of `val` (6), `lateinit` misuse (5), and inconsistent formatting (5). Three wildcard imports and duplicate imports in `MapsActivity.kt` also contribute.

4. **Zero code duplication and zero security vulnerabilities** indicate solid structural hygiene and safe coding practices for the current sprint.

5. **Technical debt is very low (0.25%)**, with all 53 code smells estimated at only 3h 38min of remediation — well within manageable bounds.

#### Actions Taken or Planned

- **[Planned] Increase comment density to meet >10% target.** Add KDoc comments to all public classes, functions, and composables — prioritizing undocumented files: `AccessibleText.kt`, `ColorFilterOverlay.kt`, `NavigationBar.kt`, `MapScreen.kt`, `CalendarScreen.kt`, and `CampusToggle.kt`.
- **[Planned] Refactor high-complexity functions.** Extract time-parsing logic from `HoursLine()` and `checkIfOpen()` into utility functions. Break `attachToMapAsync()` into per-geometry-type handlers. Reduce duplicated `when(campus)` branches in `executeSwitchCampus()` with a campus-dispatch helper.
- **[Planned] Address convention violations (details below).**
- **[Ongoing] Maintain zero duplication, zero security vulnerabilities, and low technical debt** through continued code review practices.

#### Convention Violations — Detailed Breakdown

**1. Replace wildcard imports with explicit imports**

Wildcard imports pull in unnecessary classes, can cause naming conflicts, and make it harder to see what's actually used. Found in `BuildingDetailsBottomSheet.kt` and `GeoJsonOverlay.kt`.

```kotlin
// BAD — imports everything from the package with *
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*

// GOOD — import only what you actually use
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
```

**2. Remove duplicate imports in MapsActivity.kt**

Lines 33-35 and lines 70-72 import the same symbols twice. The duplicates should be deleted.

```kotlin
// Lines 33-35 import these:
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

// Lines 70-72 import the SAME things again — redundant, delete these
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
```

**3. Change `var` to `val` in CalendarScreen.kt**

If a variable is never reassigned, it should be `val`. This prevents accidental mutation and communicates intent. Six variables inside `formattedDate()` are declared as `var` but never reassigned.

```kotlin
// BAD — var means the variable can be reassigned, but it never is
var day = date.get(Calendar.DAY_OF_MONTH)
var month = start.getDisplayName(...)
var weekStart = start.get(Calendar.DAY_OF_MONTH)

// GOOD — val means it's read-only, which is the actual intent
val day = date.get(Calendar.DAY_OF_MONTH)
val month = start.getDisplayName(...)
val weekStart = start.get(Calendar.DAY_OF_MONTH)
```

**4. Extract magic numbers into named constants**

"Magic numbers" are unexplained literal values scattered in the code. Giving them names makes the code readable and maintainable — if a value needs to change, you change it in one place. Found across `MapsActivity.kt` (request code `200`, coordinate literals), `AccessibilityState.kt` (bounds `7f`, `-2f`), `AccessibleText.kt` (bounds `15f`, `23f`), and `MarkerIconFactory.kt` (base size `64`).

```kotlin
// BAD — what does 200 mean? What are these coordinates?
ActivityCompat.requestPermissions(this, arrayOf(...), 200)
val initialLocation = LatLng(45.4972, -73.5789)

// GOOD — self-documenting named constants
companion object {
    private const val LOCATION_PERMISSION_REQUEST_CODE = 200
    private val SGW_LOCATION = LatLng(45.4972, -73.5789)
    private val LOYOLA_LOCATION = LatLng(45.4582, -73.6402)
}
```

**5. Resolve TODO comments or convert to tracked issues**

TODOs in production code are unfinished work that's easy to forget. Either implement them now, or create actual GitHub issues to track them and remove the comments. Two found in `MapsActivity.kt`:

```kotlin
// These exist in MapsActivity.kt:
onSearchQueryChange = { /* TODO: Handle search */ },          // line 129
onProfileClick = { /* TODO: Navigate to profile details */ },  // line 671
```
