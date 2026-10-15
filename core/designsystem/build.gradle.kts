plugins {
    id("pocmux.android-library")
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.venuenexa.pocmux.core.designsystem"
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation(libs.androidx.foundation.layout)
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
}
