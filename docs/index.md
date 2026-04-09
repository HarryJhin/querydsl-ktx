# querydsl-ktx

**Null-safe infix Kotlin extensions for QueryDSL dynamic queries.**

Stop writing `if (x != null) builder.and(...)` boilerplate.
Write declarative, type-safe dynamic queries that read like SQL.

---

## Why querydsl-ktx?

!!! abstract "Null-Safe by Default"
    Every extension function accepts nullable receivers and nullable arguments.
    `null` means "skip this condition" -- no `if` checks, no `BooleanBuilder`.

!!! abstract "Infix & Declarative"
    Kotlin infix syntax makes queries read like natural language.
    `entity.name eq name` instead of `entity.name.eq(name)`.

!!! abstract "Type-Safe & Scoped"
    Extensions are scoped through interface implementation -- no global pollution.
    Implement only the interfaces you need.

---

## Before & After

=== "Before -- Plain QueryDSL"

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

=== "After -- querydsl-ktx"

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

!!! tip "What changed"
    - **30 lines** became **10 lines**
    - No `BooleanBuilder`, no `if` checks
    - `between` with a `Pair` handles one-sided ranges automatically
    - `page()` handles count query and pagination in one call

---

## Quick Links

| | |
|---|---|
| [**Installation**](getting-started/installation.md) | Gradle, Maven setup and module selection |
| [**Quick Start**](getting-started/quick-start.md) | Write your first dynamic query in 5 minutes |
| [**Dynamic Queries**](guide/dynamic-queries.md) | Core concept: null = skip condition |
| [**Extension Reference**](guide/extensions.md) | All 7 interfaces with examples |
| [**Pagination**](guide/pagination.md) | slice, page, fetch helpers |
| [**Bulk DML**](guide/bulk-dml.md) | Safe update/delete with auto flush & clear |
