dependencies {
    compileOnly(project(":core-spring-support"))
    compileOnly(project(":core-bean"))
    compileOnly(project(":core-idgenerator"))
    compileOnly(project(":core-spring-data-jpa"))
    compileOnly(libs.springboot.starter.data.jpa)
    compileOnly(libs.orika.core)
    compileOnly(libs.dozer.core)
    api(libs.springboot.autoconfigure)
}