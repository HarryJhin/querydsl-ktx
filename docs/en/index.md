---
layout: home

hero:
  name: querydsl-ktx
  text: Null-safe Kotlin Extensions for QueryDSL
  tagline: Dynamic queries without BooleanBuilder or if-checks
  actions:
    - theme: brand
      text: Get Started
      link: /getting-started/installation
    - theme: alt
      text: View on GitHub
      link: https://github.com/HarryJhin/querydsl-ktx

features:
  - title: Null-safe by Design
    details: All extensions return null when receiver or argument is null. Nulls propagate safely without exceptions.
    icon: 🛡️
  - title: Infix & Declarative
    details: Write queries with Kotlin infix functions like path eq value and path contains text.
    icon: ⚡
  - title: Type-safe & Scoped
    details: Extensions are scoped inside QuerydslRepository. No namespace pollution, no misuse outside query context.
    icon: 🔒
  - title: Pagination Built-in
    details: page(), slice(), exactSlice() with SortSpec for type-safe dynamic sorting. Count queries handled automatically.
    icon: 📄
  - title: Spring Boot Auto-configuration
    details: Add the starter and JPAQueryFactory bean is registered automatically. GraalVM native image supported.
    icon: 🔧
  - title: Case/When DSL
    details: Type-safe CASE expressions with null-branch pruning. Searched and simple CASE both supported.
    icon: 🔀
---

## Before vs After

::: code-group

```kotlin [Before: Plain QueryDSL]
val builder = BooleanBuilder()
if (name != null) {
    builder.and(member.name.contains(name))
}
if (status != null) {
    builder.and(member.status.eq(status))
}
return queryFactory
    .selectFrom(member)
    .where(builder)
    .fetch()
```

```kotlin [After: querydsl-ktx]
return selectFrom(member)
    .where(
        member.name contains name,
        member.status eq status,
    )
    .fetch()
```

:::
