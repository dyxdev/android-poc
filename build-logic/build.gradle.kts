plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("com.android.tools.build:gradle:8.7.3")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.20")
    implementation("org.jetbrains.kotlin:compose-compiler-gradle-plugin:2.1.20")
    implementation("com.google.dagger:hilt-android-gradle-plugin:2.56.2")
    implementation("org.jlleitschuh.gradle:ktlint-gradle:12.1.2")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.8")
}
