---
description: Why querydsl-ktx — alternatives comparison, scoping rationale, and decision guide for null-safe dynamic queries in QueryDSL.
---

# Why querydsl-ktx?

QueryDSL is the de-facto standard for type-safe queries on the JVM, but its
dynamic-query story has gaps. `BooleanBuilder` is verbose, range filters need
3-branch `if/else`, and every team ends up reinventing helper functions. This
page compares the common approaches and explains the design decisions behind
querydsl-ktx.

## Alternatives Comparison

| Approach | Pros | Cons |
|----------|------|------|
| `BooleanBuilder` | Built-in, no extra dependency | Verbose; range filters require 3-branch `if/else`; null checks scattered |
| `BooleanExpression` chaining (`.and(...).and(...)`) | Slightly cleaner | Still manual null checks at every step |
| Spring Data `Specification` | Type-safe, JPA-native | Separate from QueryDSL; no infix syntax; awkward for complex joins |
| Top-level Kotlin extension functions (`fun StringPath.eqOrNull(...)`) | Concise call site | Global namespace pollution; name clashes with stdlib; available outside repository |
| Hand-rolled per-entity helpers (`fun memberByStatus(s: Status?)`) | Familiar to the team | Duplicated per entity; partial coverage; no standard |
| **querydsl-ktx** | Null-safe contract, concise, scoped via interfaces, tested across CI matrix | Additional dependency (compileOnly) |

## Why Interface Scoping?

querydsl-ktx delivers extensions as **interfaces**, not top-level functions.
Implement the interface on your repository to bring infix operators into scope:

```kotlin
@Repository
class MemberRepository : QuerydslRepository<Member>() {  // implements all 8 extension interfaces
    fun findByCondition(name: String?, status: Status?): List<Member> =
        selectFrom(member)
            .where(
                member.name eq name,        // null name → skipped
                member.status eq status,    // null status → skipped
            )
            .fetch()
}
```

The `eq` infix is **only available inside this class**. Outside the repository,
plain QueryDSL `member.name.eq(value)` is the only option, which protects the
rest of the codebase from accidental misuse.

This is the key trade-off vs top-level extension functions: a few extra
characters at the import site, but no namespace pollution and no risk of
calling `eq` on a path inside arbitrary business logic.

## Before vs After

::: code-group

```kotlin [Before: BooleanBuilder]
fun search(name: String?, minAge: Int?, maxAge: Int?, status: Status?): List<Member> {
    val builder = BooleanBuilder()
    if (name != null) builder.and(member.name.contains(name))
    if (minAge != null && maxAge != null) builder.and(member.age.between(minAge, maxAge))
    else if (minAge != null) builder.and(member.age.goe(minAge))
    else if (maxAge != null) builder.and(member.age.loe(maxAge))
    if (status != null) builder.and(member.status.eq(status))
    return queryFactory.selectFrom(member).where(builder).fetch()
}
```

```kotlin [After: querydsl-ktx]
fun search(name: String?, minAge: Int?, maxAge: Int?, status: Status?): List<Member> =
    selectFrom(member)
        .where(
            member.name contains name,
            member.age between (minAge to maxAge),
            member.status eq status,
        )
        .fetch()
```

:::

The 9-line range-filter ladder collapses to a single `between (minAge to maxAge)`
expression. The library applies one-sided survival automatically: only `minAge`
present → `>=`, only `maxAge` → `<=`, both null → skip.

## When to Pick Each Approach

| Situation | Recommended approach |
|-----------|----------------------|
| Existing QueryDSL project with mostly static queries | Stay on `BooleanBuilder` — migration cost not worth it |
| New project, lots of dynamic filters | querydsl-ktx |
| JPA-only project that already uses Spring Data heavily | `Specification`, unless you need joins or projections that hurt with Specification |
| Team prefers top-level extensions and accepts namespace cost | Top-level extensions (open-source ones exist) |
| One-off helper for a single entity | Hand-rolled helpers — but consider whether you'll need them elsewhere |

## What querydsl-ktx Does NOT Do

- It does not replace QueryDSL — you still write `selectFrom(...)`, `orderBy`, `groupBy`, `having`, `join`.
- It does not add new SQL features — every operator wraps an existing QueryDSL member.
- It does not silently swallow exceptions — `null` propagation is the contract, but illegal uses (e.g. `escape` on a non-LIKE expression) throw `ExpressionException`.

The library exists to remove the **null-checking boilerplate** that QueryDSL
itself does not address, while staying compatible with everything QueryDSL
already provides.
