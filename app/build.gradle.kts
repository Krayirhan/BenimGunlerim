import java.util.Properties
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
    jacoco
}

if (file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
    apply(plugin = "com.google.firebase.crashlytics")
}

val keystoreProperties = Properties().also { props ->
    val localKeystoreFile = rootProject.file("keystore.properties")
    if (localKeystoreFile.exists()) {
        localKeystoreFile.inputStream().use(props::load)
    }
}

fun releaseSigningValue(propertyName: String, environmentName: String): String? =
    (keystoreProperties[propertyName] as String?)
        ?: providers.environmentVariable(environmentName).orNull

val releaseStoreFile = releaseSigningValue("storeFile", "KEYSTORE_PATH")
val releaseStorePassword = releaseSigningValue("storePassword", "KEYSTORE_PASSWORD")
val releaseKeyAlias = releaseSigningValue("keyAlias", "KEY_ALIAS")
val releaseKeyPassword = releaseSigningValue("keyPassword", "KEY_PASSWORD")
val hasReleaseSigningConfig = listOf(
    releaseStoreFile,
    releaseStorePassword,
    releaseKeyAlias,
    releaseKeyPassword,
).all { !it.isNullOrBlank() }

android {
    namespace = "com.benimgunlerim"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.benimgunlerim"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (hasReleaseSigningConfig) {
            create("release") {
                storeFile = file(requireNotNull(releaseStoreFile))
                storePassword = requireNotNull(releaseStorePassword)
                keyAlias = requireNotNull(releaseKeyAlias)
                keyPassword = requireNotNull(releaseKeyPassword)
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isProfileable = true
            if (hasReleaseSigningConfig) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

tasks.register("verifyReleaseSigning") {
    group = "verification"
    description = "Fails when release signing credentials are not configured."
    doLast {
        check(hasReleaseSigningConfig) {
            "Release signing is not configured. Provide keystore.properties or KEYSTORE_PATH, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD."
        }
        check(file(requireNotNull(releaseStoreFile)).exists()) {
            "Release keystore file does not exist: $releaseStoreFile"
        }
    }
}

jacoco {
    toolVersion = "0.8.12"
}

val coverageExclusions = listOf(
    "**/R.class",
    "**/R$*.class",
    "**/*\$*.*",
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "**/*Test*.*",
    "**/*_Impl*.*",
    "**/*Dao_Impl*.*",
    "**/*Database_Impl*.*",
    "**/*Hilt*.*",
    "**/Hilt_*.*",
    "**/*_Factory*.*",
    "**/*_MembersInjector*.*",
    "**/*Module*.*",
    "**/*ComposableSingletons*.*",
    "**/*Screen*.*",
    "**/ui/components/**",
    "**/ui/theme/**",
    "**/ui/AppNavigation*.*",
    "**/data/local/entity/**",
    "**/MainActivity*.*",
    "**/BenimGunlerimApplication*.*",
)

tasks.register<JacocoReport>("jacocoDebugUnitTestReport") {
    group = "verification"
    description = "Generates JaCoCo coverage report for debug unit tests."
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    classDirectories.setFrom(
        fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug")) {
            exclude(coverageExclusions)
        },
    )
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(
        fileTree(layout.buildDirectory) {
            include(
                "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec",
                "jacoco/testDebugUnitTest.exec",
            )
        },
    )
}

tasks.register<JacocoCoverageVerification>("jacocoDebugUnitTestCoverageVerification") {
    group = "verification"
    description = "Fails when debug unit test coverage is below the configured threshold."
    dependsOn("jacocoDebugUnitTestReport")

    classDirectories.setFrom(
        fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug")) {
            exclude(coverageExclusions)
        },
    )
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(
        fileTree(layout.buildDirectory) {
            include(
                "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec",
                "jacoco/testDebugUnitTest.exec",
            )
        },
    )

    violationRules {
        // Baseline (2026-04-14): ~16% line / ~4% branch (without wide exclusions).
        // Sprint 5: use-case + service + ViewModel unit tests → bumped to 0.20/0.07.
        // Sprint 6: use-case tests (Add/Update/Delete/Carry), service tests, score/eval tests → bumped to 0.30/0.12.
        // Sprint 8: edge-case tests (transaction, permission, timezone), RewardDisplayService → bumped to 0.40/0.20.
        // DAO tests (TaskDaoTest, CompletionLogDaoTest, DailyStateDaoTest) run as
        // androidTest with Room.inMemoryDatabaseBuilder and are NOT counted by the
        // JVM JaCoCo report; the thresholds below apply only to JVM unit tests.
        // Sprint D tightening: raise JVM gate to LINE >= 0.42, BRANCH >= 0.22.
        rule {
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.42".toBigDecimal()
            }
        }
        rule {
            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = "0.22".toBigDecimal()
            }
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
    arg("room.expandProjection", "true")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.work.runtime.ktx)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.konfetti.compose)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.json)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.room.testing)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

// ── Detekt static analysis ────────────────────────────────────────────────────

detekt {
    config.setFrom(rootProject.files("config/detekt/detekt.yml"))
    baseline = file("$rootDir/config/detekt/baseline.xml")
    buildUponDefaultConfig = true
    parallel = true
    allRules = false
    source.setFrom("src/main/java", "src/test/java")
    // Fail the build when violations are found.
    ignoreFailures = false
}

tasks.named<io.gitlab.arturbosch.detekt.Detekt>("detekt") {
    reports {
        html.required.set(true)
        xml.required.set(false)
    }
}

// ── KtLint style enforcement ──────────────────────────────────────────────────

ktlint {
    version.set("1.3.1")
    android.set(true)
    outputToConsole.set(true)
    ignoreFailures.set(false)
    filter {
        exclude("**/generated/**")
        include("**/kotlin/**")
    }
}
