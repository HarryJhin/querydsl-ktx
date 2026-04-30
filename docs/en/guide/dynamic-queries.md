---
description: Build dynamic WHERE clauses with null-safe infix operators. No BooleanBuilder or if-checks needed.
---

# Dynamic Queries

## The Evolution of QueryDSL Dynamic Queries

If you've worked with QueryDSL in Kotlin, you've probably gone through this progression:

**Stage 1: BooleanBuilder**: the first thing most people learn.

```kotlin
val builder = BooleanBuilder()
if (name != null) builder.and(member.name.contains(name))
if (status != null) builder.and(member.status.eq(status))
```

**Stage 2: Per-field helper functions**: returning `BooleanExpression?` for each condition.
This is the pattern popularized by many blog posts and tutorials:

```kotlin
fun statusEq(status: String?): BooleanExpression? =
    status?.let { member.status.eq(it) }

fun nameLike(name: String?): BooleanExpression? =
    name?.let { member.name.contains(it) }

// Usage
selectFrom(member)
    .where(statusEq(status), nameLike(name))
    .fetch()
```

**Stage 3: querydsl-ktx**: the same null-safe behavior, but without writing a helper function per field.

```kotlin
selectFrom(member)
    .where(
        member.status eq status,
        member.name contains name,
    )
    .fetch()
```

All three approaches produce the same SQL. The difference is boilerplate.

---

## Core Concept: null = skip

Every extension function in querydsl-ktx follows one rule:

> **If the argument is null, the condition is not applied.**

This eliminates the `BooleanBuilder` + `if-null-check` pattern entirely.

::: code-group

```kotlin [Before]
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

```kotlin [After]
var where: BooleanExpression? = null
where = where and (entity.name contains name)
where = where and (entity.status eq status)
where = where and (entity.createdAt between (from to to))
```

:::

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

::: info How `.where()` handles nulls
QueryDSL's `.where()` already ignores null predicates in its vararg overload.
Combined with querydsl-ktx's null-returning extensions, null parameters are
transparently filtered at every level.
:::

---

## Comparing with Hand-Written Helpers

You've probably written (or seen) per-field helper functions like these:

::: code-group

```kotlin [Per-field helpers (hand-written)]
private fun statusEq(status: String?): BooleanExpression? =
    status?.let { member.status.eq(it) }

private fun nameLike(name: String?): BooleanExpression? =
    name?.let { member.name.contains(it) }

private fun ageBetween(min: Int?, max: Int?): BooleanExpression? {
    if (min != null && max != null) return member.age.between(min, max)
    if (min != null) return member.age.goe(min)
    if (max != null) return member.age.loe(max)
    return null
}

fun search(status: String?, name: String?, minAge: Int?, maxAge: Int?) =
    selectFrom(member)
        .where(statusEq(status), nameLike(name), ageBetween(minAge, maxAge))
        .fetch()
```

```kotlin [querydsl-ktx (no helpers needed)]
fun search(status: String?, name: String?, minAge: Int?, maxAge: Int?) =
    selectFrom(member)
        .where(
            member.status eq status,
            member.name contains name,
            member.age between (minAge to maxAge),
        )
        .fetch()
```

:::

The querydsl-ktx version does exactly the same thing: `member.status eq null` returns `null`,
which `.where()` ignores. The `between` with a `Pair` handles all four combinations
(both, min-only, max-only, neither) in a single expression.

::: tip When to still extract helpers
querydsl-ktx doesn't mean you should never extract methods. Complex conditions
that combine multiple fields or contain business logic are still better as named methods:

```kotlin
// This is still cleaner as a named method
private fun isEligibleForPromotion(): BooleanExpression? =
    (member.grade eq "VIP") or (
        (member.totalPurchase goe 100000) and (member.active eq true)
    )
```

The rule of thumb: if a condition maps 1:1 to a field, use the inline extension.
If it encodes business logic, extract it.
:::

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

::: code-group

```kotlin [Kotlin]
selectFrom(entity)
    .where(
        entity.active eq true,
        (entity.role eq "ADMIN") or (entity.role eq "MANAGER"),
    )
    .fetch()
```

```sql [SQL]
SELECT e.*
FROM entity e
WHERE e.active = true
  AND (e.role = 'ADMIN' OR e.role = 'MANAGER')
```

:::

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

::: warning Null expression vs null argument
`this` being null means the QueryDSL expression itself is null (rare in practice).
`arg` being null means the filter parameter was not provided, which is the common case.
:::

---

## Complex Conditions

### andAnyOf: AND with OR group

"Base condition AND (any of these)":

::: code-group

```kotlin [Kotlin]
val predicate = (entity.active eq true) andAnyOf listOf(
    entity.role eq role,
    entity.department eq department,
)
```

```sql [SQL]
active = true AND (role = ? OR department = ?)
```

:::

If both `role` and `department` are null, the OR group collapses to null and only the base condition remains.

### orAllOf: OR with AND group

"Base condition OR (all of these)":

::: code-group

```kotlin [Kotlin]
val predicate = (entity.vip eq true) orAllOf listOf(
    entity.age goe minAge,
    entity.active eq true,
)
```

```sql [SQL]
vip = true OR (age >= ? AND active = true)
```

:::

### vararg overloads

`andAnyOf` and `orAllOf` also accept a vararg of `BooleanExpression?` for terser
inline use without `listOf(...)`:

```kotlin
predicate.andAnyOf(
    entity.role eq role,
    entity.department eq department,
)
```

Identical null-skip semantics: each null predicate is dropped before the OR/AND
group is built.

---

## Subquery Comparisons

`SimpleExpression` supports null-safe comparison with subqueries built via
`JPAExpressions`. Pass `null` to skip the predicate entirely.

::: code-group

```kotlin [Kotlin]
import com.querydsl.jpa.JPAExpressions

