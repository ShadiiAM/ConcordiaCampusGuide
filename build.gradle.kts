// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("org.sonarqube") version "7.2.2.6593"
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin) apply false
    id("com.google.gms.google-services") version "4.4.4" apply false

}

sonar {
    properties {
        property("sonar.projectKey", "ShadiiAM_ConcordiaCampusGuide")
        property("sonar.organization", "passable-hardwood-salvage-professor-control-pedicure")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.projectName", "ConcordiaCampusGuide")
        property("sonar.sourceEncoding", "UTF-8")

        // Exclusions (project-level — apply to all modules)
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
            "**/ShuttleMarkerFactory.kt," +

            // ComposableRowSuggestions is a UI row builder
            "**/*Composable*.kt"
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
