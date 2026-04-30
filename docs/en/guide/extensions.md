---
description: Reference for all 8 null-safe extension interfaces including eq, contains, between, exists, and more.
---

# Extension Interfaces

querydsl-ktx provides **8 extension interfaces**, each scoped to a specific QueryDSL expression type.
All functions are null-safe: null arguments cause the condition to be skipped.

---

## Overview

| Interface | Expression Type | Key Functions |
|-----------|----------------|---------------|
| [BooleanExpressionExtensions](#booleanexpressionextensions) | `BooleanExpression` | `and`, `or`, `andAnyOf`, `orAllOf`, `eq`, `nullif`, `coalesce` |
| [SimpleExpressionExtensions](#simpleexpressionextensions) | `SimpleExpression<T>` | `eq`, `ne`, `in`, `notIn` |
| [ComparableExpressionExtensions](#comparableexpressionextensions) | `ComparableExpression<T>` | `gt`, `goe`, `lt`, `loe`, `between`, `nullif`, `coalesce`, `rangeTo` |
| [NumberExpressionExtensions](#numberexpressionextensions) | `NumberExpression<T>` | `gt`, `goe`, `lt`, `loe`, `between`, `nullif`, `coalesce`, `rangeTo` |
| [StringExpressionExtensions](#stringexpressionextensions) | `StringExpression` | `contains`, `startsWith`, `endsWith`, `like`, `matches`, `nullif`, `coalesce` |
| [TemporalExpressionExtensions](#temporalexpressionextensions) | `TemporalExpression<T>` | `after`, `before` |
| [CollectionExpressionExtensions](#collectionexpressionextensions) | `CollectionExpressionBase<T, E>` | `contains` |
| [SubQueryExtensions](#subqueryextensions) | `EntityPath<T>` | `exists`, `notExists` |

---

## BooleanExpressionExtensions

Null-safe AND/OR combinators. The foundation for building dynamic WHERE clauses.

### Functions

| Function | Signature | SQL |
|----------|-----------|-----|
| `and` | `BooleanExpression?.and(BooleanExpression?)` | `a AND b` |
| `or` | `BooleanExpression?.or(BooleanExpression?)` | `a OR b` |
| `andAnyOf` | `BooleanExpression?.andAnyOf(List<BooleanExpression?>)` | `a AND (b OR c OR ...)` |
| `orAllOf` | `BooleanExpression?.orAllOf(List<BooleanExpression?>)` | `a OR (b AND c AND ...)` |
| `eq` | `BooleanExpression?.eq(Boolean?)` | `active = true` |
| `nullif` | `BooleanExpression?.nullif(Boolean?)` | `NULLIF(active, true)` |
| `coalesce` | `BooleanExpression?.coalesce(Boolean?)` | `COALESCE(active, false)` |

### Examples

::: code-group

```kotlin [Kotlin]
// AND: null side is ignored
val predicate = (entity.active eq true) and (entity.name eq name)

// OR group
val rolePredicate = (entity.role eq "ADMIN") or (entity.role eq "MANAGER")

// AND with OR subgroup
val complex = (entity.active eq true) andAnyOf listOf(
    entity.role eq role,
    entity.department eq department,
)
```

```sql [SQL]
-- AND (name = 'John')
active = true AND name = 'John'

-- AND (name = null) -> only left side
active = true

-- OR group
role = 'ADMIN' OR role = 'MANAGER'

-- AND with OR subgroup
active = true AND (role = ? OR department = ?)
```

:::

::: tip Vararg overload <Badge type="tip" text="v1.2.0+" />
`andAnyOf` and `orAllOf` also accept a vararg of `BooleanExpression?`.
Call with dot notation since Kotlin disallows vararg on infix functions:
`predicate.andAnyOf(p1, p2, p3)`.
:::

---

## SimpleExpressionExtensions

Equality and membership operators for any expression type.

### Functions

| Function | Signature | SQL |
|----------|-----------|-----|
| `eq` | `SimpleExpression<T>?.eq(T?)` | `status = ?` |
| `eq` | `SimpleExpression<T>?.eq(Expression<in T>?)` | `status = default_status` |
| `eq` | `SimpleExpression<T>?.eq(SubQueryExpression<T>?)` | `price = (SELECT ...)` |
| `ne` | `SimpleExpression<T>?.ne(T?)` | `status != ?` |
| `ne` | `SimpleExpression<T>?.ne(Expression<in T>?)` | `status != default_status` |
| `in` | `SimpleExpression<T>?.in(Collection<T>?)` | `status IN (?, ?)` |
| `in` | `SimpleExpression<T>?.in(SubQueryExpression<T>?)` | `id IN (SELECT ...)` |
| `notIn` | `SimpleExpression<T>?.notIn(Collection<T>?)` | `status NOT IN (?, ?)` |
| `notIn` | `SimpleExpression<T>?.notIn(SubQueryExpression<T>?)` | `id NOT IN (SELECT ...)` |
| `inChunked` | `SimpleExpression<T>?.inChunked(Collection<T>?)` | `col IN (?) OR col IN (?)` |
| `eqAll` | `SimpleExpression<T>?.eqAll(CollectionExpression<*, in T>?)` / `eqAll(SubQueryExpression<T>?)` | `col = ALL (...)` |
| `eqAny` | `SimpleExpression<T>?.eqAny(CollectionExpression<*, in T>?)` / `eqAny(SubQueryExpression<T>?)` | `col = ANY (...)` |
| `neAll` | `SimpleExpression<T>?.neAll(CollectionExpression<*, in T>?)` | `col <> ALL (...)` |
| `neAny` | `SimpleExpression<T>?.neAny(CollectionExpression<*, in T>?)` | `col <> ANY (...)` |

### Examples

::: code-group

```kotlin [Kotlin]
// Equality
entity.status eq "ACTIVE"              // status = 'ACTIVE'
entity.status eq null                  // null (skipped)

// Not equal
entity.status ne "DELETED"             // status != 'DELETED'

// IN / NOT IN
entity.status `in` listOf("A", "B")   // status IN ('A', 'B')
entity.status notIn listOf("C")       // status NOT IN ('C')
entity.status `in` null               // null (skipped)

// Large IN clause auto-split (default 1000 per chunk)
entity.id inChunked largeIdList   // id IN (1..1000) OR id IN (1001..2000)
entity.id.inChunked(ids, 500)     // custom chunk size
```

```sql [SQL]
status = 'ACTIVE'
status != 'DELETED'
status IN ('A', 'B')
status NOT IN ('C')
```

:::

::: tip inChunked for Oracle
Oracle has a hard limit of 1000 items in a single IN clause.
`inChunked` automatically splits large collections into multiple IN clauses
joined with OR. The default chunk size is 1000, but you can customize it.
:::

::: tip Subquery comparisons <Badge type="tip" text="v1.2.0+" />
`eq`, `in`, and `notIn` also accept a `SubQueryExpression<T>?` for null-safe
comparison against subqueries built with `JPAExpressions`.

```kotlin
import com.querydsl.jpa.JPAExpressions

// price equals the max price across all products
val maxPriceSubQuery = JPAExpressions.select(product.price.max()).from(product)
selectFrom(product).where(product.price eq maxPriceSubQuery).fetch()

// category in subquery result
val cheapCategoriesSubQuery = JPAExpressions
    .selectDistinct(product.category)
    .from(product)
    .where(product.price.lt(10000))
selectFrom(product).where(product.category `in` cheapCategoriesSubQuery).fetch()
```

When the subquery argument is null, the predicate is skipped (returns null) so
optional subquery filters can be plugged in without conditional builder code.
:::

---

## ComparableExpressionExtensions

Comparison and range operators for `Comparable` types (dates, strings, enums, etc.).

### Functions

| Function | Signature | SQL |
|----------|-----------|-----|
| `gt` | `ComparableExpression<T>?.gt(T?)` | `col > ?` |
| `goe` | `ComparableExpression<T>?.goe(T?)` | `col >= ?` |
| `lt` | `ComparableExpression<T>?.lt(T?)` | `col < ?` |
| `loe` | `ComparableExpression<T>?.loe(T?)` | `col <= ?` |
| `between` | `ComparableExpression<T>?.between(Pair<T?, T?>)` | `col BETWEEN ? AND ?` |
| `between` | `ComparableExpression<T>?.between(ClosedRange<T>)` | `col BETWEEN ? AND ?` |
| `notBetween` | `ComparableExpression<T>?.notBetween(Pair<T?, T?>)` | `col NOT BETWEEN ? AND ?` |
| `between` (reverse) | `T?.between(Pair<ComparableExpression<T>?, ComparableExpression<T>?>)` | `lower <= ? AND upper >= ?` |
| `nullif` | `ComparableExpression<T>?.nullif(T?)` | `NULLIF(col, ?)` |
| `coalesce` | `ComparableExpression<T>?.coalesce(T?)` | `COALESCE(col, ?)` |
| `rangeTo` | `ComparableExpression<T>..ComparableExpression<T>` | _(creates Pair for between)_ |
| `gtAll` | `ComparableExpression<T>?.gtAll(CollectionExpression<*, in T>?)` / `gtAll(SubQueryExpression<T>?)` | `col > ALL (...)` |
| `gtAny` | `ComparableExpression<T>?.gtAny(CollectionExpression<*, in T>?)` / `gtAny(SubQueryExpression<T>?)` | `col > ANY (...)` |
| `goeAll` | `ComparableExpression<T>?.goeAll(CollectionExpression<*, in T>?)` / `goeAll(SubQueryExpression<T>?)` | `col >= ALL (...)` |
| `goeAny` | `ComparableExpression<T>?.goeAny(CollectionExpression<*, in T>?)` / `goeAny(SubQueryExpression<T>?)` | `col >= ANY (...)` |
| `ltAll` | `ComparableExpression<T>?.ltAll(CollectionExpression<*, in T>?)` / `ltAll(SubQueryExpression<T>?)` | `col < ALL (...)` |
| `ltAny` | `ComparableExpression<T>?.ltAny(CollectionExpression<*, in T>?)` / `ltAny(SubQueryExpression<T>?)` | `col < ANY (...)` |
| `loeAll` | `ComparableExpression<T>?.loeAll(CollectionExpression<*, in T>?)` / `loeAll(SubQueryExpression<T>?)` | `col <= ALL (...)` |
| `loeAny` | `ComparableExpression<T>?.loeAny(CollectionExpression<*, in T>?)` / `loeAny(SubQueryExpression<T>?)` | `col <= ANY (...)` |

All comparison functions also have `Expression<T>` overloads for comparing against other columns.

### Examples

::: code-group

```kotlin [Kotlin]
// Simple comparison
entity.date gt startDate       // date > ?
entity.date goe startDate      // date >= ?
entity.date lt endDate         // date < ?
entity.date loe endDate        // date <= ?

// BETWEEN with Pair: partial range support
entity.date between (from to to)       // BETWEEN ? AND ?
entity.date between (from to null)     // date >= ?
entity.date between (null to to)       // date <= ?
entity.date between (null to null)     // null (skipped)

// BETWEEN with ClosedRange
entity.age between (20..60)            // BETWEEN 20 AND 60

// Reverse BETWEEN: value on left, expression bounds on right
now between (sale.startAt to sale.endAt)
// -> start_at <= now AND end_at >= now

// rangeTo operator (..): syntactic sugar for creating Pair
entity.date between (entity.startDate..entity.endDate)
// equivalent to: entity.date between (entity.startDate to entity.endDate)
```

```sql [SQL]
-- Full range
created_at BETWEEN '2024-01-01' AND '2024-12-31'

-- One-sided (from only)
created_at >= '2024-01-01'

-- One-sided (to only)
created_at <= '2024-12-31'

-- ClosedRange
age BETWEEN 20 AND 60
```

:::

::: tip Pair-based between for optional date ranges
The `Pair` overload is the most powerful feature for date range filters.
A single expression handles all four combinations (both, from-only, to-only, neither)
that would otherwise require a 4-branch `if/else`.
:::

### Reverse Between: Real-World Use Cases

The reverse `between` puts a **value on the left** and **column bounds on the right**.
This pattern is surprisingly common:

**Checking if a date falls within an active period:**

```kotlin
// Is the coupon valid right now?
val now = LocalDateTime.now()
selectFrom(coupon)
    .where(now between (coupon.validFrom to coupon.validUntil))
    .fetch()
// SQL: valid_from <= '2025-04-10T12:00' AND valid_until >= '2025-04-10T12:00'
```

**Checking if a price falls within a discount range:**

```kotlin
// Which discount tier applies to this order amount?
val orderAmount = 50000
selectFrom(discountTier)
    .where(orderAmount between (discountTier.minAmount to discountTier.maxAmount))
    .fetch()
// SQL: min_amount <= 50000 AND max_amount >= 50000
```

**Checking if a point is within geo bounds:**

```kotlin
// Simplified bounding box check
selectFrom(store)
    .where(
        userLat between (store.southLat to store.northLat),
        userLng between (store.westLng to store.eastLng),
    )
    .fetch()
```

The reverse between is also null-safe: if the value is null, the entire expression returns null (skipped).

---

## NumberExpressionExtensions

Same API as `ComparableExpressionExtensions`, but for `NumberExpression`.

::: tip Arithmetic operators <Badge type="tip" text="v1.2.0+" />
`add`, `subtract`, `multiply`, `divide`, `mod` are also available with the same
null-safety contract: either side null returns null (the whole arithmetic
expression is skipped). Each has a value overload and an `Expression<T>` overload.

```kotlin
entity.price add 1000             // price + 1000
entity.price multiply taxRate     // price * tax_rate (column ref)
entity.total divide quantity      // total / quantity
```
:::

::: tip Kotlin operator overloads <Badge type="tip" text="v1.2.0+" />
Kotlin arithmetic operators (`+`, `-`, `*`, `/`, `%`, unary `-`) are also available
for expression building (sort, projection, computed columns). These have a
**non-null contract** (both sides required), distinct from the null-skip infix
forms above.

```kotlin
entity.price + 1000               // price + 1000
entity.price * taxRate            // price * tax_rate (column ref)
-entity.price                     // -price
(entity.price + entity.tax) * 2   // composes with parentheses
```

Use `+` / `-` / `*` / `/` / `%` when building expressions where both operands
are guaranteed present. Use `add` / `subtract` / etc. when either side may be
null and you want the whole expression skipped.
:::

### Why a separate interface?

In QueryDSL's type hierarchy, `NumberExpression` does **not** extend `ComparableExpression`.
This is a QueryDSL design decision: `NumberExpression` inherits from `ComparableExpressionBase`
while `ComparableExpression` is a separate branch. So querydsl-ktx provides a parallel set of
operators specifically typed for `NumberExpression`.

### Functions

| Function | Signature | SQL |
|----------|-----------|-----|
| `gt` | `NumberExpression<T>?.gt(T?)` | `col > ?` |
| `goe` | `NumberExpression<T>?.goe(T?)` | `col >= ?` |
| `lt` | `NumberExpression<T>?.lt(T?)` | `col < ?` |
| `loe` | `NumberExpression<T>?.loe(T?)` | `col <= ?` |
| `between` | `NumberExpression<T>?.between(Pair<T?, T?>)` | `col BETWEEN ? AND ?` |
| `between` | `NumberExpression<T>?.between(ClosedRange<T>)` | `col BETWEEN ? AND ?` |
| `notBetween` | `NumberExpression<T>?.notBetween(Pair<T?, T?>)` | `col NOT BETWEEN ? AND ?` |
| `between` (reverse) | `T?.between(Pair<NumberExpression<T>?, NumberExpression<T>?>)` | `lower <= ? AND upper >= ?` |
| `nullif` | `NumberExpression<T>?.nullif(T?)` | `NULLIF(col, ?)` |
| `coalesce` | `NumberExpression<T>?.coalesce(T?)` | `COALESCE(col, ?)` |
| `rangeTo` | `NumberExpression<T>..NumberExpression<T>` | _(creates Pair for between)_ |
| `add` | `NumberExpression<T>?.add(T?)` / `add(Expression<T>?)` | `col + ?` |
| `subtract` | `NumberExpression<T>?.subtract(T?)` / `subtract(Expression<T>?)` | `col - ?` |
| `multiply` | `NumberExpression<T>?.multiply(T?)` / `multiply(Expression<T>?)` | `col * ?` |
| `divide` | `NumberExpression<T>?.divide(T?)` / `divide(Expression<T>?)` | `col / ?` |
| `mod` | `NumberExpression<T>?.mod(T?)` / `mod(Expression<T>?)` | `col % ?` |
| `+` / `-` / `*` / `/` / `%` | `operator NumberExpression<T>.plus/minus/times/div/rem(T \| Expression<T>)` | `col + ?` _(non-null contract)_ |
| unary `-` | `operator NumberExpression<T>.unaryMinus()` | `-col` |
| `gtAll` | `NumberExpression<T>?.gtAll(CollectionExpression<*, in T>?)` / `gtAll(SubQueryExpression<T>?)` | `col > ALL (...)` |
| `gtAny` | `NumberExpression<T>?.gtAny(CollectionExpression<*, in T>?)` / `gtAny(SubQueryExpression<T>?)` | `col > ANY (...)` |
| `goeAll` | `NumberExpression<T>?.goeAll(CollectionExpression<*, in T>?)` | `col >= ALL (...)` |
| `goeAny` | `NumberExpression<T>?.goeAny(CollectionExpression<*, in T>?)` | `col >= ANY (...)` |
| `ltAll` | `NumberExpression<T>?.ltAll(CollectionExpression<*, in T>?)` | `col < ALL (...)` |
| `ltAny` | `NumberExpression<T>?.ltAny(CollectionExpression<*, in T>?)` | `col < ANY (...)` |
| `loeAll` | `NumberExpression<T>?.loeAll(CollectionExpression<*, in T>?)` | `col <= ALL (...)` |
| `loeAny` | `NumberExpression<T>?.loeAny(CollectionExpression<*, in T>?)` | `col <= ANY (...)` |

### Examples

::: code-group

```kotlin [Kotlin]
entity.price gt 10000
entity.price between (minPrice to maxPrice)
entity.score between (0..100)
entity.quantity loe maxQuantity

// Reverse BETWEEN: value on left, expression bounds on right
orderAmount between (tier.minAmount to tier.maxAmount)
// -> min_amount <= orderAmount AND max_amount >= orderAmount

// rangeTo operator (..): syntactic sugar for creating Pair
orderAmount between (tier.minAmount..tier.maxAmount)
```

```sql [SQL]
price > 10000
price BETWEEN ? AND ?
score BETWEEN 0 AND 100
quantity <= ?
```

:::

::: tip ALL/Any comparisons <Badge type="tip" text="v1.2.0+" />
QueryDSL provides `<op>All` / `<op>Any` members for comparing against a collection
or subquery. querydsl-ktx wraps them with the same null-skip semantics:

```kotlin
import com.querydsl.jpa.JPAExpressions

// price greater than ALL prices in the Stationery category
val stationeryPrices = JPAExpressions.select(product.price).from(product)
    .where(product.category.eq("Stationery"))
selectFrom(product).where(product.price gtAll stationeryPrices).fetch()

// price equal to ANY price in cheap categories
val cheapPrices = JPAExpressions.select(product.price).from(product)
    .where(product.price.lt(10000))
selectFrom(product).where(product.price eqAny cheapPrices).fetch()
```

QueryDSL 5.1.0 has asymmetric coverage. The wrappers mirror this exactly:

| Function | CollectionExpression | SubQueryExpression |
|---|---|---|
| `eqAll` / `eqAny` (Simple, Comparable, Number) | ✓ | ✓ |
| `neAll` / `neAny` (Simple) | ✓ | ✗ (member not provided) |
| `gtAll` / `gtAny` (Comparable, Number) | ✓ | ✓ |
| `goeAll` / `ltAll` / `loeAll` and `*Any` (Comparable) | ✓ | ✓ |
| `goeAll` / `ltAll` / `loeAll` and `*Any` (Number) | ✓ | ✗ (member not provided on `NumberExpression`) |

For Number columns that need the missing SubQuery variants, cast the path to a
`ComparableExpression` view if the underlying type is `Comparable<T>`.
:::

---

## StringExpressionExtensions

Pattern matching and string comparison operators.

### Functions

| Function | Signature | SQL |
|----------|-----------|-----|
| `contains` | `StringExpression?.contains(String?)` | `LIKE '%val%'` |
| `containsIgnoreCase` | `StringExpression?.containsIgnoreCase(String?)` | `LOWER(col) LIKE LOWER('%val%')` |
| `startsWith` | `StringExpression?.startsWith(String?)` | `LIKE 'val%'` |
| `startsWithIgnoreCase` | `StringExpression?.startsWithIgnoreCase(String?)` | `LOWER(col) LIKE LOWER('val%')` |
| `endsWith` | `StringExpression?.endsWith(String?)` | `LIKE '%val'` |
| `endsWithIgnoreCase` | `StringExpression?.endsWithIgnoreCase(String?)` | `LOWER(col) LIKE LOWER('%val')` |
| `equalsIgnoreCase` | `StringExpression?.equalsIgnoreCase(String?)` | `LOWER(col) = LOWER(?)` |
| `notEqualsIgnoreCase` | `StringExpression?.notEqualsIgnoreCase(String?)` | `LOWER(col) != LOWER(?)` |
| `like` | `StringExpression?.like(String?)` | `LIKE ?` |
| `likeIgnoreCase` | `StringExpression?.likeIgnoreCase(String?)` | `LOWER(col) LIKE LOWER(?)` |
| `notLike` | `StringExpression?.notLike(String?)` | `NOT LIKE ?` |
| `matches` | `StringExpression?.matches(String?)` | `REGEXP ?` |
| `contains` | `StringExpression?.contains(Expression<String>?)` | `LIKE '%' \|\| other_col \|\| '%'` |
| `startsWith` | `StringExpression?.startsWith(Expression<String>?)` | `LIKE other_col \|\| '%'` |
| `endsWith` | `StringExpression?.endsWith(Expression<String>?)` | `LIKE '%' \|\| other_col` |
| `nullif` | `StringExpression?.nullif(Expression<String>?)` | `NULLIF(col, other_col)` |
| `nullif` | `StringExpression?.nullif(String?)` | `NULLIF(col, ?)` |
| `coalesce` | `StringExpression?.coalesce(Expression<String>?)` | `COALESCE(col, other_col)` |
| `coalesce` | `StringExpression?.coalesce(String?)` | `COALESCE(col, ?)` |

`contains`, `startsWith`, and `endsWith` also have `Expression<String>` overloads.

### Examples

::: code-group

```kotlin [Kotlin]
// Substring search
entity.name contains keyword              // name LIKE '%keyword%'
entity.name containsIgnoreCase keyword     // case-insensitive

// Prefix / suffix
entity.name startsWith prefix             // name LIKE 'prefix%'
entity.name endsWith suffix               // name LIKE '%suffix'

// Pattern and regex
entity.name like "J%n"                    // name LIKE 'J%n'
entity.email matches "^[a-z]+@.*"         // name REGEXP '^[a-z]+@.*'

// Case-insensitive equality
entity.email equalsIgnoreCase email       // LOWER(email) = LOWER(?)
```

```sql [SQL]
name LIKE '%keyword%'
LOWER(name) LIKE LOWER('%keyword%')
name LIKE 'prefix%'
name LIKE '%suffix'
name LIKE 'J%n'
email REGEXP '^[a-z]+@.*'
LOWER(email) = LOWER(?)
```

:::

::: tip Escape character for literal % and _ <Badge type="tip" text="v1.2.0+" />
For patterns containing literal `%` or `_` characters, chain `escape '\'`
right after `like`, `notLike`, or `likeIgnoreCase`. This mirrors SQL's
`LIKE pattern ESCAPE 'char'` syntax.

```kotlin
name like "10\\%off" escape '\\'              // LIKE '10\%off' ESCAPE '\'
name notLike "10\\%off" escape '\\'           // NOT LIKE '10\%off' ESCAPE '\'
name likeIgnoreCase "10\\%OFF" escape '\\'    // LIKE '10\%OFF' ESCAPE '\' (case-insensitive)
```

`escape` is `BooleanExpression?.escape(Char)`. Calling it on any expression
that is not a like-family result throws `ExpressionException`.
:::

---

## TemporalExpressionExtensions

Temporal comparison operators for date/time expressions.

### Functions

| Function | Signature | SQL |
|----------|-----------|-----|
| `after` | `TemporalExpression<T>?.after(T?)` | `col > ?` |
| `after` | `TemporalExpression<T>?.after(Expression<T>?)` | `col > other_col` |
| `before` | `TemporalExpression<T>?.before(T?)` | `col < ?` |
| `before` | `TemporalExpression<T>?.before(Expression<T>?)` | `col < other_col` |

### Examples

::: code-group

```kotlin [Kotlin]
entity.createdAt after startDate     // created_at > ?
entity.createdAt before endDate      // created_at < ?

// Column comparison
entity.endDate after entity.startDate  // end_date > start_date

// Null-safe
entity.createdAt after null          // null (skipped)
```

```sql [SQL]
created_at > '2024-01-01'
created_at < '2024-12-31'
end_date > start_date
```

:::

::: tip after/before vs gt/goe/lt/loe
Use `after`/`before` for `TemporalExpression` (dates, timestamps) and
`gt`/`goe`/`lt`/`loe` for `ComparableExpression` or `NumberExpression`.
They generate the same SQL but are defined on different QueryDSL types.
:::

---

## CollectionExpressionExtensions

Membership check for mapped collection fields (e.g., `@ElementCollection`, `@ManyToMany`).

### Functions

| Function | Signature | SQL |
|----------|-----------|-----|
| `contains` | `CollectionExpressionBase<T, E>?.contains(E?)` | `? IN (col)` |
| `contains` | `CollectionExpressionBase<T, E>?.contains(Expression<E>?)` | `other_col IN (col)` |

### Examples

::: code-group

```kotlin [Kotlin]
entity.roles contains "ADMIN"       // 'ADMIN' IN (roles)
entity.tags contains tag            // ? IN (tags), null-safe
```

```sql [SQL]
'ADMIN' IN (roles)
```

:::

---

## SubQueryExtensions

Shorthand EXISTS / NOT EXISTS sub-query builders.

### Functions

| Function | Signature | SQL |
|----------|-----------|-----|
| `exists` | `EntityPath<T>.exists(vararg Predicate?)` | `EXISTS (SELECT 1 FROM ...)` |
| `notExists` | `EntityPath<T>.notExists(vararg Predicate?)` | `NOT EXISTS (SELECT 1 FROM ...)` |

### Examples

::: code-group

```kotlin [Kotlin]
// Before: verbose sub-query
JPAExpressions.selectOne()
    .from(orderItem)
    .where(orderItem.orderId.eq(order.id))
    .exists()

// After: concise
orderItem.exists(orderItem.orderId eq order.id)

// NOT EXISTS
orderItem.notExists(orderItem.orderId eq order.id)
```

```sql [SQL]
EXISTS (SELECT 1 FROM order_item WHERE order_item.order_id = order.id)
NOT EXISTS (SELECT 1 FROM order_item WHERE order_item.order_id = order.id)
```

:::

::: info Null predicates
Null predicates in the vararg are silently filtered out.
:::

---

## Selective Implementation

You don't have to use all 8 interfaces. Choose what you need:

### Option 1: QuerydslRepository (all included)

```kotlin
@Repository
class MyRepository : QuerydslRepository<MyEntity>() {
    // All 8 interfaces are available
}
```

### Option 2: QuerydslSupport + selected interfaces

```kotlin
@Repository
class MyRepository : QuerydslSupport<MyEntity>(),
    SimpleExpressionExtensions,
    StringExpressionExtensions {

    override val domainClass = MyEntity::class.java

    // Only eq, ne, in, notIn, contains, startsWith, etc.
    // No number/comparable/temporal/collection extensions
}
```

### Option 3: Implement on any class

```kotlin
class PredicateBuilder : BooleanExpressionExtensions, SimpleExpressionExtensions {
    // Use extensions anywhere, not just in repositories
}
```

::: info QuerydslRepository vs QuerydslSupport
| | `QuerydslRepository<T>` | `QuerydslSupport<T>` |
|---|---|---|
| Extension interfaces | All 8 included | None (add what you need) |
| `domainClass` | Auto-resolved via generics | Must override manually |
| Use when | You want everything | You want minimal surface area |
:::
