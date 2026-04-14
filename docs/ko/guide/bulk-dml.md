---
description: modifying() 래퍼를 통한 안전한 벌크 UPDATE, DELETE. 자동 flush/clear 지원.
---

# 벌크 DML

`QuerydslSupport`는 벌크 update 및 delete 작업을 위한 `modifying { }` 블록을 제공하며,
`EntityManager.flush()`와 `EntityManager.clear()`를 자동으로 처리합니다.

---

## 문제점

QueryDSL을 통해 벌크 DML(UPDATE 또는 DELETE)을 실행하면, SQL이 영속성 컨텍스트를 우회하여
데이터베이스에 직접 실행됩니다. 이로 인해 두 가지 문제가 발생합니다:

1. **오래된 쓰기** -- 영속성 컨텍스트에 플러시되지 않은 변경 사항이 손실됩니다.
2. **오래된 읽기** -- 영속성 컨텍스트가 여전히 이전 엔티티 상태를 보유합니다.

::: code-group

```kotlin [이전 -- 잊기 쉬움]
fun deactivateAll() {
    entityManager.flush()   // 실행 전 flush
    queryFactory.update(member)
        .set(member.active, false)
        .where(member.lastLogin.lt(cutoffDate))
        .execute()
    entityManager.clear()   // 실행 후 clear
}
```

```kotlin [이후 -- 보장됨]
fun deactivateAll() {
    modifying {
        update(member)
            .set(member.active, false)
            .where(member.lastLogin lt cutoffDate)
            .execute()
    }
}
```

:::

---

## modifying 동작 방식

```kotlin
protected fun <R> modifying(
    flushAutomatically: Boolean = true,
    clearAutomatically: Boolean = true,
    block: () -> R,
): R {
    if (flushAutomatically) entityManager.flush()
    return try {
        block()
    } finally {
        if (clearAutomatically) entityManager.clear()
    }
}
```

실행 순서는 다음과 같습니다:

1. **flush** -- 대기 중인 엔티티 변경 사항을 데이터베이스에 기록
2. **execute** -- 벌크 DML 문 실행
3. **clear** -- 영속성 컨텍스트에서 모든 엔티티 제거 (`finally` 블록에서)

`clear`는 `finally` 블록에서 실행되므로, DML 문에서 예외가 발생하더라도
영속성 컨텍스트가 정리됩니다.

---

## flush와 clear 제어

두 플래그 모두 기본값은 `true`입니다. 필요한 경우 오버라이드하세요:

### flush 건너뛰기

대기 중인 엔티티 변경이 없다고 확신할 때:

```kotlin
modifying(flushAutomatically = false) {
    update(member)
        .set(member.active, false)
        .where(member.status eq "EXPIRED")
        .execute()
}
```

### clear 건너뛰기

이후에 영향받는 엔티티를 읽지 않을 때:

```kotlin
modifying(clearAutomatically = false) {
    delete(auditLog)
        .where(auditLog.createdAt lt retentionDate)
        .execute()
}
```

### 둘 다 건너뛰기

전체 트랜잭션을 제어하는 경우 최대 성능을 위해:

```kotlin
modifying(flushAutomatically = false, clearAutomatically = false) {
    update(member)
        .set(member.loginCount, member.loginCount.add(1))
        .where(member.id eq memberId)
        .execute()
}
```

::: warning 건너뛰는 것의 의미를 이해하세요
이 플래그는 의미를 이해한 경우에만 비활성화하세요:

- **flush 건너뛰기** -- 벌크 DML 전에 엔티티가 수정되지 않은 경우 안전합니다.
- **clear 건너뛰기** -- 메서드가 즉시 반환되거나, 이후 코드가 영향받는 엔티티를
  읽지 않는 경우 안전합니다.
:::

---

## @Modifying과의 비교

Spring Data JPA의 `@Modifying` 어노테이션은 비슷한 목적을 가집니다.
`modifying { }`과의 비교는 다음과 같습니다:

