@file:JvmName("CaseDslKt")

package com.querydsl.ktx

import com.querydsl.core.types.Expression
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.CaseBuilder
import com.querydsl.core.types.dsl.SimpleExpression

/**
 * DSL entry point for **searched** CASE expressions.
 *
 * Wraps QueryDSL's [CaseBuilder] with a Kotlin-idiomatic builder that
 * supports null-safe predicates — a `when(null)` branch is silently skipped.
 *
 * Returns `null` when every predicate was null (i.e. no branch was added).
 *
 * ```kotlin
 * val expr = case<Int> {
 *     `when`(entity.status.eq("VIP")) then 1
 *     `when`(entity.status.eq("NORMAL")) then 2
 *     otherwise(3)
 * }
 * ```
 */
fun <T> case(block: SearchedCaseDsl<T>.() -> Unit): Expression<T>? {
    val dsl = SearchedCaseDsl<T>()
    dsl.block()
    return dsl.result
}

/**
 * DSL entry point for **simple** (value-matching) CASE expressions.
 *
 * Internally converts to a searched CASE by mapping each `when(value)` to
 * `expression.eq(value)`, which produces identical SQL.
 *
 * ```kotlin
 * val expr = case<String, Int>(entity.status) {
 *     `when`("VIP") then 1
 *     `when`("NORMAL") then 2
 *     otherwise(3)
 * }
 * ```
 */
fun <D, T> case(
    expression: SimpleExpression<D>,
    block: SimpleCaseDsl<D, T>.() -> Unit,
): Expression<T>? {
    val dsl = SimpleCaseDsl<D, T>(expression)
    dsl.block()
    return dsl.result
}

/**
 * Builder scope for **searched** CASE (`CASE WHEN pred THEN ... END`).
 *
 * Null-safe: if a predicate is null the branch is skipped.
 * If all branches are skipped, [result] is null.
 */
class SearchedCaseDsl<T> {

    private val builder = CaseBuilder()
    private var cases: CaseBuilder.Cases<T, *>? = null

    /** The finished expression, available after [otherwise] is called. */
    internal var result: Expression<T>? = null

    /** Starts a WHEN branch. Chain with [WhenClause.then]. */
    fun `when`(pred: BooleanExpression?): WhenClause = WhenClause(pred)

    /** A pending WHEN branch awaiting a THEN value or expression. */
    inner class WhenClause(private val pred: BooleanExpression?) {

        /** Completes this branch with a constant value. */
        @Suppress("UNCHECKED_CAST")
        infix fun then(value: T) {
            if (pred == null) return
            val c = cases
            cases = if (c == null) {
                builder.`when`(pred).then(value) as CaseBuilder.Cases<T, *>
            } else {
                c.`when`(pred).then(value) as CaseBuilder.Cases<T, *>
            }
        }

        /** Completes this branch with an expression result. */
        @Suppress("UNCHECKED_CAST")
        infix fun then(expr: Expression<T>) {
            if (pred == null) return
            val c = cases
            cases = if (c == null) {
                builder.`when`(pred).then(expr) as CaseBuilder.Cases<T, *>
            } else {
                c.`when`(pred).then(expr) as CaseBuilder.Cases<T, *>
            }
        }
    }

    /** Terminates the CASE with a default constant value. */
    @Suppress("UNCHECKED_CAST")
    fun otherwise(value: T) {
        result = cases?.otherwise(value) as? Expression<T>
    }

    /** Terminates the CASE with a default expression. */
    @Suppress("UNCHECKED_CAST")
    fun otherwise(expr: Expression<T>) {
        result = cases?.otherwise(expr) as? Expression<T>
    }
}

/**
 * Builder scope for **simple** CASE (`CASE expr WHEN value THEN ... END`).
 *
 * Internally delegates to [SearchedCaseDsl] by converting
 * `when(value)` into `expression.eq(value)`.
 */
class SimpleCaseDsl<D, T>(private val expression: SimpleExpression<D>) {

    private val searchedDsl = SearchedCaseDsl<T>()

    /** Starts a WHEN branch matching a specific value. */
    fun `when`(value: D): SimpleWhenClause = SimpleWhenClause(value)

    /** A pending WHEN branch awaiting a THEN value or expression. */
    inner class SimpleWhenClause(private val value: D) {

        /** Completes this branch with a constant result. */
        infix fun then(result: T) {
            searchedDsl.`when`(expression.eq(value)).then(result)
        }

        /** Completes this branch with an expression result. */
        infix fun then(expr: Expression<T>) {
            searchedDsl.`when`(expression.eq(value)).then(expr)
        }
    }

    /** Terminates the CASE with a default constant value. */
    fun otherwise(value: T) {
        searchedDsl.otherwise(value)
    }

    /** Terminates the CASE with a default expression. */
    fun otherwise(expr: Expression<T>) {
        searchedDsl.otherwise(expr)
    }

    /** The finished expression, available after [otherwise] is called. */
    internal val result: Expression<T>? get() = searchedDsl.result
}
