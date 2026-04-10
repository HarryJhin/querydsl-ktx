# querydsl-ktx

[![CI](https://github.com/HarryJhin/querydsl-ktx/actions/workflows/ci.yml/badge.svg)](https://github.com/HarryJhin/querydsl-ktx/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.harryjhin/querydsl-ktx)](https://central.sonatype.com/artifact/io.github.harryjhin/querydsl-ktx)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.7%2B-7F52FF.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0%2B-6DB33F.svg?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

[English](README.md) | [문서 사이트](https://harryjhin.github.io/querydsl-ktx/ko/)

QueryDSL 동적 쿼리의 **BooleanBuilder 보일러플레이트를 90% 제거**하는 null-safe infix Kotlin 확장 라이브러리.

## 문제

```kotlin
val builder = BooleanBuilder()
if (name != null) builder.and(member.name.contains(name))
if (status != null) builder.and(member.status.eq(status))
if (minAge != null && maxAge != null) builder.and(member.age.between(minAge, maxAge))
else if (minAge != null) builder.and(member.age.goe(minAge))
else if (maxAge != null) builder.and(member.age.loe(maxAge))
if (startDate != null && endDate != null) builder.and(member.createdAt.between(startDate, endDate))
else if (startDate != null) builder.and(member.createdAt.goe(startDate))
else if (endDate != null) builder.and(member.createdAt.loe(endDate))
```

선택적 필터 하나마다 1-3줄이 추가됩니다. 범위 필터는 3개 분기가 필요합니다. 패턴은 항상 같습니다.

## 해결

```kotlin
selectFrom(member)
    .where(
        member.name contains name,
        member.status eq status,
        member.age between (minAge to maxAge),
        member.createdAt between (startDate to endDate),
    )
    .page(pageable)
```

null 파라미터는 자동으로 건너뜁니다. `Pair`를 사용한 `between`이 한쪽만 있는 범위를 자동 처리합니다.
30줄 → 10줄.

## 빠른 시작

```kotlin
// 1. 의존성 추가
implementation("io.github.harryjhin:querydsl-ktx-spring-boot-starter:0.1.0")
```

```kotlin
// 2. QuerydslRepository 상속
@Repository
class MemberRepository : QuerydslRepository<Member>() {

    private val member = QMember.member

    fun search(name: String?, status: String?, pageable: Pageable): Page<Member> =
        selectFrom(member)
            .where(
                member.name contains name,
                member.status eq status,
            )
            .page(pageable)
}
```

## 왜 querydsl-ktx?

| 접근 방식 | 단점 |
|----------|------|
| `BooleanBuilder` | 장황하고 범위 필터에서 실수하기 쉬움 |
| `Specification` | QueryDSL과 별개, infix 문법 없음 |
| Top-level 확장 함수 | 글로벌 스코프 오염, 이름 충돌 |
| 직접 만든 헬퍼 함수 | 엔티티마다 중복, 부분적 커버리지 |
| **querydsl-ktx** | 표준화, 테스트 완료, 완전한 커버리지 |

확장 함수는 **인터페이스 구현**을 통해 스코프가 제한됩니다 — 글로벌 네임스페이스 오염 없음. [자세히 보기 →](https://harryjhin.github.io/querydsl-ktx/ko/why/)

## 기능

- **8개 확장 인터페이스** — Boolean, Simple, Comparable, Number, String, Temporal, Collection, SubQuery 표현식용 null-safe infix 연산자
- **역방향 between** — `value between (expr1 to expr2)` 단측 생존 지원
- **Expressions reified 래퍼** — `Expressions.numberTemplate(Float::class.java, ...)` 대신 `numberTemplate<Float>(...)`
- **Case/When DSL** — `case<Int> { when(pred) then value; otherwise(default) }` null-safe 분기
- **페이지네이션 헬퍼** — `slice()`, `page()`, `fetch()` + SortSpec 동적 정렬
- **Bulk DML** — `modifying { }` 자동 flush/clear

## 문서

| | |
|---|---|
| [설치](https://harryjhin.github.io/querydsl-ktx/ko/getting-started/installation/) | Gradle, Maven 설정 및 모듈 선택 |
| [빠른 시작](https://harryjhin.github.io/querydsl-ktx/ko/getting-started/quick-start/) | 5분 안에 첫 동적 쿼리 작성 |
| [사용 가이드](https://harryjhin.github.io/querydsl-ktx/ko/guide/dynamic-queries/) | 동적 쿼리, 확장 인터페이스, Expressions, Case/When DSL, 페이지네이션, Bulk DML |
| [API 레퍼런스](https://harryjhin.github.io/querydsl-ktx/api/) | Dokka 생성 API 문서 |

## 요구사항

| 의존성 | 최소 버전 |
|--------|----------|
| Spring Boot | 3.0+ |
| QueryDSL | 5.1.0+ |
| Kotlin | 1.7+ |
| Java | 17+ |

## For LLMs

이 프로젝트는 AI 컨텍스트를 위한 [`llms.txt`](llms.txt)를 [GitMCP](https://gitmcp.io/)를 통해 제공합니다.

### Claude Code

```bash
claude mcp add querydsl-ktx -- mcp-remote https://gitmcp.io/HarryJhin/querydsl-ktx
```

### Codex

```bash
codex mcp add querydsl-ktx -- mcp-remote https://gitmcp.io/HarryJhin/querydsl-ktx
```

### Gemini CLI

```bash
gemini mcp add querydsl-ktx -- mcp-remote https://gitmcp.io/HarryJhin/querydsl-ktx
```

### JSON config (수동 설정)

```json
{
  "mcpServers": {
    "querydsl-ktx": {
      "command": "npx",
      "args": ["-y", "@anthropic-ai/mcp-remote@latest", "https://gitmcp.io/HarryJhin/querydsl-ktx/sse"]
    }
  }
}
```

## 라이선스

[Apache License 2.0](LICENSE)
