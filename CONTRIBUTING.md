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

## Release

Releases are published to Maven Central via tag push:

```bash
git tag v1.0.0
git push origin v1.0.0
```

## Build

```bash
./gradlew build                   # Full build + test
./gradlew :querydsl-ktx:test      # Tests only
```

## Conventions

- All extension functions follow the null-safety contract (see AGENTS.md)
- New functions must include tests for: this-null, arg-null, both-null, both-non-null
- External dependencies must be declared as `compileOnly`
- KDoc in English with SQL examples and @param/@return

## License

Contributions are released under the [Apache License 2.0](LICENSE).
