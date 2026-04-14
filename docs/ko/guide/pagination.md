---
description: page(), slice(), exactSlice()와 타입 안전 SortSpec을 사용한 QueryDSL 페이지네이션 헬퍼.
---

# 페이지네이션

`QuerydslSupport`(및 이를 확장하는 `QuerydslRepository`)는 QueryDSL 쿼리 객체에 대한
확장 함수로 페이지네이션 헬퍼를 제공합니다.

---

## fetchCount / fetchResults 지원 중단 문제

::: warning fetchCount()와 fetchResults()는 deprecated입니다
QueryDSL 5.0부터 `fetchCount()`와 `fetchResults()`가 deprecated 되었습니다.
QueryDSL 팀이 복잡한 쿼리(조인, 서브쿼리, 그룹핑 포함)에서 카운트 쿼리를
자동 생성하면 결과가 부정확한 경우가 있다는 것을 발견했기 때문입니다.

인프런 강의 등에서 배운 기존 방식은 이렇습니다:

```kotlin
// 예전 방식 (deprecated)
val results = query.fetchResults()
val content = results.getResults()
val total = results.getTotal()

// deprecated 이후 수동 대응
val content = query.offset(offset).limit(limit).fetch()
val total = queryFactory.select(member.count())
    .from(member)
    .where(/* 같은 조건 */)
    .fetchOne() ?: 0L
return PageImpl(content, pageable, total)
```

**querydsl-ktx가 이 문제를 깔끔하게 해결합니다.** `page()` 메서드는 단순한 경우에
카운트 쿼리를 자동 생성하고, 복잡한 경우(fetch join, 그룹핑)에는 람다를 받습니다.
`slice()` 메서드는 N+1 기법으로 카운트 쿼리를 완전히 회피합니다.
:::

---

## slice vs exactSlice vs page vs fetch

| 메서드 | 반환 타입 | 카운트 쿼리 | hasNext 판별 | 사용 시기 |
|--------|---------|-------------|-------------|----------|
| `slice(pageable)` | `Slice<R>` | 아니오 | 낙관적 (pageSize행) | 무한 스크롤 |
| `exactSlice(pageable)` | `Slice<R>` | 아니오 | 정확 (pageSize + 1행) | hasNext가 정확해야 하는 전방 탐색 |
| `page(pageable)` | `Page<R>` | 예 (자동 생성) | 전체 건수 기반 | 전체 건수가 필요한 전통적 페이지네이션 |
| `fetch(pageable)` | `List<R>` | 아니오 | 해당 없음 | 윈도우 처리된 리스트만 필요한 경우 |

::: tip 어떤 메서드를 사용할까
**`slice`**(기본)를 사용하세요 -- 무한 스크롤, 모바일 앱.
정확히 pageSize개만 조회합니다. 꽉 찬 페이지가 반환되면 다음 데이터가 있다고 가정합니다.
전체 데이터가 pageSize의 정확한 배수인 경우, 마지막에 빈 요청이 1회 발생하지만
무한 스크롤 UI에서는 사용자에게 노출되지 않습니다.

**`exactSlice`**를 사용하세요 -- `hasNext` 신호가 정확해야 하는 UI
(예: 마지막 페이지에서 사라져야 하는 "더 보기" 버튼).
pageSize + 1행을 조회하므로, 추가 행도 쿼리의 모든 join을 거칩니다.

**`page`**를 사용하세요 -- "3 / 15 페이지"나 전체 결과 수를 표시하는 UI.
대용량 테이블에서 카운트 쿼리가 느릴 수 있으니, 데이터가 자주 변하지 않는다면
전체 건수를 캐싱하는 것도 고려하세요.
:::

### slice -- 카운트 쿼리 없음

정확히 `pageSize`개의 행을 조회합니다. 결과 수가 pageSize와 같으면 `hasNext`는 `true`입니다.

::: code-group

```kotlin [Kotlin]
fun searchMembers(name: String?, pageable: Pageable): Slice<Member> =
    selectFrom(member)
        .where(member.name contains name)
        .slice(pageable)
```

```sql [SQL]
SELECT m.*
FROM member m
WHERE m.name LIKE '%keyword%'
LIMIT 20  -- 정확히 pageSize
OFFSET 0
```

:::

::: info 낙관적 hasNext
전체 데이터가 pageSize의 정확한 배수인 경우, 마지막 꽉 찬 페이지에서
`hasNext = true`를 반환하여 빈 요청이 1회 추가로 발생합니다.
이는 `slice`가 주로 사용되는 무한 스크롤 UI에서는 문제가 되지 않습니다.
정확한 hasNext 판별이 필요하면 `exactSlice`를 사용하세요.
:::

