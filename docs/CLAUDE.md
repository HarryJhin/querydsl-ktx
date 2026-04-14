# Documentation Rules

## 사이트 구조 (VitePress)

- `docs/.vitepress/config.ts`: 전역 설정 (locales, rewrites, plugins)
- `docs/en/`: 영어 문서, `docs/ko/`: 한국어 문서
- `docs/en/config.ts`, `docs/ko/config.ts`: 언어별 nav/sidebar
- `docs/public/`: 정적 파일 (robots.txt, 로고)
- Dokka API 문서는 CI에서 `docs/public/api/`로 복사
- llms.txt, llms-full.txt는 CI에서 `docs/public/`로 복사

## 작성 규칙

- **AI 어투 금지**: em dash, double dash 사용 금지. 콜론(`:`) 마침표(`.`) 쉼표(`,`)로 대체
- **AI 특유 표현 금지**: "leverage", "seamlessly", "effortlessly", "robust", "comprehensive" 등
- VitePress 문법: admonition은 `::: tip/warning/info`, 탭은 `::: code-group`
- 버전 번호는 하드코딩 (Jinja 매크로 없음), 릴리스 시 Release Workflow에 따라 갱신
- 페이지 상단에 `description` frontmatter 필수 (AEO/GEO 최적화)

## 변환 참고 (MkDocs → VitePress)

| MkDocs | VitePress |
|---|---|
| `!!! tip "Title"` + 4칸 들여쓰기 | `::: tip Title` + 내용 + `:::` |
| `!!! note` | `::: info` |
| `=== "Tab"` + 코드 블록 | `::: code-group` + `` ```lang [Tab] `` + `:::` |
| `{{ version }}` | 하드코딩 (`0.5.0`) |
| `(1)!` 코드 어노테이션 | 인라인 주석으로 대체 (VitePress 미지원) |
