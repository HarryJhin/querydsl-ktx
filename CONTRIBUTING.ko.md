# 기여 가이드

[English](CONTRIBUTING.md)

## 브랜치 전략

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

## 이슈

이슈를 생성할 때는 제공된 템플릿을 사용해 주세요:

- **버그 리포트** — `.github/ISSUE_TEMPLATE/bug_report.yml`
- **기능 요청** — `.github/ISSUE_TEMPLATE/feature_request.yml`

## 개발 환경 설정

**요구 사항:** JDK 17+

1. 저장소를 Fork한 후 클론합니다.
2. IntelliJ IDEA(또는 Gradle 호환 IDE)로 프로젝트를 엽니다.
3. 빌드를 확인합니다:

```bash
./gradlew build
```

프로젝트 버전은 `gradle.properties`에서 단일 관리합니다 (빌드의 single source of truth).

## 빌드 및 테스트

```bash
./gradlew build                   # 전체 빌드 + 테스트
./gradlew :querydsl-ktx:test      # 테스트만
```

### 다른 Spring Boot 버전으로 테스트

CI는 **Java 17/21** x **Spring Boot 3.2/3.3/3.4** 매트릭스를 실행합니다. 로컬에서 동일하게 재현할 수 있습니다:

```bash
./gradlew build -PspringBootVersion=3.4.4
```

## 의존성 관리

모든 의존성은 [버전 카탈로그](gradle/libs.versions.toml)에 선언합니다. 의존성을 추가하거나 변경할 때:

1. `gradle/libs.versions.toml`에 버전과 라이브러리 항목을 추가합니다.
2. `build.gradle.kts`에서 `libs.<alias>`로 참조합니다.
3. 외부(비테스트) 의존성은 반드시 `compileOnly`로 선언합니다.

Dependabot이 활성화되어 있으며, Gradle 및 GitHub Actions 업데이트 PR을 매주 생성합니다.

## 테스트 컨벤션

- 모든 Extension 함수는 **null-safety 계약**을 따릅니다 (AGENTS.md 참고).
- 새 함수 추가 시 **null 4조합 테스트** 필수:

| 테스트 케이스 | `this` | 인자 |
|-----------|--------|----------|
| both-non-null | non-null | non-null |
| this-null | null | non-null |
| arg-null | non-null | null |
| both-null | null | null |

## 코드 컨벤션

- KDoc은 영어로 작성하며, SQL 예시와 `@param`/`@return`을 포함합니다
- Kotlin `apiVersion`/`languageVersion`은 **1.7** — 최신 언어 기능 사용 금지
- JVM 타겟은 **17**

## 라이선스

기여한 코드는 [Apache License 2.0](LICENSE)으로 배포됩니다.
