# 왜 querydsl-ktx인가?

## 문제점

QueryDSL 동적 쿼리는 반복적인 null 검사가 필요합니다:

```kotlin
fun search(
    name: String?,
    status: String?,
    minAge: Int?,
    maxAge: Int?,
    startDate: LocalDate?,
    endDate: LocalDate?,
): List<Member> {
    val builder = BooleanBuilder()
    if (name != null) builder.and(member.name.contains(name))
    if (status != null) builder.and(member.status.eq(status))
    if (minAge != null && maxAge != null) builder.and(member.age.between(minAge, maxAge))
    else if (minAge != null) builder.and(member.age.goe(minAge))
    else if (maxAge != null) builder.and(member.age.loe(maxAge))
    if (startDate != null && endDate != null) builder.and(member.createdAt.between(startDate, endDate))
    else if (startDate != null) builder.and(member.createdAt.goe(startDate))
    else if (endDate != null) builder.and(member.createdAt.loe(endDate))
    return queryFactory.selectFrom(member).where(builder).fetch()
}
```

선택적 필터 하나마다 1-3줄의 보일러플레이트가 추가됩니다. 범위 필터(`between`)는 각각 3개의 분기가 필요합니다. 패턴은 항상 같지만 -- 매번 직접 작성해야 합니다.

---

## 대안 비교

| 접근 방식 | 장점 | 단점 |
|----------|------|------|
| `BooleanBuilder` | 내장, 추가 의존성 없음 | 장황함, 범위 처리 시 오류 발생 가능 |
| `BooleanExpression` 체이닝 | 약간 더 깔끔 | 여전히 수동 null 검사 필요 |
| Spring Data `Specification` | 타입 안전 | QueryDSL과 분리, infix 구문 없음 |
| 최상위 Kotlin 확장 함수 | 간결함 | 전역 스코프 오염; `eq`, `contains`가 stdlib과 충돌 |
| 직접 만든 헬퍼 함수 | 친숙함, 커스터마이징 가능 | 프로젝트마다 중복; 부분적 커버리지 |
| **querydsl-ktx** | Null-safe, 간결, 스코프 제한, 테스트 완비 | 추가 의존성 |

---

## 인터페이스 기반 스코핑

확장 함수는 최상위 함수가 아닌 **인터페이스**로 제공됩니다.

=== "최상위 확장 함수 (문제 있음)"

    ```kotlin
    // 프로젝트의 모든 파일에서 eq(), contains(), and()가 보임
    // kotlin.collections.contains 등과 이름 충돌

    fun StringExpression.contains(value: String?) = ...  // 누구의 contains?
    fun SimpleExpression<T>.eq(value: T?) = ...          // 누구의 eq?
    ```

=== "querydsl-ktx (스코프 제한)"

    ```kotlin
    // 구현 클래스 내부에서만 확장 함수 사용 가능
    class MemberRepository : QuerydslRepository<Member>() {
        // eq, contains, between이 이 스코프에서 사용 가능
    }

    class OrderService {
        // eq, contains, between이 이 스코프에서는 사용 불가
    }
    ```

이는 다음을 의미합니다:

- **전역 네임스페이스 오염 없음** -- 다른 라이브러리의 `eq`, `contains`에 영향 없음
- **IDE 자동완성이 깔끔** -- 관련 있는 제안만 표시됨
- **필요한 것만 선택** -- `StringExpressionExtensions`만 필요하면 그것만 구현

---

## 직접 만든 헬퍼 함수

많은 한국 프로젝트에서 볼 수 있는 패턴입니다. 온라인 강의에서 소개된 방식으로, `private fun eqStatus(status: String?): BooleanExpression?` 형태의 헬퍼 함수를 엔티티마다 작성합니다:

```kotlin
@Repository
class MemberRepository(private val queryFactory: JPAQueryFactory) {

    private fun eqStatus(status: String?): BooleanExpression? =
        status?.let { member.status.eq(it) }

    private fun containsName(name: String?): BooleanExpression? =
        name?.let { member.name.contains(it) }

    private fun betweenAge(min: Int?, max: Int?): BooleanExpression? = when {
        min != null && max != null -> member.age.between(min, max)
        min != null -> member.age.goe(min)
        max != null -> member.age.loe(max)
        else -> null
    }

    fun search(name: String?, status: String?, minAge: Int?, maxAge: Int?): List<Member> =
        queryFactory.selectFrom(member)
            .where(
                containsName(name),
                eqStatus(status),
                betweenAge(minAge, maxAge),
            )
            .fetch()
}
```

이 방식은 동작하지만:

- **엔티티마다 중복** -- `Member`용 `eqStatus`, `Order`용 `eqStatus`, `Product`용 `eqStatus`...
- **부분적 커버리지** -- 대부분의 프로젝트에서 `eq`와 `contains`만 작성하고, `between` 단측 범위, `andAnyOf`, `orAllOf`는 빠짐
- **표준 없음** -- 모든 개발자가 각자 조금씩 다른 버전을 작성
- **테스트 없음** -- private 헬퍼 함수를 누가 테스트하나요?

querydsl-ktx를 사용하면 같은 쿼리가 이렇게 됩니다:

```kotlin
@Repository
class MemberRepository : QuerydslRepository<Member>() {

    private val member = QMember.member

    fun search(name: String?, status: String?, minAge: Int?, maxAge: Int?): List<Member> =
        selectFrom(member)
            .where(
                member.name contains name,
                member.status eq status,
                member.age between (minAge to maxAge),
            )
            .fetch()
}
```

private 헬퍼 없음. 중복 없음. 모든 null 조합을 커버하는 214개의 테스트.

---

## querydsl-ktx가 제공하는 것

기본적인 `eq`와 `contains` 외에 querydsl-ktx는 다음을 제공합니다:

| 기능 | 예제 | 대체하는 것 |
|---------|---------|-----------------|
| 단측 `between` | `entity.date between (from to null)` → `date >= from` | 3분기 `if/else` |
| `andAnyOf` | `base andAnyOf listOf(condA, condB)` → `base AND (A OR B)` | 수동 OR 축소 |
| `orAllOf` | `base orAllOf listOf(condA, condB)` → `base OR (A AND B)` | 수동 AND 축소 |
| `modifying { }` | 벌크 DML을 위한 자동 flush + clear | 매번 잊기 쉬운 `entityManager.flush()` + `clear()` |
| `page()` / `slice()` | 카운트 쿼리를 포함한 한 번의 호출로 페이지네이션 | 수동 offset/limit + `PageImpl` 구성 |
| `nullif` / `coalesce` | SQL 함수, null-safe | `nullif()`/`coalesce()` 주변의 null 검사 |

이 모든 것은 동일한 null-safety 계약을 따릅니다: null 인자는 건너뛰며, 에러를 발생시키지 않습니다.
