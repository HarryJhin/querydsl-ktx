package com.querydsl.ktx.extensions

import com.querydsl.core.types.Expression
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.SimpleExpression

interface SimpleExpressionExtensions {

    /**
     * null-safe 동등 비교. 어느 쪽이든 null이면 조건을 건너뛴다.
     *
     * ```sql
     * -- this = entity.status, right = 'ACTIVE'
     * status = 'ACTIVE'
     * ```
     *
     * @param right 비교 대상 값, null이면 조건을 건너뛴다
     * @return `this = right`, 어느 쪽이든 null이면 null
     */
    infix fun <T> SimpleExpression<T>?.eq(right: T?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.eq(right)
    }

    /**
     * null-safe 동등 비교 (Expression). 어느 쪽이든 null이면 조건을 건너뛴다.
     *
     * ```sql
     * -- this = entity.status, right = entity.defaultStatus
     * status = default_status
     * ```
     *
     * @param right 비교 대상 표현식, null이면 조건을 건너뛴다
     * @return `this = right`, 어느 쪽이든 null이면 null
     */
    infix fun <T> SimpleExpression<T>?.eq(right: Expression<in T>?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.eq(right)
    }

    /**
     * null-safe 부등 비교. 어느 쪽이든 null이면 조건을 건너뛴다.
     *
     * ```sql
     * -- this = entity.status, right = 'DELETED'
     * status != 'DELETED'
     * ```
     *
     * @param right 비교 대상 값, null이면 조건을 건너뛴다
     * @return `this != right`, 어느 쪽이든 null이면 null
     */
    infix fun <T> SimpleExpression<T>?.ne(right: T?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.ne(right)
    }

    /**
     * null-safe 부등 비교 (Expression). 어느 쪽이든 null이면 조건을 건너뛴다.
     *
     * ```sql
     * -- this = entity.status, right = entity.defaultStatus
     * status != default_status
     * ```
     *
     * @param right 비교 대상 표현식, null이면 조건을 건너뛴다
     * @return `this != right`, 어느 쪽이든 null이면 null
     */
    infix fun <T> SimpleExpression<T>?.ne(right: Expression<in T>?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.ne(right)
    }

    /**
     * null-safe IN. 어느 쪽이든 null이면 조건을 건너뛴다.
     *
     * ```sql
     * -- this = entity.status, right = ['ACTIVE', 'PENDING']
     * status IN ('ACTIVE', 'PENDING')
     * ```
     *
     * @param right 포함 여부를 검사할 값 목록, null이면 조건을 건너뛴다
     * @return `this IN right`, 어느 쪽이든 null이면 null
     */
    infix fun <T> SimpleExpression<T>?.`in`(right: Collection<T>?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.`in`(right)
    }

    /**
     * null-safe NOT IN. 어느 쪽이든 null이면 조건을 건너뛴다.
     *
     * ```sql
     * -- this = entity.status, right = ['DELETED', 'BLOCKED']
     * status NOT IN ('DELETED', 'BLOCKED')
     * ```
     *
     * @param right 제외할 값 목록, null이면 조건을 건너뛴다
     * @return `this NOT IN right`, 어느 쪽이든 null이면 null
     */
    infix fun <T> SimpleExpression<T>?.notIn(right: Collection<T>?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.notIn(right)
    }
}
