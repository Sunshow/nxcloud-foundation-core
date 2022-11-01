dependencies {
    compileOnly(project(":core-spring-support"))
    compileOnly(project(":core-bean"))
    compileOnly(project(":core-idgenerator"))
    compileOnly(libs.orika.core)
    compileOnly(libs.dozer.core)
    api(libs.springboot.autoconfigure)
}