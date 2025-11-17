plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.5.5"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.jpa") version "1.9.25"
}

group = "goodspace"
version = "0.0.1-SNAPSHOT"
description = "Teaming"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("com.mysql:mysql-connector-j")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // JJWT (JSON Web Token)
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

    // Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.12")
    implementation("org.apache.commons:commons-lang3:3.18.0")

    // mail
    implementation("org.springframework.boot:spring-boot-starter-mail")

    // GreenMail
    testImplementation("com.icegreen:greenmail:2.1.5")

    // retry
    implementation("org.springframework.retry:spring-retry")
    implementation("org.springframework.boot:spring-boot-starter-aop")

    // 모킹 테스트
    testImplementation("io.mockk:mockk:1.14.5")

    // 웹소켓
    implementation("org.springframework.boot:spring-boot-starter-websocket")

    //웹클라이언트(instead of RestTemplate)
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // AWS & S3
    implementation(platform("software.amazon.awssdk:bom:2.33.7"))
    implementation("software.amazon.awssdk:s3")

    // Jwt 라이브러리
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    implementation("io.jsonwebtoken:jjwt-impl:0.11.5")
    implementation("io.jsonwebtoken:jjwt-jackson:0.11.5")

    // 테스트용 H2 DB
    testRuntimeOnly("com.h2database:h2")

    //Gson
    implementation("com.google.code.gson:gson:2.10.1")

    // Coroutine
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.8.1")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
