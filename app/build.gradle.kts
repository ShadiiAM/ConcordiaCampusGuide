import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
    jacoco
    id("org.sonarqube")
}

sonar {
    properties {
        property("sonar.sources", "src/main/java")
        property("sonar.tests", "src/test/java")
        property("sonar.java.binaries", "build/intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes")
        property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
        // Exclude UI-only files that cannot be meaningfully unit tested:
        // Compose screens, Compose components, Canvas/bitmap factories,
        // accessibility overlays, theme definitions, and the Activity entry point.
        // Exclude UI-only files from coverage: Compose screens/components/theme/accessibility
        // cannot be exercised by JVM unit tests — they require the Android rendering pipeline.
        property(
            "sonar.coverage.exclusions",
            listOf(
                "**/ui/screens/**",
                "**/ui/components/**",
                "**/ui/accessibility/**",
                "**/ui/theme/**",
                "**/ui/map/**",
                "**/MainActivity.kt",
                "**/ui/directions/**",                // ShuttleMarkerFactory: Canvas/Paint/Android Context — untestable via JVM unit tests
                "**/ShuttleMarkerFactory.kt"
            ).joinToString(",")
        )
        // ShuttleMarkerFactory uses Canvas/Paint and requires Android Context — it has no
        // business logic and is already exercised by E2E tests. Fully exclude from analysis
        // so SonarQube does not count its lines against coverage.
        property(
            "sonar.exclusions",
            "**/ShuttleMarkerFactory.kt"
        )
    }
}

android {
    namespace = "com.example.campusguide"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.campusguide"
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Default placeholder so processDebugUnitTestManifest never fails without local.properties.
        // The Secrets plugin overrides this with the real value from local.properties for the app build.
        manifestPlaceholders["MAPS_API_KEY"] = ""
    }

    useLibrary("org.apache.http.legacy")


    buildTypes {
        debug {
            enableUnitTestCoverage = true
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

jacoco {
    toolVersion = "0.8.12"
}

// Configure test tasks to generate coverage
tasks.withType<Test> {
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }

    // Increase heap size to prevent OOM when running all tests
    maxHeapSize = "4g"
    jvmArgs(
        "-XX:MaxMetaspaceSize=1g",
        "-XX:+HeapDumpOnOutOfMemoryError",
        "-XX:+UseParallelGC"
    )

    // Run all tests in single process - forking adds too much overhead
    maxParallelForks = 1
}

tasks.register<JacocoReport>("jacocoTestReport") {
    group = "Reporting"
    description = "Generate Jacoco coverage reports after running tests"
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        xml.outputLocation.set(file("${layout.buildDirectory.get().asFile}/reports/jacoco/jacocoTestReport/jacocoTestReport.xml"))
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/jacocoTestReport"))
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
        "**/databinding/**/*.*",
        "**/BR.class",
        "**/*\$Lambda$*.*",
        "**/*\$inlined$*.*",
        // Compose screens — tightly coupled to Google Maps SurfaceView, untestable via JVM
        "**/ui/screens/**",
        // Compose UI components — require Android rendering pipeline
        "**/ui/components/**",
        // Map rendering layer — Canvas/Paint bitmap factories and GeoJSON overlays
        "**/ui/map/**",
        // Accessibility overlays — Compose + Android draw passes
        "**/ui/accessibility/**",
        // Theme definitions — pure styling constants, no logic to test
        "**/ui/theme/**",
        // Activity entry point — framework lifecycle, not unit testable
        "**/MainActivity*",
        // Requires live network/API calls — not unit testable on JVM
        "**/ui/directions/**"
    )

    val buildDir = layout.buildDirectory.get().asFile

    // Try multiple possible locations for Kotlin compiled classes
    val kotlinTree = fileTree(buildDir) {
        include(
            "intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes/**/*.class",
            "tmp/kotlin-classes/debug/**/*.class",
            "intermediates/classes/debug/**/*.class"
        )
        exclude(fileFilter)
    }

    val javaTree = fileTree(buildDir) {
        include(
            "intermediates/javac/debug/compileDebugJavaWithJavac/classes/**/*.class",
            "intermediates/javac/debug/classes/**/*.class"
        )
        exclude(fileFilter)
    }

    val mainSrc = "${project.projectDir}/src/main/java"

    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(kotlinTree, javaTree))

    // Collect all test task execution data
    executionData.setFrom(fileTree(buildDir) {
        include("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
        include("jacoco/testDebugUnitTest.exec")
    })

    doFirst {
        println("JaCoCo Report Configuration:")
        println("  Source dirs: ${sourceDirectories.files}")
        println("  Class dirs: ${classDirectories.files.flatMap { it.walkTopDown().filter { f -> f.isFile }.take(5).toList() }}")
        println("  Execution data: ${executionData.files}")
    }

    doLast {
        val xmlReport = file("${buildDir}/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
        if (xmlReport.exists()) {
            println("✓ JaCoCo XML report generated successfully at: ${xmlReport.absolutePath}")
            println("  Report size: ${xmlReport.length()} bytes")
        } else {
            println("✗ WARNING: JaCoCo XML report was NOT generated at expected location: ${xmlReport.absolutePath}")
        }
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.material3)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // Maps - PICK ONE version only
    implementation(libs.play.services.maps)               // remove play.services.maps.v1820 duplicate
    implementation(libs.play.services.location)           // remove play.services.location.v1750 duplicate
    implementation("com.google.maps.android:maps-compose:4.4.1")  // remove 4.3.3 duplicate
    implementation(libs.maps.utils.ktx)

    // Network
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation(libs.androidx.uiautomator)

    // Unit Tests
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.12.2")       // remove 4.11.1 duplicate
    testImplementation("org.mockito:mockito-core:5.11.0")          // remove 5.8.0 duplicate
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("org.json:json:20240303")
    testImplementation("androidx.test:core:1.6.1")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation(libs.androidx.compose.ui.test.junit4)

    // Instrumented (E2E) Tests - THIS is where your error comes from
    androidTestImplementation("androidx.test:runner:1.6.1")        // ← was MISSING
    androidTestImplementation("androidx.test:rules:1.6.1")         // ← move from implementation
    androidTestImplementation("androidx.navigation:navigation-testing:2.7.7") // ← move from implementation
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation("androidx.test:core:1.6.1")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}