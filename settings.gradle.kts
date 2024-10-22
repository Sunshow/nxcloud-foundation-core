rootProject.name = "nxcloud-foundation-core"
include(":core-base")
include(":core-bean")
include(":core-idgenerator")
include(":core-validation")
include(":core-json")
include(":core-json-jackson")
include(":core-data-support")
include(":core-spring-data-jpa")
include(":core-spring-support")
include(":core-spring-boot-autoconfigure")
include(":core-spring-boot-starter")
include(":core-spring-boot-starter-data-jpa")
include(":core-event")
include(":core-bom")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        google()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")