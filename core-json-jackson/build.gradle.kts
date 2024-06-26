dependencies {
    api(project(":core-json"))
    api(project(":core-base"))
    compileOnly("com.fasterxml.jackson.core:jackson-databind")

    testImplementation("com.fasterxml.jackson.core:jackson-databind")
}