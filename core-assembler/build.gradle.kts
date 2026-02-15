dependencies {
    api(project(":core-assembler-annotation"))
    api("org.jetbrains.kotlin:kotlin-reflect")
    implementation(libs.commons.beanutils)
    implementation("org.springframework:spring-core")
}