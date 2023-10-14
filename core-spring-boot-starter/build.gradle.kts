dependencies {
    api(project(":core-spring-boot-autoconfigure"))
    api(project(":core-spring-support"))
    api(project(":core-bean"))

    testImplementation(libs.modelmapper)
    // add logback
    testImplementation("ch.qos.logback:logback-classic")
}