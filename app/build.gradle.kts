import de.undercouch.gradle.tasks.download.Download
import java.io.File


plugins {
    alias(libs.plugins.android.application)
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp") version "2.0.21-1.0.25"
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.21"
    id("de.undercouch.download") version "5.7.0"
}

android {
    namespace = "com.housmantech.artviewer"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.housmantech.artviewer"
        minSdk = 24
        targetSdk = 36
        versionCode = 13
        versionName = "1.2.10"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}


tasks.register<Download>("fetchSampleDb") {
    src("https://deviantart-app-tools.avrohomthousman.workers.dev/sampleDB")
    dest(File(projectDir, "src/main/res/raw/sample_db.json"))
    overwrite(true)


    doLast {
        if (!dest.exists() || dest.length() == 0L) {
            println("WARNING: Failed to download sample DB. Using existing bundled file.")
        } else {
            println("Sample DB updated successfully.")
        }
    }
}


tasks.named("preBuild") {
    dependsOn("fetchSampleDb")
}



dependencies {
    implementation(platform(libs.androidx.compose.bom))
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.android)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.constraintlayout.compose)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.browser)
    implementation(libs.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit2.kotlinx.serialization.converter)
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    debugImplementation(libs.androidx.compose.ui.tooling)
}