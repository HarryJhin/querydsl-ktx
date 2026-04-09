# AGENTS.md

querydsl-ktx — Null-safe infix Kotlin extensions for QueryDSL dynamic queries.

## Project Overview

This library provides seven extension interfaces that eliminate manual `BooleanBuilder` null checks
in QueryDSL dynamic queries. Implementing an interface makes its infix functions available in scope.

### Modules

- `querydsl-ktx` — Core: 7 extension interfaces + QuerydslSupport/QuerydslRepository base classes
- `querydsl-ktx-spring-boot` — AutoConfiguration: JPAQueryFactory auto-registration
- `querydsl-ktx-spring-boot-starter` — Starter: aggregates the above modules

### Tech Stack

- Kotlin 1.7+ (compiled with 1.7.21)
- Spring Boot 3.0+ (compileOnly — does not force version)
- QueryDSL 5.1.0+ (compileOnly)
- Gradle 7.6.4 with Kotlin DSL
- JUnit 5 + kotlin-test for testing
- Dokka 1.7.20 for API documentation

## Architecture

```
querydsl-ktx/src/main/kotlin/com/querydsl/ktx/
├── extensions/          ← 7 interfaces (no state, no dependencies)
│   ├── BooleanExpressionExtensions.kt    — and, or, eq, nullif, coalesce
│   ├── SimpleExpressionExtensions.kt     — eq, ne, in, notIn
│   ├── ComparableExpressionExtensions.kt — gt, goe, lt, loe, between
│   ├── NumberExpressionExtensions.kt     — same as Comparable (separate hierarchy)
│   ├── StringExpressionExtensions.kt     — contains, startsWith, like, matches
│   ├── TemporalExpressionExtensions.kt   — after, before
│   └── CollectionExpressionExtensions.kt — contains
└── support/             ← Base classes (DI, pagination, DML)
    ├── QuerydslSupport.kt    — abstract, requires domainClass override
    └── QuerydslRepository.kt — extends Support, implements all 7 interfaces
```

## Null-Safety Contract

All extension functions follow this contract:

| Function type | this null | arg null | both null |
|--------------|-----------|----------|-----------|
| `and` / `or` | returns arg | returns this | null |
| `between(Pair)` | null | one-sided comparison (goe/loe) | null |
| All others | null | null | null |

This contract is the core design principle. Any new extension must follow it.

## Commands

```bash
./gradlew build                        # Full build + test
./gradlew :querydsl-ktx:test           # Run tests only
./gradlew :querydsl-ktx:dokkaHtml      # Generate API docs
```

## Code Style

- KDoc in English, user-perspective (explain "why to use" not "what it does internally")
- Each function: first-line summary, SQL example, @param/@return
- Each interface: class-level KDoc with before/after example
- Extension functions use `when` expression for null checks (not if/else)

## Testing

- Tests in `querydsl-ktx/src/test/kotlin/com/querydsl/ktx/extensions/`
- Each extension interface has its own test class
- Test class implements the extension interface to access infix functions
- Every function tests: this-null, arg-null, both-null, both-non-null
- QueryDSL expressions created via `Expressions.*Path()` (no DB needed)

## Conventions

- Dependencies: `compileOnly` for all external libraries (user provides versions)
- Pagination: `slice()` returns Slice, `page()` returns Page, `fetch()` returns List
- Bulk DML: wrap in `modifying { }` for flush + clear
- Naming: verb form (slice, page, fetch), not gerund (slicing, paging, fetching)
