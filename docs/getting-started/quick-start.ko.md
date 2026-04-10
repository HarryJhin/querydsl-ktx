# 빠른 시작

5분 만에 첫 null-safe 동적 쿼리를 작성하세요.

---

## Step 1: 의존성 추가

=== "Gradle (Kotlin DSL)"

    ```kotlin
    implementation("io.github.harryjhin:querydsl-ktx-spring-boot-starter:{{ version }}")
    ```

=== "Gradle (Groovy DSL)"

    ```groovy
    implementation 'io.github.harryjhin:querydsl-ktx-spring-boot-starter:{{ version }}'
    ```

Maven 및 모듈 선택에 대한 자세한 내용은 [설치](installation.md)를 참고하세요.

---

## Step 2: QuerydslRepository 상속

```kotlin
@Repository
class MemberQueryRepository : QuerydslRepository<Member>() {

    private val member = QMember.member
}
```

`QuerydslRepository<T>`는 다음을 제공합니다:

- 7개의 null-safe infix 확장 인터페이스
- `JPAQueryFactory` 래퍼 (`selectFrom`, `select`, `update`, `delete`)
- 페이지네이션 헬퍼 (`page`, `slice`, `fetch`)
- 벌크 DML 헬퍼 (`modifying`)

타입 파라미터 `T`는 자동으로 추론됩니다 -- `domainClass`를 오버라이드할 필요 없습니다.

---

## Step 3: 동적 쿼리 작성

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

1. 모든 파라미터가 nullable입니다 -- 호출자가 어떤 필터를 적용할지 결정합니다.
2. `StringExpression`의 `contains`는 `LIKE '%name%'`을 생성합니다. `name`이 null이면 조건 자체가 건너뛰어집니다.
3. `SimpleExpression`의 `eq`는 `status = ?`를 생성합니다. `status`가 null이면 건너뛰어집니다.
4. `Pair`를 사용한 `between`은 부분 범위를 처리합니다: 둘 다 null = 건너뛰기, 하나만 null = `>=` 또는 `<=`, 둘 다 값 있음 = `BETWEEN`.
5. `page()`는 offset/limit를 적용하고 카운트 쿼리를 자동으로 실행합니다.

---

## Step 4: 호출하기

```kotlin
@Service
class MemberService(
    private val memberQueryRepository: MemberQueryRepository,
) {
    // 모든 필터 적용
    fun searchAll() = memberQueryRepository.search(
        name = "John",
        status = Status.ACTIVE,
        minAge = 20,
        maxAge = 60,
        pageable = PageRequest.of(0, 20),
    )

    // 이름 필터만 -- 나머지 조건은 자동으로 건너뛰어짐
    fun searchByName() = memberQueryRepository.search(
        name = "John",
        status = null,
        minAge = null,
        maxAge = null,
        pageable = PageRequest.of(0, 20),
    )
}
```

=== "모든 필터 (생성된 SQL)"

    ```sql
    SELECT m.*
    FROM member m
    WHERE m.name LIKE '%John%'
      AND m.status = 'ACTIVE'
      AND m.age BETWEEN 20 AND 60
    LIMIT 20 OFFSET 0
    ```

=== "이름만 (생성된 SQL)"

    ```sql
    SELECT m.*
    FROM member m
    WHERE m.name LIKE '%John%'
    LIMIT 20 OFFSET 0
    ```

!!! tip "BooleanBuilder도, if 검사도 없습니다"
    같은 리포지토리 메서드가 어떤 필터 조합이든 처리합니다.
    null 파라미터는 단순히 무시됩니다 -- 조건 분기 로직이 필요 없습니다.

---

## 전체 예제

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

## 다음 단계

- [동적 쿼리](../guide/dynamic-queries.md) -- null-safety 계약을 깊이 이해하기
- [확장 함수 레퍼런스](../guide/extensions.md) -- 7개 인터페이스와 함수 목록
- [페이지네이션](../guide/pagination.md) -- slice vs page vs fetch
- [벌크 DML](../guide/bulk-dml.md) -- 안전한 update, delete 작업
