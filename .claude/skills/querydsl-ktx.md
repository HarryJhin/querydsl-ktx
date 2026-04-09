---
name: querydsl-ktx
description: QueryDSL Kotlin null-safe infix extension usage guide. Use when writing QueryDSL dynamic queries, repository implementations, or debugging null-safety issues.
triggers:
  - querydsl
  - QueryDSL
  - dynamic query
  - BooleanExpression
  - null-safe
  - querydsl-ktx
  - QuerydslRepository
---

## Quick Reference

Implement an extension interface to use infix functions in that scope:

```kotlin
@Repository
class MemberRepository : QuerydslRepository<Member>() {
    private val member = QMember.member

    fun search(name: String?, status: String?, pageable: Pageable): Page<Member> =
        selectFrom(member)
            .where(member.name contains name, member.status eq status)
            .page(pageable)
}
```

## Null Rules (CRITICAL)

```
and/or:       null side ignored, non-null side preserved
between(Pair): one side null → goe/loe, both null → skip
everything else: any null → skip (return null)
```

## Infix Cheat Sheet

```kotlin
// Boolean
condition and condition     // AND, null-safe
condition or condition      // OR, null-safe

// Equality (SimpleExpression — all types)
entity.field eq value       // =
entity.field ne value       // !=
entity.field `in` list      // IN (backticks required)
entity.field notIn list     // NOT IN

// Comparison (Comparable / Number)
entity.field gt value       // >
entity.field goe value      // >=
entity.field lt value       // <
entity.field loe value      // <=
entity.field between (a to b)   // BETWEEN (Pair, one-sided OK)
entity.field between (a..b)     // BETWEEN (ClosedRange)

// String
entity.field contains str           // LIKE '%str%'
entity.field containsIgnoreCase str // case-insensitive
entity.field startsWith str         // LIKE 'str%'
entity.field endsWith str           // LIKE '%str'
entity.field like pattern           // LIKE pattern
entity.field equalsIgnoreCase str   // case-insensitive =

// Temporal
entity.field after date     // >
entity.field before date    // <

// Collection
entity.field contains value // IN
```

## Pagination

```kotlin
query.page(pageable)                    // Page (auto count)
query.page(pageable) { countQuery() }   // Page (custom count)
query.slice(pageable)                   // Slice (pageSize+1)
query.page(page = 0, size = 20)         // value-based
query.fetch(offset = 0, limit = 20)     // raw List
query.applySort(sort) { fallback() }    // sort with fallback
query.fetch(sort)                 // sort + fetch
```

## Bulk DML

```kotlin
modifying {
    update(entity).set(...).where(...).execute()
}
// flushAutomatically=true, clearAutomatically=true by default
```

## Pitfalls

- `in` is a Kotlin keyword — always use backticks: `` `in` ``
- `NumberExpression` does NOT extend `ComparableExpression` — use `NumberExpressionExtensions`
- `modifying` defaults differ from `@Modifying` (ours: both true, JPA: both false)
- `between(null to null)` returns null, `between(value to null)` returns `goe(value)`
