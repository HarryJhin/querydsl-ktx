---
description: Production patterns for querydsl-ktx including Condition Objects, SortSpec, custom count queries, and bulk DML.
---

# Best Practices

Patterns that work well with querydsl-ktx in production repositories.
Each section is copy-paste ready and uses real domain entities.

---

## Condition Object + Private Where Extension

The most impactful pattern: wrap a search DTO into a private `where` extension on `JPAQuery`.

::: code-group

```kotlin [Before -- BooleanBuilder]
fun findAll(
    condition: MemberSearchCondition,
    pageable: Pageable,
): Page<Member> {
    val builder = BooleanBuilder()
    if (condition.name != null) {
        builder.and(member.name.contains(condition.name))
    }
    if (condition.status != null) {
        builder.and(member.status.eq(condition.status))
    }
    if (condition.minAge != null && condition.maxAge != null) {
        builder.and(member.age.between(condition.minAge, condition.maxAge))
    } else if (condition.minAge != null) {
        builder.and(member.age.goe(condition.minAge))
    } else if (condition.maxAge != null) {
        builder.and(member.age.loe(condition.maxAge))
    }
    if (condition.from != null && condition.to != null) {
        builder.and(member.createdAt.between(condition.from, condition.to))
    } else if (condition.from != null) {
        builder.and(member.createdAt.goe(condition.from))
    } else if (condition.to != null) {
        builder.and(member.createdAt.loe(condition.to))
    }

    val content = queryFactory.selectFrom(member)
        .where(builder)
        .offset(pageable.offset)
        .limit(pageable.pageSize.toLong())
        .fetch()
    val total = queryFactory.select(member.count())
        .from(member)
        .where(builder)
        .fetchOne() ?: 0L
    return PageImpl(content, pageable, total)
}
```

```kotlin [After -- Condition Object]
// 1. Condition DTO
data class MemberSearchCondition(
    val name: String? = null,
    val status: MemberStatus? = null,
    val minAge: Int? = null,
    val maxAge: Int? = null,
    val from: LocalDateTime? = null,
    val to: LocalDateTime? = null,
)

// 2. Private where extension -- maps condition fields to null-safe predicates
private fun <T> JPAQuery<T>.where(
    condition: MemberSearchCondition,
): JPAQuery<T> = this.where(
    member.name contains condition.name,
    member.status eq condition.status,
    member.age between (condition.minAge to condition.maxAge),
    member.createdAt between (condition.from to condition.to),
)

// 3. Repository method stays clean
fun findAll(condition: MemberSearchCondition, pageable: Pageable): Page<Member> =
    selectFrom(member)
        .where(condition)
        .page(pageable, memberSort)
```

:::

::: tip Why this works
- Each condition field maps to exactly one line
- Null fields are automatically skipped -- no `if` checks
- Partial ranges (only `from`, only `minAge`) degrade gracefully without extra branching
- The same `where(condition)` extension is reusable across multiple query methods
:::

### Reuse across methods

Once the private `where` extension is defined, every query method that uses the same
condition object becomes a one-liner:

```kotlin
fun findAll(condition: MemberSearchCondition, pageable: Pageable): Page<Member> =
    selectFrom(member)
        .where(condition)
        .page(pageable, memberSort)

fun findSlice(condition: MemberSearchCondition, pageable: Pageable): Slice<Member> =
    selectFrom(member)
        .where(condition)
        .slice(pageable, memberSort)

fun count(condition: MemberSearchCondition): Long =
    select(member.count())
        .from(member)
        .where(condition)  // same extension, different select
        .fetchOne() ?: 0L
```

---

## SortSpec as a Repository Property

`SortSpec` is stateless -- define it once as a property and reuse it in every pagination method.

```kotlin
@Repository
class MemberRepository : QuerydslRepository<Member>() {
    private val member = QMember.member

    // Define once, reuse everywhere
    private val memberSort = sortSpec {
        "name" by member.name
        "age" by member.age
        "createdAt" by member.createdAt
    }

    fun findAll(pageable: Pageable): Page<Member> =
        selectFrom(member).page(pageable, memberSort)

    fun findByStatus(status: MemberStatus, pageable: Pageable): Slice<Member> =
        selectFrom(member)
            .where(member.status.eq(status))
            .slice(pageable, memberSort)
}
```

::: tip Security benefit
Client-supplied sort properties (e.g., `?sort=name,asc`) are validated against the
whitelist. Unknown properties like `?sort=password,asc` are silently ignored.
No arbitrary column sorting is possible.
:::

::: info SortSpec is a val, not a function
Because `SortSpec` holds no mutable state, define it as a `private val` property.
There's no reason to rebuild it on every call.
:::

---

## Page with Custom Count Query (Fetch Join)

When a query uses fetch joins, the auto-generated count query produces incorrect results.
Provide a separate count query via the lambda overload.

```kotlin
fun findAllWithDepartment(
    condition: MemberSearchCondition,
    pageable: Pageable,
): Page<Member> =
    selectFrom(member)
        .leftJoin(member.department, department).fetchJoin()
        .where(condition)
        .page(pageable, memberSort) {
            // Separate count query without fetch join
            select(member.count())
                .from(member)
                .where(condition)
                .fetchOne() ?: 0L
        }
```