fun search(matchMaxPrice: Boolean): List<Product> {
    val maxPrice = if (matchMaxPrice)
        JPAExpressions.select(product.price.max()).from(product)
    else null
    return selectFrom(product)
        .where(product.price eq maxPrice)  // null subquery -> skipped
        .fetch()
}
```

```sql [SQL]
-- when maxPrice subquery is provided
SELECT * FROM product WHERE price = (SELECT MAX(price) FROM product)

-- when maxPrice subquery is null
SELECT * FROM product
```

:::

`eq`, `in`, `notIn` accept `SubQueryExpression<T>?`. The same null-skip rule
applies as with value/expression overloads.

## ALL / ANY Comparisons

For comparing against multiple subquery rows or collection elements, use
`*All` / `*Any` variants:

::: code-group

```kotlin [Kotlin]
// Greater than ALL prices in a category: strictly more expensive than every item
val categoryPrices = JPAExpressions.select(product.price).from(product)
    .where(product.category.eq(targetCategory))
selectFrom(product).where(product.price gtAll categoryPrices).fetch()

// Equal to ANY of the cheap-category prices: match any cheap item's price
val cheapPrices = JPAExpressions.select(product.price).from(product)
    .where(product.price.lt(threshold))
selectFrom(product).where(product.price eqAny cheapPrices).fetch()
```

```sql [SQL]
SELECT * FROM product WHERE price > ALL (SELECT price FROM product WHERE category = ?)
SELECT * FROM product WHERE price = ANY (SELECT price FROM product WHERE price < ?)
```

:::

Available variants: `eqAll`/`eqAny`, `neAll`/`neAny` (Collection only),
`gtAll`/`gtAny`, `goeAll`/`goeAny`, `ltAll`/`ltAny`, `loeAll`/`loeAny`. See the
[All/Any asymmetry table](./extensions.md#numberexpressionextensions) for the
exact coverage on `NumberExpression`.

## LIKE with ESCAPE

For patterns that need to match literal `%` or `_` characters, chain `escape`
on a `like` / `notLike` / `likeIgnoreCase` result:

```kotlin
entity.name like "10\\%off" escape '\\'  // matches the literal "10%off"
```

The escape character is preserved through `notLike` and `likeIgnoreCase` chains.
Invalid receivers (e.g. `escape` on a non-LIKE expression) throw
`com.querydsl.core.types.ExpressionException`.

## Arithmetic in Computed Columns

`NumberExpression` exposes both null-safe infix arithmetic
(`add`/`subtract`/`multiply`/`divide`/`mod`) and Kotlin operators (`+`, `-`,
`*`, `/`, `%`, unary `-`). The infix forms skip when either side is null;
operators have a non-null contract for use in projections and `orderBy`:

```kotlin
// Sort by a computed column without intermediate variables
selectFrom(entity)
    .orderBy((entity.price + entity.tax).desc())
    .fetch()

// Null-safe in dynamic WHERE
where(entity.discount add bonus gt 0)  // skipped if `bonus` is null
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

    // Null-safe conditions: no if-check needed
    where = where and (entity.status eq criteria.status)
    where = where and (entity.createdAt between (criteria.from to criteria.to))

    return selectFrom(entity).where(where).fetch()
}
```

::: tip When to use `if` vs null-safety
- **Simple null check**: let the extension handle it. `entity.status eq criteria.status` is enough.
- **Complex logic** (e.g., keyword search across multiple fields): use an explicit `if` block to build the sub-expression, then combine with `and`.
:::

---

## Real-World Example: Admin Search Page

A typical admin search with multiple optional filters:

```kotlin
@Repository
class OrderRepository : QuerydslRepository<Order>() {

    private val order = QOrder.order
    private val member = QMember.member

    fun adminSearch(
        orderNumber: String?,
        memberName: String?,
        status: OrderStatus?,
        minAmount: Int?,
        maxAmount: Int?,
        from: LocalDateTime?,
        to: LocalDateTime?,
        pageable: Pageable,
    ): Page<Order> =
        selectFrom(order)
            .join(order.member, member)
            .where(
                order.orderNumber contains orderNumber,
                member.name contains memberName,
                order.status eq status,
                order.totalAmount between (minAmount to maxAmount),
                order.createdAt between (from to to),
            )
            .page(pageable)
}
```

Every parameter is nullable. If the admin fills in only "member name" and "status",
only those two conditions appear in the SQL. No BooleanBuilder, no helper functions,
no branching logic.

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
