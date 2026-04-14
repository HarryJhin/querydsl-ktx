# Pagination

`QuerydslSupport` (and by extension `QuerydslRepository`) provides pagination helpers
as extension functions on QueryDSL query objects.

---

## The fetchCount / fetchResults Problem

::: warning fetchCount() and fetchResults() are deprecated
Since QueryDSL 5.0, `fetchCount()` and `fetchResults()` are deprecated.
The QueryDSL team found that auto-generating count queries from complex queries
(with joins, subqueries, grouping) produces unreliable results.

The community workaround is to write separate content and count queries manually:

```kotlin
// The old way (deprecated)
val results = query.fetchResults()
val content = results.getResults()
val total = results.getTotal()

// The manual workaround
val content = query.offset(offset).limit(limit).fetch()
val total = queryFactory.select(member.count())
    .from(member)
    .where(/* same conditions */)
    .fetchOne() ?: 0L
return PageImpl(content, pageable, total)
```

**querydsl-ktx handles this cleanly.** The `page()` method auto-generates the count
query for simple cases, and accepts a lambda for complex cases (fetch joins, grouping).
The `slice()` method avoids count queries entirely using the N+1 technique.
:::

---

## slice vs exactSlice vs page vs fetch

| Method | Returns | Count Query | hasNext Detection | Use When |
|--------|---------|-------------|-------------------|----------|
| `slice(pageable)` | `Slice<R>` | No | Optimistic (pageSize rows) | Infinite scroll |
| `exactSlice(pageable)` | `Slice<R>` | No | Exact (pageSize + 1 rows) | Forward-only navigation where hasNext must be accurate |
| `page(pageable)` | `Page<R>` | Yes (auto-generated) | Via total count | Traditional pagination with total count |
| `fetch(pageable)` | `List<R>` | No | N/A | You just need a windowed list |

::: tip When to use which
**Use `slice`** (default) for infinite scroll and mobile apps.
Fetches exactly pageSize rows -- if a full page comes back, it assumes more data exists.
When the total row count is an exact multiple of pageSize, this results in one extra
empty request at the end, which is invisible in infinite-scroll UIs.

**Use `exactSlice`** when your UI relies on an accurate `hasNext` signal
(e.g., a "Load more" button that should disappear on the last page).
Fetches pageSize + 1 rows, so the extra row also goes through all joins in the query.

**Use `page`** when the UI shows "Page 3 of 15" or total result count.
Note that the count query can be expensive on large tables -- consider caching
the total count if the underlying data doesn't change often.
:::

### slice -- No count query

Fetches exactly `pageSize` rows. If the result count equals pageSize, `hasNext` is `true`.

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
LIMIT 20  -- exactly pageSize
OFFSET 0
```

:::

::: info Optimistic hasNext
When the total row count is an exact multiple of pageSize, the last full page will
report `hasNext = true`, leading to one additional empty request. This is typically
acceptable for infinite-scroll UIs where `slice` is most commonly used.
If you need exact hasNext detection, use `exactSlice` instead.
:::

### exactSlice -- Exact hasNext detection

Fetches `pageSize + 1` rows to determine `hasNext` accurately, then trims the result.

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

::: tip When to prefer exactSlice over slice
The extra row goes through all joins in the query. For simple `selectFrom` queries
the overhead is negligible, but for queries with multiple joins the cost of that
extra row adds up. Prefer `slice` for join-heavy queries and `exactSlice` when
hasNext accuracy matters more than that marginal cost.
:::

### page -- With count query

Generates a count query automatically from the main query.

::: code-group

```kotlin [Kotlin]
fun searchMembers(name: String?, pageable: Pageable): Page<Member> =
    selectFrom(member)
        .where(member.name contains name)
        .page(pageable)
```

```sql [SQL]
-- Content query
SELECT m.*
FROM member m
WHERE m.name LIKE '%keyword%'
LIMIT 20 OFFSET 0

-- Count query (auto-generated)
SELECT COUNT(*)
FROM member m
WHERE m.name LIKE '%keyword%'
```

:::

::: warning Do not use with fetch joins
The auto-generated count query clones the main query and replaces the select
with `COUNT(*)`. When fetch joins are present, this produces incorrect counts.
Use the overload with a separate count query instead:

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

### fetch -- Plain list

Applies pagination (offset/limit + sorting) and returns a raw list.

```kotlin
fun recentMembers(pageable: Pageable): List<Member> =
    selectFrom(member)
        .where(member.active eq true)
        .fetch(pageable)
```

---

## No-Offset Pagination

::: tip Performance tip: No-offset pagination
Traditional offset-based pagination (`OFFSET 10000 LIMIT 20`) degrades on large
datasets because the database still scans and discards the first 10,000 rows.

The **no-offset** (or **keyset**) pattern avoids this by filtering on the last seen ID:

```kotlin
fun searchAfter(
    lastId: Long?,
    name: String?,
    size: Int = 20,
): Slice<Member> =
    selectFrom(member)
        .where(
            member.name contains name,
            member.id gt lastId,   // null-safe: skipped on first page
        )
        .orderBy(member.id.asc())
        .slice(page = 0, size = size)
```

```sql
-- First page (lastId = null): no ID filter
SELECT m.* FROM member m
WHERE m.name LIKE '%keyword%'
ORDER BY m.id ASC LIMIT 20

