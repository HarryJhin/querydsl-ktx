---
description: Type-safe CASE/WHEN DSL for QueryDSL with automatic null-branch pruning. Searched and simple CASE supported.
---

# Case/When DSL

SQL CASE expressions show up more often than you'd think -- status-based pricing tiers,
conditional display names, mapping enums to human-readable labels. In vanilla QueryDSL,
building these means wrestling with `CaseBuilder`, backtick-escaped `when`, and verbose
`.then()` chains.

querydsl-ktx replaces all of that with a Kotlin DSL that reads like the SQL it generates.
Null-safe: null predicates skip branches. If all branches are skipped, the result is `null`.

---

## When to Use CASE in SQL

::: tip CASE vs application layer
Use SQL CASE when you need the result **in the query itself** -- for ordering,
grouping, aggregation, or projections sent directly to the client.
If you're just mapping values for display after fetching, do it in Kotlin.

**Good fit for SQL CASE:**

- Sorting by business priority (VIP first, then NORMAL, then DORMANT)
- Conditional aggregation (`SUM(CASE WHEN ... THEN price ELSE 0 END)`)
- Projecting computed columns (discount tier, display label)

**Better in Kotlin:**

- Simple enum-to-string mapping after fetch
- Complex business logic that depends on multiple entities
:::

---

## Searched CASE

The most common form. Each branch has an independent predicate -- like a chain of `if/else if`.

```kotlin
fun <T> case(block: SearchedCaseDsl<T>.() -> Unit): Expression<T>?
```

### Real-World Example: Status-Based Discount Rate

You have an order system where discount rates depend on the member's tier:

::: code-group

```kotlin [Kotlin]
val discountRate = case<Int> {
    `when`(member.grade.eq("VIP")) then 20
    `when`(member.grade.eq("GOLD")) then 10
    `when`(member.grade.eq("SILVER")) then 5
    otherwise(0)
}

val results = select(member.name, member.grade, discountRate)
    .from(member)
    .fetch()
```

```sql [SQL]
SELECT m.name, m.grade,
    CASE
        WHEN m.grade = 'VIP' THEN 20
        WHEN m.grade = 'GOLD' THEN 10
        WHEN m.grade = 'SILVER' THEN 5
        ELSE 0
    END
FROM member m
```

:::

### Custom Sort Order

Sort by business priority instead of alphabetical order:

::: code-group

```kotlin [Kotlin]
val priority = case<Int> {
    `when`(order.status.eq("PENDING")) then 1
    `when`(order.status.eq("PROCESSING")) then 2
    `when`(order.status.eq("SHIPPED")) then 3
    otherwise(99)
}

selectFrom(order)
    .orderBy(priority!!.asc())
    .fetch()
```

```sql [SQL]
SELECT o.*
FROM orders o
ORDER BY
    CASE
        WHEN o.status = 'PENDING' THEN 1
        WHEN o.status = 'PROCESSING' THEN 2
        WHEN o.status = 'SHIPPED' THEN 3
        ELSE 99
    END ASC
```

:::

### Conditional Display Name

Project a computed label for the frontend:

::: code-group

```kotlin [Kotlin]
val displayStatus = case<String> {
    `when`(product.stock.gt(10)) then "In Stock"
    `when`(product.stock.gt(0)) then "Low Stock"
    otherwise("Out of Stock")
}

select(product.name, product.price, displayStatus)
    .from(product)
    .fetch()
```

```sql [SQL]
SELECT p.name, p.price,
    CASE
        WHEN p.stock > 10 THEN 'In Stock'
        WHEN p.stock > 0 THEN 'Low Stock'
        ELSE 'Out of Stock'
    END
FROM product p
```

:::

---

## Simple CASE

Matches a single expression against constant values. Cleaner when you're comparing
one column against multiple literals -- like a SQL `switch` statement.

Internally converts to a searched CASE using `expression.eq(value)`.

