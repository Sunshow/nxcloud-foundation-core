dependencies {
    api(project(":core-json"))
    compileOnly(libs.jackson.databind)

    testImplementation(libs.jackson.databind)
}