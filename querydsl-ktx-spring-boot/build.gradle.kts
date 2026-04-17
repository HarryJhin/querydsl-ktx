plugins {
    alias(libs.plugins.kotlin.jvm)
    kotlin("kapt")
}

val jpaClassifier = extra["jpaClassifier"] as String?

dependencies {
    implementation(projects.querydslKtx)
    compileOnly(libs.spring.boot.autoconfigure)
    compileOnly(libs.spring.orm)
    compileOnly(libs.querydsl.jpa) {
        if (jpaClassifier != null) artifact { classifier = jpaClassifier }
    }
    compileOnly(libs.jakarta.persistence.api)
    kapt(libs.spring.boot.autoconfigure.processor)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.starter.data.jpa)
    testImplementation(libs.querydsl.jpa) {
        if (jpaClassifier != null) artifact { classifier = jpaClassifier }
    }
    testImplementation(libs.h2)
    testImplementation(kotlin("test"))

    // Spring Boot 3.5+의 junit-bom은 Gradle 기본 번들 launcher보다 신 버전을 요구한다.
    // 명시적으로 testRuntimeOnly로 BOM 관리 버전을 끌어와야 OutputDirectoryProvider
    // 호환성 이슈를 피할 수 있다.
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}
