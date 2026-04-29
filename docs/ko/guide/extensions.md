---
description: eq, contains, between, exists 등 8개 null-safe 확장 인터페이스 레퍼런스.
---

# 확장 인터페이스

querydsl-ktx는 **8개의 확장 인터페이스**를 제공하며, 각각 특정 QueryDSL 표현식 타입에 스코프가 지정됩니다.
모든 함수는 null-safe합니다: null 인자는 조건을 건너뛰게 합니다.

---

## 개요

| 인터페이스 | 표현식 타입 | 주요 함수 |
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

Null-safe AND/OR 결합자. 동적 WHERE 절 구성의 기반입니다.

### 함수

| 함수 | 시그니처 | SQL |
|----------|-----------|-----|
| `and` | `BooleanExpression?.and(BooleanExpression?)` | `a AND b` |
| `or` | `BooleanExpression?.or(BooleanExpression?)` | `a OR b` |
| `andAnyOf` | `BooleanExpression?.andAnyOf(List<BooleanExpression?>)` | `a AND (b OR c OR ...)` |
| `orAllOf` | `BooleanExpression?.orAllOf(List<BooleanExpression?>)` | `a OR (b AND c AND ...)` |
| `eq` | `BooleanExpression?.eq(Boolean?)` | `active = true` |
| `nullif` | `BooleanExpression?.nullif(Boolean?)` | `NULLIF(active, true)` |
| `coalesce` | `BooleanExpression?.coalesce(Boolean?)` | `COALESCE(active, false)` |

### 예제

::: code-group

```kotlin [Kotlin]
// AND: null 쪽은 무시됨
val predicate = (entity.active eq true) and (entity.name eq name)

// OR 그룹
val rolePredicate = (entity.role eq "ADMIN") or (entity.role eq "MANAGER")

// AND와 OR 서브그룹
val complex = (entity.active eq true) andAnyOf listOf(
    entity.role eq role,
    entity.department eq department,
)
```

```sql [SQL]
-- AND (name = 'John')
active = true AND name = 'John'

-- AND (name = null) -> 왼쪽만 남음
active = true

-- OR 그룹
role = 'ADMIN' OR role = 'MANAGER'

-- AND와 OR 서브그룹
active = true AND (role = ? OR department = ?)
```

:::

::: tip vararg 오버로드
`andAnyOf`와 `orAllOf`는 vararg `BooleanExpression?`도 받습니다.
Kotlin은 infix 함수에 vararg를 허용하지 않으므로 점 표기법으로 호출합니다:
`predicate.andAnyOf(p1, p2, p3)`.
:::

---

## SimpleExpressionExtensions

모든 표현식 타입에 대한 동등성 및 멤버십 연산자.

### 함수

| 함수 | 시그니처 | SQL |
|----------|-----------|-----|
| `eq` | `SimpleExpression<T>?.eq(T?)` | `status = ?` |
| `eq` | `SimpleExpression<T>?.eq(Expression<in T>?)` | `status = default_status` |
| `ne` | `SimpleExpression<T>?.ne(T?)` | `status != ?` |
| `ne` | `SimpleExpression<T>?.ne(Expression<in T>?)` | `status != default_status` |
| `in` | `SimpleExpression<T>?.in(Collection<T>?)` | `status IN (?, ?)` |
| `notIn` | `SimpleExpression<T>?.notIn(Collection<T>?)` | `status NOT IN (?, ?)` |
| `inChunked` | `SimpleExpression<T>?.inChunked(Collection<T>?)` | `col IN (?) OR col IN (?)` |

### 예제

::: code-group

```kotlin [Kotlin]
// 동등성
entity.status eq "ACTIVE"              // status = 'ACTIVE'
entity.status eq null                  // null (건너뛰기)

// 부등식
entity.status ne "DELETED"             // status != 'DELETED'

// IN / NOT IN
entity.status `in` listOf("A", "B")   // status IN ('A', 'B')
entity.status notIn listOf("C")       // status NOT IN ('C')
entity.status `in` null               // null (건너뛰기)

// 대량 IN절 자동 분할 (기본 1000개씩)
entity.id inChunked largeIdList   // id IN (1..1000) OR id IN (1001..2000)
entity.id.inChunked(ids, 500)     // 커스텀 청크 사이즈
```

```sql [SQL]
status = 'ACTIVE'
status != 'DELETED'
status IN ('A', 'B')
status NOT IN ('C')
```

:::

