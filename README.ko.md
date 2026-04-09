# querydsl-ktx

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

[English](README.md)

QueryDSL의 동적 쿼리를 Kotlin 답게 작성할 수 있는 null-safe infix 확장 라이브러리입니다.

## Before & After

```kotlin
// Before — BooleanBuilder + null 체크 반복
val builder = BooleanBuilder()
if (name != null) builder.and(entity.name.eq(name))
if (status != null) builder.and(entity.status.eq(status))
if (minAge != null) builder.and(entity.age.goe(minAge))
if (maxAge != null) builder.and(entity.age.loe(maxAge))
if (keyword != null) builder.and(entity.name.contains(keyword))

// After — null-safe infix로 선언적
var where: BooleanExpression? = null
where = where and (entity.name eq name)
where = where and (entity.status eq status)
where = where and (entity.age goe minAge)
where = where and (entity.age loe maxAge)
where = where and (entity.name contains keyword)
```

null인 조건은 자동으로 무시되고, non-null인 조건만 누적됩니다.

## Requirements

| 의존성 | 최소 버전 |
|--------|----------|
| Spring Boot | 3.0+ |
| QueryDSL | 5.1.0+ |
| Kotlin | 1.7+ |
| Java | 17+ |

## Installation

### Gradle (Kotlin DSL)

```kotlin
implementation("io.github.harryjhin:querydsl-ktx-spring-boot-starter:0.0.1")
```

### Gradle (Groovy DSL)

```groovy
implementation 'io.github.harryjhin:querydsl-ktx-spring-boot-starter:0.0.1'
```

### Maven

```xml
<dependency>
    <groupId>io.github.harryjhin</groupId>
    <artifactId>querydsl-ktx-spring-boot-starter</artifactId>
    <version>0.0.1</version>
</dependency>
```

## Modules

| Module | Description |
|--------|-------------|
| `querydsl-ktx` | 코어 — Extension 인터페이스 + QuerydslRepository base class |
| `querydsl-ktx-spring-boot` | AutoConfiguration — JPAQueryFactory 자동 등록 |
| `querydsl-ktx-spring-boot-starter` | Starter — 위 두 모듈 조립 |

Extension 인터페이스만 사용하려면 `querydsl-ktx`만 의존해도 됩니다.

## Extension Interfaces

7개의 null-safe infix 확장 인터페이스를 제공합니다. 인터페이스를 implement하면 해당 스코프에서 infix 함수를 사용할 수 있습니다.

### BooleanExpressionExtensions

```kotlin
// AND/OR — null인 쪽 무시, non-null 살림
condition1 and condition2     // condition1 AND condition2
condition1 or condition2      // condition1 OR condition2
null and condition            // condition (null 무시)
condition and null            // condition (null 무시)

// Boolean 비교
entity.active eq true         // active = true
entity.active eq null         // null (조건 스킵)
```

### SimpleExpressionExtensions

```kotlin
// 동등/부등 비교 — 모든 타입에 적용
entity.status eq "ACTIVE"          // status = 'ACTIVE'
entity.status ne "DELETED"         // status != 'DELETED'
entity.status eq null              // null (조건 스킵)

// IN/NOT IN
entity.status `in` listOf("A", "B")   // status IN ('A', 'B')
entity.status notIn listOf("C")       // status NOT IN ('C')
entity.status `in` null               // null (조건 스킵)
```

### ComparableExpressionExtensions

```kotlin
// 비교 연산자
entity.date gt startDate       // date > ?
entity.date goe startDate      // date >= ?
entity.date lt endDate         // date < ?
entity.date loe endDate        // date <= ?

// BETWEEN — Pair로 범위 지정
entity.date between (from to to)         // date BETWEEN ? AND ?
entity.date between (from to null)       // date >= ? (한쪽만)
entity.date between (null to to)         // date <= ? (한쪽만)

// ClosedRange도 지원
entity.age between (20..60)              // age BETWEEN 20 AND 60
```

### NumberExpressionExtensions

`ComparableExpressionExtensions`와 동일한 API. `NumberExpression`은 `ComparableExpression`을 상속하지 않으므로 별도 인터페이스로 제공합니다.

```kotlin
entity.price gt 10000          // price > 10000
entity.price between (min to max)
entity.score between (0..100)
```

### StringExpressionExtensions

