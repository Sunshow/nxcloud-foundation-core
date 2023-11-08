dependencies {
    api(project(":core-base"))
    compileOnly("jakarta.validation:jakarta.validation-api")

    testImplementation("org.hibernate.validator:hibernate-validator")
    testImplementation(libs.jakarta.el)
}