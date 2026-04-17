package com.querydsl.ktx.extensions

import com.querydsl.core.types.Expression
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.ComparableExpression

/**
 * Null-safe comparison and range operators for [ComparableExpression].
 *
 * **Before** -- vanilla QueryDSL:
 * ```kotlin
 * if (from != null && to != null) builder.and(entity.createdAt.between(from, to))
 * else if (from != null)          builder.and(entity.createdAt.goe(from))
 * else if (to != null)            builder.and(entity.createdAt.loe(to))
 * ```
 *
 * **After** -- with this interface:
 * ```kotlin
 * val predicate = entity.createdAt between (from to to)
 * ```
 *
 * Implement this interface (or use the pre-built scope object) to bring
 * the infix operators into scope.
 */
interface ComparableExpressionExtensions {

    /**
     * Null-safe greater-than check that skips the condition when either side is null.
     *
     * ```sql
     * -- this = entity.age, right = 20
     * age > 20
     * ```
     *
     * @param right the value to compare against, or null to skip
     * @return `this > right`, or null if either side is null
     */
    infix fun <T : Comparable<T>> ComparableExpression<T>?.gt(right: T?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.gt(right)
    }

    /**
     * Null-safe greater-than check against another expression.
     *
     * Skips the condition when either side is null.
     *
     * ```sql
     * -- this = entity.score, right = entity.minScore
     * score > min_score
     * ```
     *
     * @param right the expression to compare against, or null to skip
     * @return `this > right`, or null if either side is null
     */
    infix fun <T : Comparable<T>> ComparableExpression<T>?.gt(right: Expression<T>?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.gt(right)
    }

    /**
     * Null-safe greater-than-or-equal check that skips the condition when either side is null.
     *
     * ```sql
     * -- this = entity.age, right = 20
     * age >= 20
     * ```
     *
     * @param right the value to compare against, or null to skip
     * @return `this >= right`, or null if either side is null
     */
    infix fun <T : Comparable<T>> ComparableExpression<T>?.goe(right: T?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.goe(right)
    }

    /**
     * Null-safe greater-than-or-equal check against another expression.
     *
     * Skips the condition when either side is null.
     *
     * ```sql
     * -- this = entity.score, right = entity.minScore
     * score >= min_score
     * ```
     *
     * @param right the expression to compare against, or null to skip
     * @return `this >= right`, or null if either side is null
     */
    infix fun <T : Comparable<T>> ComparableExpression<T>?.goe(right: Expression<T>?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.goe(right)
    }

    /**
     * Null-safe less-than check that skips the condition when either side is null.
     *
     * ```sql
     * -- this = entity.age, right = 60
     * age < 60
     * ```
     *
     * @param right the value to compare against, or null to skip
     * @return `this < right`, or null if either side is null
     */
    infix fun <T : Comparable<T>> ComparableExpression<T>?.lt(right: T?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.lt(right)
    }

    /**
     * Null-safe less-than check against another expression.
     *
     * Skips the condition when either side is null.
     *
     * ```sql
     * -- this = entity.score, right = entity.maxScore
     * score < max_score
     * ```
     *
     * @param right the expression to compare against, or null to skip
     * @return `this < right`, or null if either side is null
     */
    infix fun <T : Comparable<T>> ComparableExpression<T>?.lt(right: Expression<T>?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.lt(right)
    }

    /**
     * Null-safe less-than-or-equal check that skips the condition when either side is null.
     *
     * ```sql
     * -- this = entity.age, right = 60
     * age <= 60
     * ```
     *
     * @param right the value to compare against, or null to skip
     * @return `this <= right`, or null if either side is null
     */
    infix fun <T : Comparable<T>> ComparableExpression<T>?.loe(right: T?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.loe(right)
    }

    /**
     * Null-safe less-than-or-equal check against another expression.
     *
     * Skips the condition when either side is null.
     *
     * ```sql
     * -- this = entity.score, right = entity.maxScore
     * score <= max_score
     * ```
     *
     * @param right the expression to compare against, or null to skip
     * @return `this <= right`, or null if either side is null
     */
    infix fun <T : Comparable<T>> ComparableExpression<T>?.loe(right: Expression<T>?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.loe(right)
    }

    /**
     * Null-safe BETWEEN with partial range support.
     *
     * Adapts to whichever bounds are present: both yields BETWEEN,
     * only lower yields `>=`, only upper yields `<=`, neither skips.
     * This makes it ideal for optional date/value range filters.
     *
     * ```sql
     * -- from = '2024-01-01', to = '2024-12-31'
     * created_at BETWEEN '2024-01-01' AND '2024-12-31'
     *
     * -- from = '2024-01-01', to = null
     * created_at >= '2024-01-01'
     *
     * -- from = null, to = '2024-12-31'
     * created_at <= '2024-12-31'
     * ```
     *
     * @param range a `from to to` pair where either bound can be null
     * @return `this BETWEEN from AND to`, or null if this is null or both bounds are null
     */
    infix fun <T : Comparable<T>> ComparableExpression<T>?.between(range: Pair<T?, T?>): BooleanExpression? {
        val (from, to) = range
        return when {
            this == null -> null
            from != null && to != null -> this.between(from, to)
            from != null -> this.goe(from)
            to != null -> this.loe(to)
            else -> null
        }
    }

