// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("org.sonarqube") version "7.2.2.6593"
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin) apply false
}

sonar {
    properties {
        property("sonar.projectKey", "ShadiiAM_ConcordiaCampusGuide")
        property("sonar.organization", "passable-hardwood-salvage-professor-control-pedicure")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.projectName", "ConcordiaCampusGuide")
        property("sonar.sourceEncoding", "UTF-8")

        // Source paths
        property("sonar.sources", "app/src/main/java")
        property("sonar.tests", "app/src/test/java")
        property("sonar.java.binaries", "app/build/intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes")

        // JaCoCo coverage
        property("sonar.java.coveragePlugin", "jacoco")
        property("sonar.coverage.jacoco.xmlReportPaths", "app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")

        // JUnit test results
        property("sonar.junit.reportPaths", "app/build/test-results/testDebugUnitTest")

        // Android lint report
        property("sonar.androidLint.reportPaths", "app/build/reports/lint-results-debug.xml")

        // Exclusions
        property("sonar.exclusions", "**/R.class,**/R\$*.class,**/BuildConfig.*,**/Manifest*.*,**/*Test*.*,**/databinding/**,**/ShuttleMarkerFactory.kt")
        property("sonar.coverage.exclusions",
            // Generated files and tests
            "**/R.class," +
            "**/R\$*.class," +
            "**/BuildConfig.*," +
            "**/*Test*.*," +

            // UI packages (Compose UI - cannot unit test, requires instrumented tests)
            "**/ui/theme/**," +                   // Theme files (UI styling - no business logic)
            "**/ui/components/**," +              // UI components (includes DirectionsTopBar, CampusToggle, etc.)
            "**/ui/screens/**," +                 // Screen composables (includes MapScreen, CalendarScreen, etc.)
            "**/ui/accessibility/**," +           // Accessibility UI components
            "**/ui/map/**," +                     // Map rendering layer (Canvas/Paint/Android Context)

            // Activities (UI-heavy with Compose setContent - see .claude/TESTING_GUIDE.md)
            "**/MapsActivity.kt," +
            "**/MainActivity.kt," +

            // Map rendering (requires Dispatchers.Main - see .claude/TESTING_GUIDE.md)
            "**/GeoJsonOverlay.kt," +

            // ShuttleMarkerFactory: Canvas/Paint bitmap rendering, requires Android Context
            "**/ShuttleMarkerFactory.kt"
        )

        // Lower coverage threshold for UI-heavy codebase
        // Industry standard: UI code 30-50%, Business logic 80%+
        property("sonar.coverage.newCode.minimumCoverage", "50")
    }
}

// Ensure jacocoTestReport runs before sonar task
tasks.named("sonar") {
    dependsOn(":app:jacocoTestReport")
}
