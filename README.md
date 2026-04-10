# querydsl-ktx

[![CI](https://github.com/HarryJhin/querydsl-ktx/actions/workflows/ci.yml/badge.svg)](https://github.com/HarryJhin/querydsl-ktx/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.harryjhin/querydsl-ktx)](https://central.sonatype.com/artifact/io.github.harryjhin/querydsl-ktx)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.7%2B-7F52FF.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2%2B-6DB33F.svg?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

[한국어](README.ko.md) | [Documentation](https://harryjhin.github.io/querydsl-ktx/)

**Eliminate 90% of BooleanBuilder boilerplate** in QueryDSL dynamic queries with null-safe infix Kotlin extensions.

## The Problem

If you've used QueryDSL in Kotlin, you've written this pattern hundreds of times:

```kotlin
val builder = BooleanBuilder()
if (name != null) builder.and(member.name.contains(name))
if (status != null) builder.and(member.status.eq(status))
if (minAge != null && maxAge != null) builder.and(member.age.between(minAge, maxAge))
else if (minAge != null) builder.and(member.age.goe(minAge))
else if (maxAge != null) builder.and(member.age.loe(maxAge))
if (startDate != null && endDate != null) builder.and(member.createdAt.between(startDate, endDate))
else if (startDate != null) builder.and(member.createdAt.goe(startDate))
else if (endDate != null) builder.and(member.createdAt.loe(endDate))
```

Every optional filter adds 1-3 lines. Range filters need 3 branches. The pattern is always the same.

## The Solution

```kotlin
selectFrom(member)
    .where(
        member.name contains name,
        member.status eq status,
        member.age between (minAge to maxAge),
        member.createdAt between (startDate to endDate),
    )
    .page(pageable)
```

Null parameters are automatically skipped. `between` with a `Pair` handles one-sided ranges.
30 lines to 10 lines.

## Quick Start

```kotlin
// 1. Add the dependency
implementation("io.github.harryjhin:querydsl-ktx-spring-boot-starter:0.2.0")
```

```kotlin
// 2. Extend QuerydslRepository
@Repository
class MemberRepository : QuerydslRepository<Member>() {

    private val member = QMember.member

    fun search(name: String?, status: String?, pageable: Pageable): Page<Member> =
        selectFrom(member)
            .where(
                member.name contains name,
                member.status eq status,
            )
            .page(pageable)
}
```

## Why querydsl-ktx?

| Approach | Drawback |
|----------|----------|
| `BooleanBuilder` | Verbose, error-prone with ranges |
| Per-field helpers (`statusEq()`) | Duplicated per entity, partial coverage |
| `Specification` | Separate from QueryDSL, no infix syntax |
| Top-level extensions | Global scope pollution, name clashes |
| **querydsl-ktx** | Standard, tested, complete |

Extensions are scoped through **interface implementation** -- no global namespace pollution. [Learn more](https://harryjhin.github.io/querydsl-ktx/why/)

## Features

- **Null-safe dynamic queries** -- `entity.status eq null` returns `null` (skipped), no `if` check needed. Replaces both BooleanBuilder and per-field helper functions.
- **One-sided range survival** -- `entity.date between (from to null)` becomes `date >= from`. A single expression handles 4 combinations that used to need 3-branch `if/else`.
- **Pagination without fetchResults()** -- `fetchResults()` is deprecated since QueryDSL 5.0. `page()` auto-generates count queries for simple cases and accepts a lambda for complex ones. `slice()` avoids count queries entirely.
- **Type-safe dynamic sorting** -- `SortSpec` provides a whitelist mapping for `Sort` property names. No more `?sort=password,asc` security holes or broken join-column sorting.
- **8 extension interfaces** -- null-safe infix operators for Boolean, Simple, Comparable, Number, String, Temporal, Collection, SubQuery expressions.
- **Reified expression templates** -- `numberTemplate<Float>(...)` instead of `Expressions.numberTemplate(Float::class.java, ...)`.
- **Case/When DSL** -- `case<Int> { when(pred) then value; otherwise(default) }` with null-safe branches.
- **Bulk DML** -- `modifying { }` with auto flush/clear.

## Documentation

| | |
|---|---|
| [Installation](https://harryjhin.github.io/querydsl-ktx/getting-started/installation/) | Gradle, Maven setup and module selection |
| [Quick Start](https://harryjhin.github.io/querydsl-ktx/getting-started/quick-start/) | Write your first dynamic query in 5 minutes |
| [User Guide](https://harryjhin.github.io/querydsl-ktx/guide/dynamic-queries/) | Dynamic queries, extensions, expressions, Case/When DSL, pagination, bulk DML |
| [API Reference](https://harryjhin.github.io/querydsl-ktx/api/) | Dokka-generated API documentation |

## Requirements

| Dependency | Version | Note |
|------------|---------|------|
| Spring Boot | 3.2+ | Recommended 3.4+. Compiled against 3.0 APIs, but 3.0/3.1 are EOL. CI tests 3.2, 3.3, 3.4. |
| QueryDSL | 5.1.0+ | |
| Kotlin | 1.7+ | |
| Java | 17+ | |

## For LLMs

This project provides an [`llms.txt`](llms.txt) for AI context via [GitMCP](https://gitmcp.io/).

### Claude Code

```bash
claude mcp add querydsl-ktx -- mcp-remote https://gitmcp.io/HarryJhin/querydsl-ktx
```

### Codex

```bash
codex mcp add querydsl-ktx -- mcp-remote https://gitmcp.io/HarryJhin/querydsl-ktx
```

### Gemini CLI

```bash
gemini mcp add querydsl-ktx -- mcp-remote https://gitmcp.io/HarryJhin/querydsl-ktx
```

### JSON config (manual)

```json
{
  "mcpServers": {
    "querydsl-ktx": {
      "command": "npx",
      "args": ["-y", "@anthropic-ai/mcp-remote@latest", "https://gitmcp.io/HarryJhin/querydsl-ktx/sse"]
    }
  }
}
```

## License

[Apache License 2.0](LICENSE)
