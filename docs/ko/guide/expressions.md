---
description: numberTemplate, stringTemplate 등 QueryDSL 템플릿 표현식의 reified Kotlin 래퍼.
---

# Expressions

QueryDSL의 내장 연산자로 대부분의 경우를 처리할 수 있지만, 때로는 DB 고유 함수가 필요합니다.
`GROUP_CONCAT`, `CAST`, 윈도우 함수, 커스텀 저장 프로시저 등이 그런 경우입니다. 이럴 때 템플릿 표현식을
사용합니다.

기본 QueryDSL에서는 모든 템플릿 호출에 `YourType::class.java`를 명시적으로 전달해야 합니다.
querydsl-ktx는 Kotlin의 reified 타입 파라미터로 이 보일러플레이트를 제거합니다.

---

## 템플릿이 필요한 경우

::: tip QueryDSL의 내장 연산자로 부족할 때
다음과 같은 경우에 템플릿 표현식이 필요합니다:

- **DB 고유 함수**: `GROUP_CONCAT()`, `JSON_EXTRACT()`, `REGEXP_REPLACE()`
- **타입 캐스팅**: 집계 정밀도를 위한 `CAST(column AS DECIMAL)`
- **윈도우 함수**: `ROW_NUMBER() OVER (PARTITION BY ...)`
- **커스텀 SQL 함수**: `@FunctionContributor`나 Hibernate dialect로 등록한 함수
- **날짜/시간 함수**: `DATE_FORMAT()`, `TIMESTAMPDIFF()`
:::

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

::: code-group

```kotlin [Before (기본 QueryDSL)]
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

## 실전 시나리오

### GROUP_CONCAT으로 콤마 구분 리스트

태그나 카테고리를 하나의 문자열로 모으기. 어드민 대시보드에서 자주 쓰입니다:

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

### CAST로 집계 정밀도 확보

정수 컬럼의 `SUM`이나 `AVG`에서 소수점이 날아가는 경우:

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

### 날짜 포맷팅

리포트용 날짜 포맷팅이나 월별 그룹핑:

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

### 커스텀 Hibernate 함수

Hibernate의 `FunctionContributor`로 등록한 커스텀 함수를 사용하는 경우:

```kotlin
// 등록된 함수: full_text_match(column, query) -> boolean
val matches = booleanTemplate(
    "FUNCTION('full_text_match', {0}, {1})",
    product.description,
    asString(searchQuery),
)

selectFrom(product)
    .where(matches)
    .fetch()
```

### 랜덤 정렬

단순하지만 자주 필요한 케이스. 결과를 랜덤으로 섞기:

```kotlin
selectFrom(product)
    .where(product.active eq true)
    .orderBy(numberTemplate<Double>("RAND()").asc())
    .limit(5)
    .fetch()
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
val fullName = stringTemplate("CONCAT({0}, ' ', {1})", member.firstName, member.lastName)
val isActive = booleanTemplate("FUNCTION('is_active', {0})", member.id)
```

---

## 값 래핑

Kotlin 값을 쿼리에서 사용할 수 있는 QueryDSL 표현식으로 래핑합니다. 리터럴 값이
QueryDSL 표현식 체인에 참여해야 할 때 유용합니다:

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

### 값 래핑이 필요한 경우

```kotlin
// Kotlin 값을 컬럼 표현식과 비교
val threshold = asNumber(100)
selectFrom(product)
    .where(product.stock.lt(threshold))
    .fetch()

// Kotlin 값을 템플릿 인자로 사용
val now = asDateTime(LocalDateTime.now())
selectFrom(coupon)
    .where(coupon.expiresAt.after(now))
    .fetch()
```

---

## Constant

Reified 타입 추론을 사용하여 상수 표현식을 생성합니다. 상수는 JPQL 쿼리에
인라인됩니다 (파라미터 바인딩이 아님).

```kotlin
inline fun <reified T> constant(value: T): Expression<T>
```

::: code-group

```kotlin [Before (기본 QueryDSL)]
Expressions.constant(42)
```

```kotlin [After (querydsl-ktx)]
constant(42)
```

:::

::: warning 상수 vs 파라미터
상수는 바인드 파라미터가 아니라 쿼리 문자열에 직접 포함됩니다.
진짜 고정된 값(예: EXISTS 서브쿼리의 `SELECT 1`)에만 사용하세요.
사용자 입력에는 사용하지 마세요. 사용자 제공 값은 `as*` 래핑 함수를
사용하거나 `eq` 같은 확장 연산자에 직접 전달하세요.
:::

---

## 요약

이 유틸리티들은 `com.querydsl.ktx` 패키지의 **최상위 함수**입니다. 확장 인터페이스에 종속되지 않으며 인터페이스를 구현하지 않고도 어디서든 사용할 수 있습니다.

```kotlin
import com.querydsl.ktx.numberTemplate
import com.querydsl.ktx.stringTemplate
import com.querydsl.ktx.dateTimeTemplate
import com.querydsl.ktx.constant
import com.querydsl.ktx.asNumber
```
