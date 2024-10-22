plugins {
    `java-platform`
    `signing`
    `maven-publish`
}

dependencies {
    constraints {
        api(project(":core-base"))
        api(project(":core-bean"))
        api(project(":core-idgenerator"))
        api(project(":core-validation"))
        api(project(":core-json"))
        api(project(":core-json-jackson"))
        api(project(":core-data-support"))
        api(project(":core-spring-data-jpa"))
        api(project(":core-spring-support"))
        api(project(":core-spring-boot-autoconfigure"))
        api(project(":core-spring-boot-starter"))
        api(project(":core-spring-boot-starter-data-jpa"))
        api(project(":core-event"))
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenPlatform") {
            artifactId = "nxcloud-${project.name}"

            from(components["javaPlatform"])

            pom {
                name.set("NXCloud Foundation Core Libraries")
                description.set("Base libraries for NXCloud Framework")
                url.set("https://github.com/Sunshow/nxcloud-foundation-core")
                properties.set(
                    mapOf(
                    )
                )
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("sunshow")
                        name.set("Sunshow")
                        email.set("sunshow@gmail.com")
                    }
                }
                scm {
                    connection.set("https://github.com/Sunshow/nxcloud-foundation-core")
                    developerConnection.set("https://github.com/Sunshow/nxcloud-foundation-core")
                    url.set("https://github.com/Sunshow/nxcloud-foundation-core")
                }
            }
        }
    }

    if (project.hasProperty("publishUsername") && project.hasProperty("publishPassword")
        && project.hasProperty("publishReleasesRepoUrl") && project.hasProperty("publishSnapshotsRepoUrl")
    ) {
        repositories {
            maven {
                val publishReleasesRepoUrl: String by project
                val publishSnapshotsRepoUrl: String by project

                url = uri(
                    if (version.toString().endsWith("SNAPSHOT")) publishSnapshotsRepoUrl else publishReleasesRepoUrl
                )
                isAllowInsecureProtocol = true

                val publishUsername: String by project
                val publishPassword: String by project
                credentials {
                    username = publishUsername
                    password = publishPassword
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenPlatform"])
}