::: tip Oracle의 IN 절 제한 대응
Oracle은 단일 IN 절에 1000개 항목 제한이 있습니다.
`inChunked`는 대량 컬렉션을 자동으로 여러 IN 절로 분할하고 OR로 연결합니다.
기본 청크 사이즈는 1000이며, 커스텀 사이즈 지정이 가능합니다.
:::

---

## ComparableExpressionExtensions

`Comparable` 타입(날짜, 문자열, enum 등)에 대한 비교 및 범위 연산자.

### 함수

| 함수 | 시그니처 | SQL |
|----------|-----------|-----|
| `gt` | `ComparableExpression<T>?.gt(T?)` | `col > ?` |
| `goe` | `ComparableExpression<T>?.goe(T?)` | `col >= ?` |
| `lt` | `ComparableExpression<T>?.lt(T?)` | `col < ?` |
| `loe` | `ComparableExpression<T>?.loe(T?)` | `col <= ?` |
| `between` | `ComparableExpression<T>?.between(Pair<T?, T?>)` | `col BETWEEN ? AND ?` |
| `between` | `ComparableExpression<T>?.between(ClosedRange<T>)` | `col BETWEEN ? AND ?` |
| `notBetween` | `ComparableExpression<T>?.notBetween(Pair<T?, T?>)` | `col NOT BETWEEN ? AND ?` |
| `between` (역방향) | `T?.between(Pair<ComparableExpression<T>?, ComparableExpression<T>?>)` | `lower <= ? AND upper >= ?` |
| `nullif` | `ComparableExpression<T>?.nullif(T?)` | `NULLIF(col, ?)` |
| `coalesce` | `ComparableExpression<T>?.coalesce(T?)` | `COALESCE(col, ?)` |
| `rangeTo` | `ComparableExpression<T>..ComparableExpression<T>` | _(between용 Pair 생성)_ |

모든 비교 함수에는 다른 컬럼과 비교하기 위한 `Expression<T>` 오버로드도 있습니다.

### 예제

::: code-group

```kotlin [Kotlin]
// 단순 비교
entity.date gt startDate       // date > ?
entity.date goe startDate      // date >= ?
entity.date lt endDate         // date < ?
entity.date loe endDate        // date <= ?

// Pair를 사용한 BETWEEN (부분 범위 지원)
entity.date between (from to to)       // BETWEEN ? AND ?
entity.date between (from to null)     // date >= ?
entity.date between (null to to)       // date <= ?
entity.date between (null to null)     // null (건너뛰기)

// ClosedRange를 사용한 BETWEEN
entity.age between (20..60)            // BETWEEN 20 AND 60

// 역방향 BETWEEN: 값이 왼쪽, 표현식 경계가 오른쪽
now between (sale.startAt to sale.endAt)
// -> start_at <= now AND end_at >= now

// rangeTo 연산자 (..): Pair 생성 syntactic sugar
entity.date between (entity.startDate..entity.endDate)
// 동일: entity.date between (entity.startDate to entity.endDate)
```

```sql [SQL]
-- 전체 범위
created_at BETWEEN '2024-01-01' AND '2024-12-31'

-- 단측 (from만)
created_at >= '2024-01-01'

-- 단측 (to만)
created_at <= '2024-12-31'

-- ClosedRange
age BETWEEN 20 AND 60
```

:::

::: tip 선택적 날짜 범위를 위한 Pair 기반 between
`Pair` 오버로드는 날짜 범위 필터에서 가장 강력한 기능입니다.
하나의 표현식으로 네 가지 조합(양쪽 값, from만, to만, 둘 다 없음)을 모두 처리합니다.
그렇지 않으면 4분기 `if/else`가 필요합니다.
:::

### 역방향 Between: 실전 활용 사례

역방향 `between`은 **값이 왼쪽**, **컬럼 경계가 오른쪽**에 옵니다.
실무에서 의외로 자주 필요한 패턴입니다:

**할인 기간 검증: 쿠폰이 지금 유효한지 확인:**

```kotlin
val now = LocalDateTime.now()
selectFrom(coupon)
    .where(now between (coupon.validFrom to coupon.validUntil))
    .fetch()
// SQL: valid_from <= '2025-04-10T12:00' AND valid_until >= '2025-04-10T12:00'
```

**주문 금액이 할인 구간에 해당하는지 확인:**

```kotlin
val orderAmount = 50000
selectFrom(discountTier)
    .where(orderAmount between (discountTier.minAmount to discountTier.maxAmount))
    .fetch()
// SQL: min_amount <= 50000 AND max_amount >= 50000
```

