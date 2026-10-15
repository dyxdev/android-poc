plugins {
    id("pocmux.android-application")
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.kapt)
}

// Esquema propio exclusivo para la redirección de AppAuth (ADR-AND-0008): login y cierre de sesión.
val redirectScheme = "com.venuenexa.pocmux"

android {
    namespace = "com.venuenexa.pocmux"
    defaultConfig {
        manifestPlaceholders["appAuthRedirectScheme"] = redirectScheme
        buildConfigField("String", "REDIRECT_SCHEME", "\"$redirectScheme\"")
        // Keycloak: el realm y el cliente los entrega backend (PV1-T41); sin ellos la pantalla dice
        // «no configurado». Ver docs/identidad-keycloak.md:
        //   ./gradlew assembleDebug -PkeycloakIssuer=https://<host>/realms/<realm> -PkeycloakClientId=<id>
        buildConfigField(
            "String",
            "KEYCLOAK_ISSUER",
            providers.gradleProperty("keycloakIssuer").map { "\"$it\"" }.getOrElse("\"\""),
        )
        buildConfigField(
            "String",
            "KEYCLOAK_CLIENT_ID",
            providers.gradleProperty("keycloakClientId").map { "\"$it\"" }.getOrElse("\"\""),
        )
        buildConfigField(
            "String",
            "KEYCLOAK_SCOPES",
            providers.gradleProperty("keycloakScopes").map { "\"$it\"" }.getOrElse("\"openid email profile\""),
        )
    }
}

dependencies {
    implementation(project(":core:consent"))
    implementation(project(":core:common"))
    implementation(project(":core:identity"))
    implementation(project(":feature:player-lab"))
    implementation(libs.mux.player)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui)
    implementation(platform(libs.compose.bom))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.hilt.android)
    implementation(libs.timber)
    "kapt"(libs.hilt.compiler)
}
