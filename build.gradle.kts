import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
}

buildscript {
    dependencies {
        classpath(libs.gradlePlugin.kotlin)
        classpath(libs.gradlePlugin.springboot)
    }

    repositories {
        mavenCentral()
        gradlePluginPortal()
        google()
    }
}

allprojects {
    group = "nxcloud.foundation"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
        google()
    }

    tasks.create("downloadDependencies") {
        description = "Download all dependencies to the Gradle cache"
        doLast {
            for (configuration in configurations) {
                if (configuration.isCanBeResolved) {
                    configuration.files
                }
            }
        }
    }

    normalization {
        runtimeClasspath {
            metaInf {
                ignoreAttribute("Bnd-LastModified")
            }
        }
    }
}

/** Configure building for Java+Kotlin projects. */
subprojects {
    val project = this@subprojects

    apply(plugin = "java-library")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    tasks.withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.toString()
    }

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(11))
        }
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_1_8.toString()
            freeCompilerArgs = listOf(
                "-Xjvm-default=all",
            )
        }
    }

    val testJavaVersion = System.getProperty("test.java.version", "11").toInt()

    tasks.withType<Test> {
        useJUnitPlatform()
        jvmArgs = jvmArgs!! + listOf(
            "-XX:+HeapDumpOnOutOfMemoryError"
        )

        val javaToolchains = project.extensions.getByType<JavaToolchainService>()
        javaLauncher.set(javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(testJavaVersion))
        })

        maxParallelForks = Runtime.getRuntime().availableProcessors() * 2
        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
        }

        systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")
    }

    tasks.withType<JavaCompile> {
        sourceCompatibility = JavaVersion.VERSION_1_8.toString()
        targetCompatibility = JavaVersion.VERSION_1_8.toString()
    }

    dependencies {
        implementation(platform(rootProject.libs.bom.springboot))
        implementation("org.slf4j:slf4j-api")
        testImplementation("org.jetbrains.kotlin:kotlin-test")
        testImplementation("org.junit.jupiter:junit-jupiter-api")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
        testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
    }

}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}