package com.querydsl.ktx.extensions

import com.querydsl.core.types.Expression
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.NumberExpression

/**
 * Null-safe comparison and range operators for [NumberExpression].
 *
 * [NumberExpression] does not extend [ComparableExpression][com.querydsl.core.types.dsl.ComparableExpression]
 * in the QueryDSL type hierarchy, so this interface provides the same
 * set of operators (`gt`, `goe`, `lt`, `loe`, `between`) specifically for numbers.
 *
 * **Before** -- vanilla QueryDSL:
 * ```kotlin
 * if (minPrice != null && maxPrice != null) builder.and(entity.price.between(minPrice, maxPrice))
 * else if (minPrice != null)                builder.and(entity.price.goe(minPrice))
 * else if (maxPrice != null)                builder.and(entity.price.loe(maxPrice))
 * ```
 *
 * **After** -- with this interface:
 * ```kotlin
 * val predicate = entity.price between (minPrice to maxPrice)
 * ```
 *
 * Implement this interface (or use the pre-built scope object) to bring
 * the infix operators into scope.
 */
interface NumberExpressionExtensions {

    /**
     * Null-safe greater-than check that skips the condition when either side is null.
     *
     * ```sql
     * -- this = entity.price, right = 10000
     * price > 10000
     * ```
     *
     * @param right the value to compare against, or null to skip
     * @return `this > right`, or null if either side is null
     */
    infix fun <T> NumberExpression<T>?.gt(right: T?): BooleanExpression?
        where T : Number, T : Comparable<*> = when {
        this == null || right == null -> null
        else -> this.gt(right)
    }

    /**
     * Null-safe greater-than check against another expression.
     *
     * Skips the condition when either side is null.
     *
     * ```sql
     * -- this = entity.price, right = entity.minPrice
     * price > min_price
     * ```
     *
     * @param right the expression to compare against, or null to skip
     * @return `this > right`, or null if either side is null
     */
    infix fun <T> NumberExpression<T>?.gt(right: Expression<T>?): BooleanExpression?
        where T : Number, T : Comparable<*> = when {
        this == null || right == null -> null
        else -> this.gt(right)
    }

    /**
     * Null-safe greater-than-or-equal check that skips the condition when either side is null.
     *
     * ```sql
     * -- this = entity.price, right = 10000
     * price >= 10000
     * ```
     *
     * @param right the value to compare against, or null to skip
     * @return `this >= right`, or null if either side is null
     */
    infix fun <T> NumberExpression<T>?.goe(right: T?): BooleanExpression?
        where T : Number, T : Comparable<*> = when {
        this == null || right == null -> null
        else -> this.goe(right)
    }

    /**
     * Null-safe greater-than-or-equal check against another expression.
     *
     * Skips the condition when either side is null.
     *
     * ```sql
     * -- this = entity.price, right = entity.minPrice
     * price >= min_price
     * ```
     *
     * @param right the expression to compare against, or null to skip
     * @return `this >= right`, or null if either side is null
     */
    infix fun <T> NumberExpression<T>?.goe(right: Expression<T>?): BooleanExpression?
        where T : Number, T : Comparable<*> = when {
        this == null || right == null -> null
        else -> this.goe(right)
    }

    /**
     * Null-safe less-than check that skips the condition when either side is null.
     *
     * ```sql
     * -- this = entity.price, right = 50000
     * price < 50000
     * ```
     *
     * @param right the value to compare against, or null to skip
     * @return `this < right`, or null if either side is null
     */
    infix fun <T> NumberExpression<T>?.lt(right: T?): BooleanExpression?
        where T : Number, T : Comparable<*> = when {
        this == null || right == null -> null
        else -> this.lt(right)
    }

    /**
     * Null-safe less-than check against another expression.
     *
     * Skips the condition when either side is null.
     *
     * ```sql
     * -- this = entity.price, right = entity.maxPrice
     * price < max_price
     * ```
     *
     * @param right the expression to compare against, or null to skip
     * @return `this < right`, or null if either side is null
     */
    infix fun <T> NumberExpression<T>?.lt(right: Expression<T>?): BooleanExpression?
        where T : Number, T : Comparable<*> = when {
        this == null || right == null -> null
        else -> this.lt(right)
    }

    /**
     * Null-safe less-than-or-equal check that skips the condition when either side is null.
     *
     * ```sql
     * -- this = entity.price, right = 50000
     * price <= 50000
     * ```
     *
     * @param right the value to compare against, or null to skip
     * @return `this <= right`, or null if either side is null
     */
    infix fun <T> NumberExpression<T>?.loe(right: T?): BooleanExpression?
        where T : Number, T : Comparable<*> = when {
        this == null || right == null -> null
        else -> this.loe(right)
    }

