package com.querydsl.ktx.extensions

import com.querydsl.core.types.Expression
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.SimpleExpression

/**
 * Null-safe equality, inequality, and membership operators for [SimpleExpression].
 *
 * **Before** -- vanilla QueryDSL:
 * ```kotlin
 * if (status != null) builder.and(entity.status.eq(status))
 * if (ids != null)    builder.and(entity.id.`in`(ids))
 * ```
 *
 * **After** -- with this interface:
 * ```kotlin
 * val predicate = (entity.status eq status) and (entity.id `in` ids)
 * ```
 *
 * Implement this interface (or use the pre-built scope object) to bring
 * the infix operators into scope.
 */
interface SimpleExpressionExtensions {

    /**
     * Null-safe equality check that skips the condition when either side is null.
     *
     * Ideal for dynamic WHERE clauses where a filter parameter may be absent.
     *
     * ```sql
     * -- this = entity.status, right = 'ACTIVE'
     * status = 'ACTIVE'
     * ```
     *
     * @param right the value to compare against, or null to skip
     * @return `this = right`, or null if either side is null
     */
    infix fun <T> SimpleExpression<T>?.eq(right: T?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.eq(right)
    }

    /**
     * Null-safe equality check against another expression.
     *
     * Skips the condition when either side is null.
     *
     * ```sql
     * -- this = entity.status, right = entity.defaultStatus
     * status = default_status
     * ```
     *
     * @param right the expression to compare against, or null to skip
     * @return `this = right`, or null if either side is null
     */
    infix fun <T> SimpleExpression<T>?.eq(right: Expression<in T>?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.eq(right)
    }

    /**
     * Null-safe not-equal check that skips the condition when either side is null.
     *
     * ```sql
     * -- this = entity.status, right = 'DELETED'
     * status != 'DELETED'
     * ```
     *
     * @param right the value to compare against, or null to skip
     * @return `this != right`, or null if either side is null
     */
    infix fun <T> SimpleExpression<T>?.ne(right: T?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.ne(right)
    }

    /**
     * Null-safe not-equal check against another expression.
     *
     * Skips the condition when either side is null.
     *
     * ```sql
     * -- this = entity.status, right = entity.defaultStatus
     * status != default_status
     * ```
     *
     * @param right the expression to compare against, or null to skip
     * @return `this != right`, or null if either side is null
     */
    infix fun <T> SimpleExpression<T>?.ne(right: Expression<in T>?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.ne(right)
    }

    /**
     * Null-safe IN check that skips the condition when either side is null.
     *
     * ```sql
     * -- this = entity.status, right = ['ACTIVE', 'PENDING']
     * status IN ('ACTIVE', 'PENDING')
     * ```
     *
     * @param right the collection of values to match against, or null to skip
     * @return `this IN right`, or null if either side is null
     */
    infix fun <T> SimpleExpression<T>?.`in`(right: Collection<T>?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.`in`(right)
    }

    /**
     * Null-safe NOT IN check that skips the condition when either side is null.
     *
     * ```sql
     * -- this = entity.status, right = ['DELETED', 'BLOCKED']
     * status NOT IN ('DELETED', 'BLOCKED')
     * ```
     *
     * @param right the collection of values to exclude, or null to skip
     * @return `this NOT IN right`, or null if either side is null
     */
    infix fun <T> SimpleExpression<T>?.notIn(right: Collection<T>?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.notIn(right)
    }
}
