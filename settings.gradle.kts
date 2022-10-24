rootProject.name = "nxcloud-foundation-core"
include(":core-base")
include(":core-bean")
include(":core-idgenerator")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        google()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
