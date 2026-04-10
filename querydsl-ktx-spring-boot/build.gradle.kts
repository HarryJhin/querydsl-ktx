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
}
