plugins {
    alias(libs.plugins.kotlin.jvm)
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

    testImplementation(libs.querydsl.core)
    testImplementation(libs.querydsl.jpa) {
        artifact { classifier = "jakarta" }
    }
    testImplementation(libs.spring.data.commons)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
