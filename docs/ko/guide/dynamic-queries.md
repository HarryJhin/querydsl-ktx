# 동적 쿼리

## QueryDSL 동적 쿼리의 발전 과정

Kotlin에서 QueryDSL을 써봤다면, 아마 이런 순서로 발전해 왔을 겁니다:

**1단계: BooleanBuilder** -- 대부분 처음 배우는 패턴입니다.

```kotlin
val builder = BooleanBuilder()
if (name != null) builder.and(member.name.contains(name))
if (status != null) builder.and(member.status.eq(status))
```

**2단계: 필드별 헬퍼 함수** -- `BooleanExpression?`을 반환하는 메서드를 조건마다 작성합니다.
인프런 김영한 강의에서 소개하는 패턴이 이것입니다:

```kotlin
fun statusEq(status: String?): BooleanExpression? =
    status?.let { member.status.eq(it) }

fun nameLike(name: String?): BooleanExpression? =
    name?.let { member.name.contains(it) }

// 사용
selectFrom(member)
    .where(statusEq(status), nameLike(name))
    .fetch()
```

**3단계: querydsl-ktx** -- 동일한 null-safe 동작이지만, 필드마다 헬퍼 함수를 작성할 필요가 없습니다.

```kotlin
selectFrom(member)
    .where(
        member.status eq status,
        member.name contains name,
    )
    .fetch()
```

세 가지 접근 방식 모두 같은 SQL을 생성합니다. 차이는 보일러플레이트의 양입니다.

---

## 핵심 개념: null = 건너뛰기

querydsl-ktx의 모든 확장 함수는 하나의 규칙을 따릅니다:

> **인자가 null이면 조건이 적용되지 않습니다.**

이것으로 `BooleanBuilder` + `if-null-check` 패턴을 완전히 제거할 수 있습니다.

::: code-group

```kotlin [이전]
val builder = BooleanBuilder()
if (name != null) builder.and(entity.name.contains(name))
if (status != null) builder.and(entity.status.eq(status))
if (from != null && to != null) {
    builder.and(entity.createdAt.between(from, to))
} else if (from != null) {
    builder.and(entity.createdAt.goe(from))
} else if (to != null) {
    builder.and(entity.createdAt.loe(to))
}
```

```kotlin [이후]
var where: BooleanExpression? = null
where = where and (entity.name contains name)
where = where and (entity.status eq status)
where = where and (entity.createdAt between (from to to))
```

:::

`QuerydslRepository` 내부에서는 더 짧게 쓸 수 있습니다:

```kotlin
selectFrom(entity)
    .where(
        entity.name contains name,
        entity.status eq status,
        entity.createdAt between (from to to),
    )
    .fetch()
```

::: info `.where()`의 null 처리 방식
QueryDSL의 `.where()`는 vararg 오버로드에서 이미 null 프레디킷을 무시합니다.
querydsl-ktx의 null 반환 확장 함수와 결합하면, null 파라미터가
모든 레벨에서 투명하게 필터링됩니다.
:::

---

## 필드별 헬퍼 함수와 비교

인프런 강의 스타일의 필드별 헬퍼 함수를 작성해 본 적이 있을 겁니다:

::: code-group

```kotlin [필드별 헬퍼 (직접 작성)]
private fun statusEq(status: String?): BooleanExpression? =
    status?.let { member.status.eq(it) }

private fun nameLike(name: String?): BooleanExpression? =
    name?.let { member.name.contains(it) }

private fun ageBetween(min: Int?, max: Int?): BooleanExpression? {
    if (min != null && max != null) return member.age.between(min, max)
    if (min != null) return member.age.goe(min)
    if (max != null) return member.age.loe(max)
    return null
}

fun search(status: String?, name: String?, minAge: Int?, maxAge: Int?) =
    selectFrom(member)
        .where(statusEq(status), nameLike(name), ageBetween(minAge, maxAge))
        .fetch()
```

