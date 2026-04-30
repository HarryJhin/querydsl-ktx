---
description: 왜 querydsl-ktx인가. 대안 비교, scoping 결정 이유, null-safe 동적 쿼리를 위한 의사결정 가이드.
---

# 왜 querydsl-ktx인가?

QueryDSL은 JVM 타입 세이프 쿼리의 사실상 표준이지만, 동적 쿼리에는 빈틈이
있습니다. `BooleanBuilder`는 verbose하고, 범위 필터는 3분기 `if/else`가 필요하며,
모든 팀이 결국 자체 헬퍼 함수를 만들게 됩니다. 이 페이지는 일반적인 접근 방식을
비교하고 querydsl-ktx의 설계 결정을 설명합니다.

## 대안 비교

| 접근 방식 | 장점 | 단점 |
|----------|------|------|
| `BooleanBuilder` | 내장, 추가 의존성 없음 | verbose. 범위 필터는 3분기 `if/else`. null 체크가 흩어짐 |
| `BooleanExpression` 체이닝 (`.and(...).and(...)`) | 약간 더 깔끔 | 모든 단계에서 수동 null 체크 |
| Spring Data `Specification` | 타입 세이프, JPA 친화 | QueryDSL과 분리. infix 문법 없음. 복잡한 조인에 불편 |
| 최상위 Kotlin 확장 함수 (`fun StringPath.eqOrNull(...)`) | 호출부 간결 | 전역 namespace 오염. stdlib와 이름 충돌. 리포지토리 밖에서도 사용 가능 |
| 엔티티별 수동 헬퍼 (`fun memberByStatus(s: Status?)`) | 팀에 친숙 | 엔티티마다 중복. 부분 커버리지. 표준 없음 |
| **querydsl-ktx** | null-safe 계약, 간결, 인터페이스로 scoped, CI 매트릭스 검증 | 추가 의존성 (compileOnly) |

## 왜 인터페이스 Scoping인가?

querydsl-ktx는 확장을 **인터페이스**로 제공합니다. 최상위 함수가 아닙니다.
리포지토리에서 인터페이스를 구현하면 infix 연산자가 스코프에 들어옵니다.

```kotlin
@Repository
class MemberRepository : QuerydslRepository<Member>() {  // 8개 확장 인터페이스 모두 구현
    fun findByCondition(name: String?, status: Status?): List<Member> =
        selectFrom(member)
            .where(
                member.name eq name,        // name이 null이면 건너뜀
                member.status eq status,    // status가 null이면 건너뜀
            )
            .fetch()
}
```

`eq` infix는 **이 클래스 안에서만** 사용 가능합니다. 리포지토리 밖에서는 일반
QueryDSL `member.name.eq(value)`만 가능하므로, 코드베이스의 나머지 부분은 우발적
오용으로부터 보호됩니다.

이것이 최상위 확장 함수와 비교한 핵심 트레이드오프입니다. import 시점에 몇
글자 더 들지만, namespace 오염이 없고 임의의 비즈니스 로직에서 path에 `eq`를
호출할 위험이 없습니다.

## Before vs After

::: code-group

```kotlin [Before: BooleanBuilder]
fun search(name: String?, minAge: Int?, maxAge: Int?, status: Status?): List<Member> {
    val builder = BooleanBuilder()
    if (name != null) builder.and(member.name.contains(name))
    if (minAge != null && maxAge != null) builder.and(member.age.between(minAge, maxAge))
    else if (minAge != null) builder.and(member.age.goe(minAge))
    else if (maxAge != null) builder.and(member.age.loe(maxAge))
    if (status != null) builder.and(member.status.eq(status))
    return queryFactory.selectFrom(member).where(builder).fetch()
}
```

```kotlin [After: querydsl-ktx]
fun search(name: String?, minAge: Int?, maxAge: Int?, status: Status?): List<Member> =
    selectFrom(member)
        .where(
            member.name contains name,
            member.age between (minAge to maxAge),
            member.status eq status,
        )
        .fetch()
```

:::

9줄 범위 필터 사다리가 단일 `between (minAge to maxAge)` 표현식으로 압축됩니다.
라이브러리는 한쪽 생존 패턴을 자동 적용합니다. `minAge`만 있으면 `>=`, `maxAge`만
있으면 `<=`, 둘 다 null이면 건너뜁니다.

## 어느 접근 방식을 선택할까

| 상황 | 권장 |
|-----|------|
| 대부분 정적 쿼리만 쓰는 기존 QueryDSL 프로젝트 | `BooleanBuilder` 유지. 마이그레이션 비용이 가치를 넘어섬 |
| 동적 필터가 많은 신규 프로젝트 | querydsl-ktx |
| Spring Data를 많이 쓰는 JPA 전용 프로젝트 | `Specification`. 단 조인이나 projection에서 불편할 정도라면 querydsl-ktx |
| 팀이 최상위 확장을 선호하고 namespace 비용을 감수 | 최상위 확장 (오픈소스 라이브러리 존재) |
| 단일 엔티티의 일회성 헬퍼 | 수동 헬퍼. 단 다른 곳에서도 필요할지 검토 |

## querydsl-ktx가 하지 않는 것

- QueryDSL을 대체하지 않습니다. `selectFrom(...)`, `orderBy`, `groupBy`, `having`, `join`은 그대로 사용합니다.
- 새 SQL 기능을 추가하지 않습니다. 모든 연산자는 기존 QueryDSL 멤버를 래핑합니다.
- 예외를 조용히 삼키지 않습니다. `null` 전파가 계약이지만, 잘못된 사용 (예: LIKE가 아닌 표현식에 `escape` 호출)에는 `ExpressionException`을 던집니다.

이 라이브러리는 QueryDSL 자체가 다루지 않는 **null 체크 보일러플레이트**를
제거하기 위해 존재합니다. QueryDSL이 이미 제공하는 모든 기능과 호환됩니다.
