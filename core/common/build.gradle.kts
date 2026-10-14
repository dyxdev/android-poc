plugins {
    id("pocmux.android-library")
    alias(libs.plugins.kotlin.kapt)
}

android {
    namespace = "com.venuenexa.pocmux.core.common"
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.hilt.android)
    "kapt"(libs.hilt.compiler)
}
