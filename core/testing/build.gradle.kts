plugins {
    id("android-library")
}

android {
    namespace = "com.venuenexa.pocmux.core.testing"
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
}