| | `@Modifying` | `modifying { }` |
|---|---|---|
| 범위 | 메서드에 어노테이션 | 코드 블록을 래핑 |
| Flush 제어 | `@Modifying(flushAutomatically = true)` | `modifying(flushAutomatically = true)` |
| Clear 제어 | `@Modifying(clearAutomatically = true)` | `modifying(clearAutomatically = true)` |
| 쿼리 타입 | JPQL/네이티브 `@Query` 문자열 | 타입 안전한 QueryDSL 빌더 |
| 다중 문 | 메서드당 하나의 문 | 하나의 블록에 여러 문 |
| 기본 flush | `false` | **`true`** |
| 기본 clear | `false` | **`true`** |

::: tip 더 안전한 기본값
`modifying { }`은 두 플래그 모두 `true`가 기본값이고, `@Modifying`은 둘 다
`false`가 기본값입니다. 더 안전한 기본값으로 인해 보호를 직접 활성화하는 것을
기억할 필요 없이, 보호를 해제할 때만 명시적으로 설정하면 됩니다.
:::

---

## 벌크 Update 예제

### 단일 필드 설정

::: code-group

```kotlin [Kotlin]
fun deactivateExpired(cutoffDate: LocalDate): Long =
    modifying {
        update(member)
            .set(member.active, false)
            .where(member.lastLogin lt cutoffDate)
            .execute()
    }
```

```sql [SQL]
UPDATE member
SET active = false
WHERE last_login < ?
```

:::

### 여러 필드 설정

::: code-group

```kotlin [Kotlin]
fun softDelete(ids: List<Long>): Long =
    modifying {
        update(member)
            .set(member.status, Status.DELETED)
            .set(member.deletedAt, LocalDateTime.now())
            .where(member.id `in` ids)
            .execute()
    }
```

```sql [SQL]
UPDATE member
SET status = 'DELETED', deleted_at = NOW()
WHERE id IN (?, ?, ?)
```

:::

### 카운터 증가

::: code-group

```kotlin [Kotlin]
fun incrementViewCount(articleId: Long): Long =
    modifying {
        update(article)
            .set(article.viewCount, article.viewCount.add(1))
            .where(article.id eq articleId)
            .execute()
    }
```

```sql [SQL]
UPDATE article
SET view_count = view_count + 1
WHERE id = ?
```

:::

---

## 벌크 Delete 예제

::: code-group

```kotlin [Kotlin]
fun purgeOldLogs(retentionDate: LocalDateTime): Long =
    modifying {
        delete(auditLog)
            .where(auditLog.createdAt lt retentionDate)
            .execute()
    }
```

```sql [SQL]
DELETE FROM audit_log
WHERE created_at < ?
```

:::

---

## 하나의 블록에 여러 문

`@Modifying`과 달리, 하나의 `modifying` 블록에서 여러 DML 문을 실행할 수 있습니다.
flush는 모든 문 실행 전에 한 번, clear는 모든 문 실행 후에 한 번 발생합니다:

```kotlin
fun archiveAndClean(cutoffDate: LocalDate) {
    modifying {
        // 아카이브로 이동
        update(member)
            .set(member.status, Status.ARCHIVED)
            .where(member.lastLogin lt cutoffDate)
            .execute()

        // 관련 데이터 정리
        delete(memberPreference)
            .where(memberPreference.memberId `in`
                select(member.id).from(member)
                    .where(member.status eq Status.ARCHIVED)
            )
            .execute()
    }
}
```

::: tip 트랜잭션 경계
`modifying { }`은 트랜잭션을 관리하지 않습니다. 두 문 모두 호출자의 트랜잭션 내에서
실행됩니다. 하나라도 실패하면 전체 트랜잭션이 롤백됩니다.
:::

---

## 이전 & 이후

::: code-group

```kotlin [이전]
@Repository
class MemberRepository(
    private val queryFactory: JPAQueryFactory,
    private val entityManager: EntityManager,
) {
    fun deactivateExpired(cutoffDate: LocalDate): Long {
        entityManager.flush()
        val count = queryFactory.update(QMember.member)
            .set(QMember.member.active, false)
            .where(QMember.member.lastLogin.lt(cutoffDate))
            .execute()
        entityManager.clear()
        return count
    }
}
```

```kotlin [이후]
@Repository
class MemberRepository : QuerydslRepository<Member>() {

    private val member = QMember.member

    fun deactivateExpired(cutoffDate: LocalDate): Long =
        modifying {
            update(member)
                .set(member.active, false)
                .where(member.lastLogin lt cutoffDate)
                .execute()
        }
}
```

:::
