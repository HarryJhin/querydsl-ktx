# Quick Start

Write your first null-safe dynamic query in 5 minutes.

---

## Step 1: Add the dependency

=== "Gradle (Kotlin DSL)"

    ```kotlin
    implementation("io.github.harryjhin:querydsl-ktx-spring-boot-starter:0.2.0")
    ```

=== "Gradle (Groovy DSL)"

    ```groovy
    implementation 'io.github.harryjhin:querydsl-ktx-spring-boot-starter:0.2.0'
    ```

See [Installation](installation.md) for Maven and module selection details.

---

## Step 2: Extend QuerydslRepository

```kotlin
@Repository
class MemberQueryRepository : QuerydslRepository<Member>() {

    private val member = QMember.member
}
```

`QuerydslRepository<T>` gives you:

- All 7 null-safe infix extension interfaces
- `JPAQueryFactory` wrappers (`selectFrom`, `select`, `update`, `delete`)
- Pagination helpers (`page`, `slice`, `fetch`)
- Bulk DML helper (`modifying`)

The type parameter `T` is resolved automatically -- no need to override `domainClass`.

---

## Step 3: Write a dynamic query

```kotlin
@Repository
class MemberQueryRepository : QuerydslRepository<Member>() {

    private val member = QMember.member

    fun search(
        name: String?,       // (1)!
        status: Status?,
        minAge: Int?,
        maxAge: Int?,
        pageable: Pageable,
    ): Page<Member> =
        selectFrom(member)
            .where(
                member.name contains name,       // (2)!
                member.status eq status,          // (3)!
                member.age between (minAge to maxAge), // (4)!
            )
            .page(pageable)                      // (5)!
}
```

1. All parameters are nullable -- the caller decides which filters to apply.
2. `contains` on a `StringExpression` generates `LIKE '%name%'`. When `name` is null, the condition is skipped entirely.
3. `eq` on a `SimpleExpression` generates `status = ?`. When `status` is null, skipped.
4. `between` with a `Pair` handles partial ranges: both null = skip, one null = `>=` or `<=`, both present = `BETWEEN`.
5. `page()` applies offset/limit and runs a count query automatically.

---

## Step 4: Call it

```kotlin
@Service
class MemberService(
    private val memberQueryRepository: MemberQueryRepository,
) {
    // All filters applied
    fun searchAll() = memberQueryRepository.search(
        name = "John",
        status = Status.ACTIVE,
        minAge = 20,
        maxAge = 60,
        pageable = PageRequest.of(0, 20),
    )

    // Only name filter -- other conditions skipped automatically
    fun searchByName() = memberQueryRepository.search(
        name = "John",
        status = null,
        minAge = null,
        maxAge = null,
        pageable = PageRequest.of(0, 20),
    )
}
```

=== "All filters (generated SQL)"

    ```sql
    SELECT m.*
    FROM member m
    WHERE m.name LIKE '%John%'
      AND m.status = 'ACTIVE'
      AND m.age BETWEEN 20 AND 60
    LIMIT 20 OFFSET 0
    ```

=== "Name only (generated SQL)"

    ```sql
    SELECT m.*
    FROM member m
    WHERE m.name LIKE '%John%'
    LIMIT 20 OFFSET 0
    ```

!!! tip "No BooleanBuilder, no if-checks"
    The same repository method handles any combination of filters.
    Null parameters are simply ignored -- no conditional logic needed.

---

## Full Example

```kotlin
@Entity
class Member(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val name: String,
    @Enumerated(EnumType.STRING)
    val status: Status,
    val age: Int,
)

enum class Status { ACTIVE, INACTIVE, DELETED }

@Repository
class MemberQueryRepository : QuerydslRepository<Member>() {

    private val member = QMember.member

    fun search(
        name: String?,
        status: Status?,
        minAge: Int?,
        maxAge: Int?,
        pageable: Pageable,
    ): Page<Member> =
        selectFrom(member)
            .where(
                member.name contains name,
                member.status eq status,
                member.age between (minAge to maxAge),
            )
            .page(pageable)

    fun findByStatus(status: Status): List<Member> =
        selectFrom(member)
            .where(member.status eq status)
            .fetch()
}
```

---

## What's Next?

- [Dynamic Queries](../guide/dynamic-queries.md) -- Understand the null-safety contract in depth
- [Extension Reference](../guide/extensions.md) -- All 7 interfaces with function listings
- [Pagination](../guide/pagination.md) -- slice vs page vs fetch
- [Bulk DML](../guide/bulk-dml.md) -- Safe update and delete operations
