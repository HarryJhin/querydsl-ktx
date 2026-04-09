package com.querydsl.ktx.extensions

import com.querydsl.core.types.Expression
import com.querydsl.core.types.dsl.BooleanExpression

interface BooleanExpressionExtensions {

    /**
     * Combines two conditions with AND, skipping any null side.
     *
     * Use this to build dynamic WHERE clauses by chaining nullable conditions.
     * If one side is null, the other side is returned as-is.
     *
     * ```sql
     * -- this = (active = true), right = (age > 20)
     * active = true AND age > 20
     *
     * -- this = null, right = (age > 20) -> only right is returned
     * age > 20
     * ```
     *
     * @param right the condition to AND with, or null to skip
     * @return combined condition, or the non-null side if one is null, or null if both are null
     */
    infix fun BooleanExpression?.and(right: BooleanExpression?): BooleanExpression? = when {
        this == null -> right
        right == null -> this
        else -> this.and(right)
    }

    /**
     * Combines this condition with the OR-reduction of [predicates] using AND.
     *
     * Useful when you need "base condition AND (any of these)" logic.
     * If this is null, only the OR-combined result is returned.
     * If all predicates are null, only this is returned.
     *
     * ```sql
     * -- this = (active = true), predicates = [(role = 'ADMIN'), (role = 'MANAGER')]
     * active = true AND (role = 'ADMIN' OR role = 'MANAGER')
     * ```
     *
     * @param predicates conditions to be OR-combined
     * @return `this AND (p1 OR p2 OR ...)`, or null if both sides resolve to null
     */
    infix fun BooleanExpression?.andAnyOf(predicates: List<BooleanExpression?>): BooleanExpression? =
        this and predicates.reduceOrNull { acc, expr -> acc or expr }

    /**
     * Combines two conditions with OR, skipping any null side.
     *
     * Use this to build dynamic WHERE clauses where any matching condition suffices.
     * If one side is null, the other side is returned as-is.
     *
     * ```sql
     * -- this = (role = 'ADMIN'), right = (role = 'MANAGER')
     * role = 'ADMIN' OR role = 'MANAGER'
     *
     * -- this = null, right = (role = 'MANAGER') -> only right is returned
     * role = 'MANAGER'
     * ```
     *
     * @param right the condition to OR with, or null to skip
     * @return combined condition, or the non-null side if one is null, or null if both are null
     */
    infix fun BooleanExpression?.or(right: BooleanExpression?): BooleanExpression? = when {
        this == null -> right
        right == null -> this
        else -> this.or(right)
    }

    /**
     * Combines this condition with the AND-reduction of [predicates] using OR.
     *
     * Useful when you need "base condition OR (all of these)" logic.
     * If this is null, only the AND-combined result is returned.
     * If all predicates are null, only this is returned.
     *
     * ```sql
     * -- this = (vip = true), predicates = [(age > 20), (active = true)]
     * vip = true OR (age > 20 AND active = true)
     * ```
     *
     * @param predicates conditions to be AND-combined
     * @return `this OR (p1 AND p2 AND ...)`, or null if both sides resolve to null
     */
    infix fun BooleanExpression?.orAllOf(predicates: List<BooleanExpression?>): BooleanExpression? =
        this or predicates.reduceOrNull { acc, expr -> acc and expr }

    /**
     * Null-safe equality check for boolean expressions.
     *
     * Returns null (skips the condition) when either side is null,
     * making it safe for dynamic query construction.
     *
     * ```sql
     * -- this = entity.active, right = true
     * active = true
     *
     * -- right = null -> condition is skipped
     * ```
     *
     * @param right the boolean value to compare against, or null to skip
     * @return `this = right`, or null if either side is null
     */
    infix fun BooleanExpression?.eq(right: Boolean?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.eq(right)
    }

    /**
     * Null-safe NULLIF that returns SQL NULL when this equals [other].
     *
     * Skips the expression entirely when either side is Kotlin null.
     *
     * ```sql
     * NULLIF(active, is_deleted)
     * ```
     *
     * @param other the expression to compare against, or null to skip
     * @return `NULLIF(this, other)`, or null if either side is null
     */
    infix fun BooleanExpression?.nullif(other: Expression<Boolean>?): BooleanExpression? = when {
        this == null || other == null -> null
        else -> this.nullif(other)
    }

    /**
     * Null-safe NULLIF that returns SQL NULL when this equals [other].
     *
     * Skips the expression entirely when either side is Kotlin null.
     *
     * ```sql
     * NULLIF(active, true)
     * ```
     *
     * @param other the boolean value to compare against, or null to skip
     * @return `NULLIF(this, other)`, or null if either side is null
     */
    infix fun BooleanExpression?.nullif(other: Boolean?): BooleanExpression? = when {
        this == null || other == null -> null
        else -> this.nullif(other)
    }

    /**
     * Null-safe COALESCE that falls back to [expr] when this is SQL NULL.
     *
     * Skips the expression entirely when either side is Kotlin null.
     *
     * ```sql
     * COALESCE(active, is_visible)
     * ```
     *
     * @param expr the fallback expression, or null to skip
     * @return `COALESCE(this, expr)`, or null if either side is null
     */
    infix fun BooleanExpression?.coalesce(expr: Expression<Boolean>?): BooleanExpression? = when {
        this == null || expr == null -> null
        else -> this.coalesce(expr)
    }

    /**
     * Null-safe COALESCE that falls back to [arg] when this is SQL NULL.
     *
     * Skips the expression entirely when either side is Kotlin null.
     *
     * ```sql
     * COALESCE(active, false)
     * ```
     *
     * @param arg the fallback boolean value, or null to skip
     * @return `COALESCE(this, arg)`, or null if either side is null
     */
    infix fun BooleanExpression?.coalesce(arg: Boolean?): BooleanExpression? = when {
        this == null || arg == null -> null
        else -> this.coalesce(arg)
    }
}
