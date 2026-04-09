plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    api(project(":querydsl-ktx"))
    api(project(":querydsl-ktx-spring-boot-autoconfigure"))
    api(libs.querydsl.jpa) {
        artifact { classifier = "jakarta" }
    }
}