::: warning Why this matters
The auto-generated count query clones the main query and replaces the select with
`COUNT(*)`. Fetch joins cause the count to multiply -- you get the joined row count
instead of the entity count. This is a QueryDSL limitation, not a querydsl-ktx bug.
:::

::: tip Reuse the condition object
The same `where(condition)` private extension works in both the data query and the
count query. This guarantees the count always matches the data -- no risk of
forgetting to update one when the other changes.
:::

---

## modifying { }: Bulk DML

`modifying { }` calls `entityManager.flush()` before and `entityManager.clear()` after
the block. This requires an active transaction, typically provided by the **service layer**.

```kotlin
// Repository: bulk DML 메서드만 제공
fun deactivateMembers(status: MemberStatus): Long =
    modifying {
        update(member)
            .set(member.status, MemberStatus.INACTIVE)
            .where(member.status.eq(status))
            .execute()
    }
```

```kotlin
// Service: 트랜잭션은 여기서 선언
@Service
@Transactional
class MemberService(
    private val memberRepository: MemberRepository,
) {
    fun deactivateNormalMembers(): Long =
        memberRepository.deactivateMembers(MemberStatus.NORMAL)
}
```

::: warning @Transactional is required on the service, not the repository
Without an active transaction, `flush()` will fail. Declare `@Transactional` on the
service layer, and the repository method participates in that transaction.

> *"We generally recommend declaring transaction boundaries when starting a unit of work
> to ensure proper consistency and desired transaction participation."*
>
> [Spring Data JPA Reference: Transactions](https://docs.spring.io/spring-data/jpa/reference/jpa/transactions.html)

Note: `QuerydslRepository` does not extend Spring Data's `SimpleJpaRepository`,
so its methods do not inherit any default `@Transactional` configuration.
:::

::: info Multiple statements share one flush/clear cycle
When you put multiple DML statements in a single `modifying { }` block, flush
happens once before the first statement and clear happens once after the last.
This is more efficient than wrapping each statement separately.

```kotlin
// Repository
fun archiveAndNotify(cutoffDate: LocalDate): Pair<Long, Long> =
    modifying {
        val archived = update(member)
            .set(member.status, MemberStatus.ARCHIVED)
            .where(member.lastLogin lt cutoffDate)
            .execute()

        val notified = update(notification)
            .set(notification.sent, true)
            .where(notification.memberId `in`
                select(member.id).from(member)
                    .where(member.status eq MemberStatus.ARCHIVED)
            )
            .execute()

        archived to notified
    }
```
:::

---

## Reverse Between for Date Range Validation

Normal `between` checks if a column value falls within a range.
**Reverse between** checks if a value falls within column-defined bounds.

### When both bounds are always present

Use the `..` (rangeTo) operator:

```kotlin
fun findActiveSales(now: LocalDateTime): List<Product> =
    selectFrom(product)
        .where(now between (product.saleStartAt..product.saleEndAt))
        .fetch()
```

```sql
-- SQL: sale_start_at <= '2026-04-10T12:00' AND sale_end_at >= '2026-04-10T12:00'
```

### When bounds can be nullable

Use `to` (Pair) syntax for null-safe degradation:

```kotlin
fun findActiveSales(now: LocalDateTime? = null): List<Product> =
    selectFrom(product)
        .where(now between (product.saleStartAt to product.saleEndAt))
        .fetch()
```

| `now` | `saleStartAt` | `saleEndAt` | Result |
|-------|---------------|-------------|--------|
| non-null | non-null | non-null | `start <= now AND end >= now` |
| non-null | non-null | null | `start <= now` |
| non-null | null | non-null | `end >= now` |
| null | any | any | `null` (skipped) |

::: tip Common use cases
- **Coupon validity**: `now between (coupon.validFrom to coupon.validUntil)`
- **Discount periods**: `now between (discount.startAt to discount.endAt)`
- **Event schedules**: `now between (event.openAt to event.closeAt)`
- **Price tier matching**: `orderAmount between (tier.minAmount to tier.maxAmount)`
:::

---

## Putting It All Together

A complete repository combining all five patterns:

```kotlin
@Repository
class MemberRepository : QuerydslRepository<Member>() {

    private val member = QMember.member
    private val department = QDepartment.department

    // Pattern 2: SortSpec as property
    private val memberSort = sortSpec {
        "name" by member.name
        "age" by member.age
        "createdAt" by member.createdAt
        "department" by department.name
    }

    // Pattern 1: Condition object + private where extension
    private fun <T> JPAQuery<T>.where(
        condition: MemberSearchCondition,
    ): JPAQuery<T> = this.where(
        member.name contains condition.name,
        member.status eq condition.status,
        member.age between (condition.minAge to condition.maxAge),
        member.createdAt between (condition.from to condition.to),
    )

    // Pattern 3: Page with custom count query (fetch join)
    fun findAll(
        condition: MemberSearchCondition,
        pageable: Pageable,
    ): Page<Member> =
        selectFrom(member)
            .leftJoin(member.department, department).fetchJoin()
            .where(condition)
            .page(pageable, memberSort) {
                select(member.count())
                    .from(member)
                    .where(condition)
                    .fetchOne() ?: 0L
            }

    // Pattern 4: modifying (service layer provides @Transactional)
    fun deactivateInactive(cutoffDate: LocalDate): Long =
        modifying {
            update(member)
                .set(member.status, MemberStatus.INACTIVE)
                .where(member.lastLogin lt cutoffDate)
                .execute()
        }
}
```
