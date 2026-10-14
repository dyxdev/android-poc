plugins {
    id("pocmux.android-library")
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.kapt)
}

android {
    namespace = "com.venuenexa.pocmux.feature.playerlab"
    buildFeatures {
        buildConfig = true
    }
    // Por defecto reproduce un playback ID PÚBLICO (sin token ni servidor): el de «mux-1» del
    // catálogo de test-mux (Mux responde 200 sin firmar, comprobado 2026-10-14).
    //   ./gradlew assembleDebug -PpublicPlaybackId=<otro id público>
    // Para el flujo firmado, deja el ID vacío y apunta al token-server (por defecto, el loopback
    // del emulador; en un dispositivo, una URL que alcance):
    //   ./gradlew assembleDebug -PpublicPlaybackId= -PtokenUrl=https://<host>
    defaultConfig {
        buildConfigField(
            "String",
            "PUBLIC_PLAYBACK_ID",
            providers.gradleProperty("publicPlaybackId").map { "\"$it\"" }
                .getOrElse("\"EcHgOK9coz5K4rjSwOkoE7Y7O01201YMIC200RI6lNxnhs\""),
        )
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
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.foundation.layout)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.timber)
    "kapt"(libs.hilt.compiler)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(project(":core:testing"))
}
