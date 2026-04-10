# Contributing

[한국어](CONTRIBUTING.ko.md)

## Branch Strategy

This project maintains separate branches per major version.

| Branch | Version | Target |
|--------|---------|--------|
| `main` | 1.x | Spring Boot 3 + Kotlin 1.7+ + QueryDSL 5.1.0 |

When Spring Boot 4 support begins, a `1.x` maintenance branch will be created and `main` will move to v2:

| Branch | Version | Target |
|--------|---------|--------|
| `main` | 2.x | Spring Boot 4 + Kotlin 2.2+ + QueryDSL 7.x (OpenFeign) |
| `1.x` | 1.x | Spring Boot 3 + Kotlin 1.7+ + QueryDSL 5.1.0 |

Send PRs to the branch matching the target version. Bug fixes for v1 go to `1.x`, new features go to `main`.

## Issues

Use the provided issue templates when reporting bugs or requesting features:

- **Bug Report** — `.github/ISSUE_TEMPLATE/bug_report.yml`
- **Feature Request** — `.github/ISSUE_TEMPLATE/feature_request.yml`

## Development Setup

**Requirements:** JDK 17+

1. Fork and clone the repository.
2. Open the project in IntelliJ IDEA (or any Gradle-compatible IDE).
3. Verify the setup:

```bash
./gradlew build
```

The project version is managed in `gradle.properties` (single source of truth for the build).

## Build & Test

```bash
./gradlew build                   # Full build + test
./gradlew :querydsl-ktx:test      # Tests only
```

### Testing Against Different Spring Boot Versions

CI runs a matrix of **Java 17/21** and **Spring Boot 3.2/3.3/3.4**. You can reproduce any combination locally:

```bash
./gradlew build -PspringBootVersion=3.4.4
```

## Dependency Management

All dependencies are declared in the [version catalog](gradle/libs.versions.toml). When adding or updating a dependency:

1. Add the version and library entry to `gradle/libs.versions.toml`.
2. Reference it in `build.gradle.kts` via `libs.<alias>`.
3. External (non-test) dependencies must be declared as `compileOnly`.

Dependabot is active and sends weekly PRs for Gradle and GitHub Actions updates.

## Test Conventions

- All extension functions follow the **null-safety contract** (see AGENTS.md).
- New functions must include **4 null-combination tests**:

| Test Case | `this` | Argument |
|-----------|--------|----------|
| both-non-null | non-null | non-null |
| this-null | null | non-null |
| arg-null | non-null | null |
| both-null | null | null |

## Code Conventions

- KDoc in English with SQL examples and `@param`/`@return`
- Kotlin `apiVersion`/`languageVersion` is **1.7** — do not use newer language features
- JVM target is **17**

## License

Contributions are released under the [Apache License 2.0](LICENSE).
