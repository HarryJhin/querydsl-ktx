package com.querydsl.ktx.support

import com.querydsl.core.types.Order
import com.querydsl.core.types.dsl.Expressions
import org.springframework.data.domain.Sort
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SortSpecTest {

    private val name = Expressions.comparablePath(String::class.java, "name")
    private val createdAt = Expressions.comparablePath(Comparable::class.java, "createdAt")
    private val age = Expressions.comparablePath(Comparable::class.java, "age")

    private val spec = sortSpec {
        "name" by name
        "createdAt" by createdAt
        "age" by age
    }

    @Test
    fun `sortSpec builder creates mappings`() {
        val s = sortSpec {
            "a" by name
            "b" by createdAt
        }
        assertEquals(2, s.mappings.size)
        assertTrue(s.mappings.containsKey("a"))
        assertTrue(s.mappings.containsKey("b"))
    }

    @Test
    fun `resolve - mapped property ascending`() {
        val sort = Sort.by(Sort.Order.asc("name"))
        val orders = spec.resolve(sort)

        assertEquals(1, orders.size)
        assertEquals(Order.ASC, orders[0].order)
        assertEquals(name, orders[0].target as Any)
    }

    @Test
    fun `resolve - mapped property descending`() {
        val sort = Sort.by(Sort.Order.desc("createdAt"))
        val orders = spec.resolve(sort)

        assertEquals(1, orders.size)
        assertEquals(Order.DESC, orders[0].order)
        assertEquals(createdAt, orders[0].target as Any)
    }

    @Test
    fun `resolve - multiple properties preserve order`() {
        val sort = Sort.by(
            Sort.Order.asc("name"),
            Sort.Order.desc("age"),
        )
        val orders = spec.resolve(sort)

        assertEquals(2, orders.size)
        assertEquals(name, orders[0].target as Any)
        assertEquals(Order.ASC, orders[0].order)
        assertEquals(age, orders[1].target as Any)
        assertEquals(Order.DESC, orders[1].order)
    }

    @Test
    fun `resolve - unmapped property is ignored`() {
        val sort = Sort.by(
            Sort.Order.asc("unknown"),
            Sort.Order.desc("name"),
        )
        val orders = spec.resolve(sort)

        assertEquals(1, orders.size)
        assertEquals(name, orders[0].target as Any)
        assertEquals(Order.DESC, orders[0].order)
    }

    @Test
    fun `resolve - all properties unmapped returns empty`() {
        val sort = Sort.by(Sort.Order.asc("nonExistent"))
        val orders = spec.resolve(sort)

        assertTrue(orders.isEmpty())
    }

    @Test
    fun `resolve - unsorted returns empty`() {
        val orders = spec.resolve(Sort.unsorted())
        assertTrue(orders.isEmpty())
    }
}
