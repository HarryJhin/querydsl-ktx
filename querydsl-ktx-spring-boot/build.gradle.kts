plugins {
    alias(libs.plugins.kotlin.jvm)
    kotlin("kapt")
}

val jpaClassifier = extra["jpaClassifier"] as String?

dependencies {
    implementation(projects.querydslKtx)
    compileOnly(libs.spring.boot.autoconfigure)
    compileOnly(libs.querydsl.jpa) {
        if (jpaClassifier != null) artifact { classifier = jpaClassifier }
    }
    compileOnly(libs.jakarta.persistence.api)
    kapt(libs.spring.boot.autoconfigure.processor)

    testImplementation(libs.spring.boot.test.autoconfigure)
    testImplementation(libs.spring.boot.test)
    testImplementation(libs.spring.core.test)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito.core)
    testImplementation(libs.querydsl.jpa) {
        if (jpaClassifier != null) artifact { classifier = jpaClassifier }
    }
    testImplementation(libs.jakarta.persistence.api)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
