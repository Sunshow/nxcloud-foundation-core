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
include(":core-universal-task")
include(":core-bom")
include(":core-plugin")
include(":core-assembler-annotation")
include(":core-assembler")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        google()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")