```kotlin
fun <D, T> case(expression: SimpleExpression<D>, block: SimpleCaseDsl<D, T>.() -> Unit): Expression<T>?
```

### Example: Enum to Label Mapping

::: code-group

```kotlin [Kotlin]
val label = case<String, String>(order.status) {
    `when`("PENDING") then "Awaiting Payment"
    `when`("PAID") then "Payment Complete"
    `when`("SHIPPED") then "In Transit"
    otherwise("Unknown")
}
```

```sql [SQL]
CASE o.status
    WHEN 'PENDING' THEN 'Awaiting Payment'
    WHEN 'PAID' THEN 'Payment Complete'
    WHEN 'SHIPPED' THEN 'In Transit'
    ELSE 'Unknown'
END
```

:::

::: tip Searched vs Simple
Use **Simple CASE** when comparing one column against literal values.
Use **Searched CASE** when branches have different predicates or compare multiple columns.
:::

---

## Null-Safety

This is where querydsl-ktx's CASE DSL really shines compared to vanilla QueryDSL.
When building CASE expressions with dynamic conditions, null predicates are silently skipped:

| Scenario | Behavior |
|----------|----------|
| `when(null)` | Branch is skipped silently |
| All predicates null | `case {}` returns `null` |
| Non-null predicate | Branch is added normally |

### Dynamic CASE with Optional Conditions

```kotlin
val keyword: String? = request.keyword  // might be null

val matchScore = case<Int> {
    `when`(product.name.eq(keyword)) then 100    // skipped if keyword is null
    `when`(product.name contains keyword) then 50 // skipped if keyword is null
    `when`(product.active.eq(true)) then 10       // always added
    otherwise(0)
}
// If keyword is null: CASE WHEN active = true THEN 10 ELSE 0 END
// If keyword is "phone": full 3-branch CASE
```

In vanilla QueryDSL, you'd need to wrap each branch in an `if` check. The DSL handles it automatically.

---

## Before / After

::: code-group

```kotlin [Before (vanilla QueryDSL)]
// Backtick hell + verbose chaining
CaseBuilder()
    .`when`(member.grade.eq("VIP")).then(20)
    .`when`(member.grade.eq("GOLD")).then(10)
    .`when`(member.grade.eq("SILVER")).then(5)
    .otherwise(0)

// And if you need null-safety? Good luck:
val builder = CaseBuilder()
if (keyword != null) {
    builder.`when`(product.name.eq(keyword)).then(100)
}
// CaseBuilder doesn't support conditional branch addition...
```

```kotlin [After (querydsl-ktx)]
case<Int> {
    `when`(member.grade.eq("VIP")) then 20
    `when`(member.grade.eq("GOLD")) then 10
    `when`(member.grade.eq("SILVER")) then 5
    otherwise(0)
}

// Null-safe branches -- just works
case<Int> {
    `when`(product.name.eq(keyword)) then 100  // null keyword → skipped
    `when`(product.active.eq(true)) then 10
    otherwise(0)
}
```

:::

The DSL version gives you:

- **Infix `then`** -- reads like SQL, no `.then()` chain
- **Null-safe branches** -- null predicates are silently skipped
- **Type inference** -- `case<Int>` sets the return type once

---

## DSL Classes

```kotlin
class SearchedCaseDsl<T> {
    fun `when`(pred: BooleanExpression?): WhenClause
    fun otherwise(value: T)
    fun otherwise(expr: Expression<T>)

    inner class WhenClause(pred: BooleanExpression?) {
        infix fun then(value: T)
        infix fun then(expr: Expression<T>)
    }
}

class SimpleCaseDsl<D, T>(expression: SimpleExpression<D>) {
    fun `when`(value: D): SimpleWhenClause
    fun otherwise(value: T)
    fun otherwise(expr: Expression<T>)

    inner class SimpleWhenClause(value: D) {
        infix fun then(result: T)
        infix fun then(expr: Expression<T>)
    }
}
```
