---
description: Reified Kotlin wrappers for QueryDSL template expressions including numberTemplate, stringTemplate, and constants.
---

# Expressions

QueryDSL's built-in operators cover most cases, but sometimes you need to reach for
database-specific functions -- `GROUP_CONCAT`, `CAST`, window functions, or custom stored
procedures. That's when you use template expressions.

In vanilla QueryDSL, every template call requires passing `YourType::class.java` explicitly.
querydsl-ktx uses Kotlin's reified type parameters to eliminate that boilerplate.

---

## When You Need Templates

::: tip When QueryDSL's built-in operators aren't enough
You'll reach for template expressions when you need:

- **Database-specific functions** -- `GROUP_CONCAT()`, `JSON_EXTRACT()`, `REGEXP_REPLACE()`
- **Type casting** -- `CAST(column AS DECIMAL)` for aggregation precision
- **Window functions** -- `ROW_NUMBER() OVER (PARTITION BY ...)`
- **Custom SQL functions** -- registered via `@FunctionContributor` or Hibernate dialects
- **Date/time functions** -- `DATE_FORMAT()`, `TIMESTAMPDIFF()`
:::

---

## Template Functions (reified)

Create typed QueryDSL template expressions without passing `Class<T>`.

| Function | Return Type |
|----------|-------------|
| `numberTemplate<T>(template, args)` | `NumberExpression<T>` |
| `comparableTemplate<T>(template, args)` | `ComparableExpression<T>` |
| `simpleTemplate<T>(template, args)` | `SimpleExpression<T>` |
| `template<T>(template, args)` | `Expression<T>` |
| `dateTemplate<T>(template, args)` | `DateExpression<T>` |
| `dateTimeTemplate<T>(template, args)` | `DateTimeExpression<T>` |
| `timeTemplate<T>(template, args)` | `TimeExpression<T>` |
| `enumTemplate<T>(template, args)` | `EnumExpression<T>` |

### Before / After

::: code-group

```kotlin [Before (vanilla QueryDSL)]
Expressions.numberTemplate(Float::class.java, "RAND()")
Expressions.dateTimeTemplate(LocalDateTime::class.java, "NOW()")
Expressions.numberTemplate(Long::class.java, "CAST({0} AS BIGINT)", order.price)
```

```kotlin [After (querydsl-ktx)]
numberTemplate<Float>("RAND()")
dateTimeTemplate<LocalDateTime>("NOW()")
numberTemplate<Long>("CAST({0} AS BIGINT)", order.price)
```

:::

---

## Real-World Scenarios

### GROUP_CONCAT for Comma-Separated Lists

Collecting tags or categories into a single string -- common in admin dashboards:

::: code-group

```kotlin [Kotlin]
val tagList = stringTemplate(
    "GROUP_CONCAT({0} SEPARATOR ', ')",
    productTag.name,
)

select(product.name, tagList)
    .from(product)
    .join(productTag).on(productTag.productId.eq(product.id))
    .groupBy(product.id)
    .fetch()
```

```sql [SQL]
SELECT p.name, GROUP_CONCAT(pt.name SEPARATOR ', ')
FROM product p
JOIN product_tag pt ON pt.product_id = p.id
GROUP BY p.id
```

:::

### CAST for Aggregation Precision

When `SUM` or `AVG` on an integer column loses decimal precision:

::: code-group

```kotlin [Kotlin]
val avgPrice = numberTemplate<Double>(
    "CAST(AVG({0}) AS DOUBLE)",
    orderItem.price,
)

select(product.category, avgPrice)
    .from(orderItem)
    .join(product).on(orderItem.productId.eq(product.id))
    .groupBy(product.category)
    .fetch()
```

```sql [SQL]
SELECT p.category, CAST(AVG(oi.price) AS DOUBLE)
FROM order_item oi
JOIN product p ON oi.product_id = p.id
GROUP BY p.category
```

:::

### Date Formatting