    /**
     * Null-safe less-than-or-equal check against another expression.
     *
     * Skips the condition when either side is null.
     *
     * ```sql
     * -- this = entity.price, right = entity.maxPrice
     * price <= max_price
     * ```
     *
     * @param right the expression to compare against, or null to skip
     * @return `this <= right`, or null if either side is null
     */
    infix fun <T> NumberExpression<T>?.loe(right: Expression<T>?): BooleanExpression?
        where T : Number, T : Comparable<*> = when {
        this == null || right == null -> null
        else -> this.loe(right)
    }

    /**
     * Null-safe BETWEEN with partial range support.
     *
     * Adapts to whichever bounds are present: both yields BETWEEN,
     * only lower yields `>=`, only upper yields `<=`, neither skips.
     * This makes it ideal for optional price/quantity range filters.
     *
     * ```sql
     * -- from = 10000, to = 50000
     * price BETWEEN 10000 AND 50000
     *
     * -- from = 10000, to = null
     * price >= 10000
     *
     * -- from = null, to = 50000
     * price <= 50000
     * ```
     *
     * @param range a `from to to` pair where either bound can be null
     * @return `this BETWEEN from AND to`, or null if this is null or both bounds are null
     */
    infix fun <T> NumberExpression<T>?.between(range: Pair<T?, T?>): BooleanExpression?
        where T : Number, T : Comparable<*> {
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
     * -- this = entity.price, range = 10000..50000
     * price BETWEEN 10000 AND 50000
     * ```
     *
     * @param range a closed range (`from..to`)
     * @return `this BETWEEN from AND to`, or null if this is null
     */
    infix fun <T> NumberExpression<T>?.between(range: ClosedRange<T>): BooleanExpression?
        where T : Number, T : Comparable<T> =
        this?.between(range.start, range.endInclusive)

    /**
     * Null-safe NOT BETWEEN that skips the condition when this is null.
     *
     * ```sql
     * -- from = 10000, to = 50000
     * price NOT BETWEEN 10000 AND 50000
     * ```
     *
     * @param range a `from to to` pair with both bounds required
     * @return `this NOT BETWEEN from AND to`, or null if this is null
     */
    infix fun <T> NumberExpression<T>?.notBetween(range: Pair<T, T>): BooleanExpression?
        where T : Number, T : Comparable<*> =
        this?.notBetween(range.first, range.second)

    /**
     * Null-safe NULLIF that returns SQL NULL when this equals [other].
     *
     * Skips the expression entirely when either side is Kotlin null.
     *
     * ```sql
     * NULLIF(price, 0)
     * ```
     *
     * @param other the expression to compare against, or null to skip
     * @return `NULLIF(this, other)`, or null if either side is null
     */
    infix fun <T> NumberExpression<T>?.nullif(other: Expression<T>?): NumberExpression<T>?
        where T : Number, T : Comparable<*> = when {
        this == null || other == null -> null
        else -> this.nullif(other)
    }

    /**
     * Null-safe NULLIF that returns SQL NULL when this equals [other].
     *
     * Skips the expression entirely when either side is Kotlin null.
     *
     * ```sql
     * NULLIF(price, 0)
     * ```
     *
     * @param other the value to compare against, or null to skip
     * @return `NULLIF(this, other)`, or null if either side is null
     */
    infix fun <T> NumberExpression<T>?.nullif(other: T?): NumberExpression<T>?
        where T : Number, T : Comparable<*> = when {
        this == null || other == null -> null
        else -> this.nullif(other)
    }

    /**
     * Null-safe COALESCE that falls back to [expr] when this is SQL NULL.
     *
     * Skips the expression entirely when either side is Kotlin null.
     *
     * ```sql
     * COALESCE(price, default_price)
     * ```
     *
     * @param expr the fallback expression, or null to skip
     * @return `COALESCE(this, expr)`, or null if either side is null
     */
    infix fun <T> NumberExpression<T>?.coalesce(expr: Expression<T>?): NumberExpression<T>?
        where T : Number, T : Comparable<*> = when {
        this == null || expr == null -> null
        else -> this.coalesce(expr)
    }

    /**
     * Null-safe COALESCE that falls back to [arg] when this is SQL NULL.
     *
     * Skips the expression entirely when either side is Kotlin null.
     *
     * ```sql
     * COALESCE(price, 0)
     * ```
     *
     * @param arg the fallback value, or null to skip
     * @return `COALESCE(this, arg)`, or null if either side is null
     */
    infix fun <T> NumberExpression<T>?.coalesce(arg: T?): NumberExpression<T>?
        where T : Number, T : Comparable<*> = when {
        this == null || arg == null -> null
        else -> this.coalesce(arg)
    }
}
