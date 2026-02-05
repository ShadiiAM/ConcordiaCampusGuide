// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("org.sonarqube") version "6.0.1.5171"
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin) apply false
}

sonar {
    properties {
        property("sonar.projectKey", "ShadiiAM_ConcordiaCampusGuide")
        property("sonar.organization", "passable-hardwood-salvage-professor-control-pedicure")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.projectName", "ConcordiaCampusGuide")
        property("sonar.sourceEncoding", "UTF-8")

        // JaCoCo coverage report - explicit path
        property("sonar.java.coveragePlugin", "jacoco")
        property("sonar.coverage.jacoco.xmlReportPaths", "app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")

        // JUnit test results
        property("sonar.junit.reportPaths", "app/build/test-results/testDebugUnitTest")

        // Android lint report
        property("sonar.android.lint.report", "app/build/reports/lint-results-debug.xml")

        // Exclusions
        property("sonar.exclusions", "**/R.class,**/R\$*.class,**/BuildConfig.*,**/Manifest*.*,**/*Test*.*,**/databinding/**")
        property("sonar.coverage.exclusions", "**/R.class,**/R\$*.class,**/BuildConfig.*,**/*Test*.*")
    }
}

// Configure sonar for app module specifically
subprojects {
    sonar {
        properties {
            property("sonar.coverage.jacoco.xmlReportPaths", "${projectDir}/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
            property("sonar.java.binaries", "${projectDir}/build/intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes")
        }
    }
}

// Ensure jacocoTestReport runs before sonar task
tasks.named("sonar") {
    dependsOn(":app:jacocoTestReport")
}