### exactSlice -- 정확한 hasNext 판별

`pageSize + 1`개의 행을 조회하여 `hasNext`를 정확히 판단한 후 결과를 잘라냅니다.

::: code-group

```kotlin [Kotlin]
fun searchMembers(name: String?, pageable: Pageable): Slice<Member> =
    selectFrom(member)
        .where(member.name contains name)
        .exactSlice(pageable)
```

```sql [SQL]
SELECT m.*
FROM member m
WHERE m.name LIKE '%keyword%'
LIMIT 21  -- pageSize(20) + 1
OFFSET 0
```

:::

::: tip slice보다 exactSlice를 선호해야 할 때
추가 행도 쿼리의 모든 join을 거칩니다. 단순한 `selectFrom` 쿼리에서는
오버헤드가 무시할 수 있지만, join이 많은 쿼리에서는 추가 행의 비용이 누적됩니다.
join이 많은 쿼리에는 `slice`를, hasNext 정확성이 더 중요한 경우에는
`exactSlice`를 사용하세요.
:::

### page -- 카운트 쿼리 포함

메인 쿼리에서 카운트 쿼리를 자동으로 생성합니다.

::: code-group

```kotlin [Kotlin]
fun searchMembers(name: String?, pageable: Pageable): Page<Member> =
    selectFrom(member)
        .where(member.name contains name)
        .page(pageable)
```

```sql [SQL]
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

:::

::: warning fetch join과 함께 사용하지 마세요
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
:::

### fetch -- 단순 리스트

페이지네이션(offset/limit + 정렬)을 적용하고 원시 리스트를 반환합니다.

```kotlin
fun recentMembers(pageable: Pageable): List<Member> =
    selectFrom(member)
        .where(member.active eq true)
        .fetch(pageable)
```

---

## No-Offset 페이지네이션

::: tip 성능 팁: No-Offset 페이지네이션
전통적인 offset 기반 페이지네이션(`OFFSET 10000 LIMIT 20`)은 대용량 데이터에서
성능이 떨어집니다. DB가 처음 10,000행을 스캔한 후 버리기 때문입니다.

**No-offset**(또는 **keyset**) 패턴은 마지막으로 본 ID를 기준으로 필터링하여 이를 회피합니다:

```kotlin
fun searchAfter(
    lastId: Long?,
    name: String?,
    size: Int = 20,
): Slice<Member> =
    selectFrom(member)
        .where(
            member.name contains name,
            member.id gt lastId,   // null-safe: 첫 페이지에서는 건너뜀
        )
        .orderBy(member.id.asc())
        .slice(page = 0, size = size)
```

```sql
-- 첫 페이지 (lastId = null): ID 필터 없음
SELECT m.* FROM member m
WHERE m.name LIKE '%keyword%'
ORDER BY m.id ASC LIMIT 20

-- 다음 페이지 (lastId = 1000):
SELECT m.* FROM member m
WHERE m.name LIKE '%keyword%' AND m.id > 1000
ORDER BY m.id ASC LIMIT 20
```

querydsl-ktx에서는 `member.id gt null`이 `null`을 반환하므로(건너뜀) 첫 페이지
쿼리에 ID 필터가 없습니다. 특별한 분기 처리가 필요 없습니다.
:::

---

## 값 기반 오버로드

모든 페이지네이션 메서드에는 `Pageable` 객체 대신 `page`/`size` 또는 `offset`/`limit` 값을
직접 받는 오버로드가 있습니다:

```kotlin
// Pageable 기반
query.slice(pageable)
query.exactSlice(pageable)
query.page(pageable)

// 값 기반 -- 0부터 시작하는 페이지 번호
query.slice(page = 0, size = 20)
query.exactSlice(page = 0, size = 20)
query.page(page = 0, size = 20)

// Offset/limit -- fetch용
query.fetch(offset = 0, limit = 20)
```

::: tip 값 기반 오버로드 사용 시기
- Spring의 `Pageable` 추상화가 필요 없는 내부 리포지토리 메서드
- `PageRequest` 생성이 노이즈를 추가하는 테스트
- 웹이 아닌 컨텍스트 (배치 처리, CLI 도구)
:::

---

## 별도 카운트 쿼리

메인 쿼리에 fetch join이나 복잡한 구성이 있는 경우, 직접 카운트 쿼리를 제공하세요:

::: code-group

```kotlin [람다]
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

