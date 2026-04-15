# AGENTS.md

querydsl-ktx — Null-safe infix Kotlin extensions for QueryDSL dynamic queries.

## Project Overview

This library provides eight extension interfaces that eliminate manual `BooleanBuilder` null checks
in QueryDSL dynamic queries. Implementing an interface makes its infix functions available in scope.

### Modules

- `querydsl-ktx` — Core: 8 extension interfaces + top-level utilities (Expressions, CaseDsl) + QuerydslSupport/QuerydslRepository base classes
- `querydsl-ktx-spring-boot` — AutoConfiguration: JPAQueryFactory auto-registration + GraalVM RuntimeHints for native image support
- `querydsl-ktx-spring-boot-starter` — Starter: aggregates the above modules

### Tech Stack

- Kotlin 1.7+ (compiled with 1.9.25; apiVersion/languageVersion 1.7)
- Spring Boot 3.0+ (compileOnly — does not force version)
- QueryDSL 5.1.0+ (compileOnly)
- Gradle 8.5 with Kotlin DSL
- JUnit 5 + kotlin-test for testing
- Dokka 1.9.20 for API documentation

## Architecture

```
querydsl-ktx/src/main/kotlin/com/querydsl/ktx/
├── Expressions.kt       ← Reified template wrappers + value wrapping + constant
├── CaseDsl.kt           ← CASE/WHEN Kotlin DSL (searched & simple)
├── extensions/          ← 8 interfaces (no state, no dependencies)
│   ├── BooleanExpressionExtensions.kt    — and, or, eq, nullif, coalesce
│   ├── SimpleExpressionExtensions.kt     — eq, ne, in, notIn
│   ├── ComparableExpressionExtensions.kt — gt, goe, lt, loe, between, reverse between, rangeTo
│   ├── NumberExpressionExtensions.kt     — same as Comparable (separate hierarchy), includes rangeTo
│   ├── StringExpressionExtensions.kt     — contains, startsWith, like, matches, nullif, coalesce
│   ├── TemporalExpressionExtensions.kt   — after, before
│   ├── CollectionExpressionExtensions.kt — contains
│   └── SubQueryExtensions.kt            — exists, notExists
└── support/             ← Base classes (DI, pagination, DML)
    ├── QuerydslSupport.kt    — abstract, requires domainClass override
    ├── QuerydslRepository.kt — extends Support, implements all 8 interfaces
    └── SortSpec.kt           — maps Sort property names to QueryDSL expressions
```

### Documentation Site

- `docs/` — MkDocs Material source files (`*.md` English, `*.ko.md` Korean)
- `mkdocs.yml` — site configuration
- `.github/workflows/ci.yml` — Build + test on Java 17/21 matrix
- `.github/workflows/docs.yml` — Dokka API docs + MkDocs → GitHub Pages

### Publishing

- Maven Central via `com.vanniktech.maven.publish` plugin (Central Portal)
- Signing: `useInMemoryPgpKeys` from environment variables
- All 3 modules published: querydsl-ktx, querydsl-ktx-spring-boot, querydsl-ktx-spring-boot-starter

## Null-Safety Contract

All extension functions follow this contract:

| Function type | this null | arg null | both null |
|--------------|-----------|----------|-----------|
| `and` / `or` | returns arg | returns this | null |
| `between(Pair)` | null | one-sided comparison (goe/loe) | null |
| reverse `between(Pair)` | null | one-sided comparison (loe/goe) | null |
| `case {}` | n/a | null predicate skips branch | all null → null |
| All others | null | null | null |

This contract is the core design principle. Any new extension must follow it.

## Commands

```bash
./gradlew build                        # Full build + test
./gradlew :querydsl-ktx:test           # Run tests only
./gradlew :querydsl-ktx:dokkaHtml      # Generate API docs
./gradlew publishToMavenLocal             # Publish to local Maven repo (testing)
./gradlew publishAndReleaseToMavenCentral # Publish to Maven Central (requires GPG + Sonatype token)
mkdocs serve                              # Local docs preview (requires pip install mkdocs-material)
mkdocs build                              # Build docs site
```

## Code Style

- KDoc in English, user-perspective (explain "why to use" not "what it does internally")
- Each function: first-line summary, SQL example, @param/@return
- Each interface: class-level KDoc with before/after example
- Extension functions use `when` expression for null checks (not if/else)

## Testing

- Extension tests in `querydsl-ktx/src/test/kotlin/com/querydsl/ktx/extensions/`
- Top-level utility tests in `querydsl-ktx/src/test/kotlin/com/querydsl/ktx/` (`ExpressionsTest`, `CaseDslTest`)
- Each extension interface has its own test class
- Test class implements the extension interface to access infix functions
- Every function tests: this-null, arg-null, both-null, both-non-null
- QueryDSL expressions created via `Expressions.*Path()` (no DB needed)

## Conventions

- Dependencies: `compileOnly` for all external libraries (user provides versions)
- Spring Data 내부 API 의존: `Querydsl` (`o.s.d.jpa.repository.support`), `SimpleEntityPathResolver` — Spring Boot 3.0~3.4 호환 검증됨. v2.0.0(Spring Boot 4) 마이그레이션 시 검토 필요
- Pagination: `slice()` (optimistic hasNext) and `exactSlice()` (exact hasNext) return Slice, `page()` returns Page, `fetch()` returns List
- Bulk DML: wrap in `modifying { }` for flush + clear (requires active transaction; throws `IllegalStateException` otherwise)
- Naming: verb form (slice, page, fetch), not gerund (slicing, paging, fetching)
