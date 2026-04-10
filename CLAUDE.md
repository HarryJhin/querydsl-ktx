# querydsl-ktx

## Development Workflow

```
1. 마일스톤 확인 → gh api repos/HarryJhin/querydsl-ktx/milestones
2. 이슈 선택 → gh issue list --milestone "v1.0.0"
3. 구현 + 테스트 → ./gradlew :querydsl-ktx:test
4. API 설계 검토 — 기존 메서드와 호환성, 체이닝 가능 여부
5. dead code 체크
6. 문서 최신화 — AGENTS.md, llms.txt, docs/, README
7. 커밋 — Closes #N
8. 마일스톤 완료 시 릴리스:
   a. gradle.properties의 version 범프
   b. mkdocs.yml의 extra.version 범프
   c. README.md, README.ko.md, llms-full.txt의 버전 참조 교체
   d. 커밋: "v{X.Y.Z} 버전 범프"
   e. push + 태그: git tag vX.Y.Z && git push origin main && git push origin vX.Y.Z
   f. GitHub Releases에 릴리스 노트 작성
```

## Version Strategy

- `v1.x` — Spring Boot 3 + Kotlin 1.7+ + QueryDSL 5.1.0
- `v2.x` — Spring Boot 4 + Kotlin 2.2+ + OpenFeign QueryDSL 7.x
- 브랜치 전략: CONTRIBUTING.md 참고

## Build Commands

```bash
./gradlew build                          # 전체 빌드 + 테스트
./gradlew :querydsl-ktx:test             # 테스트만
./gradlew publishAndReleaseToMavenCentral # Maven Central 배포
```

## Documentation Checklist

코드 변경 후 반드시 확인:

- [ ] AGENTS.md — 아키텍처, 인터페이스 수, 시그니처
- [ ] llms.txt — 링크 목록 (내용 직접 포함 금지, llms-full.txt에 위임)
- [ ] llms-full.txt — 전체 API 시그니처, 버전 번호
- [ ] docs/guide/ — 해당 기능 EN/KO 페이지
- [ ] README.md / README.ko.md — 버전, 기능 목록
- [ ] KDoc — 예시 코드가 실제 API와 일치

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

## Conventions

- 한국어 응답
- 커밋은 요청 시에만, 파일 개별 스테이징
- Extension 함수는 null-safety 계약 준수 (AGENTS.md 참고)
- 새 함수: this-null, arg-null, both-null, both-non-null 4가지 테스트 필수
- 외부 의존성은 compileOnly
- apiVersion/languageVersion 1.7 유지 (Kotlin 1.7 호환)
