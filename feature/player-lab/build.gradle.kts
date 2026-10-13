plugins {
    id("android-library")
    alias(libs.plugins.kotlin.kapt)
}

android {
    namespace = "com.venuenexa.pocmux.feature.playerlab"
    buildFeatures {
        buildConfig = true
    }
    // URL del token-server. Por defecto el loopback del emulador local;
    // para Appetize/Codespaces pásala así:
    //   ./gradlew assembleDebug -PtokenUrl=https://<tu-forward>.app.github.dev
    defaultConfig {
        buildConfigField(
            "String",
            "TOKEN_BASE_URL",
            providers.gradleProperty("tokenUrl").map { "\"$it\"" }.getOrElse("\"http://10.0.2.2:8787\""),
        )
    }
}

dependencies {
    implementation(project(":core:consent"))
    implementation(project(":core:common"))
    implementation(libs.mux.player)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui)
    implementation(libs.media3.hls)
    implementation(platform(libs.compose.bom))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose")
    implementation(libs.androidx.foundation.layout)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.timber)
    kapt(libs.hilt.compiler)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(project(":core:testing"))
}
