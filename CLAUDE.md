# querydsl-ktx

Null-safe infix Kotlin extensions for QueryDSL dynamic queries.

## Architecture

```
querydsl-ktx/                          # 코어 라이브러리
├── extensions/                        # 8개 null-safe 확장 인터페이스
│   ├── BooleanExpressionExtensions    # and, or, andAnyOf, orAllOf, eq, nullif, coalesce
│   ├── SimpleExpressionExtensions     # eq, ne, in, notIn, inChunked
│   ├── ComparableExpressionExtensions # gt, goe, lt, loe, between, reverse between, rangeTo, nullif, coalesce
│   ├── NumberExpressionExtensions     # 위와 동일 (NumberExpression은 ComparableExpression 미상속)
│   ├── StringExpressionExtensions     # contains, startsWith, endsWith, like, matches, nullif, coalesce
│   ├── TemporalExpressionExtensions   # after, before
│   ├── CollectionExpressionExtensions # contains
│   └── SubQueryExtensions             # exists, notExists
├── support/
│   ├── QuerydslSupport.kt            # 베이스 클래스: 쿼리 팩토리, 페이지네이션, modifying
│   ├── QuerydslRepository.kt         # 전체 인터페이스 구현 베이스 (GenericTypeResolver)
│   └── SortSpec.kt                   # 타입 안전 동적 정렬 매핑
├── Expressions.kt                     # reified 템플릿 래퍼 (numberTemplate, stringTemplate 등 19개)
└── CaseDsl.kt                         # Case/When DSL (SearchedCaseDsl, SimpleCaseDsl)

querydsl-ktx-spring-boot/              # Auto-configuration
├── QuerydslKtxAutoConfiguration.kt    # JPAQueryFactory 빈 등록 (@ConditionalOnMissingBean)
└── QuerydslKtxRuntimeHints.kt         # GraalVM native image 리플렉션 힌트

querydsl-ktx-spring-boot-starter/      # 편의 aggregator (위 두 모듈 통합)
```

## Environment

- JDK 17+
- Gradle 8.5 (Kotlin DSL)
- Spring Boot 3.0+ (CI에서 3.0, 3.2, 3.3, 3.4 테스트)
- QueryDSL 5.1.0+ (jakarta classifier)

## Development Workflow

```
1. 마일스톤 확인 → gh api repos/HarryJhin/querydsl-ktx/milestones
2. 이슈 선택 → gh issue list --milestone "<마일스톤명>"
3. 구현 + 테스트
   a. 단위 테스트: null 4조합 (this-null, arg-null, both-null, both-non-null)
   b. 통합 테스트: H2 + 실제 엔티티 시나리오 (src/test/.../integration/)
   c. ./gradlew :querydsl-ktx:test
4. API 설계 검토
   - 기존 infix 메서드와 시그니처 일관성 (nullable receiver, nullable arg)
   - ComparableExpression/NumberExpression 양쪽 대칭 여부
   - QuerydslRepository에서 자동으로 사용 가능한지
5. dead code 체크: 미사용 import, 미참조 함수, deprecated 메서드 grep
6. 문서 최신화 (Documentation Checklist 참고)
7. 문서 누락 검증: 소스의 public API 시그니처와 문서를 대조
8. 커밋: Closes #N
9. 마일스톤 완료 시 릴리스 (Release Workflow 참고)
```

## Release Workflow

```
1. gradle.properties의 version 범프 (문서 버전은 빌드 시 자동 반영)
2. README.md, README.ko.md, llms-full.txt의 버전 참조 교체
3. 커밋: "vX.Y.Z 버전 범프"
4. push: git push origin main
5. 태그: git tag vX.Y.Z && git push origin vX.Y.Z
6. GitHub Release: gh release create vX.Y.Z --generate-notes
   - PR 기반이면 자동 분류됨 (.github/release.yml)
   - 직접 커밋이면 수동 보충 필요
```

## Version Strategy

- `v1.x`: Spring Boot 3 + Kotlin 1.7+ + QueryDSL 5.1.0
- `v2.x`: Spring Boot 4 + Kotlin 2.2+ + OpenFeign QueryDSL 7.x
- 브랜치 전략: CONTRIBUTING.md 참고

## Build Commands

```bash
./gradlew build                                      # 전체 빌드 + 테스트
./gradlew :querydsl-ktx:test                         # 코어 테스트만
./gradlew :querydsl-ktx-spring-boot:test             # auto-config 테스트
./gradlew build -PspringBootVersion=3.4.4            # Spring Boot 버전 오버라이드
./gradlew publishAndReleaseToMavenCentral            # Maven Central 배포
npm run docs:dev                                     # 문서 개발 서버
npm run docs:build                                   # 문서 빌드
npm run docs:preview                                 # 빌드 결과 프리뷰
```

## Documentation Checklist

코드 변경 후 반드시 확인:

- [ ] AGENTS.md: 아키텍처, 인터페이스 수, 시그니처
- [ ] llms-full.txt: 전체 API 시그니처, 버전 번호
- [ ] llms.txt: 링크 목록만 (llmstxt.org 스펙, API dump 금지, llms-full.txt에 위임)
- [ ] docs/en/guide/ 및 docs/ko/guide/: 해당 기능 EN/KO 페이지 (함수 테이블, 예제, Overview 테이블)
- [ ] README.md / README.ko.md: 버전, 기능 목록
- [ ] KDoc: 예시 코드가 실제 API와 일치
- [ ] **누락 검증**: public API 시그니처와 문서를 대조하여 빠진 항목 확인

## Testing Rules

### 단위 테스트
- Extension 함수: this-null, arg-null, both-null, both-non-null **4가지 필수**
- 테스트 클래스가 해당 Extension 인터페이스를 implement하여 infix 스코프 진입

### 통합 테스트 (src/test/.../integration/)
- H2 in-memory DB + @DataJpaTest
- 실제 도메인 엔티티 (Member, Department, Order, Product)
- 신규 API는 반드시 통합 테스트 시나리오 추가
- 구조: domain/ (엔티티), repository/ (QuerydslRepository 상속), test/ (테스트)

### Auto-configuration 테스트
- `ApplicationContextRunner` 기반
- 필수 시나리오: 빈 등록, 사용자 빈 back-off, classpath 누락 시 비활성화

## Conventions

- 한국어 응답
- 커밋은 요청 시에만, 파일 개별 스테이징
- Extension 함수는 null-safety 계약 준수 (AGENTS.md 참고)
- 외부 의존성은 compileOnly
- apiVersion/languageVersion 1.7 유지 (Kotlin 1.7 호환)
- **의존성은 gradle/libs.versions.toml에 정의**. build.gradle.kts에 버전 하드코딩 금지
- **이슈 생성 시 라벨 필수**: enhancement, bug, documentation
- **PR 생성 시 라벨 필수** — 커밋 prefix에 맞춰 부여:
  - `feat:` → enhancement
  - `fix:` → bug
  - `refactor:` → refactor
  - `test:` → test
  - `docs:` → documentation
  - `chore:`, `build:`, `ci:` → chore
- **커밋 메시지**: `<type>: <설명>` (conventional commits). type은 feat, fix, refactor, test, docs, chore, build, ci 중 하나
- **테스트 @Configuration/@Bean에 open 필수** (kotlin-spring 플러그인 미사용)

## Documentation Rules

문서 작성 규칙은 `docs/CLAUDE.md` 참고.
