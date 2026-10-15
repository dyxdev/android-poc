plugins {
    id("pocmux.android-library")
    alias(libs.plugins.kotlin.kapt)
}

android {
    namespace = "com.venuenexa.pocmux.core.identity"
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.datastore.preferences)
    implementation(libs.hilt.android)
    implementation(libs.appauth)
    implementation(libs.timber)
    implementation(project(":core:common"))
    "kapt"(libs.hilt.compiler)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
