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
        // Use wildcard pattern to find JaCoCo XML reports
        property("sonar.coverage.jacoco.xmlReportPaths", "**/build/reports/jacoco/**/*.xml")
        property("sonar.junit.reportPaths", "app/build/test-results/testDebugUnitTest")
        property("sonar.android.lint.report", "app/build/reports/lint-results-debug.xml")
    }
}

// Ensure jacocoTestReport runs before sonar task
tasks.named("sonar") {
    dependsOn(":app:jacocoTestReport")
}