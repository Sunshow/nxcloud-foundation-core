dependencies {
    api(project(":core-base"))
    api(project(":core-idgenerator"))
    compileOnly("org.springframework:spring-context")
    
    testImplementation("org.springframework:spring-context")
}