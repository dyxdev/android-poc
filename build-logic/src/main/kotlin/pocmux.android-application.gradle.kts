import com.android.build.api.dsl.ApplicationExtension

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

extensions.configure<ApplicationExtension> {
    defaultConfig {
        minSdk = 34
        targetSdk = 36
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    lint {
        error.add("MissingTranslation")
        abortOnError = true
    }
    buildFeatures {
        buildConfig = true
    }
}

// Mux Player 1.6.0 y Mux Data 1.13.2 exigen compileSdk 37. Google publica esa plataforma como
// "android-37.0" (api-level con minor); AGP 8.13.2 con un entero pediría el hash "android-37".
extensions.configure<com.android.build.gradle.BaseExtension> {
    compileSdkVersion("android-37.0")
}
