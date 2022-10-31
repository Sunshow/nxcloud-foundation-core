dependencies {
    api(project(":core-data-support"))
    api(project(":core-spring-support"))
    compileOnly(libs.jakarta.persistence.api)
}