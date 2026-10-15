plugins {
    id("pocmux.android-application")
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.kapt)
}

// Esquema propio exclusivo para la redirección de AppAuth (ADR-AND-0008): login y cierre de sesión.
val redirectScheme = "com.venuenexa.pocmux"

// Propiedad de Gradle (-P, gradle.properties, ORG_GRADLE_PROJECT_*) y, si no está, variable de entorno.
fun configuredValue(
    property: String,
    environmentVariable: String,
): Provider<String> = providers.gradleProperty(property).orElse(providers.environmentVariable(environmentVariable))

android {
    namespace = "com.venuenexa.pocmux"
    defaultConfig {
        manifestPlaceholders["appAuthRedirectScheme"] = redirectScheme
        buildConfigField("String", "REDIRECT_SCHEME", "\"$redirectScheme\"")
        // Keycloak: el realm y el cliente los entrega backend (PV1-T41); sin ellos la pantalla dice
        // «no configurado». Se leen de una propiedad de Gradle (-P) o, si no está, de una variable de
        // entorno (KEYCLOAK_ISSUER, KEYCLOAK_CLIENT_ID, KEYCLOAK_SCOPES). Ver docs/identidad-keycloak.md:
        //   ./gradlew assembleDebug -PkeycloakIssuer=https://<host>/realms/<realm> -PkeycloakClientId=<id>
        //   KEYCLOAK_ISSUER=https://<host>/realms/<realm> KEYCLOAK_CLIENT_ID=<id> ./gradlew assembleDebug
        buildConfigField(
            "String",
            "KEYCLOAK_ISSUER",
            configuredValue("keycloakIssuer", "KEYCLOAK_ISSUER").map { "\"$it\"" }.getOrElse("\"\""),
        )
        buildConfigField(
            "String",
            "KEYCLOAK_CLIENT_ID",
            configuredValue("keycloakClientId", "KEYCLOAK_CLIENT_ID").map { "\"$it\"" }.getOrElse("\"\""),
        )
        buildConfigField(
            "String",
            "KEYCLOAK_SCOPES",
            configuredValue("keycloakScopes", "KEYCLOAK_SCOPES")
                .map { "\"$it\"" }
                .getOrElse("\"openid email profile\""),
        )
    }
}

dependencies {
    implementation(project(":core:consent"))
    implementation(project(":core:common"))
    implementation(project(":core:identity"))
    implementation(project(":core:data"))
    implementation(project(":core:designsystem"))
    implementation(project(":feature:home"))
    implementation(project(":feature:player-lab"))
    implementation(libs.mux.player)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui)
    implementation(platform(libs.compose.bom))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation(libs.androidx.foundation.layout)
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.hilt.android)
    implementation(libs.timber)
    "kapt"(libs.hilt.compiler)
}
