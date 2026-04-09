# 페이지네이션

`QuerydslSupport`(및 이를 확장하는 `QuerydslRepository`)는 QueryDSL 쿼리 객체에 대한
확장 함수로 페이지네이션 헬퍼를 제공합니다.

---

## slice vs page vs fetch

| 메서드 | 반환 타입 | 카운트 쿼리 | 사용 시기 |
|--------|---------|-------------|----------|
| `slice(pageable)` | `Slice<R>` | 아니오 (N+1개 행 조회) | 무한 스크롤, 전방 탐색만 필요한 경우 |
| `page(pageable)` | `Page<R>` | 예 (자동 생성) | 전체 건수가 필요한 전통적 페이지네이션 |
| `fetch(pageable)` | `List<R>` | 아니오 | 윈도우 처리된 리스트만 필요한 경우 |

### slice -- 카운트 쿼리 없음

`pageSize + 1`개의 행을 조회하여 `hasNext`를 정확히 판단한 후 결과를 잘라냅니다.

=== "Kotlin"

    ```kotlin
    fun searchMembers(name: String?, pageable: Pageable): Slice<Member> =
        selectFrom(member)
            .where(member.name contains name)
            .slice(pageable)
    ```

=== "SQL"

    ```sql
    SELECT m.*
    FROM member m
    WHERE m.name LIKE '%keyword%'
    LIMIT 21  -- pageSize(20) + 1
    OFFSET 0
    ```

!!! tip "왜 pageSize + 1인가?"
    21개의 행이 반환되면 다음 페이지가 있다는 것을 알 수 있습니다. 처음 20개만 반환합니다.
    20개 이하가 반환되면 다음 페이지가 없습니다. 이는 `content.size == pageSize` 검사보다
    정확합니다 -- 마지막 페이지가 꽉 찬 경우 false positive가 발생하지 않습니다.

### page -- 카운트 쿼리 포함

메인 쿼리에서 카운트 쿼리를 자동으로 생성합니다.

=== "Kotlin"

    ```kotlin
    fun searchMembers(name: String?, pageable: Pageable): Page<Member> =
        selectFrom(member)
            .where(member.name contains name)
            .page(pageable)
    ```

=== "SQL"

    ```sql
    -- 콘텐츠 쿼리
    SELECT m.*
    FROM member m
    WHERE m.name LIKE '%keyword%'
    LIMIT 20 OFFSET 0

    -- 카운트 쿼리 (자동 생성)
    SELECT COUNT(*)
    FROM member m
    WHERE m.name LIKE '%keyword%'
    ```

!!! warning "fetch join과 함께 사용하지 마세요"
    자동 생성된 카운트 쿼리는 메인 쿼리를 복제하고 select를 `COUNT(*)`로 교체합니다.
    fetch join이 있으면 잘못된 카운트가 나옵니다.
    별도의 카운트 쿼리를 제공하는 오버로드를 사용하세요:

    ```kotlin
    selectFrom(member)
        .join(member.team, team).fetchJoin()
        .where(member.name contains name)
        .page(pageable) {
            select(member.count())
                .from(member)
                .where(member.name contains name)
                .fetchOne() ?: 0L
        }
    ```

### fetch -- 단순 리스트

페이지네이션(offset/limit + 정렬)을 적용하고 원시 리스트를 반환합니다.

```kotlin
fun recentMembers(pageable: Pageable): List<Member> =
    selectFrom(member)
        .where(member.active eq true)
        .fetch(pageable)
```

---

## 값 기반 오버로드

모든 페이지네이션 메서드에는 `Pageable` 객체 대신 `page`/`size` 또는 `offset`/`limit` 값을
직접 받는 오버로드가 있습니다:

```kotlin
// Pageable 기반
query.slice(pageable)
query.page(pageable)

// 값 기반 -- 0부터 시작하는 페이지 번호
query.slice(page = 0, size = 20)
query.page(page = 0, size = 20)

// Offset/limit -- fetch용
query.fetch(offset = 0, limit = 20)
```

