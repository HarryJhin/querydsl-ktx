# Expressions

querydsl-ktx provides top-level utility functions that wrap `com.querydsl.core.types.dsl.Expressions`.
Reified type parameters eliminate `::class.java` boilerplate.

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

=== "Before (vanilla QueryDSL)"

    ```kotlin
    Expressions.numberTemplate(Float::class.java, "RAND()")
    Expressions.dateTimeTemplate(LocalDateTime::class.java, "NOW()")
    ```

=== "After (querydsl-ktx)"

    ```kotlin
    numberTemplate<Float>("RAND()")
    dateTimeTemplate<LocalDateTime>("NOW()")
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
val fullName = stringTemplate("CONCAT({0}, ' ', {1})", entity.firstName, entity.lastName)
val isActive = booleanTemplate("FUNCTION('is_active', {0})", entity.id)
```

---

## Value Wrapping

Wrap Kotlin values into QueryDSL expressions for use in queries.

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

### Example

```kotlin
val threshold = asNumber(100)
val now = asDateTime(LocalDateTime.now())
```

---

## Constant

Creates a constant expression with reified type inference.

```kotlin
inline fun <reified T> constant(value: T): Expression<T>
```

### Before / After

=== "Before (vanilla QueryDSL)"

    ```kotlin
    Expressions.constant(42)
    ```

=== "After (querydsl-ktx)"

    ```kotlin
    constant(42)
    ```

---

## Summary

These utilities are **top-level functions** in the `com.querydsl.ktx` package. They are not tied to any extension interface and can be used anywhere without implementing an interface.

```kotlin
import com.querydsl.ktx.numberTemplate
import com.querydsl.ktx.dateTimeTemplate
import com.querydsl.ktx.constant
import com.querydsl.ktx.asNumber
```
