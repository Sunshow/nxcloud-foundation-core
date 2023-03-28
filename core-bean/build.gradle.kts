dependencies {
    api(project(":core-base"))
    compileOnly(libs.orika.core)
    compileOnly(libs.dozer.core)
    compileOnly(libs.modelmapper)
    testImplementation(libs.orika.core)
    testImplementation(libs.dozer.core)
    testImplementation(libs.javax.jaxb)
    testImplementation(libs.modelmapper)
}