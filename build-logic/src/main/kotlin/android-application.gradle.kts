import com.android.build.api.dsl.ApplicationExtension

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

extensions.configure<ApplicationExtension> {
    compileSdk = 36
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
