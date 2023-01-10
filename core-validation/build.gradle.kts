dependencies {
    api(project(":core-base"))
    compileOnly(libs.jakarta.validation.api)

    testImplementation(libs.hibernate.validator)
    testImplementation(libs.glassfish.javax.el)
}