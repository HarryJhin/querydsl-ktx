plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":querydsl-ktx"))
    compileOnly(libs.spring.boot.autoconfigure)
    compileOnly(libs.querydsl.jpa) {
        artifact { classifier = "jakarta" }
    }
    compileOnly(libs.jakarta.persistence.api)
}
