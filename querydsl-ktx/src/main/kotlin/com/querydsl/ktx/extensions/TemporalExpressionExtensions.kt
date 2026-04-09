package com.querydsl.ktx.extensions

import com.querydsl.core.types.Expression
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.TemporalExpression

interface TemporalExpressionExtensions {

    /**
     * null-safe AFTER (초과). 시간순으로 this가 right보다 뒤인지 검사한다.
     * 어느 쪽이든 null이면 조건을 건너뛴다.
     *
     * ```sql
     * -- this = entity.createdAt, right = '2024-01-01'
     * created_at > '2024-01-01'
     * ```
     *
     * @param right 비교 대상 시점, null이면 조건을 건너뛴다
     * @return `this > right`, 어느 쪽이든 null이면 null
     */
    infix fun <T : Comparable<T>> TemporalExpression<T>?.after(right: T?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.after(right)
    }

    /**
     * null-safe AFTER (Expression). 시간순으로 this가 right보다 뒤인지 검사한다.
     * 어느 쪽이든 null이면 조건을 건너뛴다.
     *
     * ```sql
     * -- this = entity.endDate, right = entity.startDate
     * end_date > start_date
     * ```
     *
     * @param right 비교 대상 표현식, null이면 조건을 건너뛴다
     * @return `this > right`, 어느 쪽이든 null이면 null
     */
    infix fun <T : Comparable<T>> TemporalExpression<T>?.after(right: Expression<T>?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.after(right)
    }

    /**
     * null-safe BEFORE (미만). 시간순으로 this가 right보다 앞인지 검사한다.
     * 어느 쪽이든 null이면 조건을 건너뛴다.
     *
     * ```sql
     * -- this = entity.createdAt, right = '2024-12-31'
     * created_at < '2024-12-31'
     * ```
     *
     * @param right 비교 대상 시점, null이면 조건을 건너뛴다
     * @return `this < right`, 어느 쪽이든 null이면 null
     */
    infix fun <T : Comparable<T>> TemporalExpression<T>?.before(right: T?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.before(right)
    }

    /**
     * null-safe BEFORE (Expression). 시간순으로 this가 right보다 앞인지 검사한다.
     * 어느 쪽이든 null이면 조건을 건너뛴다.
     *
     * ```sql
     * -- this = entity.startDate, right = entity.endDate
     * start_date < end_date
     * ```
     *
     * @param right 비교 대상 표현식, null이면 조건을 건너뛴다
     * @return `this < right`, 어느 쪽이든 null이면 null
     */
    infix fun <T : Comparable<T>> TemporalExpression<T>?.before(right: Expression<T>?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.before(right)
    }
}
