package com.querydsl.ktx.extensions

import com.querydsl.core.types.Expression
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.CollectionExpressionBase

interface CollectionExpressionExtensions {

    /**
     * null-safe CONTAINS. 컬렉션에 child가 포함되어 있는지 검사한다.
     * 어느 쪽이든 null이면 조건을 건너뛴다.
     *
     * ```sql
     * -- this = entity.roles, child = 'ADMIN'
     * 'ADMIN' IN (roles)
     * ```
     *
     * @param child 포함 여부를 검사할 값, null이면 조건을 건너뛴다
     * @return `child IN this`, 어느 쪽이든 null이면 null
     */
    infix fun <T : Collection<E>, E> CollectionExpressionBase<T, E>?.contains(child: E?): BooleanExpression? = when {
        this == null || child == null -> null
        else -> this.contains(child)
    }

    /**
     * null-safe CONTAINS (Expression). 컬렉션에 child가 포함되어 있는지 검사한다.
     * 어느 쪽이든 null이면 조건을 건너뛴다.
     *
     * ```sql
     * -- this = entity.roles, child = entity.defaultRole
     * default_role IN (roles)
     * ```
     *
     * @param child 포함 여부를 검사할 표현식, null이면 조건을 건너뛴다
     * @return `child IN this`, 어느 쪽이든 null이면 null
     */
    infix fun <T : Collection<E>, E> CollectionExpressionBase<T, E>?.contains(child: Expression<E>?): BooleanExpression? = when {
        this == null || child == null -> null
        else -> this.contains(child)
    }
}
