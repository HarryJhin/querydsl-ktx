---
layout: home

hero:
  name: querydsl-ktx
  text: QueryDSL을 위한 Null-safe Kotlin 확장
  tagline: BooleanBuilder와 if 검사 없이 동적 쿼리 작성
  actions:
    - theme: brand
      text: 시작하기
      link: /ko/getting-started/installation
    - theme: alt
      text: GitHub에서 보기
      link: https://github.com/HarryJhin/querydsl-ktx

features:
  - title: 기본적으로 Null-Safe
    details: 모든 확장 함수는 receiver나 인자가 null이면 null을 반환합니다. 예외 없이 안전하게 전파됩니다.
    icon: 🛡️
  - title: Infix & 선언적
    details: path eq value, path contains text처럼 Kotlin infix 함수로 읽기 쉬운 쿼리를 작성합니다.
    icon: ⚡
  - title: 타입 안전 & 스코프 제한
    details: 확장 함수는 QuerydslRepository 내부에서만 동작합니다. 네임스페이스 오염이 없습니다.
    icon: 🔒
  - title: 페이지네이션 내장
    details: page(), slice(), exactSlice()와 SortSpec으로 타입 안전 동적 정렬. 카운트 쿼리는 자동 처리됩니다.
    icon: 📄
  - title: Spring Boot 자동 설정
    details: starter만 추가하면 JPAQueryFactory 빈이 자동 등록됩니다. GraalVM native image도 지원합니다.
    icon: 🔧
  - title: Case/When DSL
    details: null 분기 자동 제거가 되는 타입 안전 CASE 표현식. Searched와 Simple CASE 모두 지원합니다.
    icon: 🔀
---

## 이전 vs 이후

::: code-group

```kotlin [이전: 기본 QueryDSL]
val builder = BooleanBuilder()
if (name != null) {
    builder.and(member.name.contains(name))
}
if (status != null) {
    builder.and(member.status.eq(status))
}
return queryFactory
    .selectFrom(member)
    .where(builder)
    .fetch()
```

```kotlin [이후: querydsl-ktx]
return selectFrom(member)
    .where(
        member.name contains name,
        member.status eq status,
    )
    .fetch()
```

:::
