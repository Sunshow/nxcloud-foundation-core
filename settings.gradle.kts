rootProject.name = "nxcloud-foundation-core"
include(":core-base")
include(":core-bean")
include(":core-idgenerator")
include(":core-data-support")
include(":core-data-jpa")
include(":core-spring-support")
include(":core-spring-boot-autoconfigure")
include(":core-spring-boot-starter")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        google()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
