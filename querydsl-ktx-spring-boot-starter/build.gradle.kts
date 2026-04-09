plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    api(projects.querydslKtx)
    api(projects.querydslKtxSpringBoot)
}
