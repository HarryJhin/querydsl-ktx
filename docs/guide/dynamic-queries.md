# Dynamic Queries

## Core Concept: null = skip

Every extension function in querydsl-ktx follows one rule:

> **If the argument is null, the condition is not applied.**

This eliminates the `BooleanBuilder` + `if-null-check` pattern entirely.

=== "Before"

    ```kotlin
    val builder = BooleanBuilder()
    if (name != null) builder.and(entity.name.contains(name))
    if (status != null) builder.and(entity.status.eq(status))
    if (from != null && to != null) {
        builder.and(entity.createdAt.between(from, to))
    } else if (from != null) {
        builder.and(entity.createdAt.goe(from))
    } else if (to != null) {
        builder.and(entity.createdAt.loe(to))
    }
    ```

=== "After"

    ```kotlin
    var where: BooleanExpression? = null
    where = where and (entity.name contains name)
    where = where and (entity.status eq status)
    where = where and (entity.createdAt between (from to to))
    ```

Or even shorter inside a `QuerydslRepository`:

```kotlin
selectFrom(entity)
    .where(
        entity.name contains name,
        entity.status eq status,
        entity.createdAt between (from to to),
    )
    .fetch()
```

!!! note "How `.where()` handles nulls"
    QueryDSL's `.where()` already ignores null predicates in its vararg overload.
    Combined with querydsl-ktx's null-returning extensions, null parameters are
    transparently filtered at every level.

---

## AND / OR Chaining

### Basic AND

Use the `and` infix function to combine nullable conditions:

```kotlin
var predicate: BooleanExpression? = null
predicate = predicate and (entity.name eq name)
predicate = predicate and (entity.active eq true)
```

| `this` | `right` | Result |
|--------|---------|--------|
| non-null | non-null | `this AND right` |
| null | non-null | `right` |
| non-null | null | `this` |
| null | null | `null` |

### Basic OR

Works symmetrically:

```kotlin
var predicate: BooleanExpression? = null
predicate = predicate or (entity.role eq "ADMIN")
predicate = predicate or (entity.role eq "MANAGER")
```

| `this` | `right` | Result |
|--------|---------|--------|
| non-null | non-null | `this OR right` |
| null | non-null | `right` |
| non-null | null | `this` |
| null | null | `null` |

### Combining AND and OR

```kotlin
val rolePredicate = (entity.role eq "ADMIN") or (entity.role eq "MANAGER")
val predicate = (entity.active eq true) and rolePredicate
```

=== "Kotlin"

    ```kotlin
    selectFrom(entity)
        .where(
            entity.active eq true,
            (entity.role eq "ADMIN") or (entity.role eq "MANAGER"),
        )
        .fetch()
    ```

=== "SQL"

    ```sql
    SELECT e.*
    FROM entity e
    WHERE e.active = true
      AND (e.role = 'ADMIN' OR e.role = 'MANAGER')
    ```

---

## Null-Safety Contract

All extension functions follow consistent null behavior:

### Combinators (and / or)

| `this` | `arg` | Both null | Result |
|--------|-------|-----------|--------|
| returns arg | returns this | `null` | Preserves the non-null side |

### Between (Pair overload)

| `this` | `from` | `to` | Result |
|--------|--------|------|--------|
| non-null | non-null | non-null | `BETWEEN from AND to` |
| non-null | non-null | null | `>= from` |
| non-null | null | non-null | `<= to` |
| non-null | null | null | `null` (skipped) |
| null | any | any | `null` (skipped) |

### All other functions (eq, gt, contains, ...)

| `this` null | `arg` null | Both null | Result |
|-------------|------------|-----------|--------|
| `null` | `null` | `null` | `null` (skipped) |

!!! warning "Null expression vs null argument"
    `this` being null means the QueryDSL expression itself is null (rare in practice).
    `arg` being null means the filter parameter was not provided -- the common case.

---

## Complex Conditions

### andAnyOf -- AND with OR group

"Base condition AND (any of these)":

=== "Kotlin"

    ```kotlin
    val predicate = (entity.active eq true) andAnyOf listOf(
        entity.role eq role,
        entity.department eq department,
    )
    ```

=== "SQL"

    ```sql
    active = true AND (role = ? OR department = ?)
    ```

If both `role` and `department` are null, the OR group collapses to null and only the base condition remains.

### orAllOf -- OR with AND group

"Base condition OR (all of these)":

=== "Kotlin"

    ```kotlin
    val predicate = (entity.vip eq true) orAllOf listOf(
        entity.age goe minAge,
        entity.active eq true,
    )
    ```

=== "SQL"

    ```sql
    vip = true OR (age >= ? AND active = true)
    ```

---

## Building Conditions Incrementally

For cases where conditions depend on runtime logic:

```kotlin
fun search(criteria: SearchCriteria): List<Entity> {
    var where: BooleanExpression? = null

    // Always-on condition
    where = where and (entity.deleted eq false)

    // Conditional group
    if (criteria.hasKeyword()) {
        where = where and (
            (entity.name contains criteria.keyword)
            or (entity.description contains criteria.keyword)
        )
    }

    // Null-safe conditions -- no if-check needed
    where = where and (entity.status eq criteria.status)
    where = where and (entity.createdAt between (criteria.from to criteria.to))

    return selectFrom(entity).where(where).fetch()
}
```

!!! tip "When to use `if` vs null-safety"
    - **Simple null check** -- Let the extension handle it. `entity.status eq criteria.status` is enough.
    - **Complex logic** (e.g., keyword search across multiple fields) -- Use an explicit `if` block to build the sub-expression, then combine with `and`.

---

## Using Extensions Without QuerydslRepository

You can use the extension interfaces independently by implementing them on any class:

```kotlin
class MyService : BooleanExpressionExtensions, SimpleExpressionExtensions {

    fun buildPredicate(name: String?, status: String?): BooleanExpression? {
        var where: BooleanExpression? = null
        where = where and (entity.name eq name)
        where = where and (entity.status eq status)
        return where
    }
}
```

Or implement a subset on a repository that extends `QuerydslSupport`:

```kotlin
@Repository
class MinimalRepository : QuerydslSupport<MyEntity>(),
    SimpleExpressionExtensions,
    StringExpressionExtensions {

    override val domainClass = MyEntity::class.java

    // Only eq, ne, in, notIn, contains, startsWith, etc. are in scope
}
```
