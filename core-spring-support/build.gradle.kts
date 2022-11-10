dependencies {
    api(project(":core-base"))
    api(project(":core-idgenerator"))
    compileOnly(libs.spring.context)
    testImplementation(libs.spring.context)
}