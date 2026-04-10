# Case/When DSL

querydsl-ktx는 SQL CASE 표현식을 위한 Kotlin 관용적 빌더를 제공합니다.
Null-safe: null predicate는 분기를 건너뜁니다. 모든 분기가 건너뛰어지면 결과는 `null`입니다.

---

## Searched CASE

가장 일반적인 형태입니다. 각 분기에 독립적인 조건이 있습니다.

### 시그니처

```kotlin
fun <T> case(block: SearchedCaseDsl<T>.() -> Unit): Expression<T>?
```

### 예제

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

단일 표현식을 상수 값과 매칭합니다. 내부적으로 `expression.eq(value)`를 사용하여 searched CASE로 변환됩니다.

### 시그니처

```kotlin
fun <D, T> case(expression: SimpleExpression<D>, block: SimpleCaseDsl<D, T>.() -> Unit): Expression<T>?
```

### 예제

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

## Null 안전성

| 시나리오 | 동작 |
|---------|------|
| `when(null)` | 분기가 조용히 건너뛰어짐 |
| 모든 predicate가 null | `case {}`가 `null`을 반환 |
| non-null predicate | 분기가 정상적으로 추가됨 |

```kotlin
val keyword: String? = null

// predicate가 null이므로 when 분기가 건너뛰어짐
val expr = case<Int> {
    `when`(entity.name.eq(keyword)) then 1   // keyword가 null → 건너뜀
    `when`(entity.active.eq(true)) then 2    // 정상 추가
    otherwise(0)
}
// 결과: CASE WHEN active = true THEN 2 ELSE 0 END
```

---

## Before / After

=== "Before (기본 QueryDSL)"

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

DSL 버전은 null 안전성(null predicate는 조용히 건너뜀)과 `infix fun then`을 사용한 자연스러운 Kotlin 문법을 제공합니다.

---

## DSL 클래스

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
