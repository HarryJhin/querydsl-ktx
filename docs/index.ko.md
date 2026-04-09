# querydsl-ktx

**QueryDSL 동적 쿼리를 위한 null-safe infix Kotlin 확장 라이브러리.**

`BooleanBuilder` 보일러플레이트를 제거하세요. SQL처럼 읽히는 동적 쿼리를 작성하세요.

---

## 문제점

모든 QueryDSL 동적 쿼리는 같은 방식으로 시작합니다:

```kotlin
val builder = BooleanBuilder()
if (name != null) builder.and(member.name.contains(name))
if (status != null) builder.and(member.status.eq(status))
if (minAge != null && maxAge != null) builder.and(member.age.between(minAge, maxAge))
else if (minAge != null) builder.and(member.age.goe(minAge))
else if (maxAge != null) builder.and(member.age.loe(maxAge))
// ...필터가 추가될 때마다 계속 늘어남
```

선택적 필터 하나마다 1-3줄이 추가됩니다. 범위 필터는 3개의 분기가 필요합니다.
패턴은 항상 같지만 -- 매번 직접 작성해야 합니다.

querydsl-ktx를 사용하면 이렇게 바뀝니다:

```kotlin
selectFrom(member).where(
    member.name contains name,
    member.status eq status,
    member.age between (minAge to maxAge),
).fetch()
```

null 파라미터는 자동으로 건너뜁니다. `if` 검사도, `BooleanBuilder`도 필요 없습니다.

[전체 비교 보기 →](why.md)

---

## 왜 querydsl-ktx인가?

!!! abstract "기본적으로 Null-Safe"
    모든 확장 함수는 nullable 리시버와 nullable 인자를 받습니다.
    `null`은 "이 조건을 건너뛴다"는 의미입니다 -- `if` 검사도, `BooleanBuilder`도 필요 없습니다.

!!! abstract "Infix & 선언적"
    Kotlin infix 구문으로 쿼리가 자연어처럼 읽힙니다.
    `entity.name.eq(name)` 대신 `entity.name eq name`.

!!! abstract "타입 안전 & 스코프 제한"
    확장 함수는 인터페이스 구현을 통해 스코프가 제한됩니다 -- 전역 오염이 없습니다.
    필요한 인터페이스만 구현하세요.

---

## 이전 & 이후

=== "이전 -- 일반 QueryDSL"

    ```kotlin
    @Repository
    class MemberRepository(private val queryFactory: JPAQueryFactory) {

        fun search(
            name: String?,
            status: String?,
            minAge: Int?,
            maxAge: Int?,
            pageable: Pageable,
        ): Page<Member> {
            val builder = BooleanBuilder()
            if (name != null) builder.and(member.name.contains(name))
            if (status != null) builder.and(member.status.eq(status))
            if (minAge != null && maxAge != null) {
                builder.and(member.age.between(minAge, maxAge))
            } else if (minAge != null) {
                builder.and(member.age.goe(minAge))
            } else if (maxAge != null) {
                builder.and(member.age.loe(maxAge))
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
    }
    ```

=== "이후 -- querydsl-ktx"

    ```kotlin
    @Repository
    class MemberRepository : QuerydslRepository<Member>() {

        private val member = QMember.member

        fun search(
            name: String?,
            status: String?,
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
    }
    ```

!!! tip "변경된 점"
    - **30줄**이 **10줄**로 줄었습니다
    - `BooleanBuilder`도, `if` 검사도 없습니다
    - `between`에 `Pair`를 사용하면 단측 범위도 자동 처리됩니다
    - `page()`가 카운트 쿼리와 페이지네이션을 한 번에 처리합니다

---

## 바로가기

| | |
|---|---|
| [**설치**](getting-started/installation.md) | Gradle, Maven 설정 및 모듈 선택 |
| [**빠른 시작**](getting-started/quick-start.md) | 5분 만에 첫 동적 쿼리 작성하기 |
| [**동적 쿼리**](guide/dynamic-queries.md) | 핵심 개념: null = 조건 건너뛰기 |
| [**확장 함수 레퍼런스**](guide/extensions.md) | 7개 인터페이스와 예제 |
| [**페이지네이션**](guide/pagination.md) | slice, page, fetch 헬퍼 |
| [**벌크 DML**](guide/bulk-dml.md) | 자동 flush & clear를 포함한 안전한 update/delete |
