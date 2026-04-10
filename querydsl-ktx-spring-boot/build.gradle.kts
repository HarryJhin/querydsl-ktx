plugins {
    alias(libs.plugins.kotlin.jvm)
    kotlin("kapt")
}

dependencies {
    implementation(projects.querydslKtx)
    compileOnly(libs.spring.boot.autoconfigure)
    compileOnly(libs.querydsl.jpa) {
        artifact { classifier = "jakarta" }
    }
    compileOnly(libs.jakarta.persistence.api)
    kapt("org.springframework.boot:spring-boot-autoconfigure-processor:${libs.versions.spring.boot.get()}")

    testImplementation("org.springframework.boot:spring-boot-test-autoconfigure:${libs.versions.spring.boot.get()}")
    testImplementation("org.springframework.boot:spring-boot-test:${libs.versions.spring.boot.get()}")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation(libs.querydsl.jpa) {
        artifact { classifier = "jakarta" }
    }
    testImplementation(libs.jakarta.persistence.api)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
