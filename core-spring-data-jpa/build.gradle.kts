dependencies {
    api(project(":core-data-support"))
    api(project(":core-spring-support"))
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    compileOnly(libs.jakarta.persistence.api)

    testImplementation(project(":core-spring-boot-starter"))
    testImplementation(libs.springboot.starter.test)
    testImplementation(libs.springboot.starter.data.jpa)
    testImplementation(libs.h2)
}