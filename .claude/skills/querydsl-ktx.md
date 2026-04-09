---
name: querydsl-ktx
description: QueryDSL Kotlin null-safe infix extensions for dynamic queries
---

# querydsl-ktx

Null-safe infix Kotlin extensions for QueryDSL. Eliminates BooleanBuilder boilerplate.

Trigger: "querydsl", "QueryDSL", "dynamic query", "BooleanExpression", "null-safe", "querydsl-ktx"

## Core Concept

Implement an extension interface → infix functions become available in that scope.

```kotlin
class MyRepository : QuerydslRepository<MyEntity>() {
    // All 7 extension interfaces available via infix
    fun search(name: String?, status: String?) =
        selectFrom(entity)
            .where(entity.name contains name, entity.status eq status)
            .page(pageable)
}
```

## Null-Safety Rules

| Function type | this null | arg null | both null |
|--------------|-----------|----------|-----------|
| `and` / `or` | returns arg | returns this | null |
| `between(Pair)` | null | one-sided (goe/loe) | null |
| All others (`eq`, `gt`, `contains`, ...) | null | null | null |

Key: `and`/`or` preserve the non-null side. `between` degrades to one-sided comparison. Everything else skips on any null.

## 7 Extension Interfaces

### BooleanExpressionExtensions
```kotlin
condition1 and condition2       // AND, null side ignored
condition1 or condition2        // OR, null side ignored
entity.active eq true           // active = true
```

### SimpleExpressionExtensions
```kotlin
entity.status eq "ACTIVE"              // status = 'ACTIVE'
entity.status ne "DELETED"             // status != 'DELETED'
entity.status `in` listOf("A", "B")   // status IN ('A', 'B')
entity.status notIn listOf("C")       // status NOT IN ('C')
```

### ComparableExpressionExtensions
```kotlin
entity.date gt startDate               // date > ?
entity.date goe startDate              // date >= ?
entity.date lt endDate                 // date < ?
entity.date loe endDate                // date <= ?
entity.date between (from to to)       // BETWEEN (Pair, one-sided OK)
entity.age between (20..60)            // BETWEEN (ClosedRange)
```

### NumberExpressionExtensions
Same API as Comparable, separate interface because `NumberExpression` does not extend `ComparableExpression`.

### StringExpressionExtensions
```kotlin
entity.name contains keyword           // LIKE '%keyword%'
entity.name containsIgnoreCase keyword // case-insensitive
entity.name startsWith prefix         // LIKE 'prefix%'
entity.name endsWith suffix           // LIKE '%suffix'
entity.name like pattern              // LIKE pattern
entity.name matches regex             // REGEXP
entity.name equalsIgnoreCase value    // case-insensitive =
```

### TemporalExpressionExtensions
```kotlin
entity.createdAt after startDate      // created_at > ?
entity.createdAt before endDate       // created_at < ?
```

### CollectionExpressionExtensions
```kotlin
entity.roles contains "ADMIN"         // 'ADMIN' IN (roles)
```

## QuerydslRepository Helpers

### Pagination
```kotlin
query.page(pageable)                   // Page with auto count
query.page(pageable) { countQuery() }  // Page with custom count
query.slice(pageable)                  // Slice with accurate hasNext
query.page(page = 0, size = 20)        // value-based overloads
query.fetch(offset = 0, limit = 20)    // raw fetch
```

### Sorting
```kotlin
query.applySort(sort) { entity.id.desc() }  // with fallback
query.fetchSorted(sort)                      // sort + fetch
```

### Bulk DML
```kotlin
modifying {
    update(entity).set(entity.active, false).where(...).execute()
}
// flush before + clear after (default: both true)
// Same flags as @Modifying: flushAutomatically, clearAutomatically
```

## Common Mistakes

1. **Don't use `BooleanBuilder`** — use `and`/`or` infix chaining instead
2. **`in` is a Kotlin keyword** — use backticks: `` entity.status `in` list ``
3. **`between` with one null** — degrades to `>=` or `<=`, doesn't skip entirely
4. **`NumberExpression` needs separate interface** — `ComparableExpressionExtensions` won't work for numbers
5. **`modifying` defaults to flush+clear** — unlike `@Modifying` which defaults to both false
