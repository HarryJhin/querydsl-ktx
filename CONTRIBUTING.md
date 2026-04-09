# Contributing

## Branch Strategy

이 프로젝트는 메이저 버전별로 브랜치를 분리하여 관리합니다.

| Branch | Version | Target |
|--------|---------|--------|
| `main` | 1.x | Spring Boot 3 + Kotlin 1.7+ + QueryDSL 5.1.0 |

향후 Spring Boot 4 지원 시 `1.x` 유지보수 브랜치를 분기하고, `main`을 v2로 올립니다:

| Branch | Version | Target |
|--------|---------|--------|
| `main` | 2.x | Spring Boot 4 + Kotlin 2.2+ + QueryDSL 7.x (OpenFeign) |
| `1.x` | 1.x | Spring Boot 3 + Kotlin 1.7+ + QueryDSL 5.1.0 |

PR은 해당 버전의 브랜치로 보내주세요. v1 버그 수정은 `1.x`, 새 기능은 `main`으로 보냅니다.

## Release

태그 기반으로 Maven Central에 배포합니다:

```bash
git tag v1.0.0
git push origin v1.0.0
```

## Build

```bash
./gradlew build                   # 전체 빌드 + 테스트
./gradlew :querydsl-ktx:test      # 테스트만
```

## Conventions

- 모든 Extension 함수는 null-safety 계약을 따릅니다 (AGENTS.md 참고)
- 새 함수 추가 시 this-null, arg-null, both-null, both-non-null 4가지 테스트 필수
- 외부 의존성은 `compileOnly`로 선언합니다
- KDoc은 영어로 작성하며, SQL 예시와 @param/@return을 포함합니다

## License

기여한 코드는 [Apache License 2.0](LICENSE)으로 배포됩니다.
