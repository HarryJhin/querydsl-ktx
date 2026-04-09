package com.querydsl.ktx.extensions

import com.querydsl.core.types.Expression
import com.querydsl.core.types.dsl.BooleanExpression

interface BooleanExpressionExtensions {

    /**
     * null-safe AND. null인 쪽은 무시하고 non-null인 쪽만 살린다.
     *
     * ```sql
     * -- this = (active = true), right = (age > 20)
     * active = true AND age > 20
     *
     * -- this = null, right = (age > 20) → right만 반환
     * age > 20
     * ```
     *
     * @param right 교집합의 오른쪽 피연산자
     * @return `this AND right`, 둘 다 null이면 null
     */
    infix fun BooleanExpression?.and(right: BooleanExpression?): BooleanExpression? = when {
        this == null -> right
        right == null -> this
        else -> this.and(right)
    }

    /**
     * null-safe AND + ANY. this와 predicates의 OR 결합을 AND로 묶는다.
     * this가 null이면 OR 결과만 반환, predicates가 모두 null이면 this만 반환.
     *
     * ```sql
     * -- this = (active = true), predicates = [(role = 'ADMIN'), (role = 'MANAGER')]
     * active = true AND (role = 'ADMIN' OR role = 'MANAGER')
     * ```
     *
     * @param predicates OR로 결합될 조건 목록
     * @return `this AND (p1 OR p2 OR ...)`, 둘 다 null이면 null
     */
    infix fun BooleanExpression?.andAnyOf(predicates: List<BooleanExpression?>): BooleanExpression? =
        this and predicates.reduceOrNull { acc, expr -> acc or expr }

    /**
     * null-safe OR. null인 쪽은 무시하고 non-null인 쪽만 살린다.
     *
     * ```sql
     * -- this = (role = 'ADMIN'), right = (role = 'MANAGER')
     * role = 'ADMIN' OR role = 'MANAGER'
     *
     * -- this = null, right = (role = 'MANAGER') → right만 반환
     * role = 'MANAGER'
     * ```
     *
     * @param right 합집합의 오른쪽 피연산자
     * @return `this OR right`, 둘 다 null이면 null
     */
    infix fun BooleanExpression?.or(right: BooleanExpression?): BooleanExpression? = when {
        this == null -> right
        right == null -> this
        else -> this.or(right)
    }

    /**
     * null-safe OR + ALL. this와 predicates의 AND 결합을 OR로 묶는다.
     * this가 null이면 AND 결과만 반환, predicates가 모두 null이면 this만 반환.
     *
     * ```sql
     * -- this = (vip = true), predicates = [(age > 20), (active = true)]
     * vip = true OR (age > 20 AND active = true)
     * ```
     *
     * @param predicates AND로 결합될 조건 목록
     * @return `this OR (p1 AND p2 AND ...)`, 둘 다 null이면 null
     */
    infix fun BooleanExpression?.orAllOf(predicates: List<BooleanExpression?>): BooleanExpression? =
        this or predicates.reduceOrNull { acc, expr -> acc and expr }

    /**
     * null-safe 동등 비교. 어느 쪽이든 null이면 조건을 건너뛴다.
     *
     * ```sql
     * -- this = entity.active, right = true
     * active = true
     *
     * -- right = null → 조건 생략
     * ```
     *
     * @param right 비교 대상 Boolean 값, null이면 조건을 건너뛴다
     * @return `this = right`, 어느 쪽이든 null이면 null
     */
    infix fun BooleanExpression?.eq(right: Boolean?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.eq(right)
    }

    /**
     * null-safe NULLIF. this가 other와 같으면 NULL을 반환한다.
     * 어느 쪽이든 null이면 조건을 건너뛴다.
     *
     * ```sql
     * NULLIF(active, is_deleted)
     * ```
     *
     * @param other 비교 대상 표현식, null이면 조건을 건너뛴다
     * @return `NULLIF(this, other)`, 어느 쪽이든 null이면 null
     */
    infix fun BooleanExpression?.nullif(other: Expression<Boolean>?): BooleanExpression? = when {
        this == null || other == null -> null
        else -> this.nullif(other)
    }

    /**
     * null-safe NULLIF. this가 other와 같으면 NULL을 반환한다.
     * 어느 쪽이든 null이면 조건을 건너뛴다.
     *
     * ```sql
     * NULLIF(active, true)
     * ```
     *
     * @param other 비교 대상 Boolean 값, null이면 조건을 건너뛴다
     * @return `NULLIF(this, other)`, 어느 쪽이든 null이면 null
     */
    infix fun BooleanExpression?.nullif(other: Boolean?): BooleanExpression? = when {
        this == null || other == null -> null
        else -> this.nullif(other)
    }

    /**
     * null-safe COALESCE. SQL 상에서 this가 NULL이면 expr을 반환한다.
     * 어느 쪽이든 Kotlin null이면 조건을 건너뛴다.
     *
     * ```sql
     * COALESCE(active, is_visible)
     * ```
     *
     * @param expr 대체 표현식, null이면 조건을 건너뛴다
     * @return `COALESCE(this, expr)`, 어느 쪽이든 null이면 null
     */
    infix fun BooleanExpression?.coalesce(expr: Expression<Boolean>?): BooleanExpression? = when {
        this == null || expr == null -> null
        else -> this.coalesce(expr)
    }

    /**
     * null-safe COALESCE. SQL 상에서 this가 NULL이면 arg를 반환한다.
     * 어느 쪽이든 Kotlin null이면 조건을 건너뛴다.
     *
     * ```sql
     * COALESCE(active, false)
     * ```
     *
     * @param arg 대체 Boolean 값, null이면 조건을 건너뛴다
     * @return `COALESCE(this, arg)`, 어느 쪽이든 null이면 null
     */
    infix fun BooleanExpression?.coalesce(arg: Boolean?): BooleanExpression? = when {
        this == null || arg == null -> null
        else -> this.coalesce(arg)
    }
}