**이벤트 기간 내 주문 조회:**

```kotlin
// 주문일이 이벤트 기간 안에 있는지
selectFrom(order)
    .where(order.orderedAt between (event.startAt to event.endAt))
    .fetch()
```

역방향 between도 null-safe입니다: 값이 null이면 전체 표현식이 null을 반환합니다(건너뜀).

::: tip 인프런에서 배운 BooleanExpression 패턴과의 비교
김영한 강의에서 배운 BooleanExpression 반환 메서드 패턴을 기억하시나요?

```kotlin
// 강의 스타일: 필드마다 메서드 하나씩
fun isWithinPeriod(now: LocalDateTime?): BooleanExpression? {
    if (now == null) return null
    return event.startAt.loe(now).and(event.endAt.goe(now))
}
```

querydsl-ktx에서는 이 패턴이 별도 메서드 없이 인라인으로 표현됩니다:

```kotlin
// querydsl-ktx: 메서드 추출 불필요
now between (event.startAt to event.endAt)
```
:::

---

## NumberExpressionExtensions

`ComparableExpressionExtensions`와 동일한 API이지만 `NumberExpression`용입니다.

### 별도 인터페이스인 이유

QueryDSL의 타입 계층에서 `NumberExpression`은 `ComparableExpression`을 **확장하지 않습니다**.
이는 QueryDSL의 설계 결정입니다. `NumberExpression`은 `ComparableExpressionBase`를 상속하고,
`ComparableExpression`은 별도의 분기입니다. 따라서 querydsl-ktx는 `NumberExpression`에
특화된 병렬 연산자 세트를 제공합니다.

### 함수

| 함수 | 시그니처 | SQL |
|----------|-----------|-----|
| `gt` | `NumberExpression<T>?.gt(T?)` | `col > ?` |
| `goe` | `NumberExpression<T>?.goe(T?)` | `col >= ?` |
| `lt` | `NumberExpression<T>?.lt(T?)` | `col < ?` |
| `loe` | `NumberExpression<T>?.loe(T?)` | `col <= ?` |
| `between` | `NumberExpression<T>?.between(Pair<T?, T?>)` | `col BETWEEN ? AND ?` |
| `between` | `NumberExpression<T>?.between(ClosedRange<T>)` | `col BETWEEN ? AND ?` |
| `notBetween` | `NumberExpression<T>?.notBetween(Pair<T?, T?>)` | `col NOT BETWEEN ? AND ?` |
| `between` (역방향) | `T?.between(Pair<NumberExpression<T>?, NumberExpression<T>?>)` | `lower <= ? AND upper >= ?` |
| `nullif` | `NumberExpression<T>?.nullif(T?)` | `NULLIF(col, ?)` |
| `coalesce` | `NumberExpression<T>?.coalesce(T?)` | `COALESCE(col, ?)` |
| `rangeTo` | `NumberExpression<T>..NumberExpression<T>` | _(between용 Pair 생성)_ |

### 예제

::: code-group

```kotlin [Kotlin]
entity.price gt 10000
entity.price between (minPrice to maxPrice)
entity.score between (0..100)
entity.quantity loe maxQuantity

// 역방향 BETWEEN: 값이 왼쪽, 표현식 경계가 오른쪽
orderAmount between (tier.minAmount to tier.maxAmount)
// -> min_amount <= orderAmount AND max_amount >= orderAmount

// rangeTo 연산자 (..): Pair 생성 syntactic sugar
orderAmount between (tier.minAmount..tier.maxAmount)
```

```sql [SQL]
price > 10000
price BETWEEN ? AND ?
score BETWEEN 0 AND 100
quantity <= ?
```

:::

---

## StringExpressionExtensions

패턴 매칭 및 문자열 비교 연산자.

### 함수

| 함수 | 시그니처 | SQL |
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

`contains`, `startsWith`, `endsWith`에는 `Expression<String>` 오버로드도 있습니다.

### 예제

::: code-group

