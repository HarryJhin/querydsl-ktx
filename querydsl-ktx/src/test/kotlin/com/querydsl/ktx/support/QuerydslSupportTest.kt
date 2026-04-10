package com.querydsl.ktx.support

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QuerydslSupportTest {

    @Test
    fun `withoutSort strips sort from pageable`() {
        val pageable = PageRequest.of(2, 10, Sort.by("name"))
        val support = TestQuerydslSupport()
        val result = support.testWithoutSort(pageable)
        assertEquals(2, result.pageNumber)
        assertEquals(10, result.pageSize)
        assertTrue(result.sort.isUnsorted)
    }

    @Test
    fun `withoutSort preserves page number and size`() {
        val pageable = PageRequest.of(5, 25, Sort.by(Sort.Order.desc("id"), Sort.Order.asc("name")))
        val support = TestQuerydslSupport()
        val result = support.testWithoutSort(pageable)
        assertEquals(5, result.pageNumber)
        assertEquals(25, result.pageSize)
        assertTrue(result.sort.isUnsorted)
    }

    @Test
    fun `withoutSort on unsorted pageable returns equivalent pageable`() {
        val pageable = PageRequest.of(0, 10)
        val support = TestQuerydslSupport()
        val result = support.testWithoutSort(pageable)
        assertEquals(0, result.pageNumber)
        assertEquals(10, result.pageSize)
        assertTrue(result.sort.isUnsorted)
    }

    private class TestQuerydslSupport : QuerydslSupport<Any>() {
        override val domainClass = Any::class.java

        fun testWithoutSort(pageable: org.springframework.data.domain.Pageable) =
            pageable.withoutSort
    }
}
