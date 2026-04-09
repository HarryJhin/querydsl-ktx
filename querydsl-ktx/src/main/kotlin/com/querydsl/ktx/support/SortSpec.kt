package com.querydsl.ktx.support

import com.querydsl.core.types.Expression
import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import org.springframework.data.domain.Sort

/**
 * Maps Spring Data [Sort] property names to QueryDSL [Expression]s,
 * enabling type-safe dynamic ordering without `PathBuilder` string lookups.
 *
 * Define a spec once and reuse it across queries:
 * ```kotlin
 * val memberSort = sortSpec {
 *     "name"       by qMember.name
 *     "createdAt"  by qMember.createdAt
 *     "department" by qDepartment.name   // join column — PathBuilder can't resolve this
 * }
 *
 * // In a query method:
 * selectFrom(qMember)
 *     .join(qMember.department, qDepartment)
 *     .where(condition)
 *     .page(pageable, memberSort)
 * ```
 *
 * Unknown property names in [Sort] are silently ignored,
 * so clients cannot inject arbitrary column references.
 */
class SortSpec {

    @PublishedApi
    internal val mappings: MutableMap<String, Expression<out Comparable<*>>> = mutableMapOf()

    /**
     * Registers a sort property mapping.
     *
     * ```kotlin
     * "name" by qMember.name
     * ```
     *
     * @receiver the property name that clients pass via [Sort]
     * @param expression the QueryDSL expression to order by
     */
    infix fun String.by(expression: Expression<out Comparable<*>>) {
        mappings[this] = expression
    }

    /**
     * Resolves a Spring Data [Sort] into a list of [OrderSpecifier]s
     * using the registered mappings. Properties not found in the mapping are skipped.
     *
     * @param sort the sort specification from the client
     * @return ordered list of [OrderSpecifier]s; empty if no properties matched
     */
    @Suppress("UNCHECKED_CAST")
    fun resolve(sort: Sort): List<OrderSpecifier<*>> {
        if (sort.isUnsorted) return emptyList()

        return sort.mapNotNull { order ->
            val expression = mappings[order.property]
                as? Expression<Comparable<Any>>
                ?: return@mapNotNull null
            val direction = if (order.isAscending) Order.ASC else Order.DESC
            OrderSpecifier(direction, expression)
        }
    }
}

/**
 * Builds a [SortSpec] using DSL syntax.
 *
 * ```kotlin
 * val spec = sortSpec {
 *     "name"      by qMember.name
 *     "createdAt" by qMember.createdAt
 * }
 * ```
 */
inline fun sortSpec(block: SortSpec.() -> Unit): SortSpec =
    SortSpec().apply(block)
