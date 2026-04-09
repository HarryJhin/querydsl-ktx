plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    api(projects.querydslKtx)
    api(projects.querydslKtxSpringBoot)
    api(libs.querydsl.jpa) {
        artifact { classifier = "jakarta" }
    }
}
