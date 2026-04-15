---
description: Safe bulk UPDATE and DELETE operations with automatic flush/clear via the modifying() wrapper.
---

# Bulk DML

`QuerydslSupport` provides a `modifying { }` block for safe bulk update and delete operations
that handles `EntityManager.flush()` and `EntityManager.clear()` automatically.

---

## The Problem

When executing bulk DML (UPDATE or DELETE) through QueryDSL, the SQL runs directly
against the database, bypassing the persistence context. This creates two issues:

1. **Stale writes**: unflushed changes in the persistence context are lost.
2. **Stale reads**: the persistence context still holds old entity state.

::: code-group

```kotlin [Before: Easy to forget]
fun deactivateAll() {
    entityManager.flush()   // flush before
    queryFactory.update(member)
        .set(member.active, false)
        .where(member.lastLogin.lt(cutoffDate))
        .execute()
    entityManager.clear()   // clear after
}
```

```kotlin [After: Guaranteed]
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

## How modifying Works

```kotlin
protected fun <R> modifying(
    flushAutomatically: Boolean = true,
    clearAutomatically: Boolean = true,
    block: () -> R,
): R {
    check(entityManager.isJoinedToTransaction()) { ... }
    if (flushAutomatically) entityManager.flush()
    return try {
        block()
    } finally {
        if (clearAutomatically) entityManager.clear()
    }
}
```

The execution order is:

1. **transaction check**: throws `IllegalStateException` if no active transaction
2. **flush**: writes any pending entity changes to the database
3. **execute**: runs your bulk DML statement
4. **clear**: evicts all entities from the persistence context (in `finally`)

The `clear` runs in a `finally` block, so the persistence context is cleaned up
even if the DML statement throws an exception.

::: warning Important notes
- **Transaction required.** `modifying {}` requires an active transaction.
  Annotate the calling method or class with `@Transactional`.
  Without a transaction, an `IllegalStateException` is thrown.
- **`clear()` detaches all entities.** `entityManager.clear()` evicts every
  managed entity in the persistence context, not just those affected by the
  bulk operation. If you hold references to other managed entities in the same
  transaction, their dirty (unflushed) changes will be lost.
:::

---

## Controlling flush and clear

Both flags default to `true`. Override them when needed:

### Skip flush

When you know there are no pending entity changes:

```kotlin
modifying(flushAutomatically = false) {
    update(member)
        .set(member.active, false)
        .where(member.status eq "EXPIRED")
        .execute()
}
```

### Skip clear

When you won't read the affected entities afterward:

```kotlin
modifying(clearAutomatically = false) {
    delete(auditLog)
        .where(auditLog.createdAt lt retentionDate)
        .execute()
}
```

### Skip both

For maximum performance when you control the full transaction:

```kotlin
modifying(flushAutomatically = false, clearAutomatically = false) {
    update(member)
        .set(member.loginCount, member.loginCount.add(1))
        .where(member.id eq memberId)
        .execute()
}
```

::: warning Know what you're skipping
Only disable these flags when you understand the implications:

- **Skip flush**: safe when no entities were modified before the bulk DML.
- **Skip clear**: safe when the method returns immediately after, or
  when subsequent code doesn't read the affected entities.
:::

---

## Comparison with @Modifying

Spring Data JPA's `@Modifying` annotation serves a similar purpose.
Here's how `modifying { }` compares:

| | `@Modifying` | `modifying { }` |
|---|---|---|
| Scope | Annotates a method | Wraps a code block |
| Flush control | `@Modifying(flushAutomatically = true)` | `modifying(flushAutomatically = true)` |
| Clear control | `@Modifying(clearAutomatically = true)` | `modifying(clearAutomatically = true)` |
| Query type | JPQL/native `@Query` strings | Type-safe QueryDSL builder |
| Multiple statements | One statement per method | Multiple statements in one block |
| Default flush | `false` | **`true`** |
| Default clear | `false` | **`true`** |

::: tip Safer defaults
`modifying { }` defaults both flags to `true`, while `@Modifying` defaults both
to `false`. The safer defaults mean you need to actively opt out of protection,
rather than remembering to opt in.
:::

---

## Bulk Update Examples

### Set a single field

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

### Set multiple fields

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

### Increment a counter

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

## Bulk Delete Examples

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

## Multiple Statements in One Block

Unlike `@Modifying`, you can run multiple DML statements in a single `modifying` block.
The flush happens once before all statements, and the clear happens once after:

```kotlin
fun archiveAndClean(cutoffDate: LocalDate) {
    modifying {
        // Move to archive
        update(member)
            .set(member.status, Status.ARCHIVED)
            .where(member.lastLogin lt cutoffDate)
            .execute()

        // Clean up related data
        delete(memberPreference)
            .where(memberPreference.memberId `in`
                select(member.id).from(member)
                    .where(member.status eq Status.ARCHIVED)
            )
            .execute()
    }
}
```

::: tip Transaction boundary
`modifying { }` does not manage transactions. Both statements execute within
the caller's transaction. If either fails, the entire transaction rolls back.
:::

---

## Before & After

::: code-group

```kotlin [Before]
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

```kotlin [After]
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