```kotlin [Kotlin]
// 부분 문자열 검색
entity.name contains keyword              // name LIKE '%keyword%'
entity.name containsIgnoreCase keyword     // 대소문자 무시

// 접두사 / 접미사
entity.name startsWith prefix             // name LIKE 'prefix%'
entity.name endsWith suffix               // name LIKE '%suffix'

// 패턴과 정규식
entity.name like "J%n"                    // name LIKE 'J%n'
entity.email matches "^[a-z]+@.*"         // name REGEXP '^[a-z]+@.*'

// 대소문자 무시 동등성
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

---

## TemporalExpressionExtensions

날짜/시간 표현식을 위한 시간 비교 연산자.

### 함수

| 함수 | 시그니처 | SQL |
|----------|-----------|-----|
| `after` | `TemporalExpression<T>?.after(T?)` | `col > ?` |
| `after` | `TemporalExpression<T>?.after(Expression<T>?)` | `col > other_col` |
| `before` | `TemporalExpression<T>?.before(T?)` | `col < ?` |
| `before` | `TemporalExpression<T>?.before(Expression<T>?)` | `col < other_col` |

### 예제

::: code-group

```kotlin [Kotlin]
entity.createdAt after startDate     // created_at > ?
entity.createdAt before endDate      // created_at < ?

// 컬럼 비교
entity.endDate after entity.startDate  // end_date > start_date

// Null-safe
entity.createdAt after null          // null (건너뛰기)
```

```sql [SQL]
created_at > '2024-01-01'
created_at < '2024-12-31'
end_date > start_date
```

:::

::: tip after/before vs gt/goe/lt/loe
`TemporalExpression`(날짜, 타임스탬프)에는 `after`/`before`를 사용하고,
`ComparableExpression`이나 `NumberExpression`에는 `gt`/`goe`/`lt`/`loe`를 사용하세요.
생성되는 SQL은 같지만 서로 다른 QueryDSL 타입에 정의되어 있습니다.
:::

---

## CollectionExpressionExtensions

매핑된 컬렉션 필드(예: `@ElementCollection`, `@ManyToMany`)에 대한 멤버십 검사.

### 함수

| 함수 | 시그니처 | SQL |
|----------|-----------|-----|
| `contains` | `CollectionExpressionBase<T, E>?.contains(E?)` | `? IN (col)` |
| `contains` | `CollectionExpressionBase<T, E>?.contains(Expression<E>?)` | `other_col IN (col)` |

### 예제

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

EXISTS / NOT EXISTS 서브쿼리 단축 빌더.

### 함수

| 함수 | 시그니처 | SQL |
|------|----------|-----|
| `exists` | `EntityPath<T>.exists(vararg Predicate?)` | `EXISTS (SELECT 1 FROM ...)` |
| `notExists` | `EntityPath<T>.notExists(vararg Predicate?)` | `NOT EXISTS (SELECT 1 FROM ...)` |

### 예시

::: code-group

```kotlin [Kotlin]
// Before: 장황한 서브쿼리
JPAExpressions.selectOne()
    .from(orderItem)
    .where(orderItem.orderId.eq(order.id))
    .exists()

// After: 간결
orderItem.exists(orderItem.orderId eq order.id)

// NOT EXISTS
orderItem.notExists(orderItem.orderId eq order.id)
```

```sql [SQL]
EXISTS (SELECT 1 FROM order_item WHERE order_item.order_id = order.id)
NOT EXISTS (SELECT 1 FROM order_item WHERE order_item.order_id = order.id)
```

:::

::: info Null 처리
vararg의 null predicate는 자동으로 필터링됩니다.
:::

---

## 선택적 구현

8개의 인터페이스를 모두 사용할 필요는 없습니다. 필요한 것만 선택하세요:

### 옵션 1: QuerydslRepository (전체 포함)

```kotlin
@Repository
class MyRepository : QuerydslRepository<MyEntity>() {
    // 8개 인터페이스 모두 사용 가능
}
```

### 옵션 2: QuerydslSupport + 선택한 인터페이스

```kotlin
@Repository
class MyRepository : QuerydslSupport<MyEntity>(),
    SimpleExpressionExtensions,
    StringExpressionExtensions {

    override val domainClass = MyEntity::class.java

    // eq, ne, in, notIn, contains, startsWith 등만 사용 가능
    // number/comparable/temporal/collection 확장은 없음
}
```

### 옵션 3: 아무 클래스에서 구현

```kotlin
class PredicateBuilder : BooleanExpressionExtensions, SimpleExpressionExtensions {
    // 리포지토리뿐만 아니라 어디서든 확장 함수 사용 가능
}
```

::: info QuerydslRepository vs QuerydslSupport
| | `QuerydslRepository<T>` | `QuerydslSupport<T>` |
|---|---|---|
| 확장 인터페이스 | 8개 전체 포함 | 없음 (필요한 것만 추가) |
| `domainClass` | 제네릭을 통해 자동 추론 | 수동 오버라이드 필요 |
| 사용 시기 | 전체 기능이 필요할 때 | 최소한의 API 표면만 원할 때 |
:::
