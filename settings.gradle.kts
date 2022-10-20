rootProject.name = "nxcloud-foundation-core"
include(":core-base")
include(":core-bean")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        google()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