-- Subsequent pages (lastId = 1000):
SELECT m.* FROM member m
WHERE m.name LIKE '%keyword%' AND m.id > 1000
ORDER BY m.id ASC LIMIT 20
```

This works naturally with querydsl-ktx because `member.id gt null` returns `null`
(skipped), so the first page query has no ID filter. No special-casing needed.
:::

---

## Value-Based Overloads

All pagination methods have overloads that accept raw `page`/`size` or `offset`/`limit` values
instead of a `Pageable` object:

```kotlin
// Pageable-based
query.slice(pageable)
query.exactSlice(pageable)
query.page(pageable)

// Value-based -- zero-indexed page number
query.slice(page = 0, size = 20)
query.exactSlice(page = 0, size = 20)
query.page(page = 0, size = 20)

// Offset/limit -- for fetch
query.fetch(offset = 0, limit = 20)
```

::: tip When to use value-based overloads
- Internal repository methods that don't need Spring's `Pageable` abstraction
- Tests where constructing a `PageRequest` adds noise
- Non-web contexts (batch processing, CLI tools)
:::

---

## Separate Count Query

When the main query has fetch joins or complex constructs, provide your own count query:

::: code-group

```kotlin [Lambda]
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

```kotlin [Value-based with lambda]
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

The count query lambda is **lazy** -- it only executes when `Page.getTotalElements()` is called,
thanks to `PageableExecutionUtils.getPage()`.

---

## applySort with Fallback

`applySort` applies Spring Data's `Sort` to a query. When the sort is empty, a fallback order is used:

```kotlin
fun searchMembers(
    name: String?,
    pageable: Pageable,
): List<Member> =
    selectFrom(member)
        .where(member.name contains name)
        .applySort(pageable.sort) {
            member.createdAt.desc()  // fallback when no sort specified
        }
        .fetch()
```

| `pageable.sort` | Result |
|-----------------|--------|
| `Sort.by("name")` | `ORDER BY m.name ASC` |
| `Sort.unsorted()` | `ORDER BY m.created_at DESC` (fallback) |

::: info Sort property names
`applySort` uses Spring Data's `Querydsl.applySorting()` internally,
which maps `Sort` property names to the entity's `PathBuilder`.
Property names must match the entity field names (e.g., `createdAt`, not `created_at`).
:::

---

## List to Page

Convert an in-memory list to a `Page` when you need to paginate after post-processing:

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

## Before & After

::: code-group

```kotlin [Before]
// Using deprecated fetchResults()
fun searchOld(name: String?, pageable: Pageable): Page<Member> {
    val results = queryFactory.selectFrom(member)
        .where(if (name != null) member.name.contains(name) else null)
        .offset(pageable.offset)
        .limit(pageable.pageSize.toLong())
        .fetchResults()  // deprecated!
    return PageImpl(results.results, pageable, results.total)
}

// Manual workaround after deprecation
fun searchManual(name: String?, pageable: Pageable): Slice<Member> {
    val content = queryFactory.selectFrom(member)
        .where(if (name != null) member.name.contains(name) else null)
        .offset(pageable.offset)
        .limit(pageable.pageSize.toLong())
        .fetch()
    val hasNext = content.size == pageable.pageSize // optimistic: assumes more if full page
    return SliceImpl(content, pageable, hasNext)
}
```

```kotlin [After]
// Slice -- no count query, optimistic hasNext (ideal for infinite scroll)
fun search(name: String?, pageable: Pageable): Slice<Member> =
    selectFrom(member)
        .where(member.name contains name)
        .slice(pageable)

// Slice -- no count query, exact hasNext
fun searchExact(name: String?, pageable: Pageable): Slice<Member> =
    selectFrom(member)
        .where(member.name contains name)
        .exactSlice(pageable)

// Page -- with auto count query
fun searchPage(name: String?, pageable: Pageable): Page<Member> =
    selectFrom(member)
        .where(member.name contains name)
        .page(pageable)
```

:::

---

## SortSpec -- Type-Safe Dynamic Ordering

Spring Data `Sort` uses string property names, which `PathBuilder` resolves implicitly.
This has limitations:

- **Security**: clients can request arbitrary columns (e.g., `?sort=password,asc`)
- **Join paths**: `PathBuilder` cannot resolve cross-entity paths
- **Implicit**: no clear definition of what's sortable

`SortSpec` solves all three by providing an explicit whitelist mapping.

### Defining a SortSpec

```kotlin
private val memberSort = sortSpec {
    "name"       by qMember.name
    "createdAt"  by qMember.createdAt
    "department" by qDepartment.name   // join column -- PathBuilder can't resolve this
}
```

### Using with Pagination

```kotlin
fun search(name: String?, pageable: Pageable): Page<Member> =
    selectFrom(qMember)
        .join(qMember.department, qDepartment)
        .where(qMember.name contains name)
        .page(pageable, memberSort)
```

The `page`, `slice`, and `exactSlice` methods accept an optional `SortSpec`:

| Method | Signature |
|--------|-----------|
| `slice` | `JPQLQuery<R>.slice(pageable, spec, fallback?)` |
| `exactSlice` | `JPQLQuery<R>.exactSlice(pageable, spec, fallback?)` |
| `page` | `JPAQuery<R>.page(pageable, spec, fallback?)` |
| `page` | `JPQLQuery<R>.page(pageable, spec, fallback?, countQuery)` |

### Fallback Sort

When the client sends no sort or all properties are unmapped, a fallback is used:

```kotlin
selectFrom(qMember)
    .page(pageable, memberSort) { qMember.createdAt.desc() }
```

### How It Works

1. `pageable.sort` is resolved through `SortSpec` (whitelist mapping)
2. Matched properties become `OrderSpecifier`s applied via `orderBy`
3. Unmatched properties are silently ignored
4. Pagination (offset/limit) is applied **without** the Pageable's sort (preventing double-sort)
