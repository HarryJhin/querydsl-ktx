package com.querydsl.ktx.extensions

import com.querydsl.core.types.Expression
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.StringExpression

/**
 * Null-safe string matching operators for [StringExpression].
 *
 * **Before** -- vanilla QueryDSL:
 * ```kotlin
 * if (keyword != null) builder.and(entity.name.contains(keyword))
 * if (prefix != null)  builder.and(entity.code.startsWith(prefix))
 * ```
 *
 * **After** -- with this interface:
 * ```kotlin
 * val predicate = (entity.name contains keyword) and (entity.code startsWith prefix)
 * ```
 *
 * Implement this interface (or use the pre-built scope object) to bring
 * the infix operators into scope.
 */
interface StringExpressionExtensions {

    /**
     * Null-safe substring match that skips the condition when either side is null.
     *
     * ```sql
     * -- this = entity.name, right = 'John'
     * name LIKE '%John%'
     * ```
     *
     * @param right the substring to search for, or null to skip
     * @return `this LIKE '%right%'`, or null if either side is null
     */
    infix fun StringExpression?.contains(right: String?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.contains(right)
    }

    /**
     * Null-safe substring match against another expression.
     *
     * Skips the condition when either side is null.
     *
     * ```sql
     * name LIKE CONCAT('%', keyword, '%')
     * ```
     *
     * @param right the expression to search for, or null to skip
     * @return `this LIKE '%right%'`, or null if either side is null
     */
    infix fun StringExpression?.contains(right: Expression<String>?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.contains(right)
    }

    /**
     * Null-safe case-insensitive substring match that skips the condition when either side is null.
     *
     * ```sql
     * LOWER(name) LIKE LOWER('%keyword%')
     * ```
     *
     * @param right the substring to search for, or null to skip
     * @return `this LIKE '%right%'` (case-insensitive), or null if either side is null
     */
    infix fun StringExpression?.containsIgnoreCase(right: String?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.containsIgnoreCase(right)
    }

    /**
     * Null-safe prefix match that skips the condition when either side is null.
     *
     * ```sql
     * -- this = entity.name, right = 'Jo'
     * name LIKE 'Jo%'
     * ```
     *
     * @param right the prefix string, or null to skip
     * @return `this LIKE 'right%'`, or null if either side is null
     */
    infix fun StringExpression?.startsWith(right: String?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.startsWith(right)
    }

    /**
     * Null-safe case-insensitive prefix match that skips the condition when either side is null.
     *
     * ```sql
     * LOWER(name) LIKE LOWER('prefix%')
     * ```
     *
     * @param right the prefix string, or null to skip
     * @return `this LIKE 'right%'` (case-insensitive), or null if either side is null
     */
    infix fun StringExpression?.startsWithIgnoreCase(right: String?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.startsWithIgnoreCase(right)
    }

    /**
     * Null-safe suffix match that skips the condition when either side is null.
     *
     * ```sql
     * -- this = entity.email, right = '@gmail.com'
     * email LIKE '%@gmail.com'
     * ```
     *
     * @param right the suffix string, or null to skip
     * @return `this LIKE '%right'`, or null if either side is null
     */
    infix fun StringExpression?.endsWith(right: String?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.endsWith(right)
    }

    /**
     * Null-safe case-insensitive suffix match that skips the condition when either side is null.
     *
     * ```sql
     * LOWER(email) LIKE LOWER('%@gmail.com')
     * ```
     *
     * @param right the suffix string, or null to skip
     * @return `this LIKE '%right'` (case-insensitive), or null if either side is null
     */
    infix fun StringExpression?.endsWithIgnoreCase(right: String?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.endsWithIgnoreCase(right)
    }

    /**
     * Null-safe case-insensitive equality check that skips the condition when either side is null.
     *
     * ```sql
     * LOWER(name) = LOWER('admin')
     * ```
     *
     * @param right the string to compare against, or null to skip
     * @return `LOWER(this) = LOWER(right)`, or null if either side is null
     */
    infix fun StringExpression?.equalsIgnoreCase(right: String?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.equalsIgnoreCase(right)
    }

