apply(plugin = "org.jetbrains.kotlin.plugin.spring")

dependencies {
    api(project(":core-data-support"))
    api(project(":core-spring-support"))
    api(project(":core-idgenerator"))
    // compileOnly(libs.jakarta.persistence.api)
    compileOnly("org.springframework.boot:spring-boot-starter-data-jpa")

    testImplementation(project(":core-spring-boot-starter"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testImplementation("org.springframework.boot:spring-boot-starter-aop")
    testImplementation("com.h2database:h2")
    testImplementation(libs.hibernate.envers)
}