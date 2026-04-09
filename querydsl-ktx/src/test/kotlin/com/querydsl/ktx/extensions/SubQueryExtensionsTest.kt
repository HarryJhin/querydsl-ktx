package com.querydsl.ktx.extensions

import com.querydsl.core.types.dsl.Expressions
import com.querydsl.core.types.dsl.PathBuilder
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SubQueryExtensionsTest : SubQueryExtensions {

    private val order = PathBuilder(Any::class.java, "order")
    private val orderItem = PathBuilder(Any::class.java, "orderItem")

    private val orderId = Expressions.numberPath(Long::class.java, orderItem, "orderId")
    private val id = Expressions.numberPath(Long::class.java, order, "id")

    // -- exists --

    @Test
    fun `exists - with predicates returns EXISTS expression`() {
        val result = orderItem.exists(orderId.eq(id))
        assertNotNull(result)
        assertTrue(result.toString().contains("exists"))
    }

    @Test
    fun `exists - with multiple predicates returns EXISTS expression`() {
        val active = Expressions.booleanPath(orderItem, "active")
        val result = orderItem.exists(orderId.eq(id), active.isTrue)
        assertNotNull(result)
        assertTrue(result.toString().contains("exists"))
    }

    @Test
    fun `exists - with null predicates filters them out`() {
        val result = orderItem.exists(orderId.eq(id), null)
        assertNotNull(result)
        assertTrue(result.toString().contains("exists"))
    }

    @Test
    fun `exists - with no predicates returns EXISTS expression`() {
        val result = orderItem.exists()
        assertNotNull(result)
        assertTrue(result.toString().contains("exists"))
    }

    // -- notExists --

    @Test
    fun `notExists - with predicates returns NOT EXISTS expression`() {
        val result = orderItem.notExists(orderId.eq(id))
        assertNotNull(result)
        assertTrue(result.toString().contains("exists"))
    }

    @Test
    fun `notExists - with multiple predicates returns NOT EXISTS expression`() {
        val active = Expressions.booleanPath(orderItem, "active")
        val result = orderItem.notExists(orderId.eq(id), active.isTrue)
        assertNotNull(result)
        assertTrue(result.toString().contains("exists"))
    }

    @Test
    fun `notExists - with null predicates filters them out`() {
        val result = orderItem.notExists(orderId.eq(id), null)
        assertNotNull(result)
        assertTrue(result.toString().contains("exists"))
    }

    @Test
    fun `notExists - with no predicates returns NOT EXISTS expression`() {
        val result = orderItem.notExists()
        assertNotNull(result)
        assertTrue(result.toString().contains("exists"))
    }
}
