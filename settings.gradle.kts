rootProject.name = "nxcloud-foundation-core"
include(":core-base")
include(":core-bean")
include(":core-idgenerator")
include(":core-data-support")
include(":core-data-jpa")
include(":core-spring-support")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        google()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
