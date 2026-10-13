plugins {
    id("android-application")
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.kapt)
}

android {
    namespace = "com.venuenexa.pocmux"
}

dependencies {
    implementation(project(":core:consent"))
    implementation(project(":core:common"))
    implementation(project(":feature:player-lab"))
    implementation(libs.mux.player)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui)
    implementation(platform(libs.compose.bom))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose")
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.hilt.android)
    implementation(libs.timber)
    kapt(libs.hilt.compiler)
}
