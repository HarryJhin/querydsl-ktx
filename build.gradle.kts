plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.dokka)
    alias(libs.plugins.maven.publish) apply false
}

allprojects {
    group = "io.github.harryjhin"
    version = "0.0.1"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "com.vanniktech.maven.publish")

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "17"
            apiVersion = "1.7"
            languageVersion = "1.7"
        }
    }

    tasks.withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
        publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
        signAllPublications()

        pom {
            name.set(project.name)
            description.set("Null-safe infix Kotlin extensions for QueryDSL dynamic queries")
            url.set("https://github.com/HarryJhin/querydsl-ktx")
            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            developers {
                developer {
                    id.set("harryjhin")
                    name.set("Jinhyun Ju")
                    url.set("https://github.com/HarryJhin")
                }
            }
            scm {
                url.set("https://github.com/HarryJhin/querydsl-ktx")
                connection.set("scm:git:git://github.com/HarryJhin/querydsl-ktx.git")
                developerConnection.set("scm:git:ssh://git@github.com/HarryJhin/querydsl-ktx.git")
            }
        }
    }
}