```kotlin [querydsl-ktx (헬퍼 불필요)]
fun search(status: String?, name: String?, minAge: Int?, maxAge: Int?) =
    selectFrom(member)
        .where(
            member.status eq status,
            member.name contains name,
            member.age between (minAge to maxAge),
        )
        .fetch()
```

:::

querydsl-ktx 버전은 정확히 같은 일을 합니다 -- `member.status eq null`은 `null`을 반환하고,
`.where()`가 이를 무시합니다. `Pair`를 사용한 `between`은 네 가지 조합
(양쪽 값, min만, max만, 둘 다 없음)을 하나의 표현식으로 처리합니다.

::: tip 그래도 헬퍼를 추출해야 할 때
querydsl-ktx가 있다고 메서드 추출이 필요 없다는 뜻은 아닙니다. 여러 필드를 조합하거나
비즈니스 로직이 포함된 복잡한 조건은 여전히 이름이 있는 메서드가 낫습니다:

```kotlin
// 이건 여전히 메서드로 추출하는 게 깔끔합니다
private fun isEligibleForPromotion(): BooleanExpression? =
    (member.grade eq "VIP") or (
        (member.totalPurchase goe 100000) and (member.active eq true)
    )
```

기준: 조건이 필드 1:1 매핑이면 인라인 확장 함수를 쓰세요.
비즈니스 로직을 인코딩하면 메서드로 추출하세요.
:::

---

## AND / OR 체이닝

### 기본 AND

`and` infix 함수를 사용하여 nullable 조건을 결합합니다:

```kotlin
var predicate: BooleanExpression? = null
predicate = predicate and (entity.name eq name)
predicate = predicate and (entity.active eq true)
```

| `this` | `right` | 결과 |
|--------|---------|--------|
| non-null | non-null | `this AND right` |
| null | non-null | `right` |
| non-null | null | `this` |
| null | null | `null` |

### 기본 OR

대칭적으로 동작합니다:

```kotlin
var predicate: BooleanExpression? = null
predicate = predicate or (entity.role eq "ADMIN")
predicate = predicate or (entity.role eq "MANAGER")
```

| `this` | `right` | 결과 |
|--------|---------|--------|
| non-null | non-null | `this OR right` |
| null | non-null | `right` |
| non-null | null | `this` |
| null | null | `null` |

### AND와 OR 결합

```kotlin
val rolePredicate = (entity.role eq "ADMIN") or (entity.role eq "MANAGER")
val predicate = (entity.active eq true) and rolePredicate
```

::: code-group

```kotlin [Kotlin]
selectFrom(entity)
    .where(
        entity.active eq true,
        (entity.role eq "ADMIN") or (entity.role eq "MANAGER"),
    )
    .fetch()
```

```sql [SQL]
SELECT e.*
FROM entity e
WHERE e.active = true
  AND (e.role = 'ADMIN' OR e.role = 'MANAGER')
```

:::

---

## Null-Safety 계약

모든 확장 함수는 일관된 null 동작을 따릅니다:

### 결합자 (and / or)

| `this` | `arg` | 둘 다 null | 결과 |
|--------|-------|-----------|--------|
| arg 반환 | this 반환 | `null` | non-null 쪽을 보존 |

### Between (Pair 오버로드)

| `this` | `from` | `to` | 결과 |
|--------|--------|------|--------|
| non-null | non-null | non-null | `BETWEEN from AND to` |
| non-null | non-null | null | `>= from` |
| non-null | null | non-null | `<= to` |
| non-null | null | null | `null` (건너뛰기) |
| null | any | any | `null` (건너뛰기) |

### 기타 모든 함수 (eq, gt, contains, ...)

| `this` null | `arg` null | 둘 다 null | 결과 |
|-------------|------------|-----------|--------|
| `null` | `null` | `null` | `null` (건너뛰기) |

::: warning null 표현식 vs null 인자
`this`가 null인 경우는 QueryDSL 표현식 자체가 null인 것입니다 (실제로는 드묾).
`arg`가 null인 경우는 필터 파라미터가 제공되지 않은 것입니다 -- 일반적인 경우.
:::

