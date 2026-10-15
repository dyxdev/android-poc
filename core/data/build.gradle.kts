plugins {
    id("pocmux.android-library")
    alias(libs.plugins.kotlin.kapt)
}

android {
    namespace = "com.venuenexa.pocmux.core.data"
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.datastore.preferences)
    implementation(libs.hilt.android)
    implementation(project(":core:model"))
    "kapt"(libs.hilt.compiler)
}