```kotlin
entity.name contains keyword              // name LIKE '%keyword%'
entity.name containsIgnoreCase keyword     // LOWER(name) LIKE LOWER('%keyword%')
entity.name startsWith prefix             // name LIKE 'prefix%'
entity.name endsWith suffix               // name LIKE '%suffix'
entity.name like pattern                  // name LIKE pattern
entity.name matches regex                 // name REGEXP regex
entity.email equalsIgnoreCase email       // LOWER(email) = LOWER(email)
```

### TemporalExpressionExtensions

```kotlin
entity.createdAt after startDate     // created_at > ?
entity.createdAt before endDate      // created_at < ?
```

### CollectionExpressionExtensions

```kotlin
entity.roles contains "ADMIN"       // 'ADMIN' IN (roles)
```

## QuerydslRepository

모든 Extension 인터페이스를 구현하고 페이지네이션/정렬/DML 헬퍼를 제공하는 base class입니다.

### 동적 쿼리 — Before & After

```kotlin
// Before — 수동 null 체크, BooleanBuilder
@Repository
class MemberRepository(private val queryFactory: JPAQueryFactory) {

    fun search(name: String?, status: String?, pageable: Pageable): Page<Member> {
        val builder = BooleanBuilder()
        if (name != null) builder.and(member.name.contains(name))
        if (status != null) builder.and(member.status.eq(status))
        return queryFactory.selectFrom(member)
            .where(builder)
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetchResults()
            .let { PageImpl(it.results, pageable, it.total) }
    }
}
```

```kotlin
// After — null-safe infix, 선언적
@Repository
class MemberRepository : QuerydslRepository<Member>() {

    private val member = QMember.member

    fun search(name: String?, status: String?, pageable: Pageable): Page<Member> =
        selectFrom(member)
            .where(
                member.name contains name,
                member.status eq status,
            )
            .page(pageable)
}
```

### 페이지네이션 — Before & After

```kotlin
// Before — 수동 Slice 생성
val content = query.offset(pageable.offset).limit(pageable.pageSize.toLong()).fetch()
val hasNext = content.size == pageable.pageSize  // 마지막 페이지 오판 가능
SliceImpl(content, pageable, hasNext)

// After — pageSize + 1 fetch로 정확한 hasNext 판정
query.slice(pageable)

// 값 기반 오버로드
query.slice(page = 0, size = 20)
query.page(page = 0, size = 20)
query.fetch(offset = 0, limit = 20)
```

### Bulk DML — Before & After

```kotlin
// Before — flush/clear 누락 위험
queryFactory.update(member).set(member.active, false).where(...).execute()
entityManager.flush()   // 빼먹으면? bulk DML이 최신 상태를 못 봄
entityManager.clear()   // 빼먹으면? 이후 조회가 stale 데이터 반환

// After — flush + clear 보장
modifying {
    update(member).set(member.active, false).where(...).execute()
}

// 개별 제어 (@Modifying과 동일한 플래그)
modifying(flushAutomatically = false) { ... }
modifying(clearAutomatically = false) { ... }
```

### QuerydslSupport

Extension 인터페이스 없이 쿼리/페이지네이션 헬퍼만 필요한 경우:

```kotlin
@Repository
class MyRepository : QuerydslSupport<MyEntity>() {
    override val domainClass = MyEntity::class.java
    // select, page, slice, modifying 사용 가능
    // infix 확장은 포함되지 않음
}
```

## Null-Safety Design

모든 Extension 함수는 nullable receiver(`Expression?`)와 nullable argument를 지원합니다.

| 함수 유형 | this null | arg null | 둘 다 null |
|-----------|-----------|----------|-----------|
| `and`/`or` | arg 반환 | this 반환 | null |
| `between(Pair)` | null | 한쪽 비교 | null |
| 그 외 (`eq`, `gt`, `contains` 등) | null | null | null |

## AutoConfiguration

`querydsl-ktx-spring-boot-starter`를 추가하면 `JPAQueryFactory` 빈이 자동 등록됩니다.

- `@ConditionalOnClass(JPAQueryFactory)` — QueryDSL이 classpath에 있을 때만
- `@ConditionalOnMissingBean` — 커스텀 빈이 있으면 존중
- JPA AutoConfiguration 이후 실행

이미 `JPAQueryFactory` 빈을 등록한 프로젝트에서는 기존 빈이 우선합니다.

## License

[Apache License 2.0](LICENSE)
