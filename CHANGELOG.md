# Changelog

All notable changes to this project are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and the project
adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.2.0] - 2026-04-30

### Added

- **`NumberExpression` arithmetic** — null-safe infix `add` / `subtract` /
  `multiply` / `divide` / `mod` (each with value and `Expression<T>` overloads),
  plus Kotlin `operator` overloads `+`, `-`, `*`, `/`, `%`, unary `-` for
  expression building (#88, #105).
- **`SimpleExpression` subquery comparisons** — `eq` / `in` / `notIn` overloads
  accepting `SubQueryExpression<T>?` for null-safe subquery filters (#89).
- **`BooleanExpression` vararg combinators** — `andAnyOf` / `orAllOf` accept
  varargs of `BooleanExpression?` in addition to the existing `List` overloads
  (#92).
- **String `LIKE ESCAPE` chain** — `name like pattern escape '\\'` works on
  `like` / `notLike` / `likeIgnoreCase` results for matching literal `%` and
  `_` characters (#91).
- **All / Any comparison variants** — 32 new functions across
  `SimpleExpressionExtensions`, `ComparableExpressionExtensions`, and
  `NumberExpressionExtensions`: `eqAll` / `eqAny` / `neAll` / `neAny` /
  `gtAll` / `gtAny` / `goeAll` / `goeAny` / `ltAll` / `ltAny` / `loeAll` /
  `loeAny`. Each pairs `CollectionExpression<*, in T>?` with
  `SubQueryExpression<T>?` where the QueryDSL 5.1.0 member exists. The
  `NumberExpression` SubQuery coverage is asymmetric per QueryDSL itself
  (`gtAll` / `gtAny` only) (#90).

### Changed

- **Exception types unified to QueryDSL hierarchy** — `inChunked` with
  non-positive chunk size now throws `com.querydsl.core.types.ExpressionException`
  (was `IllegalArgumentException`); `modifying { }` outside an active
  transaction now throws `com.querydsl.core.QueryException` (was
  `IllegalStateException`); `escape` invoked on a non-LIKE receiver throws
  `ExpressionException` (#91).
- **Spring Boot 3.5 added to CI matrix** — wrappers verified across Spring
  Boot 3.0 / 3.2 / 3.3 / 3.4 / 3.5 with both Java 17 and 21, plus OpenFeign
  QueryDSL 6.12.

### Fixed

- **`QuerydslKtxAutoConfiguration`** — registration is now keyed on
  `EntityManagerFactory` instead of `DataSource`, supporting multi-datasource
  setups via `@Primary` selection through `SharedEntityManagerCreator`.

### Documentation

- Extension reference (`docs/{en,ko}/guide/extensions.md`) covers all 32 ALL/Any
  variants, the SubQuery comparison overloads, the LIKE escape chain, and the
  arithmetic + Kotlin operator forms with an explicit asymmetry table.
- Dynamic queries guide expanded with Subquery, ALL/Any, LIKE escape, and
  computed-column patterns.
- `llms-full.txt` and `AGENTS.md` updated to reflect the new public API and
  the VitePress documentation toolchain.

[1.2.0]: https://github.com/HarryJhin/querydsl-ktx/compare/v1.1.0...v1.2.0
