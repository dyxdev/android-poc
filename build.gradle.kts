// Top-level build: la configuración común vive en build-logic (ADR-AND-0015).
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
}

detekt {
    config.setFrom(rootProject.file("detekt.yml"))
    buildUponDefaultConfig = true
    allRules = false
}

val ktlintId = libs.plugins.ktlint.get().pluginId
val detektId = libs.plugins.detekt.get().pluginId
subprojects {
    apply(plugin = ktlintId)
    apply(plugin = detektId)
}
