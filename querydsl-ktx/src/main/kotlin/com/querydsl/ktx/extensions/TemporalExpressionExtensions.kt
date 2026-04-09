package com.querydsl.ktx.extensions

import com.querydsl.core.types.Expression
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.TemporalExpression

/**
 * Null-safe temporal comparison operators for [TemporalExpression].
 *
 * **Before** -- vanilla QueryDSL:
 * ```kotlin
 * if (startDate != null) builder.and(entity.createdAt.after(startDate))
 * if (endDate != null)   builder.and(entity.createdAt.before(endDate))
 * ```
 *
 * **After** -- with this interface:
 * ```kotlin
 * val predicate = (entity.createdAt after startDate) and (entity.createdAt before endDate)
 * ```
 *
 * Implement this interface (or use the pre-built scope object) to bring
 * the infix operators into scope.
 */
interface TemporalExpressionExtensions {

    /**
     * Null-safe "after" check that tests whether this occurs later than [right].
     *
     * Skips the condition when either side is null.
     *
     * ```sql
     * -- this = entity.createdAt, right = '2024-01-01'
     * created_at > '2024-01-01'
     * ```
     *
     * @param right the point in time to compare against, or null to skip
     * @return `this > right`, or null if either side is null
     */
    infix fun <T : Comparable<T>> TemporalExpression<T>?.after(right: T?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.after(right)
    }

    /**
     * Null-safe "after" check against another temporal expression.
     *
     * Skips the condition when either side is null.
     *
     * ```sql
     * -- this = entity.endDate, right = entity.startDate
     * end_date > start_date
     * ```
     *
     * @param right the expression to compare against, or null to skip
     * @return `this > right`, or null if either side is null
     */
    infix fun <T : Comparable<T>> TemporalExpression<T>?.after(right: Expression<T>?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.after(right)
    }

    /**
     * Null-safe "before" check that tests whether this occurs earlier than [right].
     *
     * Skips the condition when either side is null.
     *
     * ```sql
     * -- this = entity.createdAt, right = '2024-12-31'
     * created_at < '2024-12-31'
     * ```
     *
     * @param right the point in time to compare against, or null to skip
     * @return `this < right`, or null if either side is null
     */
    infix fun <T : Comparable<T>> TemporalExpression<T>?.before(right: T?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.before(right)
    }

    /**
     * Null-safe "before" check against another temporal expression.
     *
     * Skips the condition when either side is null.
     *
     * ```sql
     * -- this = entity.startDate, right = entity.endDate
     * start_date < end_date
     * ```
     *
     * @param right the expression to compare against, or null to skip
     * @return `this < right`, or null if either side is null
     */
    infix fun <T : Comparable<T>> TemporalExpression<T>?.before(right: Expression<T>?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.before(right)
    }
}
