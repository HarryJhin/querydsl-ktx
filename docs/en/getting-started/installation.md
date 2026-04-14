---
description: Install querydsl-ktx via Gradle or Maven. Requires JDK 17+, Spring Boot 3.0+, and QueryDSL 5.1.0.
---

# Installation

## Requirements

| Dependency   | Version | Note |
|--------------|---------|------|
| Spring Boot  | 3.0+    | CI tests 3.0, 3.2, 3.3, 3.4. |
| QueryDSL     | 5.1.0+  | |
| Kotlin       | 1.7+    | |
| Java         | 17+     | |

::: info Prerequisites
QueryDSL Q-class generation (APT or KSP) must be configured in your project. See [QueryDSL documentation](http://querydsl.com/) for setup instructions.
:::

## Add the Dependency

::: code-group

```kotlin [Gradle (Kotlin DSL)]
implementation("io.github.harryjhin:querydsl-ktx-spring-boot-starter:0.5.0")
```

```groovy [Gradle (Groovy DSL)]
implementation 'io.github.harryjhin:querydsl-ktx-spring-boot-starter:0.5.0'
```

```xml [Maven]
<dependency>
    <groupId>io.github.harryjhin</groupId>
    <artifactId>querydsl-ktx-spring-boot-starter</artifactId>
    <version>0.5.0</version>
</dependency>
```

:::

That's it. The starter brings in everything you need.

---

## Module Selection Guide

The library ships three modules. Choose based on your needs:

| Module | What it provides | When to use |
|--------|-----------------|-------------|
| `querydsl-ktx-spring-boot-starter` | Core + AutoConfiguration | **Most projects.** Adds a `JPAQueryFactory` bean automatically. |
| `querydsl-ktx` | Extension interfaces + `QuerydslRepository` base class | You already register your own `JPAQueryFactory` bean and only need the Kotlin extensions. |
| `querydsl-ktx-spring-boot` | AutoConfiguration only | You want the auto-registered `JPAQueryFactory` but will implement extension interfaces yourself. |

### Starter (recommended)

```kotlin
// Pulls in both querydsl-ktx and querydsl-ktx-spring-boot
implementation("io.github.harryjhin:querydsl-ktx-spring-boot-starter:0.5.0")
```

### Core only

```kotlin
// Extension interfaces only, no AutoConfiguration
implementation("io.github.harryjhin:querydsl-ktx:0.5.0")
```

::: info When to use core only
Use `querydsl-ktx` alone when:

- You register `JPAQueryFactory` with custom `JPQLTemplates`
- You use a non-Spring framework
- You want zero auto-configuration magic
:::

---

## AutoConfiguration

The starter auto-registers a `JPAQueryFactory` bean with these conditions:

| Condition | Purpose |
|-----------|---------|
| `@ConditionalOnClass(JPAQueryFactory)` | Only activates when QueryDSL is on the classpath |
| `@ConditionalOnMissingBean` | Respects any custom `JPAQueryFactory` you define |
| `@AutoConfiguration(after = HibernateJpaAutoConfiguration)` | Ensures `EntityManager` is ready first |

::: tip Custom JPAQueryFactory
If you register your own `JPAQueryFactory` bean (e.g., with custom `JPQLTemplates`),
the auto-configuration backs off automatically. No exclusions needed.

```kotlin
@Configuration
class QuerydslConfig {
    @Bean
    fun jpaQueryFactory(entityManager: EntityManager) =
        JPAQueryFactory(JPQLTemplates.DEFAULT, entityManager)
}
```
:::

## Verify the Setup

Create a simple repository to confirm everything works:

```kotlin
@Repository
class TestRepository : QuerydslRepository<YourEntity>() {

    private val entity = QYourEntity.yourEntity

    fun findAll(): List<YourEntity> =
        selectFrom(entity).fetch()
}
```

If this compiles and runs, your setup is complete. Head to [Quick Start](quick-start.md) to write your first dynamic query.

## GraalVM Native Image

querydsl-ktx provides `RuntimeHintsRegistrar` for GraalVM native image compatibility.
The auto-configuration automatically registers reflection hints needed by the library.

::: warning QueryDSL Upstream Limitation
QueryDSL 5.1.0 does not officially support GraalVM native image
([querydsl/querydsl#3646](https://github.com/querydsl/querydsl/issues/3646)).
You may need additional reflection configuration for QueryDSL core classes
when building native images.
:::
