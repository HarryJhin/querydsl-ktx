plugins {
    alias(libs.plugins.kotlin.jvm)
    kotlin("kapt")
}

val springBootVersion = findProperty("springBootVersion") as String?
if (springBootVersion != null) {
    configurations.configureEach {
        resolutionStrategy.eachDependency {
            if (requested.group == "org.springframework.boot") {
                useVersion(springBootVersion)
            }
        }
    }
}

dependencies {
    implementation(projects.querydslKtx)
    compileOnly(libs.spring.boot.autoconfigure)
    compileOnly(libs.querydsl.jpa) {
        artifact { classifier = "jakarta" }
    }
    compileOnly(libs.jakarta.persistence.api)
    kapt(libs.spring.boot.autoconfigure.processor)

    testImplementation(libs.spring.boot.test.autoconfigure)
    testImplementation(libs.spring.boot.test)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito.core)
    testImplementation(libs.querydsl.jpa) {
        artifact { classifier = "jakarta" }
    }
    testImplementation(libs.jakarta.persistence.api)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
