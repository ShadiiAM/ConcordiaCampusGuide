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
- **[Planned] Address convention violations.** Replace wildcard imports with explicit imports. Remove duplicate imports in `MapsActivity.kt`. Change `var` to `val` in `CalendarScreen.kt`. Extract magic numbers (request code `200`, coordinate literals, DP bounds) into named constants. Resolve `TODO` comments or convert to tracked issues.
- **[Ongoing] Maintain zero duplication, zero security vulnerabilities, and low technical debt** through continued code review practices.
