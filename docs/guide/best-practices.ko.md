# 베스트 프랙티스

실무 리포지토리에서 querydsl-ktx와 잘 어울리는 패턴을 정리했습니다.
모든 예제는 복사-붙여넣기로 바로 사용할 수 있으며, 실제 도메인 엔티티를 사용합니다.

---

## Condition 객체 + Private Where 확장 함수

가장 효과적인 패턴: 검색 DTO를 `JPAQuery`의 private `where` 확장 함수로 매핑합니다.

=== "이전 -- BooleanBuilder"

    ```kotlin
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

=== "이후 -- Condition 객체"

    ```kotlin
    // 1. Condition DTO
    data class MemberSearchCondition(
        val name: String? = null,
        val status: MemberStatus? = null,
        val minAge: Int? = null,
        val maxAge: Int? = null,
        val from: LocalDateTime? = null,
        val to: LocalDateTime? = null,
    )

    // 2. Private where 확장 함수 -- 조건 필드를 null-safe 프레디킷으로 매핑
    private fun <T> JPAQuery<T>.where(
        condition: MemberSearchCondition,
    ): JPAQuery<T> = this.where(
        member.name contains condition.name,
        member.status eq condition.status,
        member.age between (condition.minAge to condition.maxAge),
        member.createdAt between (condition.from to condition.to),
    )

    // 3. 리포지토리 메서드는 깔끔하게 유지
    fun findAll(condition: MemberSearchCondition, pageable: Pageable): Page<Member> =
        selectFrom(member)
            .where(condition)
            .page(pageable, memberSort)
    ```

!!! tip "이 패턴이 효과적인 이유"
    - 조건 필드 하나가 코드 한 줄에 대응
    - null 필드는 자동으로 건너뜀 -- `if` 분기 불필요
    - 부분 범위(`from`만, `minAge`만)도 추가 분기 없이 자연스럽게 처리
    - 동일한 `where(condition)` 확장 함수를 여러 쿼리 메서드에서 재사용 가능

### 여러 메서드에서 재사용

private `where` 확장 함수를 한 번 정의하면, 같은 조건 객체를 사용하는
모든 쿼리 메서드가 한 줄로 줄어듭니다:

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
        .where(condition)  // 같은 확장 함수, 다른 select
        .fetchOne() ?: 0L
```

---

## SortSpec을 리포지토리 프로퍼티로

`SortSpec`은 상태가 없으므로 프로퍼티로 한 번 정의하고 모든 페이지네이션 메서드에서 재사용합니다.

```kotlin
@Repository
class MemberRepository : QuerydslRepository<Member>() {
    private val member = QMember.member

    // 한 번 정의, 어디서든 재사용
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

!!! tip "보안 이점"
    클라이언트가 보내는 정렬 프로퍼티(예: `?sort=name,asc`)는 화이트리스트에 대해
    검증됩니다. `?sort=password,asc` 같은 알 수 없는 프로퍼티는 무시됩니다.
    임의 컬럼 정렬이 불가능합니다.

!!! note "SortSpec은 val, 함수가 아님"
    `SortSpec`은 변경 가능한 상태를 갖지 않으므로 `private val` 프로퍼티로 정의하세요.
    호출할 때마다 다시 만들 이유가 없습니다.

---

## Fetch Join이 있는 Page + 별도 카운트 쿼리

fetch join을 사용하는 쿼리에서 자동 생성된 카운트 쿼리는 잘못된 결과를 만듭니다.
람다 오버로드로 별도의 카운트 쿼리를 제공하세요.

```kotlin
fun findAllWithDepartment(
    condition: MemberSearchCondition,
    pageable: Pageable,
): Page<Member> =
    selectFrom(member)
        .leftJoin(member.department, department).fetchJoin()
        .where(condition)
        .page(pageable, memberSort) {
            // fetch join 없는 별도 카운트 쿼리
            select(member.count())
                .from(member)
                .where(condition)
                .fetchOne() ?: 0L
        }
```

!!! warning "왜 중요한가"
    자동 생성된 카운트 쿼리는 메인 쿼리를 복제하고 select를 `COUNT(*)`로 교체합니다.
    fetch join이 있으면 카운트가 뻥튀기됩니다 -- 엔티티 수가 아닌 조인된 행 수를 셉니다.
    이것은 QueryDSL의 한계이며, querydsl-ktx의 버그가 아닙니다.

!!! tip "Condition 객체 재사용"
    동일한 `where(condition)` private 확장 함수가 데이터 쿼리와 카운트 쿼리 양쪽에서
    작동합니다. 카운트가 항상 데이터와 일치하는 것이 보장되며, 한쪽을 수정할 때
    다른 쪽을 빠뜨릴 위험이 없습니다.

---

## modifying { } — Bulk DML

`modifying { }`은 블록 실행 전에 `entityManager.flush()`, 후에 `entityManager.clear()`를
호출합니다. 따라서 활성 트랜잭션이 필요하며, **트랜잭션은 서비스 계층에서 선언**합니다.

```kotlin
// Repository — bulk DML 메서드만 제공
fun deactivateMembers(status: MemberStatus): Long =
    modifying {
        update(member)
            .set(member.status, MemberStatus.INACTIVE)
            .where(member.status.eq(status))
            .execute()
    }