---

## 복합 조건

### andAnyOf -- AND와 OR 그룹

"기본 조건 AND (이 중 하나라도)":

::: code-group

```kotlin [Kotlin]
val predicate = (entity.active eq true) andAnyOf listOf(
    entity.role eq role,
    entity.department eq department,
)
```

```sql [SQL]
active = true AND (role = ? OR department = ?)
```

:::

`role`과 `department`가 모두 null이면 OR 그룹이 null로 축소되고, 기본 조건만 남습니다.

### orAllOf -- OR과 AND 그룹

"기본 조건 OR (이것 모두)":

::: code-group

```kotlin [Kotlin]
val predicate = (entity.vip eq true) orAllOf listOf(
    entity.age goe minAge,
    entity.active eq true,
)
```

```sql [SQL]
vip = true OR (age >= ? AND active = true)
```

:::

---

## 조건을 점진적으로 구성하기

런타임 로직에 따라 조건을 구성해야 하는 경우:

```kotlin
fun search(criteria: SearchCriteria): List<Entity> {
    var where: BooleanExpression? = null

    // 항상 적용되는 조건
    where = where and (entity.deleted eq false)

    // 조건부 그룹
    if (criteria.hasKeyword()) {
        where = where and (
            (entity.name contains criteria.keyword)
            or (entity.description contains criteria.keyword)
        )
    }

    // Null-safe 조건 -- if 검사 불필요
    where = where and (entity.status eq criteria.status)
    where = where and (entity.createdAt between (criteria.from to criteria.to))

    return selectFrom(entity).where(where).fetch()
}
```

::: tip `if` vs null-safety 사용 시점
- **단순 null 검사** -- 확장 함수에 맡기세요. `entity.status eq criteria.status`로 충분합니다.
- **복잡한 로직** (예: 여러 필드에 걸친 키워드 검색) -- 명시적 `if` 블록으로 서브 표현식을 구성한 후 `and`로 결합하세요.
:::

---

## 실전 예제: 어드민 검색 페이지

여러 선택적 필터가 있는 전형적인 어드민 검색:

```kotlin
@Repository
class OrderRepository : QuerydslRepository<Order>() {

    private val order = QOrder.order
    private val member = QMember.member

    fun adminSearch(
        orderNumber: String?,
        memberName: String?,
        status: OrderStatus?,
        minAmount: Int?,
        maxAmount: Int?,
        from: LocalDateTime?,
        to: LocalDateTime?,
        pageable: Pageable,
    ): Page<Order> =
        selectFrom(order)
            .join(order.member, member)
            .where(
                order.orderNumber contains orderNumber,
                member.name contains memberName,
                order.status eq status,
                order.totalAmount between (minAmount to maxAmount),
                order.createdAt between (from to to),
            )
            .page(pageable)
}
```

모든 파라미터가 nullable입니다. 어드민이 "회원명"과 "상태"만 입력하면
SQL에는 그 두 조건만 나타납니다. BooleanBuilder도, 헬퍼 함수도,
분기 로직도 없습니다.

---

## QuerydslRepository 없이 확장 함수 사용하기

어떤 클래스에서든 확장 인터페이스를 직접 구현하여 사용할 수 있습니다:

```kotlin
class MyService : BooleanExpressionExtensions, SimpleExpressionExtensions {

    fun buildPredicate(name: String?, status: String?): BooleanExpression? {
        var where: BooleanExpression? = null
        where = where and (entity.name eq name)
        where = where and (entity.status eq status)
        return where
    }
}
```

또는 `QuerydslSupport`를 확장하는 리포지토리에서 일부만 구현할 수도 있습니다:

```kotlin
@Repository
class MinimalRepository : QuerydslSupport<MyEntity>(),
    SimpleExpressionExtensions,
    StringExpressionExtensions {

    override val domainClass = MyEntity::class.java

    // eq, ne, in, notIn, contains, startsWith 등만 스코프 내에서 사용 가능
}
```
