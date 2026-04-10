# querydsl-ktx

## Development Workflow

```
1. 마일스톤 확인 → gh api repos/HarryJhin/querydsl-ktx/milestones
2. 이슈 선택 → gh issue list --milestone "v1.0.0"
3. 구현 + 테스트
   a. 단위 테스트: null 4조합 (this-null, arg-null, both-null, both-non-null)
   b. 통합 테스트: H2 + 실제 엔티티 기반 시나리오 (src/test/.../integration/)
   c. ./gradlew :querydsl-ktx:test
4. API 설계 검토 — 기존 메서드와 호환성, 체이닝 가능 여부
5. dead code 체크
6. 문서 최신화 (Documentation Checklist 참고)
7. 문서 누락 검증 — 코드의 public API와 문서를 대조 확인
8. 커밋 — Closes #N
9. 마일스톤 완료 시 릴리스 (Release Workflow 참고)
```

## Release Workflow

```
1. gradle.properties의 version 범프
2. mkdocs.yml의 extra.version 범프
3. README.md, README.ko.md, llms-full.txt의 버전 참조 교체
4. 커밋: "vX.Y.Z 버전 범프"
5. push: git push origin main
6. 태그: git tag vX.Y.Z && git push origin vX.Y.Z
7. GitHub Release: gh release create vX.Y.Z --generate-notes
   - PR 기반이면 자동 분류됨 (.github/release.yml)
   - 직접 커밋이면 수동 보충 필요
```

## Version Strategy

- `v1.x` — Spring Boot 3 + Kotlin 1.7+ + QueryDSL 5.1.0
- `v2.x` — Spring Boot 4 + Kotlin 2.2+ + OpenFeign QueryDSL 7.x
- 브랜치 전략: CONTRIBUTING.md 참고

## Build Commands

```bash
./gradlew build                                      # 전체 빌드 + 테스트
./gradlew :querydsl-ktx:test                         # 테스트만
./gradlew build -PspringBootVersion=3.4.4            # Spring Boot 버전 오버라이드 테스트
./gradlew publishAndReleaseToMavenCentral            # Maven Central 배포
```

## Documentation Checklist

코드 변경 후 반드시 확인:

- [ ] AGENTS.md — 아키텍처, 인터페이스 수, 시그니처
- [ ] llms-full.txt — 전체 API 시그니처, 버전 번호
- [ ] llms.txt — 링크 목록 (내용 직접 포함 금지, llms-full.txt에 위임)
- [ ] docs/guide/ — 해당 기능 EN/KO 페이지 (함수 테이블, 예제, Overview 테이블)
- [ ] README.md / README.ko.md — 버전, 기능 목록
- [ ] KDoc — 예시 코드가 실제 API와 일치
- [ ] **누락 검증** — public API 시그니처와 문서를 대조하여 빠진 항목 확인

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
- **의존성은 gradle/libs.versions.toml 버전 카탈로그에 정의** — build.gradle.kts에 버전 하드코딩 금지
- **이슈 생성 시 라벨 필수** — enhancement, bug, docs
- **테스트 @Configuration/@Bean에 open 필수** — kotlin-spring 플러그인 미사용, Kotlin 클래스는 기본 final

## llms.txt 작성 규칙

llms.txt 스펙 (https://llmstxt.org/) 준수. API dump를 직접 넣지 말 것.

```markdown
# 프로젝트명
> 한줄 요약 (blockquote)
본문 (핵심 특징만 간결하게)
## Docs
- [페이지명](URL): 설명
## Optional
- [보조 자료](URL): 설명
```

- `llms.txt` — curated table of contents (링크 + 설명만)
- `llms-full.txt` — 전체 API 시그니처, 코드 예시 포함
- H2 섹션은 `[Name](url): description` 링크 리스트로 구성
- `## Optional` 섹션은 특별한 의미 — 컨텍스트가 부족할 때 생략 가능한 자료
