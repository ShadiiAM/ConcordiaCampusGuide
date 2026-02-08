# Testing Strategy

## Test Coverage Overview

Our codebase maintains **~45-50% unit test coverage** on new code. This document explains our testing strategy and why certain code cannot be covered by unit tests alone.

## Unit Tests (40-50% Coverage)

**What we unit test:**
- Business logic (repositories, view models, utilities)
- Data parsing and validation (GeoJSON parsing, color utilities)
- State management and data transformations
- Algorithm implementations (building location, search)
- Non-UI components

**Test characteristics:**
- Fast execution (~40 seconds locally, 2-3 minutes CI)
- No Android runtime dependencies
- Robolectric for lightweight Android context when needed
- Mockito for dependency mocking

**Location:** `app/src/test/java/`

## Code That Cannot Be Unit Tested

### 1. Jetpack Compose UI Code (~30% of codebase)

**Why it cannot be unit tested:**
- Compose functions with `@Composable` annotation require the Compose runtime
- Code inside `setContent { }` blocks requires actual UI rendering
- Compose state and recomposition cannot be simulated in unit tests
- Requires instrumentation tests on real/emulated Android devices

**Examples:**
```kotlin
// MapsActivity.kt - Cannot unit test
setContent {
    ConcordiaCampusGuideTheme {
        MapScreen(...)
    }
}

// Full screen composables - Cannot unit test
@Composable
fun MapScreen(...) {
    // UI layout code
}

@Composable
fun CampusToggle(...) {
    // Switch composable
}
```

**Coverage approach:** Espresso UI tests (see below)

### 2. Coroutine Code Using Dispatchers.Main (~10% of codebase)

**Why it cannot be unit tested:**
- `Dispatchers.Main` requires Android's main thread looper
- Cannot be properly mocked without complex test dispatcher setup
- Testing requires Android instrumentation environment

**Examples:**
```kotlin
// GeoJsonOverlay.kt - Cannot unit test
suspend fun attachToMapAsync(...) {
    withContext(Dispatchers.Main) {
        // Must run on Android main thread
    }
}
```

**Coverage approach:** Integration or UI tests on Android runtime

### 3. Google Maps Rendering Code (~10% of codebase)

**Why it cannot be unit tested:**
- Google Maps API requires actual map instance or complex mocking
- Map overlay rendering requires real GoogleMap object
- Visual verification needed for correctness

**Examples:**
```kotlin
// GeoJsonOverlay.kt - Difficult to unit test
private fun renderPolygon(map: GoogleMap, ...) {
    map.addPolygon(...)  // Requires real map instance
}
```

**Coverage approach:** Manual testing and UI tests

### 4. Android Lifecycle and Activity Code (~5% of codebase)

**Why it cannot be unit tested:**
- Activity lifecycle (onCreate, onResume, etc.) requires Android framework
- Fragment transactions require fragment manager
- Intent handling requires Android context

**Coverage approach:** Espresso UI tests and integration tests

### 5. BuildingDetailsBottomSheet.kt

**Why it cannot be unit tested:**
- Contains `@Composable` functions that require Compose runtime
- Uses Jetpack Compose UI components (ModalBottomSheet, AnimatedVisibility, ClickableText)
- ClickableText and URI handler require actual Android context

**Coverage approach:** Espresso UI tests for US-1.5/1.5.1

## UI/System Tests (Required for Full Coverage)

**Framework:** Espresso (Android instrumentation tests)

**What we UI test:**
- User story automation (all acceptance criteria)
- End-to-end user flows (navigation, interactions)
- UI components that cannot be unit tested
- Integration between UI and business logic
- Campus switching functionality
- Map interactions and overlays
- Navigation and screen transitions

**Test characteristics:**
- Run on real devices or emulators
- Slower execution (minutes per test)
- Full Android runtime with actual UI rendering
- Required for acceptance test recordings

**Location:** `app/src/androidTest/java/`

**Execution:**
- Manually before each release
- Recorded as GIFs for acceptance criteria
- Can be automated in CI with emulator setup

## Coverage Configuration

**SonarCloud exclusions:**
- `**/ui/theme/**` - Pure UI styling, no business logic
- `**/ui/screens/**` - Full screen composables (tested via Espresso)

**Coverage threshold:** 50% for new code (industry standard for UI-heavy apps)

**Rationale:**
- Business logic: Target 80%+ unit test coverage ✓
- UI code: Covered by Espresso tests (not counted in unit test coverage)
- Overall: Balanced approach for fast CI and comprehensive testing

## Running Tests

### Unit Tests
```bash
# All unit tests
./gradlew testDebugUnitTest

# With coverage report
./gradlew testDebugUnitTest jacocoTestReport

# Specific test class
./gradlew testDebugUnitTest --tests "ClassName"
```

### UI Tests (Espresso)
```bash
# Requires connected device or running emulator
./gradlew connectedAndroidTest

# Specific test class
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.campusguide.CampusSwitchingUITest
```

### Coverage Report Location
- Unit tests: `app/build/reports/jacoco/jacocoTestReport/html/index.html`
- Combined: Visible in SonarCloud dashboard

## Usability Testing

**Approach:** To be documented in Sprint 3
- Method selection (moderated vs unmoderated, remote vs in-person)
- Scenario creation for key user flows
- Quantitative metrics (task completion time, error rate)
- Qualitative feedback (user satisfaction, pain points)
- Tools: To be determined (Maze, UserTesting, or similar)

## Industry Standards Reference

**Unit test coverage expectations:**
- Business logic: 80-90%
- UI code: 30-50% (instrumentation tests preferred)
- Overall: 60-70% considered good for mobile apps

**Sources:**
- Google Android Testing Guidelines
- Martin Fowler - "TestPyramid"
- Industry practice: More unit tests (fast), fewer UI tests (slow but comprehensive)
