dependencies {
    api(project(":core-base"))
    compileOnly(libs.orika.core)
    compileOnly(libs.dozer.core)
    testImplementation(libs.orika.core)
    testImplementation(libs.dozer.core)
    testImplementation(libs.javax.jaxb)
}