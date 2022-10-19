import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    dependencies {
        classpath(libs.gradlePlugin.kotlin)
        classpath(libs.gradlePlugin.springboot)
        classpath(libs.gradlePlugin.dependency.management)
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

    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "java-library")

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

    val testRuntimeOnly: Configuration by configurations.getting
    dependencies {
        testRuntimeOnly(rootProject.libs.junit.jupiter.engine)
        testRuntimeOnly(rootProject.libs.junit.vintage.engine)
    }

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

    dependencyManagement {
        resolutionStrategy {
            cacheChangingModulesFor(0, "seconds")
            cacheDynamicVersionsFor(0, "seconds")
        }

        imports {
            mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        }

        dependencies {
            dependency("org.slf4j:slf4j-api:${slf4jVersion}")
        }
    }
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}

/*
plugins {
    id 'org.jetbrains.kotlin.jvm' version '1.7.10' apply false
    id 'org.jetbrains.kotlin.plugin.lombok' version '1.7.10' apply false
    id 'io.freefair.lombok' version '6.5.1' apply false
    id 'org.springframework.boot' version '2.7.4' apply false
    id 'org.jetbrains.kotlin.plugin.spring' version '1.7.10' apply false
}

ext {
    slf4jVersion = '2.0.3'
}

subprojects {
    apply plugin: 'idea'
    apply plugin: 'java-library'
    apply plugin: 'io.spring.dependency-management'
    apply plugin: 'org.jetbrains.kotlin.jvm'
    apply plugin: 'org.jetbrains.kotlin.plugin.spring'
    apply plugin: 'org.jetbrains.kotlin.plugin.lombok'
    apply plugin: 'io.freefair.lombok'

    repositories {
        mavenCentral()
    }

    apply plugin: 'io.spring.dependency-management'
    apply plugin: 'idea'

    dependencyManagement {
        resolutionStrategy {
            cacheChangingModulesFor 0, 'seconds'
            cacheDynamicVersionsFor 0, 'seconds'
        }

        imports {
            mavenBom SpringBootPlugin.BOM_COORDINATES
        }

        dependencies {
            dependency "org.slf4j:slf4j-api:${slf4jVersion}"
        }
    }

    dependencies {
        testImplementation 'org.jetbrains.kotlin:kotlin-test'
        implementation 'org.jetbrains.kotlin:kotlin-stdlib'
        implementation 'org.slf4j:slf4j-api'
    }

    test {
        useJUnitPlatform()
    }

    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11

    tasks.compileKotlin {
        kotlinOptions {
            jvmTarget = "11"
        }
    }

    tasks.compileTestKotlin {
        kotlinOptions {
            jvmTarget = "11"
        }
    }

    tasks.withType(JavaCompile) {
        options.encoding = 'UTF-8'
    }

    tasks.withType(GenerateModuleMetadata) {
        enabled = false
    }

    tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile).configureEach {
        kotlinOptions {
            freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
        }
    }
}
*/