```kotlin [람다가 포함된 값 기반]
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

:::

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

::: info Sort 프로퍼티 이름
`applySort`는 내부적으로 Spring Data의 `Querydsl.applySorting()`을 사용하며,
이는 `Sort` 프로퍼티 이름을 엔티티의 `PathBuilder`에 매핑합니다.
프로퍼티 이름은 엔티티 필드명과 일치해야 합니다 (예: `created_at`이 아닌 `createdAt`).
:::

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

::: code-group

```kotlin [이전]
// deprecated된 fetchResults() 사용
fun searchOld(name: String?, pageable: Pageable): Page<Member> {
    val results = queryFactory.selectFrom(member)
        .where(if (name != null) member.name.contains(name) else null)
        .offset(pageable.offset)
        .limit(pageable.pageSize.toLong())
        .fetchResults()  // deprecated!
    return PageImpl(results.results, pageable, results.total)
}

// deprecated 이후 수동 대응
fun searchManual(name: String?, pageable: Pageable): Slice<Member> {
    val content = queryFactory.selectFrom(member)
        .where(if (name != null) member.name.contains(name) else null)
        .offset(pageable.offset)
        .limit(pageable.pageSize.toLong())
        .fetch()
    val hasNext = content.size == pageable.pageSize // 낙관적: 페이지가 가득 차면 다음이 있다고 가정
    return SliceImpl(content, pageable, hasNext)
}
```

```kotlin [이후]
// Slice -- 카운트 쿼리 없음, 낙관적 hasNext (무한 스크롤에 적합)
fun search(name: String?, pageable: Pageable): Slice<Member> =
    selectFrom(member)
        .where(member.name contains name)
        .slice(pageable)

// Slice -- 카운트 쿼리 없음, 정확한 hasNext
fun searchExact(name: String?, pageable: Pageable): Slice<Member> =
    selectFrom(member)
        .where(member.name contains name)
        .exactSlice(pageable)

// Page -- 자동 카운트 쿼리
fun searchPage(name: String?, pageable: Pageable): Page<Member> =
    selectFrom(member)
        .where(member.name contains name)
        .page(pageable)
```

:::

---

## SortSpec -- 타입 세이프 동적 정렬

Spring Data `Sort`는 문자열 프로퍼티명을 사용하며, `PathBuilder`가 암묵적으로 해석합니다.
이 방식의 한계:

- **보안**: 클라이언트가 임의 컬럼을 요청할 수 있음 (예: `?sort=password,asc`)
- **조인 경로**: `PathBuilder`는 크로스 엔티티 경로를 해석할 수 없음
- **암묵적**: 어떤 필드가 정렬 가능한지 코드에서 안 보임

`SortSpec`은 명시적 화이트리스트 매핑으로 세 가지 문제를 모두 해결합니다.

### SortSpec 정의

```kotlin
private val memberSort = sortSpec {
    "name"       by qMember.name
    "createdAt"  by qMember.createdAt
    "department" by qDepartment.name   // 조인 컬럼 -- PathBuilder로 해석 불가
}
```

### 페이지네이션과 함께 사용

```kotlin
fun search(name: String?, pageable: Pageable): Page<Member> =
    selectFrom(qMember)
        .join(qMember.department, qDepartment)
        .where(qMember.name contains name)
        .page(pageable, memberSort)
```

`page`, `slice`, `exactSlice` 메서드는 선택적으로 `SortSpec`을 받습니다:

| 메서드 | 시그니처 |
|--------|----------|
| `slice` | `JPQLQuery<R>.slice(pageable, spec, fallback?)` |
| `exactSlice` | `JPQLQuery<R>.exactSlice(pageable, spec, fallback?)` |
| `page` | `JPAQuery<R>.page(pageable, spec, fallback?)` |
| `page` | `JPQLQuery<R>.page(pageable, spec, fallback?, countQuery)` |

### Fallback 정렬

클라이언트가 정렬을 보내지 않거나 모든 프로퍼티가 매핑에 없을 때 fallback이 사용됩니다:

```kotlin
selectFrom(qMember)
    .page(pageable, memberSort) { qMember.createdAt.desc() }
```

### 동작 원리

1. `pageable.sort`가 `SortSpec`을 통해 해석 (화이트리스트 매핑)
2. 매핑된 프로퍼티는 `OrderSpecifier`로 변환되어 `orderBy` 적용
3. 매핑 안 된 프로퍼티는 무시
4. 페이지네이션(offset/limit)은 Pageable의 sort **없이** 적용 (이중 정렬 방지)
