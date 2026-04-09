package com.querydsl.ktx.extensions

import com.querydsl.core.types.Expression
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.NumberExpression

interface NumberExpressionExtensions {

    /**
     * null-safe 초과 비교. 어느 쪽이든 null이면 조건을 건너뛴다.
     *
     * ```sql
     * -- this = entity.price, right = 10000
     * price > 10000
     * ```
     *
     * @param right 비교 대상 값, null이면 조건을 건너뛴다
     * @return `this > right`, 어느 쪽이든 null이면 null
     */
    infix fun <T> NumberExpression<T>?.gt(right: T?): BooleanExpression?
        where T : Number, T : Comparable<*> = when {
        this == null || right == null -> null
        else -> this.gt(right)
    }

    /**
     * null-safe 초과 비교 (Expression). 어느 쪽이든 null이면 조건을 건너뛴다.
     *
     * ```sql
     * -- this = entity.price, right = entity.minPrice
     * price > min_price
     * ```
     *
     * @param right 비교 대상 표현식, null이면 조건을 건너뛴다
     * @return `this > right`, 어느 쪽이든 null이면 null
     */
    infix fun <T> NumberExpression<T>?.gt(right: Expression<T>?): BooleanExpression?
        where T : Number, T : Comparable<*> = when {
        this == null || right == null -> null
        else -> this.gt(right)
    }

    /**
     * null-safe 이상 비교. 어느 쪽이든 null이면 조건을 건너뛴다.
     *
     * ```sql
     * -- this = entity.price, right = 10000
     * price >= 10000
     * ```
     *
     * @param right 비교 대상 값, null이면 조건을 건너뛴다
     * @return `this >= right`, 어느 쪽이든 null이면 null
     */
    infix fun <T> NumberExpression<T>?.goe(right: T?): BooleanExpression?
        where T : Number, T : Comparable<*> = when {
        this == null || right == null -> null
        else -> this.goe(right)
    }

    /**
     * null-safe 이상 비교 (Expression). 어느 쪽이든 null이면 조건을 건너뛴다.
     *
     * ```sql
     * -- this = entity.price, right = entity.minPrice
     * price >= min_price
     * ```
     *
     * @param right 비교 대상 표현식, null이면 조건을 건너뛴다
     * @return `this >= right`, 어느 쪽이든 null이면 null
     */
    infix fun <T> NumberExpression<T>?.goe(right: Expression<T>?): BooleanExpression?
        where T : Number, T : Comparable<*> = when {
        this == null || right == null -> null
        else -> this.goe(right)
    }

    /**
     * null-safe 미만 비교. 어느 쪽이든 null이면 조건을 건너뛴다.
     *
     * ```sql
     * -- this = entity.price, right = 50000
     * price < 50000
     * ```
     *
     * @param right 비교 대상 값, null이면 조건을 건너뛴다
     * @return `this < right`, 어느 쪽이든 null이면 null
     */
    infix fun <T> NumberExpression<T>?.lt(right: T?): BooleanExpression?
        where T : Number, T : Comparable<*> = when {
        this == null || right == null -> null
        else -> this.lt(right)
    }

    /**
     * null-safe 미만 비교 (Expression). 어느 쪽이든 null이면 조건을 건너뛴다.
     *
     * ```sql
     * -- this = entity.price, right = entity.maxPrice
     * price < max_price
     * ```
     *
     * @param right 비교 대상 표현식, null이면 조건을 건너뛴다
     * @return `this < right`, 어느 쪽이든 null이면 null
     */
    infix fun <T> NumberExpression<T>?.lt(right: Expression<T>?): BooleanExpression?
        where T : Number, T : Comparable<*> = when {
        this == null || right == null -> null
        else -> this.lt(right)
    }

    /**
     * null-safe 이하 비교. 어느 쪽이든 null이면 조건을 건너뛴다.
     *
     * ```sql
     * -- this = entity.price, right = 50000
     * price <= 50000
     * ```
     *
     * @param right 비교 대상 값, null이면 조건을 건너뛴다
     * @return `this <= right`, 어느 쪽이든 null이면 null
     */
    infix fun <T> NumberExpression<T>?.loe(right: T?): BooleanExpression?
        where T : Number, T : Comparable<*> = when {
        this == null || right == null -> null
        else -> this.loe(right)
    }

