package com.querydsl.ktx.extensions

import com.querydsl.core.types.Expression
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.CollectionExpressionBase

interface CollectionExpressionExtensions {

    /**
     * Null-safe collection membership check that skips the condition when either side is null.
     *
     * ```sql
     * -- this = entity.roles, child = 'ADMIN'
     * 'ADMIN' IN (roles)
     * ```
     *
     * @param child the value to check for membership, or null to skip
     * @return `child IN this`, or null if either side is null
     */
    infix fun <T : Collection<E>, E> CollectionExpressionBase<T, E>?.contains(child: E?): BooleanExpression? = when {
        this == null || child == null -> null
        else -> this.contains(child)
    }

    /**
     * Null-safe collection membership check against another expression.
     *
     * Skips the condition when either side is null.
     *
     * ```sql
     * -- this = entity.roles, child = entity.defaultRole
     * default_role IN (roles)
     * ```
     *
     * @param child the expression to check for membership, or null to skip
     * @return `child IN this`, or null if either side is null
     */
    infix fun <T : Collection<E>, E> CollectionExpressionBase<T, E>?.contains(
        child: Expression<E>?,
    ): BooleanExpression? = when {
        this == null || child == null -> null
        else -> this.contains(child)
    }
}