Formatting dates for reports or grouping by month:

::: code-group

```kotlin [Kotlin]
val yearMonth = stringTemplate(
    "DATE_FORMAT({0}, '%Y-%m')",
    order.createdAt,
)

select(yearMonth, order.count())
    .from(order)
    .groupBy(yearMonth)
    .orderBy(yearMonth.asc())
    .fetch()
```

```sql [SQL]
SELECT DATE_FORMAT(o.created_at, '%Y-%m'), COUNT(o.id)
FROM orders o
GROUP BY DATE_FORMAT(o.created_at, '%Y-%m')
ORDER BY DATE_FORMAT(o.created_at, '%Y-%m') ASC
```

:::

### Custom Hibernate Functions

If you've registered a custom function via Hibernate's `FunctionContributor`:

```kotlin
// Registered function: full_text_match(column, query) -> boolean
val matches = booleanTemplate(
    "FUNCTION('full_text_match', {0}, {1})",
    product.description,
    asString(searchQuery),
)

selectFrom(product)
    .where(matches)
    .fetch()
```

### Random Ordering

A simple but common need -- randomizing results:

```kotlin
selectFrom(product)
    .where(product.active eq true)
    .orderBy(numberTemplate<Double>("RAND()").asc())
    .limit(5)
    .fetch()
```

---

## Template Functions (non-reified)

String and Boolean templates have fixed return types, so no type parameter is needed.

| Function | Return Type |
|----------|-------------|
| `stringTemplate(template, args)` | `StringExpression` |
| `booleanTemplate(template, args)` | `BooleanExpression` |

### Example

```kotlin
val fullName = stringTemplate("CONCAT({0}, ' ', {1})", member.firstName, member.lastName)
val isActive = booleanTemplate("FUNCTION('is_active', {0})", member.id)
```

---

## Value Wrapping

Wrap Kotlin values into QueryDSL expressions for use in queries. Useful when you need
a literal value to participate in a QueryDSL expression chain:

| Function | Return Type |
|----------|-------------|
| `asNumber(value)` | `NumberExpression<T>` |
| `asString(value)` | `StringExpression` |
| `asBoolean(value)` | `BooleanExpression` |
| `asComparable(value)` | `ComparableExpression<T>` |
| `asDate(value)` | `DateExpression<T>` |
| `asDateTime(value)` | `DateTimeExpression<T>` |
| `asTime(value)` | `TimeExpression<T>` |
| `asEnum(value)` | `EnumExpression<T>` |

### When You Need Value Wrapping

```kotlin
// Comparing a Kotlin value against a column expression
val threshold = asNumber(100)
selectFrom(product)
    .where(product.stock.lt(threshold))
    .fetch()

// Using a Kotlin value as a template argument
val now = asDateTime(LocalDateTime.now())
selectFrom(coupon)
    .where(coupon.expiresAt.after(now))
    .fetch()
```

---

## Constant

Creates a constant expression with reified type inference. Constants are inlined
into the JPQL query (not bound as parameters).

```kotlin
inline fun <reified T> constant(value: T): Expression<T>
```

::: code-group

```kotlin [Before (vanilla QueryDSL)]
Expressions.constant(42)
```

```kotlin [After (querydsl-ktx)]
constant(42)
```

:::

::: warning Constants vs Parameters
Constants are embedded directly in the query string, not as bind parameters.
Use them for truly fixed values (like `SELECT 1` in EXISTS subqueries),
not for user input. For user-provided values, use the `as*` wrapping functions
or pass values directly to extension operators like `eq`.
:::

---

## Summary

These utilities are **top-level functions** in the `com.querydsl.ktx` package. They are not tied to any extension interface and can be used anywhere without implementing an interface.

```kotlin
import com.querydsl.ktx.numberTemplate
import com.querydsl.ktx.stringTemplate
import com.querydsl.ktx.dateTimeTemplate
import com.querydsl.ktx.constant
import com.querydsl.ktx.asNumber
```
