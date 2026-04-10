plugins {
    alias(libs.plugins.kotlin.jvm)
    kotlin("kapt")
}

dependencies {
    compileOnly(libs.querydsl.jpa) {
        artifact { classifier = "jakarta" }
    }
    compileOnly(libs.querydsl.core)
    compileOnly(libs.spring.data.commons)
    compileOnly(libs.spring.data.jpa)
    compileOnly(libs.jakarta.persistence.api)
    compileOnly(libs.jakarta.annotation.api)

    // Q-class generation for test entities
    kaptTest(libs.querydsl.apt) {
        artifact { classifier = "jakarta" }
    }

    testImplementation(libs.querydsl.core)
    testImplementation(libs.querydsl.jpa) {
        artifact { classifier = "jakarta" }
    }
    testImplementation(libs.spring.data.commons)
    testImplementation(kotlin("test"))

    // Integration test dependencies
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.starter.data.jpa)
    testImplementation(libs.h2)
}

tasks.test {
    useJUnitPlatform()
}
