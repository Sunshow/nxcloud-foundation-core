dependencies {
    api(project(":core-base"))
    compileOnly(libs.spring.context)
    testImplementation(libs.spring.context)
}