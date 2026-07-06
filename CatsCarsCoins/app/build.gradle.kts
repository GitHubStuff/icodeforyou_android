// app/build.gradle.kts
// CatsCarsCoins — spec 24.0.3. Complete file. Every change from the wizard
// output is marked: ── ADDED (new), ── UPDATED (value changed, old value shown).
// Unmarked lines are wizard-generated and unchanged.

import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    // ── ADDED: kotlinx.serialization compiler plugin (DTOs + Nav3 @Serializable NavKeys)
    alias(libs.plugins.kotlin.serialization)
    // ── ADDED: KSP — runs the Room 3 compiler
    alias(libs.plugins.ksp)
    // ── ADDED: Room 3 Gradle plugin — provides the room3 { } block below
    alias(libs.plugins.room3)
    // ── ADDED: Kover coverage (spec 0.6 gate: ./gradlew koverHtmlReport)
    alias(libs.plugins.kover)
}

// ── ADDED: read the Cat API key from local.properties (gitignored) so it
//           reaches code via BuildConfig, never via source control (spec §6).
//           Falls back to "" so machines without a key still build.
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}
val catApiKey: String = localProperties.getProperty("CAT_API_KEY", "")

android {
    namespace = "com.icodeforyou.catscarscoins"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.icodeforyou.catscarscoins"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // ── ADDED: expose the key as BuildConfig.CAT_API_KEY (spec §6)
        buildConfigField("String", "CAT_API_KEY", "\"$catApiKey\"")
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        // ── UPDATED: was JavaVersion.VERSION_11 — spec §2 pins JVM target 17
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        // ── ADDED: required for the buildConfigField above (spec 0.3)
        buildConfig = true
    }
    sourceSets {
        // ── ADDED 24.3.6: MigrationTestHelper reads the exported schema
        //    JSONs from instrumented-test assets (per its own KDoc); this
        //    ships schemas/ inside the androidTest APK.
        getByName("androidTest").assets.srcDir("$projectDir/schemas")
    }
}

// ── ADDED: Kotlin toolchain pin — guarantees JVM-17 bytecode regardless of
//           which JDK launches Gradle (spec §2; pairs with compileOptions)
kotlin {
    jvmToolchain(17)
}

// ── ADDED: Room 3 schema export (replaces Room 2.x's
//           ksp { arg("room.schemaLocation", ...) } — spec §2 row superseded).
//           The schemas/ dir is tracked in git intentionally (spec 0.7).
room3 {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    // ── 24.2.10 correction: pin coroutines core in the app APK to the same
    //    version as coroutines-test in the test APK (NoSuchMethodError fix)
    implementation(libs.kotlinx.coroutines.android)
    // ── ADDED 24.1.12: collectAsStateWithLifecycle (declared 24.1.11)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // ── ADDED: navigation — Nav3 core + adaptive (spec §3, §5)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.material3.adaptive.navigation3)
    implementation(libs.androidx.material3.adaptive.navigation.suite)
    // ── ADDED: Icons.Default.* vectors for the navigation chrome
    implementation(libs.androidx.material.icons.core)
    // ── ADDED 24.3.7: Bitcoin currency symbol for the Coins rail icon
    //    (extended icon set; declared 24.3.7 in the catalog)
    implementation(libs.androidx.material.icons.extended)

    // ── ADDED: DI — Koin runtime DSL, versions via BOM (spec §5)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    // ── ADDED: persistence — Room 3 + Preferences DataStore (spec §5)
    implementation(libs.room3.runtime)
    ksp(libs.room3.compiler)
    // ── ADDED 24.2.7: BundledSQLiteDriver — Room 3 requires an explicit
    //    SQLiteDriver on the builder (declared 24.2.6)
    implementation(libs.sqlite.bundled)
    implementation(libs.androidx.datastore.preferences)

    // ── ADDED: networking — Retrofit + OkHttp via BOM (spec §5)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    // ── ADDED: (de)serialization (spec §5)
    implementation(libs.kotlinx.serialization.json)

    // ── ADDED: images — Coil 3: Compose, SVG decoding, OkHttp fetcher (spec §5)
    implementation(libs.coil.compose)
    implementation(libs.coil.svg)
    implementation(libs.coil.network.okhttp)

    // ── ADDED: platform SplashScreen compat for installSplashScreen() (spec §4)
    implementation(libs.androidx.core.splashscreen)

    testImplementation(libs.junit)
    // ── ADDED: unit-test toolchain (spec §24.6: virtual time, Flow, mocks)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    // ── ADDED: Room DAO/FTS instrumented tests (spec §24.6)
    androidTestImplementation(libs.room3.testing)
    // ── ADDED 24.2.10: flow assertions + runTest on the instrumented classpath
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.turbine)

    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}