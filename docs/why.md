# Why querydsl-ktx?

## The Problem

QueryDSL dynamic queries require repetitive null checks:

```kotlin
fun search(
    name: String?,
    status: String?,
    minAge: Int?,
    maxAge: Int?,
    startDate: LocalDate?,
    endDate: LocalDate?,
): List<Member> {
    val builder = BooleanBuilder()
    if (name != null) builder.and(member.name.contains(name))
    if (status != null) builder.and(member.status.eq(status))
    if (minAge != null && maxAge != null) builder.and(member.age.between(minAge, maxAge))
    else if (minAge != null) builder.and(member.age.goe(minAge))
    else if (maxAge != null) builder.and(member.age.loe(maxAge))
    if (startDate != null && endDate != null) builder.and(member.createdAt.between(startDate, endDate))
    else if (startDate != null) builder.and(member.createdAt.goe(startDate))
    else if (endDate != null) builder.and(member.createdAt.loe(endDate))
    return queryFactory.selectFrom(member).where(builder).fetch()
}
```

Every optional filter adds 1-3 lines of boilerplate. Range filters (`between`) need 3 branches each. The pattern is always the same -- but you write it every time.

---

## Alternatives Comparison

| Approach | Pros | Cons |
|----------|------|------|
| `BooleanBuilder` | Built-in, no dependency | Verbose, error-prone with ranges |
| `BooleanExpression` chaining | Slightly cleaner | Still manual null checks everywhere |
| Spring Data `Specification` | Type-safe | Separate from QueryDSL, no infix syntax |
| Top-level Kotlin extensions | Concise | Pollutes global scope; `eq`, `contains` clash with stdlib |
| Hand-rolled helper functions | Familiar, customizable | Duplicated across projects; partial coverage |
| **querydsl-ktx** | Null-safe, concise, scoped, tested | Additional dependency |

---

## Interface-Based Scoping

Extensions are delivered as **interfaces**, not top-level functions.

=== "Top-level extensions (problematic)"

    ```kotlin
    // Every file in your project sees eq(), contains(), and()
    // Name clashes with kotlin.collections.contains, etc.

    fun StringExpression.contains(value: String?) = ...  // Whose contains?
    fun SimpleExpression<T>.eq(value: T?) = ...          // Whose eq?
    ```

=== "querydsl-ktx (scoped)"

    ```kotlin
    // Extensions only available inside implementing classes
    class MemberRepository : QuerydslRepository<Member>() {
        // eq, contains, between are in scope here
    }

    class OrderService {
        // eq, contains, between are NOT in scope here
    }
    ```

This means:

- **No global namespace pollution** -- other libraries' `eq`, `contains` are unaffected
- **IDE autocomplete stays clean** -- only relevant suggestions appear
- **Pick only what you need** -- implement `StringExpressionExtensions` alone if that's all you use

---

## Hand-Rolled Helper Functions

A common pattern in many projects:

```kotlin
@Repository
class MemberRepository(private val queryFactory: JPAQueryFactory) {

    private fun eqStatus(status: String?): BooleanExpression? =
        status?.let { member.status.eq(it) }

    private fun containsName(name: String?): BooleanExpression? =
        name?.let { member.name.contains(it) }

    private fun betweenAge(min: Int?, max: Int?): BooleanExpression? = when {
        min != null && max != null -> member.age.between(min, max)
        min != null -> member.age.goe(min)
        max != null -> member.age.loe(max)
        else -> null
    }

    fun search(name: String?, status: String?, minAge: Int?, maxAge: Int?): List<Member> =
        queryFactory.selectFrom(member)
            .where(
                containsName(name),
                eqStatus(status),
                betweenAge(minAge, maxAge),
            )
            .fetch()
}
```

This works, but:

- **Duplicated per entity** -- `eqStatus` for `Member`, `eqStatus` for `Order`, `eqStatus` for `Product`...
- **Partial coverage** -- most projects only write `eq` and `contains`, missing `between` one-sided ranges, `andAnyOf`, `orAllOf`
- **No standard** -- every developer writes their own slightly different version
- **No tests** -- who tests their private helper functions?

With querydsl-ktx, the same query becomes:

```kotlin
@Repository
class MemberRepository : QuerydslRepository<Member>() {

    private val member = QMember.member

    fun search(name: String?, status: String?, minAge: Int?, maxAge: Int?): List<Member> =
        selectFrom(member)
            .where(
                member.name contains name,
                member.status eq status,
                member.age between (minAge to maxAge),
            )
            .fetch()
}
```

No private helpers. No duplication. 214 tests covering every null combination.

---

## What querydsl-ktx Covers

Beyond basic `eq` and `contains`, querydsl-ktx provides:

| Feature | Example | What it replaces |
|---------|---------|-----------------|
| One-sided `between` | `entity.date between (from to null)` → `date >= from` | 3-branch `if/else` |
| `andAnyOf` | `base andAnyOf listOf(condA, condB)` → `base AND (A OR B)` | Manual OR reduction |
| `orAllOf` | `base orAllOf listOf(condA, condB)` → `base OR (A AND B)` | Manual AND reduction |
| `modifying { }` | Auto flush + clear for bulk DML | `entityManager.flush()` + `clear()` you keep forgetting |
| `page()` / `slice()` | One-call pagination with count query | Manual offset/limit + `PageImpl` construction |
| `nullif` / `coalesce` | SQL functions, null-safe | Null checks around `nullif()`/`coalesce()` |

All of these follow the same null-safety contract: null arguments are skipped, never cause errors.