```

```kotlin
// Service — 트랜잭션은 여기서 선언
@Service
@Transactional
class MemberService(
    private val memberRepository: MemberRepository,
) {
    fun deactivateNormalMembers(): Long =
        memberRepository.deactivateMembers(MemberStatus.NORMAL)
}
```

!!! warning "@Transactional은 서비스 계층에서 — 리포지토리가 아님"
    `@Transactional` 없이 사용하면 `flush()` 호출이 실패합니다. 트랜잭션은 서비스
    계층에서 선언하고, 리포지토리 메서드는 그 트랜잭션에 참여합니다.

    > *"We generally recommend declaring transaction boundaries when starting a unit of work
    > to ensure proper consistency and desired transaction participation."*
    >
    > — [Spring Data JPA Reference: Transactions](https://docs.spring.io/spring-data/jpa/reference/jpa/transactions.html)

    참고: `QuerydslRepository`는 Spring Data의 `SimpleJpaRepository`를 상속하지 않으므로,
    메서드에 기본 `@Transactional`이 적용되지 않습니다.

!!! note "여러 문이 하나의 flush/clear 사이클을 공유"
    하나의 `modifying { }` 블록에 여러 DML 문을 넣으면, flush는 첫 번째 문 전에 한 번,
    clear는 마지막 문 후에 한 번 발생합니다. 각각 따로 감싸는 것보다 효율적입니다.

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

---

## 역방향 Between -- 날짜 범위 유효성 검사

일반 `between`은 컬럼 값이 범위 안에 있는지 확인합니다.
**역방향 between**은 값이 컬럼으로 정의된 범위 안에 있는지 확인합니다.

### 양쪽 경계가 항상 있는 경우

`..` (rangeTo) 연산자를 사용합니다:

```kotlin
fun findActiveSales(now: LocalDateTime): List<Product> =
    selectFrom(product)
        .where(now between (product.saleStartAt..product.saleEndAt))
        .fetch()
```

```sql
-- SQL: sale_start_at <= '2026-04-10T12:00' AND sale_end_at >= '2026-04-10T12:00'
```

### 경계가 nullable인 경우

`to` (Pair) 문법으로 null-safe 디그레이드:

```kotlin
fun findActiveSales(now: LocalDateTime? = null): List<Product> =
    selectFrom(product)
        .where(now between (product.saleStartAt to product.saleEndAt))
        .fetch()
```

| `now` | `saleStartAt` | `saleEndAt` | 결과 |
|-------|---------------|-------------|------|
| non-null | non-null | non-null | `start <= now AND end >= now` |
| non-null | non-null | null | `start <= now` |
| non-null | null | non-null | `end >= now` |
| null | any | any | `null` (건너뜀) |

!!! tip "자주 쓰는 활용 사례"
    - **쿠폰 유효기간**: `now between (coupon.validFrom to coupon.validUntil)`
    - **할인 기간**: `now between (discount.startAt to discount.endAt)`
    - **이벤트 일정**: `now between (event.openAt to event.closeAt)`
    - **가격 구간 매칭**: `orderAmount between (tier.minAmount to tier.maxAmount)`

---

## 전체 조합 예제

다섯 가지 패턴을 모두 결합한 완성된 리포지토리:

```kotlin
@Repository
class MemberRepository : QuerydslRepository<Member>() {

    private val member = QMember.member
    private val department = QDepartment.department

    // 패턴 2: SortSpec을 프로퍼티로
    private val memberSort = sortSpec {
        "name" by member.name
        "age" by member.age
        "createdAt" by member.createdAt
        "department" by department.name
    }

    // 패턴 1: Condition 객체 + private where 확장 함수
    private fun <T> JPAQuery<T>.where(
        condition: MemberSearchCondition,
    ): JPAQuery<T> = this.where(
        member.name contains condition.name,
        member.status eq condition.status,
        member.age between (condition.minAge to condition.maxAge),
        member.createdAt between (condition.from to condition.to),
    )

    // 패턴 3: fetch join + 별도 카운트 쿼리
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

    // 패턴 4: modifying (서비스 계층에서 @Transactional 선언)
    fun deactivateInactive(cutoffDate: LocalDate): Long =
        modifying {
            update(member)
                .set(member.status, MemberStatus.INACTIVE)
                .where(member.lastLogin lt cutoffDate)
                .execute()
        }
}
```
