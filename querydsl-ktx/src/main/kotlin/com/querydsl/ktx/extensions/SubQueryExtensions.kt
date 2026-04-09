package com.querydsl.ktx.extensions

import com.querydsl.core.types.EntityPath
import com.querydsl.core.types.Predicate
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.JPAExpressions

/**
 * Shorthand EXISTS / NOT EXISTS sub-query builders for [EntityPath].
 *
 * **Before** -- vanilla QueryDSL:
 * ```kotlin
 * JPAExpressions.selectOne()
 *     .from(orderItem)
 *     .where(orderItem.orderId.eq(order.id))
 *     .exists()
 * ```
 *
 * **After** -- with this interface:
 * ```kotlin
 * orderItem.exists(orderItem.orderId eq order.id)
 * ```
 *
 * Implement this interface (or use the pre-built scope object) to bring
 * the extension functions into scope.
 */
interface SubQueryExtensions {

    /**
     * Builds an EXISTS sub-query: `EXISTS (SELECT 1 FROM this WHERE predicates)`.
     *
     * Null predicates in the array are silently filtered out.
     *
     * ```sql
     * -- this = orderItem, predicates = [orderItem.orderId = order.id]
     * EXISTS (SELECT 1 FROM order_item WHERE order_item.order_id = order.id)
     * ```
     *
     * @param predicates WHERE conditions for the sub-query; nulls are ignored
     * @return a [BooleanExpression] representing the EXISTS check
     */
    fun <T> EntityPath<T>.exists(vararg predicates: Predicate?): BooleanExpression {
        val nonNull = predicates.filterNotNull().toTypedArray()
        return JPAExpressions.selectOne().from(this).where(*nonNull).exists()
    }

    /**
     * Builds a NOT EXISTS sub-query: `NOT EXISTS (SELECT 1 FROM this WHERE predicates)`.
     *
     * Null predicates in the array are silently filtered out.
     *
     * ```sql
     * -- this = orderItem, predicates = [orderItem.orderId = order.id]
     * NOT EXISTS (SELECT 1 FROM order_item WHERE order_item.order_id = order.id)
     * ```
     *
     * @param predicates WHERE conditions for the sub-query; nulls are ignored
     * @return a [BooleanExpression] representing the NOT EXISTS check
     */
    fun <T> EntityPath<T>.notExists(vararg predicates: Predicate?): BooleanExpression {
        val nonNull = predicates.filterNotNull().toTypedArray()
        return JPAExpressions.selectOne().from(this).where(*nonNull).notExists()
    }
}
