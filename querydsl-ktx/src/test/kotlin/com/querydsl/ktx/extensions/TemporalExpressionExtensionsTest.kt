package com.querydsl.ktx.extensions

import com.querydsl.core.types.dsl.DateTimePath
import com.querydsl.core.types.dsl.Expressions
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TemporalExpressionExtensionsTest : TemporalExpressionExtensions, ComparableExpressionExtensions {

    private val createdAt: DateTimePath<LocalDateTime> =
        Expressions.dateTimePath(LocalDateTime::class.java, "createdAt")
    private val updatedAt: DateTimePath<LocalDateTime> =
        Expressions.dateTimePath(LocalDateTime::class.java, "updatedAt")
    private val nullExpr: DateTimePath<LocalDateTime>? = null

    private val sampleDate: LocalDateTime = LocalDateTime.of(2024, 1, 1, 0, 0)

    // ── after(T?) ──

    @Test
    fun `after value - both non-null returns AFTER expression`() {
        val result = createdAt after sampleDate
        assertNotNull(result)
        assertTrue(result.toString().contains(">"))
    }

    @Test
    fun `after value - this null returns null`() {
        val result = nullExpr after sampleDate
        assertNull(result)
    }

    @Test
    fun `after value - right null returns null`() {
        val result = createdAt after (null as LocalDateTime?)
        assertNull(result)
    }

    @Test
    fun `after value - both null returns null`() {
        val result = nullExpr after (null as LocalDateTime?)
        assertNull(result)
    }

    // ── after(Expression) ──

    @Test
    fun `after expression - both non-null returns AFTER expression`() {
        val result = createdAt after updatedAt
        assertNotNull(result)
        assertTrue(result.toString().contains(">"))
    }

    @Test
    fun `after expression - this null returns null`() {
        val result = nullExpr after updatedAt
        assertNull(result)
    }

    @Test
    fun `after expression - right null returns null`() {
        val result = createdAt after (null as DateTimePath<LocalDateTime>?)
        assertNull(result)
    }

    @Test
    fun `after expression - both null returns null`() {
        val result = nullExpr after (null as DateTimePath<LocalDateTime>?)
        assertNull(result)
    }

    // ── before(T?) ──

    @Test
    fun `before value - both non-null returns BEFORE expression`() {
        val result = createdAt before sampleDate
        assertNotNull(result)
        assertTrue(result.toString().contains("<"))
    }

    @Test
    fun `before value - this null returns null`() {
        val result = nullExpr before sampleDate
        assertNull(result)
    }

    @Test
    fun `before value - right null returns null`() {
        val result = createdAt before (null as LocalDateTime?)
        assertNull(result)
    }

    @Test
    fun `before value - both null returns null`() {
        val result = nullExpr before (null as LocalDateTime?)
        assertNull(result)
    }

    // ── before(Expression) ──

    @Test
    fun `before expression - both non-null returns BEFORE expression`() {
        val result = createdAt before updatedAt
        assertNotNull(result)
        assertTrue(result.toString().contains("<"))
    }

    @Test
    fun `before expression - this null returns null`() {
        val result = nullExpr before updatedAt
        assertNull(result)
    }

    @Test
    fun `before expression - right null returns null`() {
        val result = createdAt before (null as DateTimePath<LocalDateTime>?)
        assertNull(result)
    }

    @Test
    fun `before expression - both null returns null`() {
        val result = nullExpr before (null as DateTimePath<LocalDateTime>?)
        assertNull(result)
    }

    // ── between(Pair) from ComparableExpressionExtensions ──

    @Test
    fun `between Pair from ComparableExpressionExtensions works with TemporalExpression`() {
        val result = createdAt between (LocalDateTime.of(2024, 1, 1, 0, 0) to LocalDateTime.of(2024, 12, 31, 23, 59))
        assertNotNull(result)
    }

    @Test
    fun `between Pair - partial range with TemporalExpression`() {
        val result = createdAt between (LocalDateTime.of(2024, 1, 1, 0, 0) to null)
        assertNotNull(result)
    }
}
