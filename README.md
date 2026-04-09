# querydsl-ktx

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

Null-safe infix Kotlin extensions for QueryDSL dynamic queries.

[한국어](README.ko.md)

## Before & After

```kotlin
// Before — verbose BooleanBuilder with manual null checks
val builder = BooleanBuilder()
if (name != null) builder.and(entity.name.eq(name))
if (status != null) builder.and(entity.status.eq(status))
if (minAge != null) builder.and(entity.age.goe(minAge))
if (maxAge != null) builder.and(entity.age.loe(maxAge))

// After — declarative null-safe infix
var where: BooleanExpression? = null
where = where and (entity.name eq name)
where = where and (entity.status eq status)
where = where and (entity.age goe minAge)
where = where and (entity.age loe maxAge)
```

Null conditions are automatically skipped. Only non-null conditions accumulate.

## Requirements

| Dependency | Minimum Version |
|------------|----------------|
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
| `querydsl-ktx` | Core — Extension interfaces + QuerydslRepository base class |
| `querydsl-ktx-spring-boot` | AutoConfiguration — JPAQueryFactory auto-registration |
| `querydsl-ktx-spring-boot-starter` | Starter — aggregates the above modules |

Use only `querydsl-ktx` if you just need the extension interfaces.

## Extension Interfaces

Seven null-safe infix extension interfaces, scoped through interface implementation.

### BooleanExpressionExtensions

```kotlin
// AND/OR — null side ignored, non-null side preserved
condition1 and condition2     // condition1 AND condition2
null and condition            // condition
condition and null            // condition

// Boolean equality
entity.active eq true         // active = true
entity.active eq null         // null (skipped)
```

### SimpleExpressionExtensions

```kotlin
// Equality — works for all types
entity.status eq "ACTIVE"              // status = 'ACTIVE'
entity.status ne "DELETED"             // status != 'DELETED'

// IN / NOT IN
entity.status `in` listOf("A", "B")   // status IN ('A', 'B')
entity.status notIn listOf("C")       // status NOT IN ('C')
```

### ComparableExpressionExtensions

```kotlin
// Comparison operators
entity.date gt startDate       // date > ?
entity.date goe startDate      // date >= ?
entity.date lt endDate         // date < ?
entity.date loe endDate        // date <= ?

// BETWEEN with Pair — one-sided when partially null
entity.date between (from to to)       // date BETWEEN ? AND ?
entity.date between (from to null)     // date >= ?
entity.date between (null to to)       // date <= ?

// BETWEEN with ClosedRange
entity.age between (20..60)            // age BETWEEN 20 AND 60
```

### NumberExpressionExtensions

Same API as `ComparableExpressionExtensions`, provided separately because `NumberExpression` does not extend `ComparableExpression`.

```kotlin
entity.price gt 10000
entity.price between (min to max)
entity.score between (0..100)
```

### StringExpressionExtensions

```kotlin
entity.name contains keyword              // name LIKE '%keyword%'
entity.name containsIgnoreCase keyword     // case-insensitive
entity.name startsWith prefix             // name LIKE 'prefix%'
entity.name endsWith suffix               // name LIKE '%suffix'
entity.name like pattern                  // name LIKE pattern
entity.name matches regex                 // name REGEXP regex
entity.email equalsIgnoreCase email       // case-insensitive equality
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

Full base class implementing all extension interfaces with pagination, sorting, and DML helpers.

### Dynamic Query — Before & After

```kotlin
// Before — manual null checks, verbose BooleanBuilder
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
// After — null-safe infix, declarative
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

### Pagination — Before & After

```kotlin
// Before — manual Slice construction
val content = query.offset(pageable.offset).limit(pageable.pageSize.toLong()).fetch()
val hasNext = content.size == pageable.pageSize  // false positive on last page
SliceImpl(content, pageable, hasNext)

// After — accurate hasNext via pageSize + 1
query.slice(pageable)

// Or with raw values
query.slice(page = 0, size = 20)
query.page(page = 0, size = 20)
query.fetch(offset = 0, limit = 20)
```

### Bulk DML — Before & After

```kotlin
// Before — easy to forget flush/clear
queryFactory.update(member).set(member.active, false).where(...).execute()
entityManager.flush()   // forgot this? stale persistence context
entityManager.clear()   // forgot this? stale reads

// After — flush + clear guaranteed
modifying {
    update(member).set(member.active, false).where(...).execute()
}

// Control individually (same flags as @Modifying)
modifying(flushAutomatically = false) { ... }
modifying(clearAutomatically = false) { ... }
```

### QuerydslSupport

Use when you only need query/pagination helpers without the infix extensions:

```kotlin
@Repository
class MyRepository : QuerydslSupport<MyEntity>() {
    override val domainClass = MyEntity::class.java
    // select, page, slice, modifying available
    // infix extensions not included
}
```

## Null-Safety Design

All extension functions accept nullable receivers and nullable arguments.

| Function type | this null | arg null | both null |
|--------------|-----------|----------|-----------|
| `and` / `or` | returns arg | returns this | null |
| `between(Pair)` | null | one-sided comparison | null |
| Others (`eq`, `gt`, `contains`, ...) | null | null | null |

## AutoConfiguration

The starter auto-registers a `JPAQueryFactory` bean:

- `@ConditionalOnClass(JPAQueryFactory)` — only when QueryDSL is on the classpath
- `@ConditionalOnMissingBean` — respects custom beans
- Runs after `HibernateJpaAutoConfiguration`

If you already register a `JPAQueryFactory` bean (e.g., with custom `JPQLTemplates`), it takes precedence.

## License

[Apache License 2.0](LICENSE)
