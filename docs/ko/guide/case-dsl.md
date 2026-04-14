# Case/When DSL

SQL CASE 표현식은 생각보다 자주 필요합니다 -- 등급별 할인율, 조건부 표시명, enum을 사람이
읽을 수 있는 라벨로 변환하는 경우 등. 기본 QueryDSL에서는 `CaseBuilder`와 백틱으로 감싼
`when`, 장황한 `.then()` 체이닝과 싸워야 합니다.

querydsl-ktx는 이 모든 것을 생성되는 SQL처럼 읽히는 Kotlin DSL로 대체합니다.
Null-safe: null predicate는 분기를 건너뜁니다. 모든 분기가 건너뛰어지면 결과는 `null`입니다.

---

## 언제 SQL CASE를 사용할까

::: tip SQL CASE vs 애플리케이션 레이어
**쿼리 안에서** 결과가 필요할 때 SQL CASE를 사용하세요 -- 정렬, 그룹핑,
집계, 클라이언트에 직접 보내는 프로젝션 등.
조회 후 표시용으로 값을 매핑하는 거라면 Kotlin에서 하는 게 낫습니다.

**SQL CASE에 적합한 경우:**

- 비즈니스 우선순위로 정렬 (VIP 먼저, 그다음 NORMAL, 그다음 DORMANT)
- 조건부 집계 (`SUM(CASE WHEN ... THEN price ELSE 0 END)`)
- 계산된 컬럼 프로젝션 (할인 등급, 표시 라벨)

**Kotlin에서 하는 게 나은 경우:**

- 조회 후 단순 enum-to-string 매핑
- 여러 엔티티에 의존하는 복잡한 비즈니스 로직
:::

---

## Searched CASE

가장 일반적인 형태입니다. 각 분기에 독립적인 조건이 있습니다 -- `if/else if` 체인과 비슷합니다.

```kotlin
fun <T> case(block: SearchedCaseDsl<T>.() -> Unit): Expression<T>?
```

### 실전 예제: 등급별 할인율

주문 시스템에서 회원 등급에 따라 할인율이 달라지는 경우:

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

### 커스텀 정렬 순서

알파벳 순이 아닌 비즈니스 우선순위로 정렬:

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

### 조건부 표시명

프론트엔드에 보여줄 계산된 라벨을 프로젝션:

::: code-group

```kotlin [Kotlin]
val displayStatus = case<String> {
    `when`(product.stock.gt(10)) then "재고 충분"
    `when`(product.stock.gt(0)) then "재고 부족"
    otherwise("품절")
}

select(product.name, product.price, displayStatus)
    .from(product)
    .fetch()
```

```sql [SQL]
SELECT p.name, p.price,
    CASE
        WHEN p.stock > 10 THEN '재고 충분'
        WHEN p.stock > 0 THEN '재고 부족'
        ELSE '품절'
    END
FROM product p
```

:::

---

## Simple CASE

단일 표현식을 상수 값과 매칭합니다. 하나의 컬럼을 여러 리터럴과 비교할 때 더 깔끔합니다
-- SQL의 `switch` 문과 비슷합니다.

내부적으로 `expression.eq(value)`를 사용하여 searched CASE로 변환됩니다.

```kotlin
fun <D, T> case(expression: SimpleExpression<D>, block: SimpleCaseDsl<D, T>.() -> Unit): Expression<T>?
```

### 예제: 상태값을 라벨로 변환

::: code-group

```kotlin [Kotlin]
val label = case<String, String>(order.status) {
    `when`("PENDING") then "결제 대기"
    `when`("PAID") then "결제 완료"
    `when`("SHIPPED") then "배송 중"
    otherwise("알 수 없음")
}
```

```sql [SQL]
CASE o.status
    WHEN 'PENDING' THEN '결제 대기'
    WHEN 'PAID' THEN '결제 완료'
    WHEN 'SHIPPED' THEN '배송 중'
    ELSE '알 수 없음'
END
```

:::

::: tip Searched vs Simple
하나의 컬럼을 리터럴 값들과 비교할 때는 **Simple CASE**를 사용하세요.
각 분기가 서로 다른 조건을 갖거나 여러 컬럼을 비교할 때는 **Searched CASE**를 사용하세요.
:::

---

## Null 안전성

기본 QueryDSL의 CASE 빌더와 비교했을 때 querydsl-ktx의 CASE DSL이 진짜 빛나는 부분입니다.
동적 조건으로 CASE 표현식을 만들 때, null predicate는 자동으로 건너뜁니다:

| 시나리오 | 동작 |
|---------|------|
| `when(null)` | 분기가 조용히 건너뛰어짐 |
| 모든 predicate가 null | `case {}`가 `null`을 반환 |
| non-null predicate | 분기가 정상적으로 추가됨 |

### 선택적 조건이 포함된 동적 CASE

```kotlin
val keyword: String? = request.keyword  // null일 수 있음

val matchScore = case<Int> {
    `when`(product.name.eq(keyword)) then 100    // keyword가 null이면 건너뜀
    `when`(product.name contains keyword) then 50 // keyword가 null이면 건너뜀
    `when`(product.active.eq(true)) then 10       // 항상 추가
    otherwise(0)
}
// keyword가 null이면: CASE WHEN active = true THEN 10 ELSE 0 END
// keyword가 "phone"이면: 3개 분기 전체 CASE
```

기본 QueryDSL에서는 각 분기를 `if`로 감싸야 합니다. DSL이 자동으로 처리해 줍니다.

---

## Before / After

::: code-group

```kotlin [Before (기본 QueryDSL)]
// 백틱 지옥 + 장황한 체이닝
CaseBuilder()
    .`when`(member.grade.eq("VIP")).then(20)
    .`when`(member.grade.eq("GOLD")).then(10)
    .`when`(member.grade.eq("SILVER")).then(5)
    .otherwise(0)

// null 안전성이 필요하다면? 행운을 빕니다:
val builder = CaseBuilder()
if (keyword != null) {
    builder.`when`(product.name.eq(keyword)).then(100)
}
// CaseBuilder는 조건부 분기 추가를 지원하지 않습니다...
```

```kotlin [After (querydsl-ktx)]
case<Int> {
    `when`(member.grade.eq("VIP")) then 20
    `when`(member.grade.eq("GOLD")) then 10
    `when`(member.grade.eq("SILVER")) then 5
    otherwise(0)
}

// Null-safe 분기 -- 그냥 됩니다
case<Int> {
    `when`(product.name.eq(keyword)) then 100  // null keyword → 건너뜀
    `when`(product.active.eq(true)) then 10
    otherwise(0)
}
```

:::

DSL 버전이 제공하는 것:

- **Infix `then`** -- SQL처럼 읽힘, `.then()` 체인 없음
- **Null-safe 분기** -- null predicate는 자동으로 건너뜀
- **타입 추론** -- `case<Int>`로 반환 타입을 한 번만 지정

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
