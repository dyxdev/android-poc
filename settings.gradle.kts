pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://muxinc.jfrog.io/artifactory/default-maven-release-local") }
    }
}
rootProject.name = "poc-android-mux"
include(":app")
include(":core:consent")
include(":core:common")
include(":core:identity")
include(":core:model")
include(":core:data")
include(":core:designsystem")
include(":feature:home")
include(":core:testing")
include(":feature:player-lab")