    /**
     * null-safe 이하 비교 (Expression). 어느 쪽이든 null이면 조건을 건너뛴다.
     *
     * ```sql
     * -- this = entity.price, right = entity.maxPrice
     * price <= max_price
     * ```
     *
     * @param right 비교 대상 표현식, null이면 조건을 건너뛴다
     * @return `this <= right`, 어느 쪽이든 null이면 null
     */
    infix fun <T> NumberExpression<T>?.loe(right: Expression<T>?): BooleanExpression?
        where T : Number, T : Comparable<*> = when {
        this == null || right == null -> null
        else -> this.loe(right)
    }

    /**
     * null-safe BETWEEN. null인 쪽은 무시하고 non-null인 쪽만 살린다.
     * 둘 다 non-null이면 BETWEEN, 한쪽만 있으면 `>=` 또는 `<=`, 둘 다 null이면 스킵.
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
     * @param range `from to to` 형태의 범위
     * @return `this BETWEEN from AND to`, this가 null이거나 양쪽 모두 null이면 null
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
     * null-safe BETWEEN (ClosedRange). 양쪽 값이 확정된 범위에 사용한다.
     * this가 null이면 조건을 건너뛴다.
     *
     * ```sql
     * -- this = entity.price, range = 10000..50000
     * price BETWEEN 10000 AND 50000
     * ```
     *
     * @param range 닫힌 범위 (`from..to`)
     * @return `this BETWEEN from AND to`, this가 null이면 null
     */
    infix fun <T> NumberExpression<T>?.between(range: ClosedRange<T>): BooleanExpression?
        where T : Number, T : Comparable<T> =
        this?.between(range.start, range.endInclusive)

    /**
     * null-safe NOT BETWEEN. this가 null이면 조건을 건너뛴다.
     *
     * ```sql
     * -- from = 10000, to = 50000
     * price NOT BETWEEN 10000 AND 50000
     * ```
     *
     * @param range `from to to` 형태의 범위
     * @return `this NOT BETWEEN from AND to`, this가 null이면 null
     */
    infix fun <T> NumberExpression<T>?.notBetween(range: Pair<T, T>): BooleanExpression?
        where T : Number, T : Comparable<*> =
        this?.notBetween(range.first, range.second)

    /**
     * null-safe NULLIF. this가 other와 같으면 NULL을 반환한다.
     * 어느 쪽이든 null이면 조건을 건너뛴다.
     *
     * ```sql
     * NULLIF(price, 0)
     * ```
     *
     * @param other 비교 대상 표현식, null이면 조건을 건너뛴다
     * @return `NULLIF(this, other)`, 어느 쪽이든 null이면 null
     */
    infix fun <T> NumberExpression<T>?.nullif(other: Expression<T>?): NumberExpression<T>?
        where T : Number, T : Comparable<*> = when {
        this == null || other == null -> null
        else -> this.nullif(other)
    }

    /**
     * null-safe NULLIF. this가 other와 같으면 NULL을 반환한다.
     * 어느 쪽이든 null이면 조건을 건너뛴다.
     *
     * ```sql
     * NULLIF(price, 0)
     * ```
     *
     * @param other 비교 대상 값, null이면 조건을 건너뛴다
     * @return `NULLIF(this, other)`, 어느 쪽이든 null이면 null
     */
    infix fun <T> NumberExpression<T>?.nullif(other: T?): NumberExpression<T>?
        where T : Number, T : Comparable<*> = when {
        this == null || other == null -> null
        else -> this.nullif(other)
    }

    /**
     * null-safe COALESCE. SQL 상에서 this가 NULL이면 expr을 반환한다.
     * 어느 쪽이든 Kotlin null이면 조건을 건너뛴다.
     *
     * ```sql
     * COALESCE(price, default_price)
     * ```
     *
     * @param expr 대체 표현식, null이면 조건을 건너뛴다
     * @return `COALESCE(this, expr)`, 어느 쪽이든 null이면 null
     */
    infix fun <T> NumberExpression<T>?.coalesce(expr: Expression<T>?): NumberExpression<T>?
        where T : Number, T : Comparable<*> = when {
        this == null || expr == null -> null
        else -> this.coalesce(expr)
    }

    /**
     * null-safe COALESCE. SQL 상에서 this가 NULL이면 arg를 반환한다.
     * 어느 쪽이든 Kotlin null이면 조건을 건너뛴다.
     *
     * ```sql
     * COALESCE(price, 0)
     * ```
     *
     * @param arg 대체 값, null이면 조건을 건너뛴다
     * @return `COALESCE(this, arg)`, 어느 쪽이든 null이면 null
     */
    infix fun <T> NumberExpression<T>?.coalesce(arg: T?): NumberExpression<T>?
        where T : Number, T : Comparable<*> = when {
        this == null || arg == null -> null
        else -> this.coalesce(arg)
    }
}
