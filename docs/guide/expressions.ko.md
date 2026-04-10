# Expressions

querydsl-ktx는 `com.querydsl.core.types.dsl.Expressions`를 래핑하는 최상위 유틸리티 함수를 제공합니다.
Reified 타입 파라미터로 `::class.java` 보일러플레이트를 제거합니다.

---

## 템플릿 함수 (reified)

`Class<T>`를 전달하지 않고 타입이 지정된 QueryDSL 템플릿 표현식을 생성합니다.

| 함수 | 반환 타입 |
|------|----------|
| `numberTemplate<T>(template, args)` | `NumberExpression<T>` |
| `comparableTemplate<T>(template, args)` | `ComparableExpression<T>` |
| `simpleTemplate<T>(template, args)` | `SimpleExpression<T>` |
| `template<T>(template, args)` | `Expression<T>` |
| `dateTemplate<T>(template, args)` | `DateExpression<T>` |
| `dateTimeTemplate<T>(template, args)` | `DateTimeExpression<T>` |
| `timeTemplate<T>(template, args)` | `TimeExpression<T>` |
| `enumTemplate<T>(template, args)` | `EnumExpression<T>` |

### Before / After

=== "Before (기본 QueryDSL)"

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

## 템플릿 함수 (non-reified)

String과 Boolean 템플릿은 반환 타입이 고정되어 있으므로 타입 파라미터가 필요 없습니다.

| 함수 | 반환 타입 |
|------|----------|
| `stringTemplate(template, args)` | `StringExpression` |
| `booleanTemplate(template, args)` | `BooleanExpression` |

### 예제

```kotlin
val fullName = stringTemplate("CONCAT({0}, ' ', {1})", entity.firstName, entity.lastName)
val isActive = booleanTemplate("FUNCTION('is_active', {0})", entity.id)
```

---

## 값 래핑

Kotlin 값을 쿼리에서 사용할 수 있는 QueryDSL 표현식으로 래핑합니다.

| 함수 | 반환 타입 |
|------|----------|
| `asNumber(value)` | `NumberExpression<T>` |
| `asString(value)` | `StringExpression` |
| `asBoolean(value)` | `BooleanExpression` |
| `asComparable(value)` | `ComparableExpression<T>` |
| `asDate(value)` | `DateExpression<T>` |
| `asDateTime(value)` | `DateTimeExpression<T>` |
| `asTime(value)` | `TimeExpression<T>` |
| `asEnum(value)` | `EnumExpression<T>` |

### 예제

```kotlin
val threshold = asNumber(100)
val now = asDateTime(LocalDateTime.now())
```

---

## Constant

Reified 타입 추론을 사용하여 상수 표현식을 생성합니다.

```kotlin
inline fun <reified T> constant(value: T): Expression<T>
```

### Before / After

=== "Before (기본 QueryDSL)"

    ```kotlin
    Expressions.constant(42)
    ```

=== "After (querydsl-ktx)"

    ```kotlin
    constant(42)
    ```

---

## 요약

이 유틸리티들은 `com.querydsl.ktx` 패키지의 **최상위 함수**입니다. 확장 인터페이스에 종속되지 않으며 인터페이스를 구현하지 않고도 어디서든 사용할 수 있습니다.

```kotlin
import com.querydsl.ktx.numberTemplate
import com.querydsl.ktx.dateTimeTemplate
import com.querydsl.ktx.constant
import com.querydsl.ktx.asNumber
```