    /**
     * Null-safe BETWEEN using a Kotlin [ClosedRange] where both bounds are guaranteed present.
     *
     * Skips the condition when this is null.
     *
     * ```sql
     * -- this = entity.age, range = 20..60
     * age BETWEEN 20 AND 60
     * ```
     *
     * @param range a closed range (`from..to`)
     * @return `this BETWEEN from AND to`, or null if this is null
     */
    infix fun <T : Comparable<T>> ComparableExpression<T>?.between(range: ClosedRange<T>): BooleanExpression? =
        this?.between(range.start, range.endInclusive)

    /**
     * Null-safe NOT BETWEEN with partial range support.
     *
     * Mirrors [between] semantics: both bounds yields NOT BETWEEN,
     * only lower yields `<`, only upper yields `>`, neither skips.
     *
     * ```sql
     * -- from = '2024-01-01', to = '2024-12-31'
     * created_at NOT BETWEEN '2024-01-01' AND '2024-12-31'
     *
     * -- from = '2024-01-01', to = null
     * created_at < '2024-01-01'
     *
     * -- from = null, to = '2024-12-31'
     * created_at > '2024-12-31'
     * ```
     *
     * @param range a `from to to` pair where either bound can be null
     * @return NOT BETWEEN clause, one-sided comparison, or null if this is null or both bounds are null
     */
    infix fun <T : Comparable<T>> ComparableExpression<T>?.notBetween(range: Pair<T?, T?>): BooleanExpression? {
        val (from, to) = range
        return when {
            this == null -> null
            from != null && to != null -> this.notBetween(from, to)
            from != null -> this.lt(from)
            to != null -> this.gt(to)
            else -> null
        }
    }

    /**
     * Null-safe NULLIF that returns SQL NULL when this equals [other].
     *
     * Skips the expression entirely when either side is Kotlin null.
     *
     * ```sql
     * NULLIF(score, default_score)
     * ```
     *
     * @param other the expression to compare against, or null to skip
     * @return `NULLIF(this, other)`, or null if either side is null
     */
    infix fun <T : Comparable<T>> ComparableExpression<T>?.nullif(
        other: Expression<T>?,
    ): ComparableExpression<T>? = when {
        this == null || other == null -> null
        else -> this.nullif(other)
    }

    /**
     * Null-safe NULLIF that returns SQL NULL when this equals [other].
     *
     * Skips the expression entirely when either side is Kotlin null.
     *
     * ```sql
     * NULLIF(score, 0)
     * ```
     *
     * @param other the value to compare against, or null to skip
     * @return `NULLIF(this, other)`, or null if either side is null
     */
    infix fun <T : Comparable<T>> ComparableExpression<T>?.nullif(other: T?): ComparableExpression<T>? = when {
        this == null || other == null -> null
        else -> this.nullif(other)
    }

    /**
     * Null-safe COALESCE that falls back to [expr] when this is SQL NULL.
     *
     * Skips the expression entirely when either side is Kotlin null.
     *
     * ```sql
     * COALESCE(score, default_score)
     * ```
     *
     * @param expr the fallback expression, or null to skip
     * @return `COALESCE(this, expr)`, or null if either side is null
     */
    infix fun <T : Comparable<T>> ComparableExpression<T>?.coalesce(
        expr: Expression<T>?,
    ): ComparableExpression<T>? = when {
        this == null || expr == null -> null
        else -> this.coalesce(expr)
    }

    /**
     * Null-safe COALESCE that falls back to [arg] when this is SQL NULL.
     *
     * Skips the expression entirely when either side is Kotlin null.
     *
     * ```sql
     * COALESCE(score, 0)
     * ```
     *
     * @param arg the fallback value, or null to skip
     * @return `COALESCE(this, arg)`, or null if either side is null
     */
    infix fun <T : Comparable<T>> ComparableExpression<T>?.coalesce(arg: T?): ComparableExpression<T>? = when {
        this == null || arg == null -> null
        else -> this.coalesce(arg)
    }

    /**
     * Null-safe reverse BETWEEN: checks if a value falls between two expression bounds.
     *
     * Adapts to whichever bounds are present: both yields `lower <= value AND upper >= value`,
     * only lower yields `lower <= value`, only upper yields `upper >= value`, neither skips.
     *
     * ```sql
     * -- this = '2024-06-15', lower = entity.startDate, upper = entity.endDate
     * start_date <= '2024-06-15' AND end_date >= '2024-06-15'
     * ```
     *
     * @param range a `lower to upper` pair of expressions where either bound can be null
     * @return `lower <= this AND upper >= this`, or null if this is null or both bounds are null
     */
    infix fun <T : Comparable<T>> T?.between(
        range: Pair<ComparableExpression<T>?, ComparableExpression<T>?>,
    ): BooleanExpression? {
        val (lower, upper) = range
        return when {
            this == null -> null
            lower != null && upper != null -> lower.loe(this).and(upper.goe(this))
            lower != null -> lower.loe(this)
            upper != null -> upper.goe(this)
            else -> null
        }
    }

    /**
     * Creates a [Pair] of expressions using the `..` operator, for use with reverse [between].
     *
     * ```kotlin
     * now between (entity.startAt..entity.endAt)
     * // equivalent to: now between (entity.startAt to entity.endAt)
     * ```
     *
     * @param other the upper bound expression
     * @return a pair of `(this, other)`
     */
    operator fun <T : Comparable<T>> ComparableExpression<T>.rangeTo(
        other: ComparableExpression<T>,
    ): Pair<ComparableExpression<T>, ComparableExpression<T>> = this to other
}
