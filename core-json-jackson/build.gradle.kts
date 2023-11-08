dependencies {
    api(project(":core-json"))
    compileOnly("com.fasterxml.jackson.core:jackson-databind")

    testImplementation("com.fasterxml.jackson.core:jackson-databind")
}