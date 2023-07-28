dependencies {
    compileOnly(project(":core-spring-support"))
    compileOnly(project(":core-bean"))
    compileOnly(project(":core-idgenerator"))
    compileOnly(project(":core-spring-data-jpa"))
    compileOnly("org.springframework.boot:spring-boot-starter-data-jpa")
    compileOnly(libs.orika.core)
    compileOnly(libs.dozer.core)
    compileOnly(libs.modelmapper)
    api("org.springframework.boot:spring-boot-autoconfigure")
}