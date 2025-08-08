# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build System

This is a multi-module Kotlin/Java project using Gradle with Kotlin DSL.

### Key Commands

- **Build all modules**: `./gradlew build`
- **Test all modules**: `./gradlew test`
- **Test specific module**: `./gradlew :core-[module-name]:test`
- **Clean build**: `./gradlew clean build`
- **Publish to local repository**: `./gradlew publishToMavenLocal`
- **Generate sources/javadoc JARs**: `./gradlew sourcesJar javadocJar`

### Module Structure

The project consists of 17 modules organized as follows:

- **core-base**: Foundation classes including sealed enums, utilities, and base exceptions
- **core-bean**: Bean mapping facades with support for Dozer, ModelMapper, and Orika
- **core-idgenerator**: ID generation (SnowFlake implementation)
- **core-validation**: Custom validation constraints (China ID card, mobile number)
- **core-json**: JSON processing annotations
- **core-json-jackson**: Jackson-specific JSON serializers/deserializers for sealed enums
- **core-data-support**: Data access support annotations and contexts
- **core-spring-data-jpa**: Advanced JPA repository support with soft delete, lifecycle listeners
- **core-spring-support**: Spring context helpers and property configurations
- **core-spring-boot-autoconfigure**: Auto-configuration classes
- **core-spring-boot-starter**: Main Spring Boot starter
- **core-spring-boot-starter-data-jpa**: JPA-specific Spring Boot starter
- **core-event**: Event publishing SPI
- **core-universal-task**: Universal task management system
- **core-bom**: Bill of Materials for dependency management
- **core-plugin**: Plugin system support

## Architecture Overview

### Sealed Enums Pattern
The project uses Kotlin sealed classes for type-safe enumerations:
- Base class: `IntSealedEnum` in `core-base/src/main/kotlin/nxcloud/foundation/core/lang/enumeration/SealedEnums.kt:9`
- Provides value-based lookup and serialization support
- Jackson integration via custom serializers in `core-json-jackson`

### JPA Entity System
- Base entity: `DefaultJpaEntity` in `core-spring-data-jpa/src/main/kotlin/nxcloud/foundation/core/data/jpa/entity/DefaultJpaEntity.kt:11`
- Configurable ID generation strategies (database auto-increment, SnowFlake, assigned)
- Soft delete support via `SoftDeleteJpaEntity` and `@EnableSoftDelete` annotation
- Entity lifecycle listeners for audit trails and business logic hooks

### Plugin Architecture
- Built on PF4J framework
- Support for modular application development
- Located in `core-plugin` module

## Development Setup

### Java Version
- **Target**: Java 17 (both source and target compatibility)
- **Test execution**: Configurable via `test.java.version` system property (default: 17)

### Kotlin Configuration
- **Version**: 1.9.20 
- **Target JVM**: 17
- **Compiler args**: `-Xjvm-default=all`

### Plugin Configuration
All subprojects automatically apply:
- `kotlin-allopen` for JPA entities, Spring configurations, and transactional classes
- `kotlin-noarg` for JPA entities and classes annotated with `@NoArgs`
- `kotlin-lombok` and `io.freefair.lombok` for Lombok support

### Test Configuration
- **Framework**: JUnit 5 Platform with JUnit Vintage support
- **Parallel execution**: `maxParallelForks = availableProcessors * 2`
- **JVM args**: Heap dump on OOM enabled
- **Logging**: Full exception format

## Module Dependencies

All modules include:
- Spring Boot BOM for dependency management
- SLF4J for logging
- kotlin-logging-jvm for Kotlin-friendly logging
- JUnit 5 for testing

## Publishing

- **Group ID**: `net.sunshow.nxcloud`
- **Artifact naming**: `nxcloud-[module-name]`
- **Release version**: 0.8.0
- **Development version**: 0.6.0-SNAPSHOT
- Maven Central compatible with sources and javadoc JARs