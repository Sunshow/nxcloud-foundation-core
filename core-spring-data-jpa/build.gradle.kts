apply(plugin = "org.jetbrains.kotlin.plugin.spring")

dependencies {
    api(project(":core-data-support"))
    api(project(":core-spring-support"))
    api(project(":core-idgenerator"))
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    // compileOnly(libs.jakarta.persistence.api)
    compileOnly(libs.springboot.starter.data.jpa)

    testImplementation(project(":core-spring-boot-starter"))
    testImplementation(libs.springboot.starter.test)
    testImplementation(libs.springboot.starter.data.jpa)
    testImplementation(libs.springboot.starter.aop)
    testImplementation(libs.h2)
}