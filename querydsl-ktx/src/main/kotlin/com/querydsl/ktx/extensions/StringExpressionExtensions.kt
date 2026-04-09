package com.querydsl.ktx.extensions

import com.querydsl.core.types.Expression
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.StringExpression

interface StringExpressionExtensions {

    /**
     * null-safe LIKE 포함 검색. 어느 쪽이든 null이면 조건을 건너뛴다.
     *
     * ```sql
     * -- this = entity.name, right = '홍길동'
     * name LIKE '%홍길동%'
     * ```
     *
     * @param right 검색할 문자열, null이면 조건을 건너뛴다
     * @return `this LIKE '%right%'`, 어느 쪽이든 null이면 null
     */
    infix fun StringExpression?.contains(right: String?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.contains(right)
    }

    /**
     * null-safe LIKE 포함 검색 (Expression). 어느 쪽이든 null이면 조건을 건너뛴다.
     *
     * ```sql
     * name LIKE CONCAT('%', keyword, '%')
     * ```
     *
     * @param right 검색할 표현식, null이면 조건을 건너뛴다
     * @return `this LIKE '%right%'`, 어느 쪽이든 null이면 null
     */
    infix fun StringExpression?.contains(right: Expression<String>?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.contains(right)
    }

    /**
     * null-safe 대소문자 무시 포함 검색. 어느 쪽이든 null이면 조건을 건너뛴다.
     *
     * ```sql
     * LOWER(name) LIKE LOWER('%keyword%')
     * ```
     *
     * @param right 검색할 문자열, null이면 조건을 건너뛴다
     * @return `this LIKE '%right%'` (대소문자 무시), 어느 쪽이든 null이면 null
     */
    infix fun StringExpression?.containsIgnoreCase(right: String?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.containsIgnoreCase(right)
    }

    /**
     * null-safe 접두사 검색. 어느 쪽이든 null이면 조건을 건너뛴다.
     *
     * ```sql
     * -- this = entity.name, right = '홍'
     * name LIKE '홍%'
     * ```
     *
     * @param right 접두사 문자열, null이면 조건을 건너뛴다
     * @return `this LIKE 'right%'`, 어느 쪽이든 null이면 null
     */
    infix fun StringExpression?.startsWith(right: String?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.startsWith(right)
    }

    /**
     * null-safe 대소문자 무시 접두사 검색. 어느 쪽이든 null이면 조건을 건너뛴다.
     *
     * ```sql
     * LOWER(name) LIKE LOWER('prefix%')
     * ```
     *
     * @param right 접두사 문자열, null이면 조건을 건너뛴다
     * @return `this LIKE 'right%'` (대소문자 무시), 어느 쪽이든 null이면 null
     */
    infix fun StringExpression?.startsWithIgnoreCase(right: String?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.startsWithIgnoreCase(right)
    }

    /**
     * null-safe 접미사 검색. 어느 쪽이든 null이면 조건을 건너뛴다.
     *
     * ```sql
     * -- this = entity.email, right = '@gmail.com'
     * email LIKE '%@gmail.com'
     * ```
     *
     * @param right 접미사 문자열, null이면 조건을 건너뛴다
     * @return `this LIKE '%right'`, 어느 쪽이든 null이면 null
     */
    infix fun StringExpression?.endsWith(right: String?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.endsWith(right)
    }

    /**
     * null-safe 대소문자 무시 접미사 검색. 어느 쪽이든 null이면 조건을 건너뛴다.
     *
     * ```sql
     * LOWER(email) LIKE LOWER('%@gmail.com')
     * ```
     *
     * @param right 접미사 문자열, null이면 조건을 건너뛴다
     * @return `this LIKE '%right'` (대소문자 무시), 어느 쪽이든 null이면 null
     */
    infix fun StringExpression?.endsWithIgnoreCase(right: String?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.endsWithIgnoreCase(right)
    }

    /**
     * null-safe 대소문자 무시 동등 비교. 어느 쪽이든 null이면 조건을 건너뛴다.
     *
     * ```sql
     * LOWER(name) = LOWER('admin')
     * ```
     *
     * @param right 비교 대상 문자열, null이면 조건을 건너뛴다
     * @return `LOWER(this) = LOWER(right)`, 어느 쪽이든 null이면 null
     */
    infix fun StringExpression?.equalsIgnoreCase(right: String?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.equalsIgnoreCase(right)
    }

    /**
     * null-safe 대소문자 무시 부등 비교. 어느 쪽이든 null이면 조건을 건너뛴다.
     *
     * ```sql
     * LOWER(name) != LOWER('admin')
     * ```
     *
     * @param right 비교 대상 문자열, null이면 조건을 건너뛴다
     * @return `LOWER(this) != LOWER(right)`, 어느 쪽이든 null이면 null
     */
    infix fun StringExpression?.notEqualsIgnoreCase(right: String?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.notEqualsIgnoreCase(right)
    }

    /**
     * null-safe LIKE 패턴 검색. 어느 쪽이든 null이면 조건을 건너뛴다.
     *
     * ```sql
     * -- this = entity.name, right = '홍%동'
     * name LIKE '홍%동'
     * ```
     *
     * @param right LIKE 패턴, null이면 조건을 건너뛴다
     * @return `this LIKE right`, 어느 쪽이든 null이면 null
     */
    infix fun StringExpression?.like(right: String?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.like(right)
    }

    /**
     * null-safe 대소문자 무시 LIKE 패턴 검색. 어느 쪽이든 null이면 조건을 건너뛴다.
     *
     * ```sql
     * LOWER(name) LIKE LOWER('pattern%')
     * ```
     *
     * @param right LIKE 패턴, null이면 조건을 건너뛴다
     * @return `this LIKE right` (대소문자 무시), 어느 쪽이든 null이면 null
     */
    infix fun StringExpression?.likeIgnoreCase(right: String?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.likeIgnoreCase(right)
    }

    /**
     * null-safe NOT LIKE 패턴 검색. 어느 쪽이든 null이면 조건을 건너뛴다.
     *
     * ```sql
     * name NOT LIKE 'test%'
     * ```
     *
     * @param right LIKE 패턴, null이면 조건을 건너뛴다
     * @return `this NOT LIKE right`, 어느 쪽이든 null이면 null
     */
    infix fun StringExpression?.notLike(right: String?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.notLike(right)
    }

    /**
     * null-safe 정규식 매칭. 어느 쪽이든 null이면 조건을 건너뛴다.
     *
     * ```sql
     * name REGEXP '^[가-힣]+$'
     * ```
     *
     * @param regex 정규식 패턴, null이면 조건을 건너뛴다
     * @return `this REGEXP regex`, 어느 쪽이든 null이면 null
     */
    infix fun StringExpression?.matches(regex: String?): BooleanExpression? = when {
        this == null || regex == null -> null
        else -> this.matches(regex)
    }
}
