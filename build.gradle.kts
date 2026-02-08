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

        // JaCoCo coverage
        property("sonar.java.coveragePlugin", "jacoco")
        property("sonar.coverage.jacoco.xmlReportPaths", "app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")

        // JUnit test results
        property("sonar.junit.reportPaths", "app/build/test-results/testDebugUnitTest")

        // Android lint report
        property("sonar.androidLint.reportPaths", "app/build/reports/lint-results-debug.xml")

        // Exclusions
        property("sonar.exclusions", "**/R.class,**/R\$*.class,**/BuildConfig.*,**/Manifest*.*,**/*Test*.*,**/databinding/**")
        property("sonar.coverage.exclusions",
            "**/R.class," +
            "**/R\$*.class," +
            "**/BuildConfig.*," +
            "**/*Test*.*," +
            "**/ui/theme/**," +              // Theme files (UI styling - no business logic)
            "**/ui/screens/**," +             // Full screen composables (pure UI)
            "**/MapsActivity.kt," +           // UI-heavy activity with Compose setContent (see TESTING.md)
            "**/MainActivity.kt," +           // UI-heavy activity with Compose setContent (see TESTING.md)
            "**/GeoJsonOverlay.kt," +         // Map rendering with Dispatchers.Main (see TESTING.md)
            "**/CampusToggle.kt"  +            // Pure UI composable (see TESTING.md)
            "**/ui/accessibility/**" +         // Accessibility UI components
            "**/AccessibilityScreen.kt" +        // Accessibility screen UI
            "**/BuildingDetailsBottomSheet.kt" //UI activity with screen composables
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
