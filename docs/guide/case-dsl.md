# Case/When DSL

querydsl-ktx provides a Kotlin-idiomatic builder for SQL CASE expressions.
Null-safe: null predicates skip branches. If all branches are skipped, the result is `null`.

---

## Searched CASE

The most common form. Each branch has an independent predicate.

### Signature

```kotlin
fun <T> case(block: SearchedCaseDsl<T>.() -> Unit): Expression<T>?
```

### Example

=== "Kotlin"

    ```kotlin
    val expr = case<Int> {
        `when`(entity.status.eq("VIP")) then 1
        `when`(entity.status.eq("NORMAL")) then 2
        otherwise(3)
    }
    ```

=== "SQL"

    ```sql
    CASE
        WHEN status = 'VIP' THEN 1
        WHEN status = 'NORMAL' THEN 2
        ELSE 3
    END
    ```

---

## Simple CASE

Matches a single expression against constant values. Internally converts to a searched CASE using `expression.eq(value)`.

### Signature

```kotlin
fun <D, T> case(expression: SimpleExpression<D>, block: SimpleCaseDsl<D, T>.() -> Unit): Expression<T>?
```

### Example

=== "Kotlin"

    ```kotlin
    val expr = case<String, Int>(entity.status) {
        `when`("VIP") then 1
        `when`("NORMAL") then 2
        otherwise(0)
    }
    ```

=== "SQL"

    ```sql
    CASE status
        WHEN 'VIP' THEN 1
        WHEN 'NORMAL' THEN 2
        ELSE 0
    END
    ```

---

## Null-Safety

| Scenario | Behavior |
|----------|----------|
| `when(null)` | Branch is skipped silently |
| All predicates null | `case {}` returns `null` |
| Non-null predicate | Branch is added normally |

```kotlin
val keyword: String? = null

// The when branch is skipped because the predicate is null
val expr = case<Int> {
    `when`(entity.name.eq(keyword)) then 1   // keyword is null → skipped
    `when`(entity.active.eq(true)) then 2    // added normally
    otherwise(0)
}
// Result: CASE WHEN active = true THEN 2 ELSE 0 END
```

---

## Before / After

=== "Before (vanilla QueryDSL)"

    ```kotlin
    CaseBuilder()
        .`when`(entity.status.eq("VIP")).then(1)
        .`when`(entity.status.eq("NORMAL")).then(2)
        .otherwise(3)
    ```

=== "After (querydsl-ktx)"

    ```kotlin
    case<Int> {
        `when`(entity.status.eq("VIP")) then 1
        `when`(entity.status.eq("NORMAL")) then 2
        otherwise(3)
    }
    ```

The DSL version adds null-safety (null predicates are silently skipped) and a more natural Kotlin syntax with `infix fun then`.

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