!!! tip "값 기반 오버로드 사용 시기"
    - Spring의 `Pageable` 추상화가 필요 없는 내부 리포지토리 메서드
    - `PageRequest` 생성이 노이즈를 추가하는 테스트
    - 웹이 아닌 컨텍스트 (배치 처리, CLI 도구)

---

## 별도 카운트 쿼리

메인 쿼리에 fetch join이나 복잡한 구성이 있는 경우, 직접 카운트 쿼리를 제공하세요:

=== "람다"

    ```kotlin
    fun searchWithJoin(name: String?, pageable: Pageable): Page<Member> =
        selectFrom(member)
            .join(member.team, team).fetchJoin()
            .where(member.name contains name)
            .page(pageable) {
                select(member.count())
                    .from(member)
                    .where(member.name contains name)
                    .fetchOne() ?: 0L
            }
    ```

=== "람다가 포함된 값 기반"

    ```kotlin
    fun searchWithJoin(name: String?): Page<Member> =
        selectFrom(member)
            .join(member.team, team).fetchJoin()
            .where(member.name contains name)
            .page(page = 0, size = 20) {
                select(member.count())
                    .from(member)
                    .where(member.name contains name)
                    .fetchOne() ?: 0L
            }
    ```

카운트 쿼리 람다는 **지연 실행**됩니다 -- `Page.getTotalElements()`가 호출될 때만 실행되며,
이는 `PageableExecutionUtils.getPage()` 덕분입니다.

---

## 폴백이 있는 applySort

`applySort`는 Spring Data의 `Sort`를 쿼리에 적용합니다. 정렬이 비어 있으면 폴백 정렬이 사용됩니다:

```kotlin
fun searchMembers(
    name: String?,
    pageable: Pageable,
): List<Member> =
    selectFrom(member)
        .where(member.name contains name)
        .applySort(pageable.sort) {
            member.createdAt.desc()  // 정렬이 지정되지 않았을 때의 폴백
        }
        .fetch()
```

| `pageable.sort` | 결과 |
|-----------------|--------|
| `Sort.by("name")` | `ORDER BY m.name ASC` |
| `Sort.unsorted()` | `ORDER BY m.created_at DESC` (폴백) |

!!! note "Sort 프로퍼티 이름"
    `applySort`는 내부적으로 Spring Data의 `Querydsl.applySorting()`을 사용하며,
    이는 `Sort` 프로퍼티 이름을 엔티티의 `PathBuilder`에 매핑합니다.
    프로퍼티 이름은 엔티티 필드명과 일치해야 합니다 (예: `created_at`이 아닌 `createdAt`).

---

## List를 Page로 변환

후처리 후 인메모리 리스트를 `Page`로 변환해야 할 때:

```kotlin
fun complexSearch(pageable: Pageable): Page<MemberDto> {
    val rawResults = selectFrom(member)
        .where(member.active eq true)
        .fetch(pageable)

    val dtos = rawResults.map { it.toDto() }

    return dtos.page(pageable) {
        select(member.count())
            .from(member)
            .where(member.active eq true)
            .fetchOne() ?: 0L
    }
}
```

---

## 이전 & 이후

=== "이전"

    ```kotlin
    fun search(name: String?, pageable: Pageable): Slice<Member> {
        val content = queryFactory.selectFrom(member)
            .where(if (name != null) member.name.contains(name) else null)
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()
        val hasNext = content.size == pageable.pageSize // (1)!
        return SliceImpl(content, pageable, hasNext)
    }
    ```

    1. 마지막 페이지가 꽉 찬 경우 false positive가 발생합니다.

=== "이후"

    ```kotlin
    fun search(name: String?, pageable: Pageable): Slice<Member> =
        selectFrom(member)
            .where(member.name contains name)
            .slice(pageable)
    ```
