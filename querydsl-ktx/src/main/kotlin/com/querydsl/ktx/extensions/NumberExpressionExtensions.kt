package com.querydsl.ktx.extensions

import com.querydsl.core.types.Expression
import com.querydsl.core.types.Ops
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.Expressions
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
     * Null-safe NOT BETWEEN with partial range support.
     *
     * Mirrors [between] semantics: both bounds yields NOT BETWEEN,
     * only lower yields `<`, only upper yields `>`, neither skips.
     *
     * ```sql
     * -- from = 10000, to = 50000
     * price NOT BETWEEN 10000 AND 50000
     *
     * -- from = 10000, to = null
     * price < 10000
     *
     * -- from = null, to = 50000
     * price > 50000
     * ```
     *
     * @param range a `from to to` pair where either bound can be null
     * @return NOT BETWEEN clause, one-sided comparison, or null if this is null or both bounds are null
     */
    infix fun <T> NumberExpression<T>?.notBetween(range: Pair<T?, T?>): BooleanExpression?
        where T : Number, T : Comparable<*> {
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
     * Null-safe addition that skips the operation when either side is null.
     *
     * ```sql
     * -- this = entity.price, right = 1000
     * price + 1000
     * ```
     *
     * @param right the value to add, or null to skip
     * @return `this + right`, or null if either side is null
     */
    infix fun <T> NumberExpression<T>?.add(right: T?): NumberExpression<T>?
        where T : Number, T : Comparable<*> = when {
        this == null || right == null -> null
        else -> Expressions.numberOperation(this.type, Ops.ADD, this, Expressions.constant(right))
    }

    /**
     * Null-safe addition against another expression.
     *
     * ```sql
     * price + tax
     * ```
     *
     * @param right the expression to add, or null to skip
     * @return `this + right`, or null if either side is null
     */
    infix fun <T> NumberExpression<T>?.add(right: Expression<T>?): NumberExpression<T>?
        where T : Number, T : Comparable<*> = when {
        this == null || right == null -> null
        else -> Expressions.numberOperation(this.type, Ops.ADD, this, right)
    }

    /**
     * Null-safe subtraction that skips the operation when either side is null.
     *
     * ```sql
     * -- this = entity.price, right = discount
     * price - discount
     * ```
     *
     * @param right the value to subtract, or null to skip
     * @return `this - right`, or null if either side is null
     */
    infix fun <T> NumberExpression<T>?.subtract(right: T?): NumberExpression<T>?
        where T : Number, T : Comparable<*> = when {
        this == null || right == null -> null
        else -> Expressions.numberOperation(this.type, Ops.SUB, this, Expressions.constant(right))
    }

    /**
     * Null-safe subtraction against another expression.
     *
     * ```sql
     * price - discount
     * ```
     *
     * @param right the expression to subtract, or null to skip
     * @return `this - right`, or null if either side is null
     */
    infix fun <T> NumberExpression<T>?.subtract(right: Expression<T>?): NumberExpression<T>?
        where T : Number, T : Comparable<*> = when {
        this == null || right == null -> null
        else -> Expressions.numberOperation(this.type, Ops.SUB, this, right)
    }

    /**
     * Null-safe multiplication that skips the operation when either side is null.
     *
     * ```sql
     * -- this = entity.price, right = taxRate
     * price * 1.1
     * ```
     *
     * @param right the value to multiply by, or null to skip
     * @return `this * right`, or null if either side is null
     */
    infix fun <T> NumberExpression<T>?.multiply(right: T?): NumberExpression<T>?
        where T : Number, T : Comparable<*> = when {
        this == null || right == null -> null
        else -> Expressions.numberOperation(this.type, Ops.MULT, this, Expressions.constant(right))
    }

    /**
     * Null-safe multiplication against another expression.
     *
     * ```sql
     * price * tax_rate
     * ```
     *
     * @param right the expression to multiply by, or null to skip
     * @return `this * right`, or null if either side is null
     */
    infix fun <T> NumberExpression<T>?.multiply(right: Expression<T>?): NumberExpression<T>?
        where T : Number, T : Comparable<*> = when {
        this == null || right == null -> null
        else -> Expressions.numberOperation(this.type, Ops.MULT, this, right)
    }

    /**
     * Null-safe division that skips the operation when either side is null.
     *
     * ```sql
     * -- this = entity.total, right = quantity
     * total / quantity
     * ```
     *
     * @param right the value to divide by, or null to skip
     * @return `this / right`, or null if either side is null
     */
    infix fun <T> NumberExpression<T>?.divide(right: T?): NumberExpression<T>?
        where T : Number, T : Comparable<*> = when {
        this == null || right == null -> null
        else -> Expressions.numberOperation(this.type, Ops.DIV, this, Expressions.constant(right))
    }

    /**
     * Null-safe division against another expression.
     *
     * ```sql
     * total / quantity
     * ```
     *
     * @param right the expression to divide by, or null to skip
     * @return `this / right`, or null if either side is null
     */
    infix fun <T> NumberExpression<T>?.divide(right: Expression<T>?): NumberExpression<T>?
        where T : Number, T : Comparable<*> = when {
        this == null || right == null -> null
        else -> Expressions.numberOperation(this.type, Ops.DIV, this, right)
    }

    /**
     * Null-safe modulo that skips the operation when either side is null.
     *
     * ```sql
     * -- this = entity.id, right = 10
     * id % 10
     * ```
     *
     * @param right the divisor, or null to skip
     * @return `this % right`, or null if either side is null
     */
    infix fun <T> NumberExpression<T>?.mod(right: T?): NumberExpression<T>?
        where T : Number, T : Comparable<*> = when {
        this == null || right == null -> null
        else -> Expressions.numberOperation(this.type, Ops.MOD, this, Expressions.constant(right))
    }

    /**
     * Null-safe modulo against another expression.
     *
     * ```sql
     * id % shard_count
     * ```
     *
     * @param right the divisor expression, or null to skip
     * @return `this % right`, or null if either side is null
     */
    infix fun <T> NumberExpression<T>?.mod(right: Expression<T>?): NumberExpression<T>?
        where T : Number, T : Comparable<*> = when {
        this == null || right == null -> null
        else -> Expressions.numberOperation(this.type, Ops.MOD, this, right)
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

    /**
     * Null-safe reverse BETWEEN: checks if a numeric value falls between two expression bounds.
     *
     * Adapts to whichever bounds are present: both yields `lower <= value AND upper >= value`,
     * only lower yields `lower <= value`, only upper yields `upper >= value`, neither skips.
     *
     * ```sql
     * -- this = 30000, lower = entity.minPrice, upper = entity.maxPrice
     * min_price <= 30000 AND max_price >= 30000
     * ```
     *
     * @param range a `lower to upper` pair of expressions where either bound can be null
     * @return `lower <= this AND upper >= this`, or null if this is null or both bounds are null
     */
    infix fun <T> T?.between(
        range: Pair<NumberExpression<T>?, NumberExpression<T>?>,
    ): BooleanExpression? where T : Number, T : Comparable<*> {
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
     * score between (entity.minScore..entity.maxScore)
     * // equivalent to: score between (entity.minScore to entity.maxScore)
     * ```
     *
     * @param other the upper bound expression
     * @return a pair of `(this, other)`
     */
    operator fun <T> NumberExpression<T>.rangeTo(
        other: NumberExpression<T>,
    ): Pair<NumberExpression<T>, NumberExpression<T>> where T : Number, T : Comparable<*> = this to other

    /**
     * Kotlin `+` operator for numeric expression building.
     *
     * Non-null contract: both sides must be present. For null-skip semantics
     * in dynamic WHERE building use [add] instead.
     *
     * ```sql
     * price + 1000
     * ```
     */
    operator fun <T> NumberExpression<T>.plus(right: T): NumberExpression<T>
        where T : Number, T : Comparable<*> =
        Expressions.numberOperation(this.type, Ops.ADD, this, Expressions.constant(right))

    /**
     * Kotlin `+` operator against another expression.
     *
     * ```sql
     * price + tax
     * ```
     */
    operator fun <T> NumberExpression<T>.plus(right: Expression<T>): NumberExpression<T>
        where T : Number, T : Comparable<*> =
        Expressions.numberOperation(this.type, Ops.ADD, this, right)

    /**
     * Kotlin `-` operator for numeric expression building.
     *
     * Non-null contract: both sides must be present. For null-skip semantics
     * use [subtract] instead.
     *
     * ```sql
     * price - 100
     * ```
     */
    operator fun <T> NumberExpression<T>.minus(right: T): NumberExpression<T>
        where T : Number, T : Comparable<*> =
        Expressions.numberOperation(this.type, Ops.SUB, this, Expressions.constant(right))

    /**
     * Kotlin `-` operator against another expression.
     *
     * ```sql
     * price - discount
     * ```
     */
    operator fun <T> NumberExpression<T>.minus(right: Expression<T>): NumberExpression<T>
        where T : Number, T : Comparable<*> =
        Expressions.numberOperation(this.type, Ops.SUB, this, right)

    /**
     * Kotlin `*` operator for numeric expression building.
     *
     * Non-null contract: both sides must be present. For null-skip semantics
     * use [multiply] instead.
     *
     * ```sql
     * price * 2
     * ```
     */
    operator fun <T> NumberExpression<T>.times(right: T): NumberExpression<T>
        where T : Number, T : Comparable<*> =
        Expressions.numberOperation(this.type, Ops.MULT, this, Expressions.constant(right))

    /**
     * Kotlin `*` operator against another expression.
     *
     * ```sql
     * price * tax_rate
     * ```
     */
    operator fun <T> NumberExpression<T>.times(right: Expression<T>): NumberExpression<T>
        where T : Number, T : Comparable<*> =
        Expressions.numberOperation(this.type, Ops.MULT, this, right)

    /**
     * Kotlin `/` operator for numeric expression building.
     *
     * Non-null contract: both sides must be present. For null-skip semantics
     * use [divide] instead.
     *
     * ```sql
     * total / 2
     * ```
     */
    operator fun <T> NumberExpression<T>.div(right: T): NumberExpression<T>
        where T : Number, T : Comparable<*> =
        Expressions.numberOperation(this.type, Ops.DIV, this, Expressions.constant(right))

    /**
     * Kotlin `/` operator against another expression.
     *
     * ```sql
     * total / quantity
     * ```
     */
    operator fun <T> NumberExpression<T>.div(right: Expression<T>): NumberExpression<T>
        where T : Number, T : Comparable<*> =
        Expressions.numberOperation(this.type, Ops.DIV, this, right)

    /**
     * Kotlin `%` operator for numeric expression building.
     *
     * Non-null contract: both sides must be present. For null-skip semantics
     * use [mod] instead.
     *
     * ```sql
     * id % 10
     * ```
     */
    operator fun <T> NumberExpression<T>.rem(right: T): NumberExpression<T>
        where T : Number, T : Comparable<*> =
        Expressions.numberOperation(this.type, Ops.MOD, this, Expressions.constant(right))

    /**
     * Kotlin `%` operator against another expression.
     *
     * ```sql
     * id % shard_count
     * ```
     */
    operator fun <T> NumberExpression<T>.rem(right: Expression<T>): NumberExpression<T>
        where T : Number, T : Comparable<*> =
        Expressions.numberOperation(this.type, Ops.MOD, this, right)

    /**
     * Kotlin unary `-` operator for numeric expression building.
     *
     * ```sql
     * -price
     * ```
     */
    operator fun <T> NumberExpression<T>.unaryMinus(): NumberExpression<T>
        where T : Number, T : Comparable<*> = this.negate()
}
