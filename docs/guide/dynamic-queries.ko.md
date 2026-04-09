# 동적 쿼리

## 핵심 개념: null = 건너뛰기

querydsl-ktx의 모든 확장 함수는 하나의 규칙을 따릅니다:

> **인자가 null이면 조건이 적용되지 않습니다.**

이것으로 `BooleanBuilder` + `if-null-check` 패턴을 완전히 제거할 수 있습니다.

=== "이전"

    ```kotlin
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

=== "이후"

    ```kotlin
    var where: BooleanExpression? = null
    where = where and (entity.name contains name)
    where = where and (entity.status eq status)
    where = where and (entity.createdAt between (from to to))
    ```

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

!!! note "`.where()`의 null 처리 방식"
    QueryDSL의 `.where()`는 vararg 오버로드에서 이미 null 프레디킷을 무시합니다.
    querydsl-ktx의 null 반환 확장 함수와 결합하면, null 파라미터가
    모든 레벨에서 투명하게 필터링됩니다.

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

=== "Kotlin"

    ```kotlin
    selectFrom(entity)
        .where(
            entity.active eq true,
            (entity.role eq "ADMIN") or (entity.role eq "MANAGER"),
        )
        .fetch()
    ```

=== "SQL"

    ```sql
    SELECT e.*
    FROM entity e
    WHERE e.active = true
      AND (e.role = 'ADMIN' OR e.role = 'MANAGER')
    ```

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

!!! warning "null 표현식 vs null 인자"
    `this`가 null인 경우는 QueryDSL 표현식 자체가 null인 것입니다 (실제로는 드묾).
    `arg`가 null인 경우는 필터 파라미터가 제공되지 않은 것입니다 -- 일반적인 경우.

---

## 복합 조건

### andAnyOf -- AND와 OR 그룹

"기본 조건 AND (이 중 하나라도)":

=== "Kotlin"

    ```kotlin
    val predicate = (entity.active eq true) andAnyOf listOf(
        entity.role eq role,
        entity.department eq department,
    )
    ```

=== "SQL"

    ```sql
    active = true AND (role = ? OR department = ?)
    ```

`role`과 `department`가 모두 null이면 OR 그룹이 null로 축소되고, 기본 조건만 남습니다.

### orAllOf -- OR과 AND 그룹

"기본 조건 OR (이것 모두)":

=== "Kotlin"

    ```kotlin
    val predicate = (entity.vip eq true) orAllOf listOf(
        entity.age goe minAge,
        entity.active eq true,
    )
    ```

=== "SQL"

    ```sql
    vip = true OR (age >= ? AND active = true)
    ```

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

!!! tip "`if` vs null-safety 사용 시점"
    - **단순 null 검사** -- 확장 함수에 맡기세요. `entity.status eq criteria.status`로 충분합니다.
    - **복잡한 로직** (예: 여러 필드에 걸친 키워드 검색) -- 명시적 `if` 블록으로 서브 표현식을 구성한 후 `and`로 결합하세요.

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