    /**
     * Null-safe case-insensitive not-equal check that skips the condition when either side is null.
     *
     * ```sql
     * LOWER(name) != LOWER('admin')
     * ```
     *
     * @param right the string to compare against, or null to skip
     * @return `LOWER(this) != LOWER(right)`, or null if either side is null
     */
    infix fun StringExpression?.notEqualsIgnoreCase(right: String?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.notEqualsIgnoreCase(right)
    }

    /**
     * Null-safe LIKE pattern match that skips the condition when either side is null.
     *
     * ```sql
     * -- this = entity.name, right = 'J%n'
     * name LIKE 'J%n'
     * ```
     *
     * @param right the LIKE pattern, or null to skip
     * @return `this LIKE right`, or null if either side is null
     */
    infix fun StringExpression?.like(right: String?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.like(right)
    }

    /**
     * Null-safe case-insensitive LIKE pattern match that skips the condition when either side is null.
     *
     * ```sql
     * LOWER(name) LIKE LOWER('pattern%')
     * ```
     *
     * @param right the LIKE pattern, or null to skip
     * @return `this LIKE right` (case-insensitive), or null if either side is null
     */
    infix fun StringExpression?.likeIgnoreCase(right: String?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.likeIgnoreCase(right)
    }

    /**
     * Null-safe NOT LIKE pattern match that skips the condition when either side is null.
     *
     * ```sql
     * name NOT LIKE 'test%'
     * ```
     *
     * @param right the LIKE pattern to negate, or null to skip
     * @return `this NOT LIKE right`, or null if either side is null
     */
    infix fun StringExpression?.notLike(right: String?): BooleanExpression? = when {
        this == null || right == null -> null
        else -> this.notLike(right)
    }

    /**
     * Null-safe regex match that skips the condition when either side is null.
     *
     * ```sql
     * name REGEXP '^[A-Z]+$'
     * ```
     *
     * @param regex the regular expression pattern, or null to skip
     * @return `this REGEXP regex`, or null if either side is null
     */
    infix fun StringExpression?.matches(regex: String?): BooleanExpression? = when {
        this == null || regex == null -> null
        else -> this.matches(regex)
    }

    /**
     * Null-safe NULLIF that returns SQL NULL when this equals [other].
     *
     * Skips the expression entirely when either side is Kotlin null.
     *
     * ```sql
     * NULLIF(name, other_name)
     * ```
     *
     * @param other the expression to compare against, or null to skip
     * @return `NULLIF(this, other)`, or null if either side is null
     */
    infix fun StringExpression?.nullif(other: Expression<String>?): StringExpression? = when {
        this == null || other == null -> null
        else -> this.nullif(other)
    }

    /**
     * Null-safe NULLIF that returns SQL NULL when this equals [other].
     *
     * Skips the expression entirely when either side is Kotlin null.
     *
     * ```sql
     * NULLIF(name, 'default')
     * ```
     *
     * @param other the value to compare against, or null to skip
     * @return `NULLIF(this, other)`, or null if either side is null
     */
    infix fun StringExpression?.nullif(other: String?): StringExpression? = when {
        this == null || other == null -> null
        else -> this.nullif(other)
    }

    /**
     * Null-safe COALESCE that falls back to [expr] when this is SQL NULL.
     *
     * Skips the expression entirely when either side is Kotlin null.
     *
     * ```sql
     * COALESCE(name, other_name)
     * ```
     *
     * @param expr the fallback expression, or null to skip
     * @return `COALESCE(this, expr)`, or null if either side is null
     */
    infix fun StringExpression?.coalesce(expr: Expression<String>?): StringExpression? = when {
        this == null || expr == null -> null
        else -> this.coalesce(expr)
    }

    /**
     * Null-safe COALESCE that falls back to [arg] when this is SQL NULL.
     *
     * Skips the expression entirely when either side is Kotlin null.
     *
     * ```sql
     * COALESCE(name, 'unknown')
     * ```
     *
     * @param arg the fallback value, or null to skip
     * @return `COALESCE(this, arg)`, or null if either side is null
     */
    infix fun StringExpression?.coalesce(arg: String?): StringExpression? = when {
        this == null || arg == null -> null
        else -> this.coalesce(arg)
    }
}
